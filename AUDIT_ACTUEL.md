# Audit consolidé — CreateNuclearForge (branche V2-CorrectifAudit)

**Dernière mise à jour : 2026-07-12 (après commits jusqu'à `8b0428e3` inclus).** Nouvel audit indépendant réalisé en repartant exclusivement du code actuel (`src/main/java`, **291 fichiers, ~25 070 lignes**, hors `src/generated` et `src/main/resources`). `AUDIT_V1.md` et l'historique de ce fichier (versions précédentes) n'ont servi qu'à comparer les résultats — chaque point a été **revérifié par lecture directe des fichiers cités** (et, pour les zones les plus actives, par `git show`/`git log` sur les commits du 2026-06-24 au 2026-06-28).

> Ce document ne liste que **ce qui reste à corriger ou à décider**. Les bugs résolus sont récapitulés en §6 pour mémoire, puis ne sont plus repris.

**Limite de couverture honnête** : les bugs mineurs/robustesse listés au §1 de l'historique (`B7, B10, B17, B18` et la liste « mineurs » non numérotée d'`AUDIT_V1.md`) n'ont pas été re-vérifiés ici faute de signal de changement — ni confirmés ni infirmés, à re-vérifier ponctuellement avant traitement.

---

## 0. Constat global

Depuis le dernier audit (2026-06-24), le projet a continué d'évoluer activement (16 commits en 4 jours) : extraction de `CNBuilderTransformers` (SRP propre, 97 lignes, 3 builders isolés), ajout de l'item **Biome Restore Cell** (code neuf, propre, bien isolé dans `BiomeRestoreCellItem`/`BiomeIrradiationService`), refonte du système de radiation pour couvrir **toutes les `LivingEntity`** et non plus seulement les joueurs (`9ded502c`), centralisation de l'attribution des advancements de pose via le contrôleur plutôt que la pièce posée (`19617118`), et suppression du `LOGGER.warn` redondant dans `ReactorAssembler` (`b309f231`, **quick win déjà traité**).

Aucun de ces changements ne casse les acquis des audits précédents : le point de vérité unique radiation (`canBeIrradiated`) tient toujours, l'extension aux mobs ne boucle pas sur le monde (elle s'appuie sur `LivingTickEvent`, déclenché nativement par tick et par entité), et la centralisation des advancements est un vrai gain (élimine un risque de double-attribution). Un commit au message trompeur (`20c1df0b`, *"IO blocks are now natively Casings"*) ne change **rien** à l'architecture : il ne fait que nettoyer 22 lignes d'une scène Ponder ; les blocs I/O (`ReactorOutput`, `ReactorRodInput`, `ReactorFluidInput`, `ReactorAlarm`) restent des classes `Block` séparées de `ReactorCasing`.

**Nouveauté la plus significative de cet audit** : en creusant le scan multiblock fusionné (déjà crédité « corrigé » au tour précédent), la boucle partagée `scanControllerCandidates` (`ReactorPattern.java:40-56`) ne court-circuite en réalité que via `findControllerPos` (utilisé par `MultiblockHelpers`, donc par `ReactorOutput`/`ReactorRodInput`/`ReactorFluidInput`/`ReactorAlarm`). La méthode `findController` — appelée **directement** par `ReactorCasing`, `ReactorCooler` et `ReactorFrame` — a un visiteur qui retourne toujours `false` et **ne s'arrête jamais avant d'avoir parcouru les ~3971 positions** de la zone de recherche, même après avoir trouvé et traité le contrôleur pertinent. Comme `ReactorCasing` constitue le gros de la coque d'un réacteur (le bloc le plus posé/cassé), c'est en réalité le chemin le plus coûteux des deux — voir §2 point 1.

**Depuis cette mise à jour**, trois commits supplémentaires : retrait de `SimpleMultiBlockPattern.test()` (`2aaddb4d`, quick win déjà acquis), puis l'exécution en deux temps du chantier « tags vs `RodType` » assigné à Gio — `562ce99d` migre l'essentiel (5 des 6 sites) et enfile enfin `Level` jusqu'à `DefaultHeatCalculator`, et `30de52a7` corrige les deux sites manqués par le premier commit (`ReactorBluePrintMenu`, lookup du voisin dans `DefaultHeatCalculator`). Le chantier est désormais **intégralement terminé et cohérent** (vérifié par grep : plus aucun site de classification ne lit `CNItemTags.FUEL`/`COOLER`, seules les déclarations de tag de craft dans `CNItems.java` subsistent) — voir §2 point 8, reclassé en §6.

La dette la plus structurelle reste **l'absence totale de tests** (`src/test` toujours vide, confirmé) — précisément là où des bugs silencieux (sur-extraction fluide, tautologie `IHeat`, scan non court-circuité) survivent à plusieurs passes d'audit. Ce chantier de classification illustre d'ailleurs bien le risque : la première passe (`562ce99d`) a laissé passer deux sites incohérents, repérés seulement par relecture manuelle puis corrigés en quelques minutes (`30de52a7`) — un test unitaire sur `TypeRodPredicate.isFuel/isCooled` aurait détecté l'écart immédiatement.

---

## 1. 🐛 Bugs confirmés toujours présents (revérifiés)

