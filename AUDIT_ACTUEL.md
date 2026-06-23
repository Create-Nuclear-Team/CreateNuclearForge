# Audit consolidé — CreateNuclearForge (branche V2-CorrectifAudit)

**Dernière fusion : 2026-06-22.** Ce document fusionne `AUDIT_ACTUEL.md` (7 audits croisés sur `src/main/java`) et `AUDIT_INDEPENDANT.md` (relecture indépendante du 2026-06-20). Chaque point litigieux entre les deux a été **revérifié directement dans le code** (lecture de fichier + grep des appelants) avant classement. Les divergences entre les deux audits sont tranchées et annotées ci-dessous.

> **Mise à jour 2026-06-23** : tous les problèmes radiation (ancienne §2) ont été corrigés et déplacés en §6 « pour mémoire ». Les sections et les points ont été renumérotés en conséquence.

> Ce document ne liste que **ce qui reste à corriger ou à décider**. Les bugs résolus sont récapitulés en §6 pour mémoire, puis ne sont plus repris.

**Périmètre** : `src/main/java` (286 fichiers, ~24 600 lignes), hors `src/generated` et `src/main/resources`. Recoupé avec l'historique Git.

**Limite de couverture honnête** : les bugs mineurs/robustesse `B7, B10, B17, B18` et la liste « mineurs » non numérotée d'`AUDIT_V1.md` n'ont pas été re-vérifiés ici faute de signal de changement — ni confirmés ni infirmés, à re-vérifier ponctuellement avant traitement.

---

## 0. Constat global

Le **chantier architectural n°1 des audits précédents — la décomposition de `ReactorControllerBlockEntity` — est désormais réellement effectif** (commits `dd918582`, `6e717c44`, `8611ed76`, `56373bf7`, `39092ca3`). La classe fait **492 lignes** et délègue à `service/` (8 fichiers), `manager/` (5 fichiers), `consumable/` (6 fichiers), `display/` (2 fichiers). Ce qui reste est de la **coordination de multiblock** (positions I/O, état de pattern, délégation de verrou), plus un hub god-class. **Ce chantier est considéré comme clos** (voir §3).

En contrepartie, deux affirmations structurelles d'`AUDIT_V1.md`, reprises sans re-vérification, sont **fausses sur le code actuel** : l'« inversion de dépendance `api/` » et les « frameworks multiblock concurrents » (voir §3). À l'inverse, des bugs simples et jamais mis en doute (`B15`, tautologie `isNotDanger`) restent présents — signe que le nettoyage a suivi les zones très signalées (radiation, dead code, controller) plutôt qu'une revue ligne à ligne.

La **dette la plus structurelle reste l'absence totale de tests** (`src/test` vide) sur une logique métier non triviale (chaleur, pattern matching, verrouillage fluide) — précisément les zones où des bugs silencieux survivent à plusieurs passes d'audit.

---

## 1. 🐛 Bugs confirmés toujours présents (revérifiés)

