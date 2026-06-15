# Réévaluation indépendante — CreateNuclearForge (V2-CorrectifAudit)

**Méthodologie** : 7 audits indépendants couvrant la totalité de `src/main/java` (hors `src/generated` et `src/main/resources`), réalisés à partir de l'état actuel du code uniquement, puis recoupés avec `AUDIT_V1.md` et vérifiés par greps ciblés.

---

## 0. Constat global

Le code est toujours en **refactor V2 actif** (cohérent avec les commits récents : extraction `ReactorInputSnapshotBuilder`, `ReactorGoggleTooltipRenderer`, `ReactorDisplayState`). Quelques nettoyages annoncés par `AUDIT_V1.md` ont bien eu lieu :

- ✅ `lib.multiblock.manager.*` (`MultiBlockManager`, `MultiBlockCache`, `RegisteredMultiBlockPattern`) + `IBetterPattern` + `SimpleMultiBlockPatternBuilder` (non-aisle) **ont disparu** du dépôt.
- ✅ `ReactorAlarmEntity.tick()` distingue maintenant proprement `tickAudio()` (client) / `tickServer()` (serveur) — **corrigé**.
- ✅ `B11` (LOGGER avant null-check dans `SimpleMultiBlockAislePatternBuilder`) — **corrigé**, le message d'erreur utilise désormais un ternaire sûr.
- ✅ La feature radiation a été regroupée sous `content/radiation/**` (capability/client) — déplacement, pas (encore) une consolidation des deux contrats (`IRadiationSource` vs `RadiationRegistry`, toujours dupliqués).
- ⚠️ `B2` (RadiationSyncPacket no-op) est **obsolète** : `RadiationSyncPacket`/`ClientRadiationData` n'existent plus du tout — le mécanisme décrit a disparu (probablement remplacé par la capability resync via `RadiationCapability`/`InventoryHashUtil`, qui elle fonctionne).