| Réf. | Fichier:ligne | Gravité | Détail (vérifié 2026-06-28) |
|---|---|---|---|
| **ReactorInputFluidManager — sur-extraction** | `controller/manager/ReactorInputFluidManager.java:137-155` | 🟠 | `fluidNeeded` (paramètre de `extractFluids`) n'est **toujours jamais décrémenté** entre handlers (l.146-151) → chaque handler de la boucle tente d'extraire le besoin complet, sur-extraction possible avec plusieurs inputs. `if (toExtract > 1)` (l.147) ignore toujours les extractions de 1 unité. Toléré aujourd'hui (tank mono-slot), reste une violation de contrat latente. |
| **Flicker vitesse de sortie en fin de ressources (`overHeat` + division entière)** *(nouveau, trouvé 2026-07-05)* | `content/multiblock/reactorLogic/DefaultOverheatController.java:13-30` + `controller/manager/ReactorOutputManager.java:90` | 🟡 | `overHeat` s'incrémente de `+1` (fluide insuffisant) et se décrémente de `-2` (fluide suffisant) à un rythme régulier — un oscillateur auto-entretenu, **indépendant de tout réapprovisionnement**. Ce `overHeat` est additionné au heat des barres (`DefaultHeatCalculator.java:93`) puis divisé en entier par `RPM_DIVIDER=32` (`ReactorOutputManager.java:90`) pour obtenir le RPM des sorties. En fin de ressources, le heat de base est faible et peut se trouver juste à cheval sur un multiple de 32 : le va-et-vient de `overHeat` fait alors basculer la vitesse de sortie entre `0` et `X` en boucle, sans lien avec le stock de combustible. Hystérésis sur le heat appliqué à `rotateOutputs` dans `ReactorControllerBlockEntity.handleAssembledState` (nouveau champ `lastAppliedOutputHeat`, ne réactualiser que si `\|heat - lastAppliedOutputHeat\| >= RPM_DIVIDER/2`). Pas besoin de persister ce champ en NBT (comme `heat`/`totalHeatRatio`, recalculé chaque tick). |

---

## 2. 🆕 Autres problèmes (hors radiation)

### 🟠 Important — performance résiduelle du scan multiblock (était 🔴, deux des trois sous-points corrigés le 2026-06-28)