| Réf. | Fichier:ligne | Gravité | Détail (vérifié) |
|---|---|---|---|
| **isNotDanger tautologie** | `content/multiblock/IHeat.java:88-90` | 🟠 | `return of(heat,size) != DANGER \|\| of(heat,size) != NONE;` — toujours `true` (un `HeatLevel` ne peut être les deux). Appelant **confirmé `ReactorControllerBlockEntity.java:391`** (et non ~454 comme indiqué auparavant) : la garde « ne pas faire tourner les sorties en `DANGER` » est **inopérante**, les sorties tournent même en surchauffe. Correctif d'1 ligne (`!= DANGER` seul), à confirmer côté balance. |
| **ReactorInputFluidManager — sur-extraction** | `ReactorInputFluidManager.java:146-151` | 🟠 | `fluidNeeded` **n'est jamais décrémenté** entre handlers dans la boucle d'extraction → chaque handler tente d'extraire le besoin complet (sur-extraction si plusieurs inputs). De plus `if (toExtract > 1)` **ignore les extractions de 1 unité**. ⚠️ L'off-by-one `getFluidInTank(getTanks())` est en revanche **corrigé** → `getFluidInTank(0)` (l.126, l.144). *(Tranche le désaccord entre les deux audits : `AUDIT_ACTUEL` avait raison sur l'off-by-one corrigé ; `AUDIT_INDEPENDANT` se trompait en le disant encore présent.)* Toléré aujourd'hui car `SmartFluidTank` est mono-tank, mais reste une violation de contrat `IFluidHandler` qui deviendra active au premier tank multi-slot. |
| **InventoryHashUtil — resync durabilité** | `InventoryHashUtil` | 🟡 | Toujours `h = 31*h + stack.getDamageValue()` → resync radiation à chaque variation de durabilité d'un item. |

---

## 2. 🆕 Autres problèmes (hors radiation)

### 🔴 Critique — performance/correction du scan multiblock

1. **Scan géométrique du contrôleur : aucune garde client + double appel sur un même événement** *(tous confirmés)*
   - `ReactorPattern.findController`/`findControllerPos` (`ReactorPattern.java:34-71`) scannent ~3 971 blocs (11×19×19, `new BlockPos` par itération, borne `!=`) avec **aucune garde `level.isClientSide()`** nulle part dans la chaîne `onPlace/onRemove/playerDestroy → findController → ReactorAssembler.assemble/disassemble`. Or `assemble`/`disassemble` **mutent l'état de la BlockEntity** (`setAssembled`, `setMultiblockSize`, `removeIOAll`) et envoient un message joueur → mutation métier potentiellement **exécutée deux fois** (client + serveur) à chaque pose/casse autour d'un réacteur.
   - **Double appel à la casse** : `ReactorCasing.java` appelle `findController` dans **`playerDestroy` (l.60) ET `onRemove` (l.66)** pour le même événement → deux scans complets. Idem `ReactorCooler.java` (l.39 + l.45). ⚠️ `ReactorFrame.java` est en revanche **plus malin** : son `onRemove` garde `if (!state.is(newState.getBlock()))` (l.128-135) — modèle à généraliser.
   - **Spam de log** : `ReactorAssembler.assemble` logue en `LOGGER.warn` (`ReactorAssembler.java:37-38`) à **chaque** réussite de pattern → spam en conditions normales de jeu.
   - *Recommandation* : `if (level.isClientSide()) return;` en tête de `findController`/`findControllerPos` ; supprimer l'appel redondant dans `ReactorCasing.playerDestroy`/`ReactorCooler.playerDestroy` (garder `onRemove`, idéalement avec la garde de `ReactorFrame`) ; `LOGGER.warn` → `LOGGER.debug`. Risque très faible, gain net en multijoueur. **C'est le problème de perf/correction le plus concret du projet.**

### 🟠 Importants

2. **`DefaultHeatCalculator.computeHeat` — coût ~O(n²) avec désérialisation NBT répétée + asymétrie fuel/cooler** *(confirmé `DefaultHeatCalculator.java:35-75`)*
   Pour chaque item du pattern, boucle sur la grille pour localiser sa position, puis pour chaque voisin **re-boucle sur la totalité des items** (l.59) pour retrouver le voisin par slot — chaque itération désérialise un `ItemStack.of(...)` depuis NBT sans cache (l.36, l.62). Coût pire cas ~57×81×4×57 ≈ 1 050 000 itérations/tick sur réacteur plein. Non couvert par les audits historiques (`reactorLogic/`).
   - **Asymétrie** (l.61) : le calcul de proximité ne fire que si `"fuel".equals(currentRod)` ; un cooler ne déclenche jamais l'examen de ses voisins. De plus `heat += rod.baseRodHeat() / neighborRod.proximityRodHeat()` (l.67, **division**) côté fuel→voisin vs additions ailleurs.
   - *Recommandation* : remplacer la boucle de recherche du voisin (l.59) par une `Map<Integer slot, ItemStack>` construite une fois par appel → O(n) au lieu de O(n²), **sans changer le comportement**. **Ne pas toucher** à l'asymétrie/division sans confirmation balance (la logique de chaleur a des distinctions intentionnelles entre chemins — ne pas unifier sans vérification).

3. **`ReactorSummaryDisplaySource` — sentinelle de taille + accès positionnel fragiles**
   `getComponents()` retourne une liste de taille **1** (pas de contrôleur) ou **6** (normal) ; les appelants testent `components.size() < 6` et `components.get(2).get(1)` accède positionnellement à la ligne « fuel ». Tout ajout/réordonnancement de ligne casse silencieusement ces contrats implicites.

4. **`ReactorSummaryDisplaySource.formatValue` — incohérence de mode**
   En mode « normal » (0), `HeatDisplaySource` affiche `"500 °C"` alors que `ReactorSummaryDisplaySource` affiche une **jauge** pour le heat dans le même mode (`gaugeOnNormal=true` pour heat uniquement) — incohérence visuelle pour un même mode utilisateur.

### 🟡 Mineurs

5. **`CreateNuclearJEI`** — champ statique mutable `Categories` (nom non conventionnel), vidé/reconstruit à chaque `registerCategories` ; risque si JEI ré-appelle le cycle (reload ressources).
6. **`CNPonderReactorScenes.showReactorStructure`** — boucle triple (~11×13×13 ≈ 1859 itérations) avec 6 comparaisons positionnelles par cellule ; remplaçable par une `Map` précalculée. Coût ponctuel (ouverture ponder).
7. **`ReactorFrameDisplayManager.write`** — persiste systématiquement les sentinelles `Integer.MAX_VALUE`/`MIN_VALUE` même quand `hasFrameColumn()` est faux — pollution NBT mineure.
8. **`NuclearExplosionEntity.tick()` (`:96`)** — tri d'une pile de `BlockPos` par `distManhattan` en un tick. ⚠️ La taille réelle est `(2·chunksAffected+1)³` (≈125 pour `size=2`), **pas** 1331 comme indiqué auparavant — coût modeste, corrigé ici.

---

## 3. 🏗️ Architecture — état réévalué (sans objectif imposé)

> Plusieurs « problèmes structurels » des audits V1/ACTUEL sont **réfutés par le code actuel**. On ne raisonne pas par règle automatique (« classe > N lignes → découper »).

| Affirmation historique | Verdict actuel |
|---|---|
| **Inversion `api/`** (`MultiBlockManagerBeta`/`RodType`/`ReactorFluidType` importeraient `content.*`) | ❌ **Réfuté** — aucun des trois n'importe `content.*` ; ils utilisent leurs propres types `*Value` (api). Aucune action. |
| **Deux/trois frameworks multiblock concurrents** | ❌ **Réfuté** — `lib.multiblock.manager.*` et `IBetterPattern` **n'existent plus**. Le reste de `lib/multiblock` est **activement utilisé** par `CNMultiblock`. Ce n'est pas une couche concurrente mais un **pipeline séquentiel** : scan géométrique → `ReactorAssembler` → `CNMultiblock.findStructure` → données `RodType`/`ReactorFluidType`. Le vrai problème n'est pas la coexistence mais la **garde d'exécution de la 1ʳᵉ étape** (§2 point 1). Ne pas fusionner (gros risque, gain incertain). |
| **God class `ReactorControllerBlockEntity`, cible <300 lignes** | ✅ **Chantier clos** — 492 lignes, délégation réelle (service/manager/consumable/display). Ce qui reste est la coordination légitime d'un multiblock. **Continuer à viser <300 lignes serait une erreur** : il n'y a plus de logique métier à extraire, seulement de la coordination — découper ajouterait de l'indirection. |
| **Coordinateur de verrou fluide `ReactorFluidLockCoordinator`** | ✅ **Non justifié** (les deux audits convergent) — `tryLockFluid`/`canAcceptFluid`/`clearLock` sont de simples délégations 3-7 lignes vers `PersistentFluidLocks` (le double-système `FluidLockManager` a été supprimé, B14 résolu). `clearLockIfAllInputsEmpty` n'a plus de scan 3D (utilise `inputFluidManager.getFuildHandlers(level)`). Ne plus y revenir. |
| **`run/` à `.gitignore`** | ✅ Règles `run/*` présentes — mais résidu de fichiers déjà trackés (voir §4). |

**Vrai writ-large structurel : l'absence de tests.** Ni la taille des classes ni le nombre de packages radiation ne limitent autant la maintenabilité que l'absence totale de couverture sur la chaleur, le pattern matching et le verrouillage fluide — exactement là où les bugs silencieux (sur-extraction fluide, asymétrie `DefaultHeatCalculator`, tautologie `IHeat`) survivent à plusieurs audits.

---

## 4. 🧹 Dead code & features inachevées — reste à faire

> **⚠️ Faux positifs à NE PAS supprimer** : `CNTabulaModelRenderUtils` (utilisé par `CNAdvancedModelBox` sur le chemin de rendu vivant du champignon atomique) ; `ReactorOutputEntity.outputPos` (lu/écrit en NBT) ; `setRotateAngle` (appelé ~10× par `NuclearMushroomCloudModel`).

**Pur dead code (retrait sûr)**
- **`SimpleMultiBlockPattern.test()`** (`lib/multiblock/SimpleMultiBlockPattern.java:72-74`) — aucun appelant (grep confirmé). Retrait trivial.
- **`IrradiatedBiomes.monsters()`** (l.17-18) — corps **vide**, appelé `(95, 5, 100)` (`:55`) silencieusement ignorés. À retirer ou implémenter.
- **Méthodes d'animation mortes** dans `CNAdvancedEntityModel`/`CNAdvancedModelBox` (`chainSwing`, `chainWave`, `chainFlap`, `faceTarget`, `walk`, `flap`, `swing`, `bob`, `moveBox`, `progressRotation*`, `progressPosition*`, `getMovementScale`/`setMovementScale`, `transitionTo`, `calculateChain*`, `displayList`/`compiled`) — zéro appelant, mais **retrait non trivial** (cascade interne : `calculateRotation`/`bob` appellent `getMovementScale()`). Nettoyage dédié + dédup `ModelBox`. **Pas un quick win.**

**Code commencé puis abandonné (hygiène de fin de PR)**
- `PlayerInteracteReactorFluidInput.java:57-61` — bloc d'interaction fluide commenté/inachevé.
- `ReactorOutput.java:43,52,114` — propriété `SPEED` entièrement commentée.
- `CNStandardRecipeGen.java:226` — `// FIXME 5.1 refactor - recipe categories as markers...` à trancher.

**Reliquats Git (`run/`)** — 19 fichiers encore trackés malgré `.gitignore`. `git rm --cached` sur les **6 artefacts de debug/env** uniquement :
`run/hs_err_pid21100.log`, `run/hs_err_pid27848.log`, `run/imgui.ini`, `run/servers.dat`, `run/servers.dat_old`, `run/mods/Jade-1.20.1-Forge-11.12.2.jar.disabled`.
⚠️ **Conserver** les `run/schematics/*.nbt` (assets de gameplay légitimes : `reactor*.nbt`, ponder, etc.).

**Décisions produit (pas du pur dead code — choisir puis exécuter)**

- **Collier teignable chat/loup non câblé** *(confirmé)* : `IrradiatedWoldCollarLayer.render()` est un **corps vide** (l.21-22) ; `IrradiatedCatCollarLayer` n'est **jamais enregistré** via `addLayer(...)` (grep confirmé, renderers dans `CNEntityType.java:51,71` sans layer). Pourtant `IrradiatedCat` câble entièrement `DyeItem → setCollarColor/getCollarColor` (`IrradiatedCat.java:334-342`). ⚠️ `IrradiatedWolf` ne câble **même pas** l'interaction dye (`DATA_COLLAR_COLOR` commenté l.61, `mobInteract` sans logique dye). → Un joueur peut teindre le collier d'un chat irradié, l'action est acceptée, **rien ne s'affiche jamais**. *Décision* : enregistrer le layer + implémenter `render`, ou retirer toute la mécanique `DyeItem`/`setCollarColor`.

- ~~**Teinture de tissu sur l'armure anti-radiation — icône d'item non colorée**~~ — **✅ RÉSOLU (2026-06-22), vérifié en jeu**. Le diagnostic des deux audits était incomplet : l'**armure portée** prenait déjà la bonne couleur via `getArmorTexture` (chemin `HumanoidArmorLayer`, qui lit bien le NBT `ClothColor` en Forge 47.x), mais l'**icône d'item** (inventaire + slot résultat de la smithing table) restait sur la texture par défaut car le **modèle d'item est statique** et ne lit aucun NBT — deux chemins de rendu distincts. Correctif appliqué : enregistrement d'une `ItemProperty` client `createnuclear:cloth_color` (`CreateNuclearClient.registerItemProperties`, valeur normalisée `(id+1)/16` pour passer l'écrêtage `[0,1]`) + génération en datagen de 64 modèles enfants colorés et des `overrides` correspondants (`CNItems.coloredArmorModel`, clés texture `layer0`/`particle` pour le casque, `14` pour les autres pièces, pointant vers les sheets `item/armors/<couleur>_anti_radiation_suit` déjà présentes). Le tooltip de pis-aller (`AntiRadiationArmorItem.makeSummary`/`appendHoverText`) et la clé de lang `tooltip.cloth.color` ont été retirés. `getArmorTexture` + `ClothTagHelper` sont donc **bien vivants et fonctionnels** — ni l'un ni l'autre n'est à supprimer.

