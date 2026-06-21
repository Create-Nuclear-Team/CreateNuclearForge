# Audit indépendant — CreateNuclearForge (état au 2026-06-20, branche V2-CorrectifAudit)

## Méthodologie

Cette analyse part du code actuel (`src/main/java`, 286 fichiers, ~24 600 lignes — contre 282 fichiers / ~21 000 lignes au moment d'`AUDIT_V1.md`) et de l'historique Git, **pas** des conclusions d'`AUDIT_V1.md` / `AUDIT_ACTUEL.md`. Chaque point repris de ces deux documents a été revérifié directement dans le code (lecture de fichier, grep des appelants, lecture des call-sites) avant d'être classé. Deux audits internes générés en parallèle se sont contredits sur l'état du scan multiblock ; j'ai tranché en relisant moi-même `ReactorPattern.java`, `ReactorCasing.java` et `ReactorAssembler.java` plutôt que de reporter une conclusion non vérifiée.

**Limite de couverture honnête** : tous les bugs mineurs/robustesse listés en fin de section 2 d'`AUDIT_V1.md` (B7, B10, B17, B18 et la liste "mineurs" non numérotée) n'ont pas été revérifiés ici faute de signal qu'ils aient changé — ils ne sont ni confirmés ni infirmés dans ce document, à re-vérifier ponctuellement avant de les traiter.

---

## 1. Constat global

Le refactor V2 annoncé dans les deux audits précédents n'est plus "en cours et à moitié fait" pour son chantier principal : **la décomposition de `ReactorControllerBlockEntity` est maintenant réellement effective** (commits `dd918582`, `6e717c44`, `8611ed76`, `56373bf7`, `39092ca3`). C'est le changement le plus net par rapport aux deux audits précédents, qui tous deux pointaient cette classe comme le problème architectural n°1.

En contrepartie, deux affirmations structurelles d'`AUDIT_V1.md`, reprises sans being revérifiées par `AUDIT_ACTUEL.md`, se révèlent **fausses sur le code actuel** :
- L'« inversion de dépendance dans `api/` » n'existe pas (vérifié par lecture directe des imports).
- Les « deux/trois frameworks multiblock concurrents » ne sont pas concurrents : c'est un pipeline séquentiel (scan géométrique → matcher formel → données datapack), dont seule la première étape pose un vrai problème.

À l'inverse, un bug très simple et jamais mis en doute (`B15`) est **toujours présent** alors qu'il est trivial à corriger — signe que le nettoyage a suivi les zones où les audits insistaient (radiation, dead code, controller) plutôt qu'une revue systématique de toutes les lignes signalées. `B12` a été corrigé pendant la rédaction de ce document (voir §2.2).

---

## 2. Revue des recommandations historiques

### 2.1 À conserver (toujours valides aujourd'hui)

| Réf. | Constat | Pourquoi ça tient toujours |
|---|---|---|
| B15 | `NuclearExplosionEntity` (~l.224-228) utilise `try{...}catch(Exception){destroyBlock(...,true)}` comme branchement de contrôle, avec sémantique de drop différente entre les deux branches | Toujours présent, anti-pattern réel (avale les vrais bugs) |
| ReactorInputFluidManager | `getFluidInTank(getTanks())` (off-by-one, viole le contrat `IFluidHandler`) ; `extractFluids` ne décrémente jamais `fluidNeeded` ; ignore l'extraction quand `toExtract>1` | Confirmé toujours présent (l.127, l.146-157). Toléré aujourd'hui parce que `SmartFluidTank` est mono-tank et ignore l'index, mais reste un vrai bug de contrat, pas une légende d'audit |
| Aucun test | `src/test` toujours vide (0 fichier) | Confirmé. Pour un mod avec une logique métier non triviale (calcul de chaleur, pattern matching, verrouillage fluide), c'est la dette la plus structurelle du projet — bien plus que la forme des classes |
| Dead code animation (`chainSwing`, `chainWave`, `chainFlap`, `faceTarget`, `walk`, `flap`, `bob`, `moveBox`, `progressRotation*`, `transitionTo`, etc.) | Grep confirme : zéro appelant dans tout le repo, en dehors de leurs propres définitions internes | Toujours mort. `AUDIT_ACTUEL.md` avait raison de noter que le retrait est non trivial (cascade interne dans `CNAdvancedModelBox`) — ce n'est pas un "quick win" malgré l'apparence |
| `RadiationOverlay` + `EasingHudOverlay` | Toujours commenté dans `HudRenderer.overlays` ; `HelmetOverlay.setCoverage` écrit toujours dans un champ que personne ne rend | Confirmé inchangé depuis `AUDIT_ACTUEL.md` — décision produit à prendre (supprimer ou réactiver), pas un bug en soi |
| `SimpleMultiBlockPattern.test()` | Méthode sans appelant | Confirmé toujours mort, retrait trivial et sans risque |
| `IrradiatedBiomes.monsters()` | Corps vide, appelé avec des paramètres ignorés silencieusement | Confirmé inchangé |
| Collier teignable chat/loup non câblé | `IrradiatedWoldCollarLayer.render()` toujours vide, aucun `addLayer(...)` enregistrant `IrradiatedCatCollarLayer` | Confirmé inchangé — feature visiblement abandonnée en cours de route (`IrradiatedWolf` a du code commenté `DATA_COLLAR_COLOR` qui le prouve) |

### 2.2 À supprimer du plan (obsolètes ou réfutées par le code actuel)

| Réf. | Recommandation historique | Pourquoi elle ne tient plus |
|---|---|---|
| AUDIT_V1 §1.1 | « Inversion de dépendance dans `api/` » : `MultiBlockManagerBeta`, `RodType`, `ReactorFluidType` importeraient du `content.*` concret | **Réfuté** par lecture directe des imports actuels : aucun des trois fichiers n'importe quoi que ce soit de `content.*`. `RodType`/`ReactorFluidType` utilisent leurs propres types `*Value` (api) comme fallback. Soit ce couplage a été retiré depuis, soit l'audit V1 décrivait un état antérieur — dans tous les cas, ce n'est plus vrai aujourd'hui |
| AUDIT_V1 §1.3 / §3 | « Deux/trois frameworks multiblock concurrents », `lib.multiblock.manager.*` et `IBetterPattern` comme code mort à supprimer | `lib.multiblock.manager.*` et `IBetterPattern` **n'existent plus dans le repo** — déjà supprimés. Ce qui reste de `lib/multiblock/` (`SimpleMultiBlockPattern`, `SimpleMultiBlockAislePatternBuilder`, `impl/`, `misc/`) est **activement utilisé** par `CNMultiblock` pour construire les patterns 5×5/7×7. Ce n'est pas une couche concurrente mais une étape du même pipeline (scan géométrique → `ReactorAssembler` → `CNMultiblock.REGISTRATE_MULTIBLOCK.findStructure` → données `RodType`/`ReactorFluidType`). Le vrai problème n'est pas la coexistence de "frameworks" mais la première étape de ce pipeline (voir §3.1) |
| AUDIT_V1 §1.2 / §8.5 | « God class » `ReactorControllerBlockEntity`, décomposition "à moitié faite", cible <300 lignes | La classe fait aujourd'hui **492 lignes** et délègue réellement : `service/` (8 fichiers, 557 lignes), `manager/` (5 fichiers, 957 lignes), `consumable/` (6 fichiers, 380 lignes), `display/` (2 fichiers). Le tick, le NBT, la chaleur, l'alarme/meltdown, le tooltip sont tous délégués. Ce qui reste dans la BlockEntity (positions I/O, état du pattern, délégation du verrou fluide) est le périmètre légitime d'un coordinateur de multiblock, pas un hub. **Continuer à viser "<300 lignes" comme objectif en soi serait une erreur** : il n'y a plus de logique métier à extraire, seulement de la coordination d'état — découper davantage ajouterait de l'indirection sans retirer de responsabilité, exactement le défaut qu'`AUDIT_V1.md` reprochait à l'extraction inachevée |
| AUDIT_ACTUEL §4.6 | Coordinateur de verrou fluide (`ReactorFluidLockCoordinator`) jugé non justifié | Confirmé inchangé et la conclusion d'`AUDIT_ACTUEL.md` reste correcte : `tryLockFluid`/`canAcceptFluid`/`clearLock` sont de simples délégations de 3-7 lignes vers `PersistentFluidLocks` (`FluidLockManager`, le double-système, a bien été supprimé). Pas de raison de revenir sur ce point |
| AUDIT_V1 §1.5 | `run/` à versionner dans `.gitignore` | **Déjà fait** : `.gitignore` contient bien les règles `run/*`. Reste un résidu mineur — voir §4.7 |
| B3, B4, B5, B6, B8, B9, B11, B13, B16, B19 | Bugs détaillés dans `AUDIT_V1.md` §2 | Tous vérifiés **corrigés** dans le code actuel (cooldown via `Map<UUID,Long>`, loot/tag du bloc de thorium normalisés, `BiomeTagRule.apply()` retourne une `Condition`, packet blueprint envoie les deux valeurs distinctes, plus de scan null-unsafe dans `clearLockIfAllInputsEmpty`, paliers de radiation à 4 niveaux distincts, logger après null-check, bras de l'armure anti-radiation sur les bons membres, garde `conditions` non-null dans le datagen, `IrradiatedSurfaceRules` v1 disparu). Rien à reprendre ici |
| B14 / `FluidLockManager` | Map statique sans dimension, jamais purgée, doublon de `PersistentFluidLocks` | Fichier **supprimé** du repo, remplacé par `PersistentFluidLocks` seul. Le double-système n'existe plus |
| B12 | `AnimalUtil.isFood` teste un tag NBT `"Ingredient"` jamais écrit au lieu du stack tenu | **Corrigé** pendant la rédaction de ce document : `isFood` teste désormais directement `foodItems.test(stack)` / `extraTest.test(stack)` (le yellowcake reste un cas spécial toujours accepté). Le poulet irradié reconnaît maintenant les graines comme nourriture, plus seulement le yellowcake |
| B2 | `RadiationSyncPacket` jette l'instance sans la stocker | Ce packet **n'existe plus** ; la synchro radiation passe par la capability Forge + `PlayerTickEvent`. Point obsolète |
| AUDIT_ACTUEL §3.3 (partie double-comptage) | `IRadiationSource` vs `RadiationRegistry` : risque de double comptage actif | À nuancer plutôt qu'à supprimer : le code appelle bien les deux mécanismes sans court-circuit (`RadiationCapability` l.106-113), **mais** `RadiationRegistry` lève une `IllegalStateException` à l'enregistrement si un item implémente déjà `IRadiationSource` (l.81-82) — ce qui empêche structurellement qu'un item soit compté deux fois. Le risque n'est donc pas "actif", il est "architecturalement bloqué mais maladroit" : deux mécanismes pour un seul concept, sans qu'aucun item ne cumule les deux aujourd'hui. La duplication de design reste valide à signaler (voir §3.2), mais "double comptage" comme bug concret est à retirer |

### 2.3 À adapter (le constat reste vrai mais la cause ou le correctif a changé)

- **Radiation éparpillée sur plusieurs couches** (`AUDIT_V1.md` §1.4, `AUDIT_ACTUEL.md` §3 point 3) — toujours vrai, mais le détail a évolué : `RadiationEffectHandler.apply()` (l.15-21) applique encore un effet de radiation à **toute** entité affectée par les potions, sans aucune garde (pas de check `enabledItemRadiation`, pas de tag `IRRADIATED_IMMUNE`, pas de check spectateur, pas de résistance anti-radiation) — c'est confirmé identique à ce que décrivait `AUDIT_ACTUEL.md`. La correction proposée dans ce document (extraire un gate binaire commun `RadiationCapability.canBeIrradiated`, réutilisé par `RadiationEffectHandler` et `RadiationEffect`) reste pertinente et n'a pas été appliquée — à conserver comme recommandation concrète, pas comme vague "consolidation à 1 module" (objectif trop large pour être actionnable).
- **`RadiationEffect` reconstruit un `HashSet<EntityType>` + reparse les `ResourceLocation` de la blacklist à chaque entité, à chaque tick** (l.38-43) — confirmé inchangé, et c'est un vrai point de perf (pas juste de style) puisque `isDurationEffectTick` retourne `true` : ce predicate tourne sur toutes les entités proches de tout joueur irradié, à chaque tick. La solution proposée par `AUDIT_ACTUEL.md` (cache statique invalidé au reload config) reste la bonne réponse.

---

## 3. Nouveaux problèmes détectés (absents des deux audits précédents)

### 3.1 Le scan géométrique du contrôleur n'a aucune garde côté client et travaille en double sur un même événement

Lecture directe de `ReactorPattern.java` (l.34-51, 53-71) et de ses appelants (`ReactorCasing`, `ReactorCooler`, `ReactorFrame`, `ReactorOutputEntity`) :

- `findController`/`findControllerPos` scannent toujours ~3 971 blocs (11×19×19, `new BlockPos` par itération, borne `!=`) — confirmé identique aux deux audits précédents.
- **Aucune garde `level.isClientSide`** n'existe nulle part dans la chaîne `Block.onPlace/onRemove/playerDestroy` → `ReactorPattern.findController` → `ReactorAssembler.assemble/disassemble`. Or `ReactorAssembler.assemble`/`disassemble` **mutent l'état de la BlockEntity** (`entity.setAssembled(true)`, `entity.setMultiblockSize(...)`, `entity.removeIOAll()`) et envoient un message au joueur (`sendMessageToPlayer`). Ce n'est pas qu'un coût CPU inutile côté client : c'est une mutation d'état métier potentiellement exécutée deux fois (client + serveur) à chaque pose/casse de bloc autour d'un réacteur.
- Sur `ReactorCasing`, **`playerDestroy` et `onRemove` appellent chacun `pattern.findController(pos, level, false)`** (l.60 et l.66) pour le même événement de casse — donc deux scans complets de 3 971 blocs au lieu d'un. `ReactorCooler`/`ReactorFrame` ont le même schéma à la pose (`onPlace` + un second appel).
- `ReactorAssembler.assemble` logue en `LOGGER.warn` (pas `debug`) à **chaque** réussite de pattern (l.37-38) — donc à chaque pose/casse de bloc qui retrouve le contrôleur, y compris en jouant normalement. C'est un spam de log en conditions normales de jeu, pas seulement en debug.

C'est, de loin, le problème de performance/correction le plus concret du projet aujourd'hui — plus précis que les deux audits précédents qui notaient le coût du scan sans vérifier l'absence de garde client ni le double appel.

**Recommandation** : ajouter `if (level.isClientSide()) return;` en tête de `findController`/`findControllerPos` (ou dans leurs appelants), supprimer l'appel redondant dans `ReactorCasing` (garder `onRemove` qui couvre tous les cas de suppression, retirer celui de `playerDestroy`), passer le `LOGGER.warn` en `LOGGER.debug`. Coût de correction : quelques lignes, risque très faible, gain net sur chaque pose/casse de bloc en partie multijoueur.

### 3.2 `DefaultHeatCalculator.computeHeat` — coût quadratique avec désérialisation NBT répétée, et asymétrie fuel/cooler dans le calcul de proximité

`content/multiblock/reactorLogic/DefaultHeatCalculator.java` (l.30-77), appelé à chaque tick de calcul de chaleur quand le réacteur est assemblé :

- Pour chaque item du pattern (jusqu'à 57 slots), la méthode refait une boucle 9×9 (81 cellules) pour localiser sa position, puis pour chacun des 4 voisins, une **nouvelle boucle sur la totalité des items** (l.59) pour retrouver le voisin par son slot — soit, dans le pire cas (réacteur plein), de l'ordre de 57 × 81 × 4 × 57 ≈ 1 050 000 itérations élémentaires par tick, chacune désérialisant un `ItemStack` depuis NBT (`ItemStack.of(list.getCompound(i))`, l.36 et l.62) sans aucun cache.
- C'est un coût bien réel à chaque tick d'un réacteur plein, indépendant du scan multiblock (§3.1) — non documenté dans les deux audits précédents, qui ne couvraient pas `reactorLogic/`.
- **Asymétrie de calcul** (l.61-69) : le bonus/malus de proximité n'est calculé que lorsque `currentRod` vaut `"fuel"` (`if ("fuel".equals(currentRod))`, l.61) — un rod de type cooler ne déclenche jamais l'examen de ses propres voisins. Le calcul de proximité est donc unilatéral : il dépend de la position relative fuel→voisin, jamais cooler→voisin. Sans connaître la balance voulue, je ne peux pas affirmer qu'il s'agit d'un bug plutôt que d'un choix de design (un cooler n'a peut-être pas vocation à "détecter" ses voisins, c'est le fuel qui "regarde" s'il est entouré de coolers) — mais la formule `heat += rod.baseRodHeat() / neighborRod.proximityRodHeat()` (l.67, une **division**) à côté de `heat += rod.proximityRodHeat()` (l.65, une **addition** directe) sur la branche fuel mérite une vérification avec les valeurs de config réelles : une division par un coefficient de proximité produit un résultat très sensible aux petites valeurs, contrairement à l'addition symétrique de la branche fuel-fuel.

**Recommandation** : si la fréquence de calcul ne descend pas naturellement (ex. seulement aux changements de pattern plutôt qu'à chaque tick — à vérifier dans `ReactorHeatUpdateCoordinator`/`FluidConsumptionRateCalculator`, hors périmètre de cette lecture), remplacer la boucle de recherche du voisin (l.59) par une `Map<Integer slot, ItemStack>` construite une fois par appel au lieu d'une boucle O(n) répétée par cellule — ça ramène le coût à O(n) au lieu de O(n²) sans changer le comportement. Ne pas toucher à l'asymétrie fuel/cooler sans confirmation du comportement voulu auprès de l'équipe de balance (cf. note mémoire existante : la logique de chaleur a déjà des distinctions intentionnelles entre chemins — ne pas unifier sans vérification).

### 3.3 `IHeat.HeatLevel.isNotDanger` — condition tautologique, confirmée inchangée

```java
public static boolean isNotDanger(int heat, int reactorSize) {
    return of(heat, reactorSize) != DANGER || of(heat, reactorSize) != NONE;
}
```
`X != DANGER || X != NONE` est toujours vrai (un `HeatLevel` ne peut être les deux à la fois) — déjà documenté dans `AUDIT_ACTUEL.md` point 11, toujours présent. Confirmé que son unique appelant (`ReactorControllerBlockEntity`, ~l.391, garde de `outputManager.rotateOutputs`) rend la garde "ne pas faire tourner les sorties en danger" inopérante. Bug isolé, correctif d'une ligne (`!= DANGER` seul, sous réserve de confirmer le comportement voulu — produire les items même en surchauffe n'est peut-être pas dramatique en soi, mais ce n'est manifestement pas ce que le code voulait faire).

### 3.4 Fonctionnalité de teinture de tissu sur l'armure anti-radiation : un système entier, fonctionnel, mais d'utilité à requestionner

Contrairement à ce que pointait `AUDIT_ACTUEL.md` (qui annonçait `getArmorTexture` comme un hook mort en Forge 1.20.1), le système est en réalité câblé : `ClothTagHelper.getArmorTexturePath` est appelé, les tags de tissu sont persistés et lus via le mixin `SmithingTransformRecipeMixin`. Je n'ai pas pu vérifier en jeu si le rendu visuel change réellement (cette vérification nécessiterait de lancer le client) — donc ni confirmation ni infirmation du rendu effectif, juste un signal que le câblage data existe contrairement à ce qu'affirmait l'audit précédent. À reverifier en jeu avant de décider quoi que ce soit ici ; ne pas planifier de suppression sur la base de l'audit précédent.

### 3.5 Reliquats de fichiers `run/` toujours suivis par Git malgré le `.gitignore`

19 fichiers sous `run/` sont encore suivis par Git (`git ls-files`), dont des artefacts clairement non-projet : `run/hs_err_pid21100.log`, `run/hs_err_pid27848.log` (crash dumps JVM), `run/servers.dat`, `run/servers.dat_old`, `run/imgui.ini`, `run/mods/Jade-1.20.1-Forge-11.12.2.jar.disabled`. Le `.gitignore` a bien été corrigé (point 2.2), mais ces fichiers avaient déjà été commités avant la règle et n'ont jamais été détrackés. Les fichiers `run/schematics/*.nbt` en revanche sont vraisemblablement des assets de gameplay légitimes (structures de ponder/reactor) et ne doivent pas être traités comme des déchets de debug.

**Recommandation** : `git rm --cached` sur les 6 fichiers de debug/environnement listés ci-dessus (pas les `.nbt`). Risque nul, gain cosmétique sur la taille du repo.

### 3.6 Code commencé puis abandonné, au-delà du collier teignable déjà documenté

- `PlayerInteracteReactorFluidInput.java` (~l.58-60) : bloc entier commenté, logique d'interaction fluide inachevée.
- `ReactorOutput.java` : propriété `SPEED` commentée, jamais réactivée.
- `foundation/data/recipe/CNStandardRecipeGen.java:226` : `// FIXME 5.1 refactor - recipe categories as markers instead of sections?` — note de refactor laissée en place, à trancher (faire ou retirer le commentaire) plutôt qu'à laisser indéfiniment.

Aucun de ces trois points n'est urgent ; ils sont mentionnés parce qu'ils n'apparaissent dans aucun des deux audits précédents et illustrent que le code "en cours de refactor" laisse régulièrement des fragments derrière lui — un signal d'hygiène de fin de PR (supprimer le code mort avant de merger) plus qu'un problème ponctuel à corriger.

---

## 4. Architecture actuelle — évaluation sans objectif imposé

Je n'applique pas de règle automatique ("classe > N lignes → découper", "ajouter un service/coordinateur par défaut"). Sur cette base :

- **`ReactorControllerBlockEntity` (492 lignes) n'a plus besoin d'être découpée davantage.** La décomposition manager/service/consumable a atteint un point où ce qui reste dans la classe est la coordination légitime d'un multiblock (positions I/O, état de pattern, délégation des verrous). Continuer à découper produirait des classes d'1-2 méthodes dont la seule fonction serait d'exister — le coût de la fragmentation (un fichier de plus à ouvrir pour suivre un flux simple) dépasserait le bénéfice. **Recommandation : clore ce chantier**, sauf si une responsabilité métier concrète et identifiable apparaît plus tard.
- **Le pipeline multiblock (scan géométrique → matcher formel → données datapack) n'est pas un problème de design** — c'est un problème de garde d'exécution (§3.1). Fusionner les trois étapes en un seul mécanisme serait un gros risque (le matcher formel `lib/multiblock` et le scan géométrique `ReactorPattern` ne font pas le même travail : l'un valide une forme exacte, l'autre cherche un contrôleur dans le voisinage d'un bloc qui vient de changer) pour un gain incertain. **Ne pas toucher à la structure ; corriger la garde client et la duplication d'appel.**
- **La feature radiation reste éparpillée sur plusieurs mécanismes**, mais "consolider en un seul module" (recommandation §6 d'`AUDIT_V1.md`) est un objectif trop large pour être actionné directement. Le point concret et actionnable est `RadiationEffectHandler` sans garde (§2.3) — corriger ce point réduit le risque réel (radiation appliquée à un joueur protégé) sans nécessiter de réorganisation de packages.
- **`api/` n'a pas le problème d'inversion de dépendance qu'on lui prêtait** — aucune action nécessaire ici.
- **Absence de tests reste le vrai writ-large structurel** : ni la taille des classes, ni le nombre de packages radiation, ne sont aussi limitants pour la maintenabilité à long terme que l'absence totale de couverture sur la logique de chaleur, de pattern matching et de verrouillage fluide — ce sont précisément les zones où les bugs silencieux (B12, l'asymétrie de `DefaultHeatCalculator`, la tautologie d'`IHeat`) survivent malgré plusieurs passes d'audit successives.

---

## 5. Feuille de route réaliste

**Corrections triviales, risque quasi nul (à faire en premier, gain immédiat)**
1. Garde `level.isClientSide()` sur la chaîne `findController`/`findControllerPos`/`ReactorAssembler` (§3.1).
2. Retirer l'appel redondant à `findController` dans `ReactorCasing.playerDestroy` (garder `onRemove`) ; vérifier `ReactorCooler`/`ReactorFrame` pour le même schéma à la pose.
3. `ReactorAssembler` : `LOGGER.warn` → `LOGGER.debug` (l.37).
4. `IHeat.HeatLevel.isNotDanger` : corriger la tautologie (`!= DANGER` seul), après confirmation du comportement voulu.
5. ~~`AnimalUtil.isFood` (B12) : tester le stack tenu, pas le tag NBT mort.~~ **Fait.**
6. `NuclearExplosionEntity` (B15) : remplacer le try/catch de contrôle par une vérification explicite.
7. `git rm --cached` sur les 6 fichiers de debug suivis sous `run/` (§3.5).

**Corrections ciblées, risque faible à moyen**
8. `RadiationEffectHandler.apply` : ajouter les mêmes gardes que `RadiationEffect`/`RadiationCapability` (config, immunité, spectateur, résistance) — recommandation déjà détaillée dans `AUDIT_ACTUEL.md`, toujours valable et non appliquée.
9. `RadiationEffect` : cache statique pour la blacklist d'entités au lieu de la reconstruire à chaque entité/tick.
10. `ReactorInputFluidManager` : corriger l'off-by-one `getFluidInTank(getTanks())` et la non-décrémentation de `fluidNeeded`, même si le bug est aujourd'hui toléré par l'implémentation mono-tank actuelle — il deviendra actif au premier `SmartFluidTank` multi-tank.
11. `DefaultHeatCalculator` : remplacer la recherche de voisin O(n) répétée par une map précalculée par appel (réduit O(n²) → O(n)) ; vérifier séparément (avec l'équipe balance) si l'asymétrie fuel/cooler et la division `baseRodHeat()/proximityRodHeat()` sont voulues.

**Décisions produit (pas des bugs — choisir puis exécuter)**
12. Collier teignable chat/loup : finir le câblage (`addLayer` + implémenter `IrradiatedWoldCollarLayer.render`) ou retirer la mécanique `DyeItem`/`setCollarColor`.
13. `RadiationOverlay`/`EasingHudOverlay` : supprimer si `IrradiatedOverlayRendererVision` suffit, ou réactiver et corriger le `Math.round` qui annule le fade.
14. `PlayerInteracteReactorFluidInput` : terminer ou retirer le bloc commenté.

**Chantier de fond, pas urgent mais le plus rentable à long terme**
15. Démarrer une couverture de tests sur `DefaultHeatCalculator`, le pattern matcher (`ReactorPattern`/`CNMultiblock`) et `PersistentFluidLocks` — ce sont les trois zones où des bugs silencieux ont survécu à plusieurs audits successifs sans qu'aucun test ne les détecte automatiquement.

**Explicitement retiré du plan (ne plus y revenir sans nouvel élément)**
- Découpage supplémentaire de `ReactorControllerBlockEntity`.
- "Correction" de l'inversion de dépendance `api/` (n'existe pas).
- Fusion ou suppression du pipeline multiblock à trois étapes (le pipeline est sain, seule l'exécution manque de garde).
- Coordinateur dédié au verrouillage fluide (`ReactorFluidLockCoordinator`) — confirmé non justifié, comme déjà tranché dans `AUDIT_ACTUEL.md`.