La **grande majorité des bugs** de `AUDIT_V1.md` sont **toujours présents**, et plusieurs **nouveaux problèmes** ont émergé dans les zones où les refactors V2 sont en cours (managers `controller/`, displaySources, radiation). En revanche, **le dead code a été en grande partie nettoyé le 2026-06-15** (voir l'encart ✅ du §3) ; il ne reste qu'un résidu non trivial.

---

## 1. 🐛 Bugs critiques toujours présents

| Réf. | Fichier:ligne | État | Détail |
|---|---|---|---|
| **B1** | `foundation/events/overlay/EventTextOverlay.java:40` | **Toujours présent** | `return timer > 0 && false;` — overlay jamais actif. `triggerEvent()` confirmé **jamais appelé** (grep global) → la classe entière est morte, enregistrée et rendue chaque frame pour rien. |
| **B7** | `infrastructure/worldgen/biome/CNNoiseData.java` | **Toujours présent** | `bootstrapRegistries()` reste un corps **vide** ; `EROSION` jamais enregistré dans `Registries.NOISE`, alors que `IrradiatedSurfaceRules` (fusion v1/v2, un seul fichier désormais) fait toujours 3× `noiseCondition(EROSION,...)` → `getOrThrow` lèvera à la génération. |
| **B8** | `content/multiblock/controller/ReactorControllerBlockEntity.java:646` | **Toujours présent, code réécrit mais bug identique** | `clearLockIfAllInputsEmpty()` fait désormais `CNMultiblock.REGISTRATE_MULTIBLOCK.findStructure(level, getBlockPos(), this).data().getSize()` **sans null-check** sur le résultat de `findStructure`, qui peut retourner `null` si la structure n'est plus formée → NPE serveur. La méthode fait en plus un scan cubique `O(n³)` (`getBlockEntity` + capability lookup) alors que `inputFluidManager.getFuildHandlers(level)` donne déjà la liste exacte. |

---

## 2. 🐛 Bugs importants toujours présents

| Réf. | Fichier:ligne | État | Détail |
|---|---|---|---|
| **B3** | `content/effects/VicinityEffect.java:32-49` | **Toujours présent, identique** | `int cooldownTicks = 0;` réinitialisé à chaque itération → branche `else` morte, `cooldowns`/`getCooldown`/`setCooldown` toujours jamais appelés (confirmé grep). `RadiationEffect` continue donc à `addEffect` un `MobEffectInstance(RADIATION,300)` toutes les 5 ticks sur chaque entité proche, sans throttling réel. |
| **B6** | `content/multiblock/bluePrintItem/ReactorBluePrintItemScreen.java` / `ReactorBluePrintItemPacket.java` | **Toujours présent** | Le 6ᵉ argument du packet duplique le 5ᵉ (uranium perdu) ; `coef=0.1F` envoyé à la place de `heat`. |
| **B9** | `content/radiation/capability/RadiationCapability.java` (`applyEffects`) | **Toujours présent, identique** | Les deux premières branches (`radiation < level1`, `radiation < level2`) donnent toutes deux `amplifierLevel0` → `radiationLevel2` reste un palier inopérant. |
| **B12** | `content/contraptions/irradiated/AnimalUtil.java:72-81` | **Toujours présent** | `isFood` ne reconnaît la yellowcake comme aliment **que** si elle porte un tag NBT `"Ingredient"` — incohérent avec `mobInteract` (ligne ~39) qui accepte *toute* yellowcake pour la conversion. Le chemin "food" (élevage/soin) reste cassé pour la yellowcake sans tag. |
| **B13** | `content/equipment/armor/AntiRadiationArmorClientExtensions.java` (case `CHEST`) | **Toujours présent, identique** | `this.model.rightArm.visible = true; this.model.leftArm.visible = true;` (champs hérités, non rendus par le `renderToBuffer` custom) au lieu de `right_arm`/`left_arm`. Bras toujours invisibles sur l'armure anti-radiation en mode plastron. |
| **B14** | `content/multiblock/input/fluid/FluidLockManager.java` | **Toujours présent** | `Map<BlockPos,Fluid>` statique, sans clé de dimension, jamais purgée, doublon de `PersistentFluidLocks` ; toujours appelé depuis `clearLockIfAllInputsEmpty()` (B8) et `ReactorFluidInputEntity.FilteredFluidHandler`. |
| **B15** | `content/explosion/NuclearExplosionEntity.java` (~224) | **Toujours présent** | `try { onBlockExploded } catch(Exception){ destroyBlock(...,true) }` — exception comme branchement, avale les vrais bugs, sémantiques de drop différentes entre les deux branches. |
| **B16** | `foundation/data/recipe/CNStandardRecipeGen.java:351/391` | **Toujours présent (latent)** | `new ModdedCookingRecipeResult(result, compatDatagenOutput, null)` puis `serializeRecipeData` fait `conditions.forEach(...)` sur ce `null` → NPE si une recette `isOtherMod` est un jour sérialisée. |
| **B17** | `foundation/events/RodsTooltipHandler.java:25-26` | **Toujours présent, identique** | `if (id != null && CreateNuclear.MOD_ID.equals(id.getNamespace())) return;` — exclut justement les items du mod (les rods), donc `RodsStats` n'est jamais appliqué via ce handler. Code mort fonctionnel à risque si "corrigé" naïvement (double tooltip avec Registrate). |
| **B19** | `infrastructure/worldgen/biome/surfacerule/IrradiatedSurfaceRules.java` (fusion v1/v2) | **Toujours présent** | `IS_HIGHLANDS = biome()` (varargs vide) → toujours faux ; branches `MOON_DIRT`/`LEAD_TURF` mortes. Les constantes `MOON_DIRT`/`LEAD_ROCK`/`LEAD_TURF`/`RAW_LEAD_BASALT` pointent vers `STEEL_BLOCK`/`LEAD_BLOCK`/`LEAD_ORE`/`RAW_LEAD_BLOCK` — noms totalement déconnectés des blocs réels, signe d'un template non ré-thémé. |

### Bugs mineurs confirmés toujours présents (liste condensée)
- `ReactorInputFluidManager.extractFluids/getBlocksPosition` : toujours `getFluidInTank(getTanks())` (off-by-one), `fluidNeeded` jamais décrémenté, `toExtract>1` ignore les extractions de 1 unité.
- `ReactorInput.use()` : retourne toujours `PASS` côté serveur après `NetworkHooks.openScreen`, vs `SUCCESS` côté client → désync.
- `InventoryHashUtil` : toujours `h = 31*h + stack.getDamageValue()` → resync radiation à chaque variation de durabilité.
- `ReactorSizeDisplaySource` : toujours `tier*100/3` comme "%" — désormais creusé en détail (voir §4, nouveau finding lié).
- `ReactorFluidType.getTypeForFluid` : `ForgeRegistries.FLUIDS.getKey(fluid)` toujours recalculé dans la boucle interne pour une valeur invariante.
- `ReactorOutput.use()` : toujours `Objects.requireNonNull(...)` + `assert entity != null` (no-op en prod) au lieu d'une garde gracieuse.

---

## 3. 🧹 Dead code — reste à faire

> **✅ Corrigé le 2026-06-15** (branche `V2-CorrectifAudit`, compilation validée) — supprimés du dépôt :
> framework d'animation mort (`CNModelAnimator`, `CNAnimation`, `CNIAnimatedEntity`, `CNTransform`, `CNTextureOffset`) + `Maths.cullAnimationTick` + méthodes mortes associées de `CNAdvancedEntityModel` (`rotate`/`rotateMinus`/`setCNTextureOffset`/`getCNTextureOffset`/`modelTextureMap`) ;
> `CreateNuclearDamageSources` (consolidé dans `CNDamageSources`) ; `CommentEventClients.java` ; `foundation/events/possible code` ; `CNShapelessRecipeGen.java` (+ réf. dans `CreateNuclearDatagen`) ; `ReactorPattern.VerifyPattern5x5/7x7/9x9` ; `ReactorBluePrintMenu.saveData2` ; `ReactorAssembler.getPlayersInRadius` ; `ReactorOutputEntity.getStressConfigKey` ; `ReactorInputEntity.inputPos`/`inputLevelKey` ; `ReactorCasingEntity.controller`/`setController`/`getController` ; `TextUtils` (5 helpers) ; `BigFluidStack.isInfinite`/`comparator`/`duplicateWrappers` ; `CExplode` (+ réf. `CNCServer`/`CNCCommon`) ; `CNRecipeTypes.CAN_BE_AUTOMATED` ; `ReactorOutputManager.distributeSU` (+ déclaration dans `ReactorOutputManagerI`) ; imports en double de `CNBlocks.java`.
>
> **⚠️ Corrections de l'audit V1/V2** :
> - `CNTabulaModelRenderUtils` **n'est PAS du dead code** : il est utilisé par `CNAdvancedModelBox.cubeList`/`doRender`, sur le chemin de rendu **vivant** du champignon atomique (`NuclearMushroomCloudModel`). Conservé. La duplication `ModelBox` reste une opportunité de *refactor*, pas une suppression.
> - `ReactorOutputEntity.outputPos` **n'est PAS mort** : il est lu/écrit dans `read()`/`write()` (sérialisation NBT). Laissé en place.

Restant (grep global, zéro référence externe sauf mention) :

- **Méthodes d'animation mortes restantes** dans `CNAdvancedEntityModel`/`CNAdvancedModelBox` : `chainSwing`, `chainWave`, `chainFlap`, `faceTarget`, `walk`, `flap`, `swing`, `bob`, `moveBox`, `setRotateAngle`, `progressRotation*`, `progressPosition*`, `getMovementScale`/`setMovementScale`/`movementScale`, `transitionTo`, `calculateChain*`, `displayList`/`compiled`. Mortes mais **non triviales** : leur retrait déclenche une cascade dans des classes **vivantes** (ex. `CNAdvancedModelBox.calculateRotation`/`bob` appellent `model.getMovementScale()`). À traiter en nettoyage dédié + dédup `ModelBox`.
- **`RadiationOverlay` + `EasingHudOverlay`** : toujours commenté dans `HudRenderer.overlays` ; **`HelmetOverlay.setCoverage(...)` écrit dans le champ statique de `RadiationOverlay`, qui n'est lu par personne** — no-op silencieux (lié au §4 points 5 & 6 : décider entre supprimer `RadiationOverlay` ou réactiver et corriger le fade).
- **`SimpleMultiBlockPattern.test()`** : à vérifier — la classe ne semble plus exister (à confirmer/retirer si résidu).
- **`IrradiatedBiomes.monsters()`** : no-op appelé avec `(95, 5, 100)` silencieusement ignorés — à retirer ou implémenter (lié au §4 point 12, worldgen "irradié" template non terminé).

---

## 4. 🆕 Nouveaux problèmes / régressions apparus depuis l'audit

Ces points n'apparaissaient pas (ou pas sous cette forme) dans `AUDIT_V1.md` — ils résultent soit de zones non couvertes par l'audit précédent (cat/wolf/cow, displaySources détaillés), soit de refactors V2 en cours.

### 🔴 Critiques / Majeurs

1. **`ReactorCasingEntity.setController` — offset `+4/+4` codé en dur, appliqué inconditionnellement**
   `controller = new BlockPos(pos.getX()+4, pos.getY(), pos.getZ()+4)` est correct **uniquement** pour le pattern 9×9 (T3, rayon 4). Pour T1 (5×5, rayon 2) et T2 (7×7, rayon 3), le champ pointe vers un bloc erroné. `AUDIT_V1.md` classait ce champ en "dead code à supprimer" (offset magique) ; en l'état c'est en plus un **bug latent** si quiconque consomme `getController()` à l'avenir sur un réacteur T1/T2.
   *Gravité* : Majeur (latent, dépend de l'usage futur). *Recommandation* : supprimer le champ (toujours non lu en pratique) ou le calculer depuis la taille réelle assemblée.

2. **`IrradiatedCatCollarLayer` / `IrradiatedWoldCollarLayer` — feature de teinture de collier non câblée**
   `IrradiatedWoldCollarLayer.render()` est un corps **vide** ; `IrradiatedCatCollarLayer` (implémentation complète) n'est **jamais ajouté** via `addLayer(...)` au renderer. Pourtant `IrradiatedCat` câble entièrement l'interaction `DyeItem` → `setCollarColor`/`getCollarColor` (`IrradiatedCat.java:333-342`). `IrradiatedWolf` contient même des lignes commentées (`DATA_COLLAR_COLOR`) montrant une tentative abandonnée.
   *Conséquence* : un joueur peut teindre le collier d'un chat/loup irradié apprivoisé, l'action est acceptée, mais **rien ne s'affiche jamais** — feature visible cassée, non documentée dans `AUDIT_V1.md` (qui ne couvrait pas `content/contraptions/irradiated/{cat,wolf,cow}`).
   *Recommandation* : enregistrer `IrradiatedCatCollarLayer` dans le renderer du chat, implémenter `IrradiatedWoldCollarLayer.render`, ou retirer toute la mécanique `setCollarColor`/interaction `DyeItem` si hors scope.

3. **`AntiRadiationArmorItem.getArmorTexture` — hook mort en Forge 1.20.1**
   L'override `getArmorTexture(ItemStack, Entity, EquipmentSlot, String)` (renvoyant un chemin de texture par couleur de tissu via `ClothTagHelper.getArmorTexturePath`) **n'est plus appelé par Forge 47.2.x** — ce hook a été retiré depuis longtemps ; le rendu passe désormais exclusivement par `IClientItemExtensions`/`AntiRadiationArmorClientExtensions`, qui n'utilise jamais la couleur de tissu. Conséquence : toute la mécanique `ClothTagHelper`/`SmithingTransformRecipeMixin` (NBT `ClothColor`) n'a **aucun effet visuel** — teindre l'armure anti-radiation via tissu ne change rien à son apparence. C'est un **bug de fonctionnalité silencieuse**, distinct de B13 (qui concerne la visibilité des bras), non couvert par `AUDIT_V1.md`.
   *Recommandation* : implémenter la sélection de texture colorée dans `AntiRadiationArmorClientExtensions`/`AntiRadiationArmorModel`, ou retirer `getArmorTexture` + `ClothTagHelper.getArmorTexturePath` (et documenter que la teinture de tissu est cosmétiquement inerte).

4. **`RadiationEffectHandler` — 3ᵉ chemin d'application de la radiation, sans aucune garde**
   `RadiationEffectHandler.apply` (fuite de tuyau) applique `MobEffectInstance(RADIATION,3,2,...)` à **tout** `LivingEntity` à proximité, sans vérifier `CNConfigs.server().radiation.enabledItemRadiation`, sans vérifier `CNTags.CNEntityTags.IRRADIATED_IMMUNE`, sans vérifier le spectateur, et **sans tenir compte de la résistance anti-radiation**. C'est un chemin indépendant des deux autres (`RadiationCapability`/`RadiationEffect`), qui eux respectent ces gardes.
   *Conséquence* : une fuite de fluide irradiant un joueur en armure anti-radiation complète, ou un mob immunisé, même avec `enabledItemRadiation=false`.
   *Lien AUDIT_V1.md* : prolonge directement le constat §1.4 ("contrat IRadiationSource vs RadiationRegistry dupliqué sans règle de priorité") — ici un **troisième** mécanisme d'application apparaît, renforçant l'absence de propriétaire unique pour la feature radiation.
   *Recommandation* : router via un helper unique "peut être irradié" partagé par les 3 chemins.

5. **Deux overlays de "vision irradiée" actifs en parallèle**
   `IrradiatedOverlayRendererVision` (actif) et `RadiationOverlay` (commenté dans `HudRenderer` mais toujours alimenté via `HelmetOverlay.setCoverage`) sont **deux implémentations concurrentes du même effet visuel**. `AUDIT_V1.md` mentionnait `RadiationOverlay` comme mort sans relever explicitement la duplication fonctionnelle avec `IrradiatedOverlayRendererVision` — confirmé ici comme une vraie redondance architecturale (deux fichiers, deux mécanismes de fade, un seul réellement rendu).

6. **`RenderHelper.renderTextureOverlay` — `Math.round(alpha*coverage)` annule le fade**
   `RadiationOverlay.java:49` passe `Math.round(alpha * coverage)` (un `int` 0 ou 1) à un paramètre `float alpha ∈ [0,1]` — le fade progressif de `EasingHudOverlay` est **binarisé** (apparition/disparition instantanée). Non décrit explicitement dans `AUDIT_V1.md` (qui notait juste "RadiationOverlay:49 binarise l'alpha via Math.round" en passant, dans une remarque connexe sur `RenderHelper`) — confirmé ici comme un bug à part entière, mais **actuellement sans impact visible** puisque `RadiationOverlay` n'est pas rendu (cf. point 5).

### 🟠 Importants

7. **✅ Corrigé (2026-06-15)** — `ReactorOutputManager.distributeSU` (dead code) supprimé, ainsi que sa déclaration dans `ReactorOutputManagerI`. Il ne reste plus que `ReactorControllerBlockEntity.rotate()` (round-robin simple) ; plus de duplication d'algorithme de distribution.

8. **`ReactorInputFluidManager.getBlocksPosition` — `LOGGER.warn` à chaque appel (tick)**
   Un `LOGGER.warn("getBlocksPosition: {} {}", ...)` tourne en boucle au niveau `WARN` à chaque tick via `ReactorInputSnapshotBuilder` — pollution de logs en production introduite par le refactor récent (non présente dans `AUDIT_V1.md`, qui ne connaissait pas encore `ReactorInputSnapshotBuilder`).

9. **`RadiationCapability.lastBiomeLocation` — non persisté (correction : PAS du dead code)**
   ⚠️ Correction de l'audit : contrairement à ce qui était écrit, `lastBiomeLocation` **est bien lu pour une décision** (`RadiationCapability:89`, comparaison `!Objects.equals(biomeLoc, cap.getLastBiomeLocation())`). Ce n'est donc **pas** du dead code et il n'a pas été supprimé. Le seul point restant : `RadiationProvider.serializeNBT` ne le persiste pas → l'état est perdu au save/déconnexion (bug mineur, à corriger ou assumer).

10. **`ReactorSummaryDisplaySource` — sentinelle de taille de liste fragile + accès positionnel**
    `getComponents()` retourne une liste de taille **1** (pas de contrôleur) ou **6** (normal) ; les appelants testent `components.size() < 6` pour détecter le cas "pas de contrôleur", et `components.get(2).get(1)` accède positionnellement à la ligne "fuel". Tout ajout/réordonnancement futur de ligne casse silencieusement ces deux contrats implicites. `AUDIT_V1.md` ne détaillait pas `ReactorSummaryDisplaySource` à ce niveau.

11. **`ReactorSummaryDisplaySource.formatValue` — incohérence de mode entre résumé et displaySource individuel**
    En mode "normal" (mode 0), `HeatDisplaySource` affiche `"500 °C"` alors que `ReactorSummaryDisplaySource` affiche une **jauge** pour le heat dans le même mode (`gaugeOnNormal=true` pour heat uniquement) — incohérence visuelle entre les deux affichages pour un même mode utilisateur, non relevée dans `AUDIT_V1.md`.

12. **`IrradiatedBiomes` — contenu de worldgen visiblement copié d'un autre mod**
    `addDefaultIrradiatedOres`/`addDefaultSoftDisks` ajoutent `MiscOverworldPlacements.BLUE_ICE`, `Carvers.NETHER_CAVE`, `VOID_START_PLATFORM` — du contenu vanilla sans rapport avec un biome "irradié", et `monsters(...)` est un no-op appelé avec des paramètres `(95, 5, 100)` silencieusement ignorés. Combiné aux bugs B7/B19, le pipeline worldgen "irradié" semble être un **template non terminé/non re-thémé**, plus large que ce que `AUDIT_V1.md` avait documenté (qui se concentrait sur les surface rules).

13. **`CNNoiseGeneratorSettings.IRRADIATED`** définit `STEEL_BLOCK` comme bloc de remplissage par défaut du terrain (équivalent "stone"). Si jamais relié à une dimension, génèrerait un terrain massivement en acier — combiné aux surface rules cassées (B7/B19), point d'alerte non couvert par `AUDIT_V1.md`.

### 🟡 Mineurs

14. **`CreateNuclearJEI` — champ statique mutable `Categories` (nom non conventionnel)**, vidé/reconstruit à chaque appel de `registerCategories` — risque si JEI ré-appelle ce cycle (reload de ressources).
15. **`CNPonderReactorScenes.showReactorStructure`** — boucle triple (jusqu'à 11×13×13 ≈ 1859 itérations) avec 5 comparaisons positionnelles séquentielles par cellule ; remplaçable par une `Map` précalculée. Coût ponctuel (ouverture de ponder) mais signe de code à clarifier.
16. **`ReactorFrameDisplayManager.write`** persiste systématiquement les sentinelles `Integer.MAX_VALUE`/`MIN_VALUE` même quand `hasFrameColumn()` est faux — pollution NBT mineure.
17. **`ReactorBluePrintItemPacket.totalInit`** : champ `static double` partagé, lu dans `calculatePostgres()` avant d'être écrit par `write()` côté émission — état global non thread-safe, partiellement lié à B6 mais distinct.

---

## 5. 🏗️ Architecture — état actuel (synthèse)

Les 5 problèmes structurels de `AUDIT_V1.md` §1 restent **tous valides** :

1. **Inversion `api/`** : `api.multiblock.MultiBlockManagerBeta`/`RodType`/`ReactorFluidType` dépendent toujours de `content.*` — confirmé.
2. **God class `ReactorControllerBlockEntity`** : toujours ~680 lignes, hub pour 5 managers, verrous fluides (`tryLockFluid`/`canAcceptFluid`/`clearLock`/`clearLockIfAllInputsEmpty`, ~65 lignes), rotation/output (`rotate`, ~35 lignes), tooltips. La décomposition progresse (managers/services/snapshot extraits) mais la BE reste le hub central — **non terminé**, conforme au plan d'action §8.5 de `AUDIT_V1.md`.
3. **Deux frameworks multiblock concurrents** : le ménage a réduit `lib/multiblock` (suppression de `MultiBlockManager`/`MultiBlockCache`/`IBetterPattern`), mais **trois couches coexistent toujours** pour la détection de contrôleur — `lib/multiblock` (matching générique), `api.multiblock.MultiBlockManagerBeta` (vérification de structure), et `content.multiblock.pattern.ReactorPattern`/`MultiblockHelpers` (scan géométrique brut, ~3971 blocs, **toujours** appelé en double — `findController` + `findControllerPos` — à chaque pose/casse, y compris côté client sans garde `isClientSide`). C'est le point de performance/architecture le plus critique et **entièrement non corrigé**.
4. **Radiation sur 4+ couches** : toujours vrai, et **aggravé** par le 3ᵉ chemin `RadiationEffectHandler` (point 4 ci-dessus) découvert dans cette analyse.
5. **Couplage hub + accès infra direct** : `ConfigValueResolver` existe toujours dans `foundation/utility` mais reste sous-utilisé (ex: `RadiationEffect` l'utilise pour la blacklist mais reconstruit un `HashSet` + reparse les `ResourceLocation` à **chaque tick par entité** — perf non corrigée, AUDIT §4 ligne 2).

---

## 6. ⚡ Performance — état actuel

Tous les points perf de `AUDIT_V1.md` §4 restent **non corrigés** :
- `ReactorPattern.findController`/`findControllerPos` : scan ~3971 blocs ×2 par placement/casse, sans garde client — confirmé, **aggravé** par `MultiBlockManagerBeta.findStructure` qui peut ajouter jusqu'à 4×3×729 lookups supplémentaires.
- `RadiationEffect` : `HashSet<EntityType>` + parsing reconstruits à chaque entité/tick — confirmé.
- `ReactorFluidType.getTypeForFluid` : `ForgeRegistries.FLUIDS.getKey(fluid)` recalculé en boucle interne — confirmé.
- `HelmetOverlay.renderHotbar` + 3× `getArmor(HEAD)`/frame — confirmé.
- **Nouveau** : `DefaultHeatCalculator.computeHeat` (package `reactorLogic`, non couvert par `AUDIT_V1.md`) — boucle imbriquée ~`57×81×4×57` avec désérialisation NBT répétée par cellule, exécutée à chaque tick de calcul de chaleur ; branche de proximité "cooler" manquante (asymétrie fuel/cooler).
- **Nouveau** : `NuclearExplosionEntity.tick()` — tri complet d'une pile de jusqu'à 11³=1331 `BlockPos` avec comparateur `distManhattan` coûteux, en un seul tick.

---

## 7. Plan d'action — priorités mises à jour

**Quick wins toujours en attente** (1 ligne, fort impact) : **B1** (one-liner mais classe morte — décider câbler/supprimer), **B9** (mapping amplificateurs ambigu — décision produit), **B7** (enregistrer `EROSION`).

**Nouveaux quick wins identifiés** :
- `AntiRadiationArmorClientExtensions` (B13) : remplacer `rightArm/leftArm` par `right_arm/left_arm` en case `CHEST`.
- `RadiationEffectHandler` : ajouter les mêmes gardes (config/immunité/résistance) que les deux autres chemins.
- Enregistrer `IrradiatedCatCollarLayer` / implémenter `IrradiatedWoldCollarLayer.render`, ou retirer la mécanique de teinture de collier.
- Retirer `RenderHelper.renderTextureOverlay(..., Math.round(...))` → passer `alpha*coverage` brut (si `RadiationOverlay` est un jour réactivé).

**Priorité structurelle inchangée** : le scan géométrique `ReactorPattern`/`MultiblockHelpers` (point architecture #3) reste le risque de performance serveur le plus sérieux et n'a reçu **aucune** correction — c'est le chantier le plus rentable avant de poursuivre la décomposition de `ReactorControllerBlockEntity`.

**Nettoyage dead-code** : la majeure partie de la liste `§3` a été traitée le 2026-06-15 (voir l'encart ✅ du §3). **Reste à faire** :
- Méthodes d'animation mortes restantes dans `CNAdvancedEntityModel`/`CNAdvancedModelBox` + dédup `ModelBox` (cascade dans classes vivantes — nettoyage dédié).
- `RadiationOverlay`/`EasingHudOverlay` + `HelmetOverlay.setCoverage` no-op (décider : supprimer ou réactiver+corriger le fade — §4 points 5 & 6).
- `IrradiatedBiomes.monsters()` no-op et reliquat éventuel `SimpleMultiBlockPattern.test()`.
- Features à finir ou supprimer (pas du pur dead code, décision produit) : `IrradiatedWoldCollarLayer`/`IrradiatedCatCollarLayer`, `AntiRadiationArmorItem.getArmorTexture`/`ClothTagHelper.getArmorTexturePath`.