**Worldgen « irradié » — template non terminé**
- `IrradiatedBiomes.addDefaultIrradiatedOres`/`addDefaultSoftDisks` ajoutent du contenu vanilla sans rapport (`MiscOverworldPlacements.BLUE_ICE`, `Carvers.NETHER_CAVE`, `VOID_START_PLATFORM`) + `monsters()` no-op — pipeline copié/non re-thémé.
- `CNNoiseGeneratorSettings.IRRADIATED` définit `STEEL_BLOCK` comme bloc de remplissage par défaut (équivalent « stone ») — terrain massivement en acier si relié à une dimension. Point d'alerte indépendant des surface rules (corrigées).

---

## 5. ⚡ Performance — synthèse des points ouverts

| Point | État | Réf. |
|---|---|---|
| `ReactorPattern.findController/findControllerPos` : ~3 971 blocs ×2 par casse, **sans garde client**, mutation d'état métier potentiellement double | ❌ non corrigé — **priorité n°1** | §2.1 |
| `DefaultHeatCalculator.computeHeat` : ~O(n²) + désérialisation NBT par cellule, à chaque tick | ❌ non corrigé | §2.2 |
| `HelmetOverlay.renderHotbar` + 3× `getArmor(HEAD)`/frame | ❌ non corrigé | — |
| `clearLockIfAllInputsEmpty` : ancien scan cubique `O(n³)` | ✅ **corrigé** (utilise `inputFluidManager.getFuildHandlers`) | §3 |