1. **Scan géométrique du contrôleur : court-circuit incomplet (findController)** *(garde client et double scan corrigés 2026-06-28, vérifié par lecture directe du code modifié)*
   - ✅ **Toujours acquis** — `ReactorPattern` n'a qu'**une seule** boucle de scan partagée (`scanControllerCandidates`, `ReactorPattern.java:40-56`) sur ~3971 positions (`y∈[-5,+5]`, `x,z∈[-9,+9]`), filtrée par `isInReactorRange` (l.111-113).
   - ❌ **Toujours non corrigé** — le court-circuit (`return true` dans le visiteur dès qu'un contrôleur pertinent est trouvé) n'existe que dans **`findControllerPos`** (`ReactorPattern.java:73-94`, utilisé via `MultiblockHelpers.handleOnPlace/handleRemoval/getControllerForPart` → donc par `ReactorOutput`, `ReactorRodInput`, `ReactorFluidInput`, `ReactorAlarm`). **`findController`** (`ReactorPattern.java:58-71`), appelé **directement** par `ReactorCasing.java:49,61`, `ReactorCooler.java:28,40` et `ReactorFrame.java:113,129`, a un visiteur qui **retourne toujours `false`** (l.69) : il continue de parcourir les ~3971 positions restantes même après avoir traité le contrôleur pertinent. `ReactorCasing` étant le bloc le plus posé/cassé (coque du réacteur), c'est le chemin le plus chaud des deux et il n'est **toujours pas** optimisé. Fix trivial inchangé : faire retourner `inRange` au lieu de `false` dans le visiteur de `findController`, comme c'est déjà fait dans `findControllerPos`.
   - ✅ **Corrigé le 2026-06-28** — garde `level.isClientSide` ajoutée en tête de `ReactorAssembler.assemble` (`ReactorAssembler.java:34`) et `ReactorAssembler.disassemble` (l.58), plutôt que dans `findController`/`findControllerPos` comme envisagé initialement : ces deux méthodes sont le point de convergence unique de toute la chaîne (`findController` direct sur Casing/Cooler/Frame, et `findControllerPos` via `MultiblockHelpers` sur Alarm/Output/FluidInput/RodInput), donc une garde centralisée ici couvre l'intégralité du chemin `onPlace/onRemove/playerDestroy → ... → assemble/disassemble` sans dupliquer le test dans 7 classes de blocs.
   - ✅ **Corrigé le 2026-06-28** — double scan à la casse résolu en généralisant le modèle déjà correct de `ReactorRodInput` (un seul appel dans `onRemove`, pas de `playerDestroy`) : `ReactorCasing`, `ReactorCooler`, `ReactorFrame`, `ReactorAlarm`, `ReactorOutput`, `ReactorFluidInput` n'ont plus d'override `playerDestroy` (vérifié par lecture directe des 6 fichiers — l'override a été supprimé, pas seulement vidé). Comme `onRemove` se déclenche déjà systématiquement lors d'une casse joueur (avant l'appel `playerDestroy` du `PlayerInteractionManager`), retirer l'appel redondant dans `playerDestroy` élimine le double scan sans changer de comportement observable. `ReactorAlarm`/`ReactorFrame` conservent en plus leur garde `!state.is(newState.getBlock())` dans `onRemove` (utile pour leurs propriétés mutables `POWERED`/`PART`).
     - **Point résiduel mineur, non corrigé** — `ReactorOutput` (propriété `DIR`) et `ReactorFluidInput` (propriété `FACING`) ont une propriété mutable mais leur `onRemove` (`ReactorOutput.java:71-74`, `ReactorFluidInput.java:93-97`) n'a **pas** la garde `!state.is(newState.getBlock())`. Pas un bug confirmé (aucun chemin connu ne change `DIR`/`FACING` via `setBlock` sans remplacer le bloc) mais à surveiller si une interaction clé/wrench s'avère un jour déclencher un retrait/réenregistrement non désiré.
   - ✅ **Corrigé et confirmé stable** — `ReactorAssembler.assemble` ne logue plus rien en succès (`ReactorAssembler.java`, vérifié : aucun `LOGGER.warn`/`LOGGER.info` dans `assemble`/`disassemble`). *(Quick win traité par le commit `b309f231`.)*
   - *Reste à faire* : `findController` doit court-circuiter comme `findControllerPos` (1 ligne, gain net immédiat sur le chemin le plus chaud — Casing). Risque très faible, **reste la priorité n°1 du projet** (perf uniquement désormais, plus une question de correction).

### 🟠 Importants

2. **`DefaultHeatCalculator.computeHeat` — partiellement amélioré, reste O(n×81) + asymétrie fuel/cooler** *(réévalué 2026-06-28, moins grave qu'estimé précédemment)*
   `DefaultHeatCalculator.java:35-97` a changé de forme depuis le dernier audit : les items du pattern sont désormais désérialisés **une seule fois** dans une `Map<Integer, ItemStack> actualRods` (l.44-54), et la recherche du **voisin** se fait par lookup direct `actualRods.containsKey(neighborSlot)` (l.81) — **l'ancienne re-boucle O(n) sur tous les items à chaque voisin a disparu**, ce n'est donc plus un O(n²) sur les items. Reste cependant, pour chaque rod, une boucle complète sur la grille `formattedPattern` (9×9=81 cellules, l.70-73) pour retrouver sa propre position — non remplacée par une map slot→position précalculée (coût ~57×81 ≈ 4617 itérations/tick sur réacteur plein, contre le ~1 050 000 estimé par l'ancien audit, qui est donc à reclasser **🟡 mineur** plutôt que 🟠).
   - **Asymétrie toujours présente** (l.82) : l'examen des voisins ne se fait que si `"fuel".equals(currentRod)` ; un cooler ne déclenche jamais l'examen de ses voisins. `heat += rod.baseRodHeat() / neighborRod.proximityRodHeat()` (l.88, **division**) côté fuel→cooler-voisin vs addition (l.86) côté fuel→fuel-voisin. **Ne pas toucher sans confirmation balance** (toujours valable).
   - ✅ **Corrigé le 2026-06-28** (`562ce99d`) — `IHeatCalculator.computeHeat` reçoit maintenant un `Level` (`IHeatCalculator.java:10`), propagé par `HeatManager.calculateHeat` (`HeatManager.java:34`) puis utilisé pour résoudre le rod courant **et**, depuis `30de52a7`, le rod voisin (`DefaultHeatCalculator.java:60` et `:85`) via `RodType.resolveRodType`. Plus aucun appel à l'ancienne `ItemRodTypesValue.getRodType` dans ce fichier. Voir §6 pour le détail du chantier.
   - *Recommandation* : précalculer une `Map<Integer slot, int[] position>` (ou inverser `formattedPattern` en `Map<Integer,int[]>` statique au chargement de la classe) pour éliminer la boucle 81 cellules — gain mineur, risque nul, mais moins urgent qu'estimé avant.

3. **`ReactorSummaryDisplaySource` — sentinelle de taille + accès positionnel fragiles** *(inchangé)*
   `getComponents()` retourne toujours une liste de taille 1 (pas de contrôleur) ou 6 (normal) ; les appelants gardent `if (components.size() < 6)` avant un accès positionnel `components.get(2).get(1)` (ligne « fuel »). Toujours fragile à tout réordonnancement de ligne.

4. **`ReactorSummaryDisplaySource.formatValue` — incohérence de mode** *(inchangé)*
   `HeatDisplaySource` affiche `"500 °C"` en mode normal alors que `ReactorSummaryDisplaySource` force une jauge pour le heat dans le même mode (`gaugeOnNormal=true` pour heat uniquement) — incohérence visuelle toujours présente.

### 🟡 Mineurs

5. **`CreateNuclearJEI`** — champ statique mutable `Categories`, vidé/reconstruit à chaque `registerCategories` ; risque si JEI ré-appelle le cycle (reload ressources). *(non revérifié ce tour, signal inchangé)*
6. **`CNPonderReactorScenes.showReactorStructure`** — boucle triple (~11×13×13) avec comparaisons positionnelles ; coût ponctuel (ouverture ponder), remplaçable par une `Map` précalculée.
~~7. **`ReactorFrameDisplayManager.write`** — persiste systématiquement les sentinelles `Integer.MAX_VALUE`/`MIN_VALUE` même quand `hasFrameColumn()` est faux — pollution NBT mineure.~~ ✅ **Corrigé le 2026-07-11** — `write()` (`ReactorFrameDisplayManager.java:129-134`) n'écrit plus `COMPONENT_FRAME_COLUMN_MIN_Y`/`MAX_Y` que si `hasFrameColumn()` est vrai. Sans risque : `read()` (l.118-126) protège déjà chaque champ par `compound.contains(...)`, donc l'absence de clé laisse simplement les champs à leurs valeurs par défaut (identique à l'état pré-assignation).
~~8. **Duplication `drawGauge`** *(nouveau point, déjà signalé dans `AUDIT_V1` mais jamais repris dans ce document — confirmé toujours présent)* — la méthode `drawGauge(...)` est copiée à l'identique dans `FuelDisplaySource.java:49-51`, `CoolerDisplaySource.java:52-54`, `HeatDisplaySource.java:47-50`, `LiquidLevelDisplaySource.java:43-45` et `ReactorSummaryDisplaySource.java:178-180`, avec des magic numbers (`maxFuel=64`, `maxCooler=64`, `maxFluid=16000`, `maxHeat=1000`) éparpillés dans chaque classe. Aucune classe abstraite commune. Risque faible, mais 5 points de maintenance pour 1 seule logique de rendu — extraire une `AbstractReactorStatDisplaySource`.~~ ✅ **Corrigé le 2026-07-11** — `ReactorGaugeRenderer.drawGauge` (nouvelle classe utilitaire) centralise le rendu, appelé par les 4 sous-classes via le nouveau template `AbstractReactorStatDisplaySource` (`FuelDisplaySource`, `CoolerDisplaySource`, `HeatDisplaySource`, `LiquidLevelDisplaySource` héritent désormais de cette classe abstraite au lieu de dupliquer `provideLine`/`drawGauge`) et directement par `ReactorSummaryDisplaySource` (qui reste sur `DisplaySource`, structure multi-lignes incompatible avec le template). Les magic numbers des 4 sous-classes sont centralisés dans `ReactorDisplayConstants` (`MAX_FUEL`, `MAX_COOLER`, `MAX_FLUID`, `MAX_HEAT`). `ReactorSizeDisplaySource` reste volontairement en dehors (sémantique différente : jauge à 3 segments discrets par tier, pas un ratio `current/max` continu — n'avait de toute façon pas de duplication de `drawGauge`). `ReactorSummaryDisplaySource.getComponents()` (l.150-153) référence désormais aussi `ReactorDisplayConstants.MAX_FUEL/MAX_COOLER/MAX_FLUID/MAX_HEAT` au lieu de magic numbers en dur — déduplication complète sur les 5 classes.

---

## 3. 🏗️ Architecture — état réévalué

| Affirmation historique | Verdict actuel (revérifié 2026-06-28) |
|---|---|
| **Inversion `api/`** | ❌ **Toujours réfuté** — `MultiBlockManagerBeta`, `api/multiblock/rods/RodType`, `api/multiblock/fluid/ReactorFluidType` n'importent toujours aucun `content.*` (imports vérifiés ligne par ligne). Aucune action. |
| **Deux/trois frameworks multiblock concurrents** | ❌ **Toujours réfutté** — `lib/multiblock/manager/*` et `IBetterPattern` n'existent toujours pas (8 fichiers restants dans `lib/multiblock/`, tous utilisés). Le commit `20c1df0b` (*"IO blocks are now natively Casings"*) ne change rien à l'architecture : il nettoie 22 lignes d'une scène Ponder, les blocs I/O restent des `Block` séparés. |
| **God class `ReactorControllerBlockEntity`** | ✅ **Chantier toujours clos, classe en légère croissance maîtrisée** — **529 lignes** (492 au dernier audit), délégation à **34 fichiers** de support : `service/` (14, était 8), `manager/` (12, était 5), `consumable/` (6), `display/` (2). La croissance suit l'ajout de fonctionnalités (gestion de la frame, alarmes, etc.), pas une régression de la délégation — le constructeur injecte toujours les dépendances via interfaces (`IHeatService`, `IPersistenceService`, etc.). Pas d'action. |
| **Coordinateur de verrou fluide** | ✅ **Toujours non justifié** — `PersistentFluidLocks` reste le seul système ; pas de `FluidLockManager` concurrent retrouvé. `clearLockIfAllInputsEmpty` utilise toujours `inputFluidManager.getFuildHandlers(level)` (pas de scan 3D), avec une double itération mineure (boucle sur handlers puis sur `getTanks()`) sans gravité. |
| **`src/test`** | ❌ **Toujours vide** — confirmé, 0 fichier `.java` sous `src/test`. Le point structurel le plus important du projet reste entier. |
| **Radiation étendue à `LivingEntity` (nouveau, `9ded502c`)** | ✅ **Extension propre** — le point de vérité unique `canBeIrradiated(LivingEntity)` est toujours respecté (un seul appel, `RadiationCapability.java:107`), le hook passe par `LivingTickEvent` (pas de boucle manuelle sur les entités du monde). ⚠️ **Point à surveiller, pas un bug** : pour les non-joueurs, `tickRadiation` recalcule `computeItemRadiation` à **chaque tick sans dirty-check** (le hash d'inventaire existant ne couvre que le joueur, l.90-95 vs l.96-98) — assumé dans le commit (« no equivalent inventory to diff against »), mais à reconsidérer si la densité de mobs équipés en zone irradiée augmente. |
| **Advancements de pose centralisés sur le contrôleur (nouveau, `19617118`)** | ✅ **Amélioration confirmée** — `MultiblockHelpers.handleAdvancedPlacedBy` (l.58-66) redirige `setPlacedBy` vers le contrôleur via `getControllerForPart`, et `CNAdvancementBehaviour.java:106` vérifie maintenant `!advancement.isAlreadyAwardedTo(player)` (état Minecraft natif) au lieu d'une liste interne. Élimine un risque de double-attribution. Limite résiduelle (pas une régression) : si le contrôleur n'est pas encore résolu au moment de la pose, l'advancement de pose est silencieusement sauté pour cette pièce. |

**Vrai writ-large structurel : l'absence de tests**, inchangé — c'est toujours là (chaleur, pattern matching, verrouillage fluide, et maintenant le double-scan multiblock) que les bugs silencieux survivent aux audits successifs.

---

## 4. 🧹 Dead code & features inachevées — reste à faire

> **⚠️ Faux positifs à NE PAS supprimer** : `CNTabulaModelRenderUtils` (rendu vivant du champignon atomique) ; `ReactorOutputEntity.outputPos` (lu/écrit en NBT) ; `setRotateAngle` (appelé par `NuclearMushroomCloudModel`).

**Pur dead code (retrait sûr)**
- ~~**`lib/multiblock/SimpleMultiBlockPattern.java` méthode `test()`**~~ ✅ **Retiré** — confirmé par `git diff` : la méthode a disparu du fichier de travail, aucun appelant n'était présent.
- ~~**`IrradiatedBiomes.monsters()`**~~ ✅ **Retiré le 2026-07-12** (`8b0428e3`) — la méthode (corps vide) et son unique site d'appel dans le builder du biome irradié ont été supprimés ; les réglages de spawn du biome irradié passent désormais uniquement par `MobSpawnSettings.Builder`.

**Code commencé puis abandonné (hygiène de fin de PR)**
- ~~`content/multiblock/input/fluid/PlayerInteractReactorFluidInput.java:57-61` — quatre problèmes de dead code~~ ✅ **Déjà corrigé le 2026-07-03** (`f2490469`) — **stale depuis la mise à jour précédente de ce document** : ce commit avait renommé le fichier (suppression du 'e' parasite dans "Interacte") et **supprimé** les 4 problèmes (variable morte `fluidInItem`, bloc commenté référençant `ReactorLiquidInput` inexistant, bloc vide `TANK_TO_ITEM`, `instanceof` toujours vrai) directement dans le `.java`, mais la description ci-dessus n'avait, par erreur, pas été retirée de l'audit — elle avait au contraire été détaillée davantage dans ce même commit. Vérifié par lecture directe du fichier actuel (`PlayerInteractReactorFluidInput.java`) : plus aucun des 4 problèmes n'est présent.
- `content/multiblock/output/ReactorOutput.java:43,52,87` — propriété `SPEED` toujours entièrement commentée (pas supprimée). *(Numéros de lignes mis à jour : l.93 → l.87 après décalage.)*
- `foundation/data/recipe/CNStandardRecipeGen.java:226` — `// FIXME 5.1 refactor - recipe categories as markers...` toujours présent, à trancher.

~~**Reliquats Git (`run/`)** — confirmé via `git ls-files run/` : il reste **6 fichiers de debug/env trackés** malgré `.gitignore` :
`run/hs_err_pid21100.log`, `run/hs_err_pid27848.log`, `run/imgui.ini`, `run/servers.dat`, `run/servers.dat_old`, `run/mods/Jade-1.20.1-Forge-11.12.2.jar.disabled`.~~
⚠️ **Conserver** les 10 `run/schematics/*.nbt` (assets de gameplay légitimes).

~~**Collier teignable chat/loup non câblé**~~ ✅ **Résolu le 2026-07-11** — décision prise : retrait complet de la mécanique plutôt que câblage. `IrradiatedCatCollarLayer.java` et `IrradiatedWoldCollarLayer.java` ont été supprimés (aucun appelant ne les référençait, confirmé par grep). Dans `IrradiatedCat.java`, le champ `DATA_COLLAR_COLOR`, les accesseurs `getCollarColor()`/`setCollarColor()` et le bloc `readAdditionalSaveData`/`addAdditionalSaveData` associé ont été retirés ; l'interaction `DyeItem` dans `mobInteract` a été retirée (le fix a complété une suppression partielle laissée par l'édition en cours, qui laissait `mobInteract` appeler des méthodes déjà supprimées — code non compilable en l'état intermédiaire). `IrradiatedWolf` n'avait de toute façon jamais câblé cette mécanique (`DATA_COLLAR_COLOR` déjà en commentaire). Note : `getBreedOffspring` (`IrradiatedCat.java:248-260`) appelle toujours `cat.setCollarColor(...)`/`cat2.getCollarColor()`, mais sur des `Cat` vanilla (API native Minecraft), sans rapport avec la mécanique retirée — laissé tel quel.

**Worldgen « irradié » — template non terminé** *(inchangé)*
- `IrradiatedBiomes.addDefaultIrradiatedOres`/`addDefaultSoftDisks` ajoutent toujours du contenu vanilla sans rapport (`BLUE_ICE`, `NETHER_CAVE`, `VOID_START_PLATFORM`).
- `CNNoiseGeneratorSettings.IRRADIATED` utilise toujours `STEEL_BLOCK` comme bloc de remplissage par défaut.

---

## 5. ⚡ Performance — synthèse des points ouverts

| Point | État | Réf. |
|---|---|---|
| `ReactorPattern.findController` (Casing/Cooler/Frame) : **ne court-circuite jamais**, scanne ~3971 positions à chaque pose/casse même après avoir trouvé le contrôleur | ❌ **non corrigé** — chemin le plus chaud, dernier point ouvert | §2.1 |
| `ReactorPattern.findControllerPos` (Output/RodInput/FluidInput/Alarm via `MultiblockHelpers`) : court-circuite bien | ✅ déjà correct | §2.1 |
| Aucune garde `level.isClientSide()` sur toute la chaîne de scan | ✅ **corrigé le 2026-06-28** — garde centralisée dans `ReactorAssembler.assemble`/`disassemble` | §2.1 |
| Double scan à la casse (`playerDestroy` + `onRemove`) sur Casing/Cooler/Output/FluidInput (sans garde) et Alarm/Frame (garde partielle) | ✅ **corrigé le 2026-06-28** — modèle `ReactorRodInput` généralisé aux 6 autres blocs (override `playerDestroy` supprimé) | §2.1 |
| `DefaultHeatCalculator.computeHeat` : neighbor lookup passé de O(n) à O(1) (map), reste une boucle O(81) de recherche de position par rod | 🟡 **partiellement corrigé, gravité réévaluée à la baisse** | §2.2 |
| `clearLockIfAllInputsEmpty` : pas de scan cubique O(n³), double itération mineure | ✅ acceptable | §3 |

---

## 6. ✅ Pour mémoire — déjà corrigé (ne plus reprendre)

Vérifiés résolus dans le code actuel (cumul des tours précédents) : **B2, B3/B4/B5/B6/B8/B9/B11/B13/B16/B19, B12, B14**, off-by-one `getFluidInTank`, icône d'item de l'armure anti-radiation teinte, tous les problèmes radiation historiques (point de vérité unique `canBeIrradiated`).

**Confirmé corrigé le 2026-06-26** : **spam de log `ReactorAssembler.assemble`** — plus aucun `LOGGER.warn`/`info` dans le chemin de succès (commit `b309f231`). *(Quick win n°3 de la feuille de route précédente, traité.)*

**Confirmé corrigé (NuclearExplosionEntity, ex-B15)** : l'ancien anti-pattern `try { onBlockExploded } catch(Exception){ destroyBlock(...,true) }` a disparu. Le code actuel (`NuclearExplosionEntity.java:224-232`) fait une vérification explicite (`if (level().getBlockState(immutablePos).is(state.getBlock())) level().destroyBlock(...)`) au lieu d'avaler une exception. Plus d'anti-pattern de contrôle par exception.

**Confirmé sain, nouveau code 2026-06-25/27** : `CNBuilderTransformers` (extraction propre des builders de modèles d'item/spawn-egg hors de `CNItems`, 97 lignes, SRP respectée) et l'item **Biome Restore Cell** (`BiomeRestoreCellItem`, 75 lignes, logique métier déléguée à `BiomeIrradiationService`, gardes serveur/client correctes) — aucun problème structurel à signaler, rien à ajouter au plan d'action.

**Corrigé le 2026-06-28** : `InventoryHashUtil.stackHash` n'inclut plus `getDamageValue()` (`InventoryHashUtil.java:90-91` retiré). Vérifié : aucune implémentation de `IRadiationSource`/`RadiationRegistry` (`UraniumOreItem`, `RadiationItem`, `RadiationBucketItem`, `RadiationEffect`) ne lit la durabilité, donc l'inclure dans le hash ne faisait que déclencher `RadiationCapability.computeItemRadiation` (`RadiationCapability.java:91-95`) sans raison à chaque variation de durabilité (minage, combat) — précisément sur les hot paths que le cache devait protéger. Pour mémoire, le terme « resync » des audits précédents désignait ce recalcul serveur, pas un trafic réseau (`RadiationProvider` ne fait que sérialiser en NBT, aucun `RadiationSyncPacket` impliqué).

**Corrigé le 2026-07-05** : `IHeat.HeatLevel.isNotDanger` (ex-§1, `IHeat.java:89-91`) — la tautologie `of(heat,size) != DANGER || of(heat,size) != NONE` (toujours `true`) a été remplacée par `of(heat,size) != DANGER`. La garde « ne pas faire tourner les sorties en `DANGER` » (appelant unique `ReactorControllerBlockEntity.java:428`) est désormais réellement opérante : les sorties s'arrêtent en surchauffe. ⚠️ **Changement de comportement de balance réel** (avant : les sorties tournaient toujours ; maintenant : elles s'arrêtent en `DANGER`) — à valider en jeu, un joueur peut désormais se retrouver sans écoulement de sortie tant que le réacteur reste en surchauffe.

**Corrigé le 2026-06-28** : garde `level.isClientSide` manquante sur la chaîne multiblock et double scan `playerDestroy`/`onRemove` (§2.1). Fix appliqué de façon centralisée plutôt que classe par classe : (1) `if (level.isClientSide) return;` ajouté en tête de `ReactorAssembler.assemble`/`disassemble` — point de convergence unique de toute la chaîne `onPlace/onRemove/playerDestroy → findController/findControllerPos → ReactorAssembler` ; (2) l'override `playerDestroy` a été supprimé de `ReactorCasing`, `ReactorCooler`, `ReactorFrame`, `ReactorAlarm`, `ReactorOutput` et `ReactorFluidInput`, généralisant le modèle déjà correct de `ReactorRodInput` (un seul appel de retrait, dans `onRemove`, qui se déclenche systématiquement y compris lors d'une casse joueur). Vérifié par lecture directe des 7 fichiers modifiés. Seul le court-circuit manquant de `findController` (perf pure) reste ouvert — voir §2.1 et §7.

**Corrigé le 2026-06-28** (`562ce99d` + `30de52a7`) : désynchronisation `CNItemTags.FUEL`/`COOLER` vs `RodType.type()` (ex-§2 point 8, assigné Gio). `RodType.TypeRodPredicate.IS_FUEL`/`IS_COOLED` (anciens `Predicate<ItemStack>` basés sur `ItemRodTypesValue.getRodType`, sans `Level`) ont été remplacés par `isFuel(ItemStack, Level)`/`isCooled(ItemStack, Level)` basés uniquement sur `RodType.resolveRodType` (`RodType.java:303-309`). `Level` a été enfilé jusqu'à `IHeatCalculator.computeHeat`/`HeatManager.calculateHeat`/`DefaultHeatCalculator.computeHeat`. Premier commit (`562ce99d`) avait migré 5 des 6 sites mais laissé deux endroits sur l'ancien critère mixte `stack.is(TAG) || rod.type() == X` (`ReactorBluePrintMenu.java:165-166`) ou l'ancienne API sans `Level` (`DefaultHeatCalculator.java:85`, lookup du voisin) ; second commit (`30de52a7`) les a corrigés. Vérifié par grep sur tout `src/main/java` : plus aucun site de classification ne lit `CNItemTags.FUEL`/`COOLER` ou n'appelle `ItemRodTypesValue.getRodType` en dehors de l'implémentation interne de `RodType.resolveRodType` lui-même. Le tag `CNItemTags.FUEL` reste posé sur `THORIUM_ROD` (`CNItems.java:220`) à titre de tag de craft/recette, explicitement découplé de la classification — cohérent, pas un résidu de désync.

**Confirmé neutre** : le commit `20c1df0b` (*"IO blocks are now natively Casings in NBT"*) ne fusionne rien dans le code de jeu — nettoyage cosmétique d'une scène Ponder uniquement.

**Corrigé le 2026-07-11** : `ReactorFrameDisplayManager.write` (ex-§2 point 7) persistait systématiquement les sentinelles `Integer.MAX_VALUE`/`MIN_VALUE` même quand `hasFrameColumn()` était faux. `write()` (`ReactorFrameDisplayManager.java:129-134`) n'écrit désormais `COMPONENT_FRAME_COLUMN_MIN_Y`/`MAX_Y` que si `hasFrameColumn()` est vrai. Sans risque de régression : `read()` (l.118-126) protège déjà chaque champ par `compound.contains(...)`, donc l'absence de clé laisse les champs à leurs valeurs par défaut (identique à l'état pré-assignation).

**Corrigé le 2026-07-03** : `PlayerInteractReactorFluidInput` (ex-§4, renommage + 4 problèmes de dead code). Fichier renommé (`PlayerInteracteReactorFluidInput` → `PlayerInteractReactorFluidInput`, suppression du 'e' parasite) et nettoyé (`f2490469`) : variable morte `fluidInItem`, bloc commenté référençant la classe inexistante `ReactorLiquidInput`, bloc vide `TANK_TO_ITEM`, et `instanceof ReactorFluidInputEntity` toujours vrai (branche morte) — tous supprimés. *(Correction bien réelle dans le code depuis le 2026-07-03, mais était restée décrite comme non résolue dans ce document jusqu'au 2026-07-12 — staleness de documentation repérée et corrigée à cette date.)*

**Corrigé le 2026-07-12** : `IrradiatedBiomes.monsters()` (ex-§4, `8b0428e3`) — méthode au corps vide et son unique site d'appel (arguments 95/5/100 silencieusement ignorés) supprimés ; les réglages de spawn du biome irradié passent désormais uniquement par `MobSpawnSettings.Builder`, sans couche de délégation morte.

**Corrigé le 2026-07-11** : duplication `drawGauge` (ex-§2 point 8). Nouvelle classe `ReactorGaugeRenderer.drawGauge` centralise le rendu de jauge, jusque-là copié à l'identique dans 5 classes. `FuelDisplaySource`, `CoolerDisplaySource`, `HeatDisplaySource` et `LiquidLevelDisplaySource` héritent désormais d'un nouveau template `AbstractReactorStatDisplaySource extends NumericSingleLineDisplaySource` (`provideLine`, `initConfigurationWidgets`, `allowsLabeling` mutualisés ; chaque sous-classe ne fournit plus que `getLabelKey`, `getMax`, `computeValue`, `getColor`, et optionnellement `getUnitSuffix`). `ReactorSummaryDisplaySource` reste sur `DisplaySource` (structure multi-lignes non compatible avec le template single-line) mais appelle désormais `ReactorGaugeRenderer.drawGauge` au lieu de sa propre copie. Les magic numbers des 5 classes (les 4 sous-classes migrées **et** `ReactorSummaryDisplaySource`, l.150-153) sont désormais tous nommés dans `ReactorDisplayConstants`. Vérifié par lecture directe des 8 fichiers concernés et par `Grep` confirmant l'usage de `ReactorDisplayConstants.MAX_*` partout où figuraient auparavant `64`/`16000`/`1000` en dur. `ReactorSizeDisplaySource` a été délibérément exclue du template (jauge à 3 segments discrets par tier, sémantique incompatible avec un ratio `current/max`) — elle n'avait de toute façon pas de `drawGauge` dupliqué. Déduplication complète, aucun point résiduel.

---

## 7. 🗺️ Feuille de route — priorités

**Quick wins (risque quasi nul, gain immédiat)**
1. **`ReactorPattern.findController` : faire retourner `inRange` au lieu de `false` dans le visiteur** (l.69), pour court-circuiter comme `findControllerPos` le fait déjà. **← dernier point ouvert de ce chantier, le plus rentable : c'est le chemin appelé par le bloc le plus posé/cassé (Casing).**
2. ~~Garde `level.isClientSide()` en tête de `findController`/`findControllerPos`/`ReactorAssembler.assemble/disassemble`.~~ ✅ **fait le 2026-06-28** (garde centralisée dans `ReactorAssembler.assemble`/`disassemble`).
3. ~~Généraliser le modèle `ReactorRodInput`...~~ ✅ **fait le 2026-06-28** — `playerDestroy` supprimé de `ReactorCasing`, `ReactorCooler`, `ReactorFrame`, `ReactorAlarm`, `ReactorOutput`, `ReactorFluidInput` ; la logique de retrait vit désormais uniquement dans `onRemove` pour ces 6 classes (comme `ReactorRodInput`). Point résiduel mineur non traité : `ReactorOutput`/`ReactorFluidInput` n'ont pas la garde `!state.is(newState.getBlock())` dans `onRemove` malgré leurs propriétés mutables `DIR`/`FACING` (cf. §2.1).
4. ~~`IHeat.HeatLevel.isNotDanger` : corriger la tautologie (`!= DANGER` seul), après confirmation balance.~~ ✅ **fait le 2026-07-05** — voir §6 pour le changement de comportement associé à valider en jeu.
5. ~~`git rm --cached` sur les 6 fichiers de debug `run/` (§4).~~ ✅ **fait le 2026-06-28**
6. ~~Retirer `SimpleMultiBlockPattern.test()`~~ ✅ **fait le 2026-06-28** (commit `2aaddb4d`) et ~~retirer `IrradiatedBiomes.monsters()`~~ ✅ **fait le 2026-07-12** (commit `8b0428e3`).

**Corrections ciblées (risque faible à moyen)**
7. `ReactorInputFluidManager` : décrémenter `fluidNeeded` entre handlers + gérer `toExtract == 1` (§1).
8. `DefaultHeatCalculator` : précalculer une map slot→position pour éliminer la boucle 81 cellules (gain mineur désormais, priorité baissée par rapport au tour précédent) ; **ne pas** toucher l'asymétrie fuel/cooler sans validation balance.
9. ~~Extraire `AbstractReactorStatDisplaySource.drawGauge` pour dédupliquer les 5 copies identiques.~~ ✅ **fait le 2026-07-11** (§2.8).
10. Flicker vitesse de sortie en fin de ressources (oscillateur `overHeat` × division entière `RPM_DIVIDER`) : ajouter une hystérésis sur le heat appliqué à `rotateOutputs`, seuil à valider (§1).

**Décisions produit (choisir puis exécuter)**
~~11. Collier teignable chat/loup : finir le câblage (`addLayer` + `render` + interaction `DyeItem` sur `IrradiatedWolf`) ou retirer toute la mécanique (§4).~~ ✅ **fait le 2026-07-11** — mécanique retirée entièrement (§4).
12. ~~`PlayerInteractReactorFluidInput` *(renommé)* : retirer le bloc créatif ITEM_TO_TANK...~~ ✅ **fait le 2026-07-03** (`f2490469`, staleness de documentation corrigée le 2026-07-12 — voir §6). *Reste ouvert* : `ReactorOutput.SPEED` — retirer les 3 commentaires (l.43, 52, 87) ou décider si la propriété doit être réactivée (§4).

**Chantier de fond (le plus rentable à long terme)**
13. Démarrer une couverture de tests sur `DefaultHeatCalculator`, le pattern matcher (`ReactorPattern`/`CNMultiblock`) et `PersistentFluidLocks` — les trois zones où des bugs/inefficacités silencieuses ont survécu à plusieurs audits successifs.

**À surveiller sans action immédiate**
14. `RadiationCapability.tickRadiation` pour les `LivingEntity` non-joueurs : pas de dirty-check d'inventaire (recalcul à chaque tick) — compromis assumé par le commit `9ded502c`, à reconsidérer seulement si la densité de mobs équipés irradiés pose un problème de perf mesuré.

**Explicitement retiré du plan (ne plus y revenir sans nouvel élément)**
- Découpage supplémentaire de `ReactorControllerBlockEntity` (chantier clos, croissance maîtrisée).
- « Correction » de l'inversion de dépendance `api/` (n'existe pas).
- Fusion/suppression du pipeline multiblock (sain).
- Coordinateur dédié au verrouillage fluide (non justifié).
- Spam de log `ReactorAssembler` (corrigé le 2026-06-26).
- Anti-pattern try/catch `NuclearExplosionEntity` / B15 (corrigé).
- Tags `CNItemTags.FUEL`/`COOLER` désynchronisés de `RodType.type()` (assigné Gio, **terminé le 2026-06-28** via `562ce99d` + `30de52a7` — voir §6).