---

## 6. ✅ Pour mémoire — déjà corrigé (ne plus reprendre)

Vérifiés résolus dans le code actuel : **B2** (`RadiationSyncPacket` supprimé, synchro via capability + `PlayerTickEvent`), **B3/B4/B5/B6/B8/B9/B11/B13/B16/B19** (cooldown `Map<UUID,Long>`, loot/tag thorium, `BiomeTagRule.apply` → `Condition`, packet blueprint, scan null-safe, paliers radiation 4 niveaux, logger après null-check, bras armure anti-radiation, garde `conditions` datagen, surface rules v1 disparues), **B12** (`AnimalUtil.isFood` teste `foodItems.test(stack)`), **B14** (`FluidLockManager` supprimé, `PersistentFluidLocks` seul), **off-by-one `getFluidInTank`** (→ `getFluidInTank(0)`). Le « double comptage `IRadiationSource` vs `RadiationRegistry` » est **architecturalement bloqué** (`RadiationRegistry` lève `IllegalStateException` à l'enregistrement si l'item implémente déjà `IRadiationSource`) — duplication de design maladroite mais pas de bug concret.

**Corrigé le 2026-06-22** : **icône d'item de l'armure anti-radiation teinte** — l'icône en inventaire / slot résultat de la smithing table suivait pas la couleur du tissu (l'armure *portée*, elle, fonctionnait déjà). Cause : modèle d'item statique, chemin de rendu distinct de `getArmorTexture`. Câblé via `ItemProperty` client `createnuclear:cloth_color` + 64 modèles d'overrides générés en datagen (`CNItems.coloredArmorModel`, `CreateNuclearClient.registerItemProperties`). Tooltip de pis-aller + clé de lang `tooltip.cloth.color` retirés. Détail en §4.

**Corrigé le 2026-06-23** : **tous les problèmes radiation (ancienne §2)**. Cause racine commune des 3 chemins d'application : la logique d'éligibilité était dupliquée et divergente.
- **Point de vérité unique** : nouveau `RadiationCapability.canBeIrradiated(LivingEntity)` (spectateur → tag `IRRADIATED_IMMUNE` → config `enabledItemRadiation` → blacklist → résistance < 1.0), réutilisé par les trois chemins.
- **`RadiationEffectHandler.apply`** (fuite de tuyau, 3ᵉ chemin) : appliquait la radiation **sans aucune garde** → filtre désormais via `canBeIrradiated`.
- **`RadiationEffect`** : le filtre du `VicinityEffect` reconstruisait un `HashSet` + reparsait la blacklist **par entité/tick** → réduit à `canBeIrradiated(e) && !e.hasEffect(...)`. La blacklist est mise en **cache statique** dans `RadiationCapability`, reconstruite uniquement quand l'instance de liste de config change (auto-invalidée au reload, sans event ni couplage `config → content`). Élimine le point de perf §5.
- **`RadiationCapability.applyEffects`** : pas de garde spectateur → `onPlayerTick` gate désormais sur `canBeIrradiated(player)`. L'atténuation continue par résistance reste séparée (non touchée).
- **`RadiationCapability.lastBiomeLocation`** : lu mais non persisté → sérialisé/désérialisé en NBT (`RadiationProvider`).
- **Overlays concurrents** : `RadiationOverlay` (jamais rendu, fade binarisé par `Math.round`) + `EasingHudOverlay` + l'appel no-op `HelmetOverlay.setCoverage`/`COVERAGE_FACTORS` **supprimés** ; `IrradiatedOverlayRendererVision` (fade lisse, garde spectateur) reste seul. Compilation vérifiée (`gradlew compileJava`).

---

## 7. 🗺️ Feuille de route — priorités

**Quick wins (risque quasi nul, gain immédiat)**
1. Garde `level.isClientSide()` sur `findController`/`findControllerPos`/`ReactorAssembler` (§2.1). **← le plus rentable.**
2. Retirer l'appel redondant à `findController` dans `ReactorCasing.playerDestroy` + `ReactorCooler.playerDestroy` (généraliser la garde de `ReactorFrame.onRemove`).
3. `ReactorAssembler` : `LOGGER.warn` → `LOGGER.debug` (l.37).
4. `IHeat.HeatLevel.isNotDanger` : corriger la tautologie (`!= DANGER` seul), après confirmation balance.
5. `NuclearExplosionEntity` (B15) : remplacer le try/catch de contrôle par une vérification explicite.
6. `git rm --cached` sur les 6 fichiers de debug `run/` (§4).
7. Retirer `SimpleMultiBlockPattern.test()` et `IrradiatedBiomes.monsters()`.

**Corrections ciblées (risque faible à moyen)**
8. `ReactorInputFluidManager` : décrémenter `fluidNeeded` entre handlers + gérer `toExtract == 1` (§1).
9. `DefaultHeatCalculator` : map slot→ItemStack précalculée (O(n²) → O(n)) ; **ne pas** toucher l'asymétrie fuel/cooler sans validation balance (§2.2).

**Décisions produit (choisir puis exécuter)**
10. Collier teignable chat/loup : finir le câblage (`addLayer` + `render`) ou retirer `DyeItem`/`setCollarColor` (§4).
11. `PlayerInteracteReactorFluidInput` / `ReactorOutput.SPEED` : terminer ou retirer le code commenté (§4).

**Chantier de fond (le plus rentable à long terme)**
12. Démarrer une couverture de tests sur `DefaultHeatCalculator`, le pattern matcher (`ReactorPattern`/`CNMultiblock`) et `PersistentFluidLocks` — les trois zones où des bugs silencieux ont survécu à plusieurs audits.

**Explicitement retiré du plan (ne plus y revenir sans nouvel élément)**
- Découpage supplémentaire de `ReactorControllerBlockEntity` (chantier clos).
- « Correction » de l'inversion de dépendance `api/` (n'existe pas).
- Fusion/suppression du pipeline multiblock (sain ; seule la garde d'exécution manque).
- Coordinateur dédié au verrouillage fluide `ReactorFluidLockCoordinator` (non justifié).
