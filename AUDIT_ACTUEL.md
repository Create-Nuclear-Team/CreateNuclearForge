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

La **grande majorité des bugs** de `AUDIT_V1.md` sont **toujours présents**, et plusieurs **nouveaux problèmes** ont émergé dans les zones où les refactors V2 sont en cours (managers `controller/`, displaySources, radiation). Le dead code a été en grande partie nettoyé ; il ne reste qu'un résidu non trivial (voir §3).

---

## 1. 🐛 Bugs critiques toujours présents

_Aucun. Tous les bugs critiques recensés ont été corrigés._

---

## 2. 🐛 Bugs importants toujours présents

| Réf. | Fichier:ligne | État | Détail |
|---|---|---|---|
| **B3** | `content/effects/VicinityEffect.java:32-49` | **Toujours présent, identique** | `int cooldownTicks = 0;` réinitialisé à chaque itération → branche `else` morte, `cooldowns`/`getCooldown`/`setCooldown` toujours jamais appelés (confirmé grep). `RadiationEffect` continue donc à `addEffect` un `MobEffectInstance(RADIATION,300)` toutes les 5 ticks sur chaque entité proche, sans throttling réel. |
| **B6** | `content/multiblock/bluePrintItem/ReactorBluePrintItemScreen.java` / `ReactorBluePrintItemPacket.java` | **Toujours présent** | Le 6ᵉ argument du packet duplique le 5ᵉ (uranium perdu) ; `coef=0.1F` envoyé à la place de `heat`. |
| **B9** | `content/radiation/capability/RadiationCapability.java` (`applyEffects`) | **Toujours présent, identique** | Les deux premières branches (`radiation < level1`, `radiation < level2`) donnent toutes deux `amplifierLevel0` → `radiationLevel2` reste un palier inopérant. |
| **B12** | `content/contraptions/irradiated/AnimalUtil.java:72-81` | **Toujours présent** | `isFood` ne reconnaît la yellowcake comme aliment **que** si elle porte un tag NBT `"Ingredient"` — incohérent avec `mobInteract` (ligne ~39) qui accepte *toute* yellowcake pour la conversion. Le chemin "food" (élevage/soin) reste cassé pour la yellowcake sans tag. |
| **B14** | `content/multiblock/input/fluid/FluidLockManager.java` | **Toujours présent** | `Map<BlockPos,Fluid>` statique, sans clé de dimension, jamais purgée, doublon de `PersistentFluidLocks` ; toujours appelé depuis `clearLockIfAllInputsEmpty()` et `ReactorFluidInputEntity.FilteredFluidHandler`. |
| **B15** | `content/explosion/NuclearExplosionEntity.java` (~224) | **Toujours présent** | `try { onBlockExploded } catch(Exception){ destroyBlock(...,true) }` — exception comme branchement, avale les vrais bugs, sémantiques de drop différentes entre les deux branches. |
| **B16** | `foundation/data/recipe/CNStandardRecipeGen.java:351/391` | **Toujours présent (latent)** | `new ModdedCookingRecipeResult(result, compatDatagenOutput, null)` puis `serializeRecipeData` fait `conditions.forEach(...)` sur ce `null` → NPE si une recette `isOtherMod` est un jour sérialisée. |
| **B17** | `foundation/events/RodsTooltipHandler.java:25-26` | **Reclassé — comportement intentionnel, risque de lisibilité uniquement** | La condition `if (id != null && CreateNuclear.MOD_ID.equals(id.getNamespace())) return;` est **volontaire** : les items du mod disposent déjà de leur tooltip via `setTooltipModifierFactory` dans `CreateNuclear.java:48` (chemin Registrate). Ce handler sert exclusivement aux items **externes** (autres mods ou items définis via datapack), car `RodType.resolveRodType` interroge `world.registryAccess()` au runtime — seul mécanisme capable de lire les `RodType` injectés par datapack, que `setTooltipModifierFactory` ne peut pas couvrir (les datapacks ne sont pas connus à l'enregistrement). **Ce n'est donc pas un bug.** Le vrai risque est la lisibilité : la condition semble inversée sans contexte, et un contributeur naïf qui la retire ou l'inverse provoquera un double tooltip sur les rods du mod. **Correction recommandée** : ajouter un commentaire explicite sur la ligne de garde — `// mod items already handled by Registrate's setTooltipModifierFactory (CreateNuclear.java:48)` — pour verrouiller l'intention et prévenir toute régression future. |
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

> **⚠️ Corrections de l'audit V1/V2** (ne pas supprimer ces éléments — code vivant) :
> - `CNTabulaModelRenderUtils` **n'est PAS du dead code** : il est utilisé par `CNAdvancedModelBox.cubeList`/`doRender`, sur le chemin de rendu **vivant** du champignon atomique (`NuclearMushroomCloudModel`). Conservé. La duplication `ModelBox` reste une opportunité de *refactor*, pas une suppression.
> - `ReactorOutputEntity.outputPos` **n'est PAS mort** : il est lu/écrit dans `read()`/`write()` (sérialisation NBT). Laissé en place.

Restant (grep global, zéro référence externe sauf mention) :

- **Méthodes d'animation mortes restantes** dans `CNAdvancedEntityModel`/`CNAdvancedModelBox` : `chainSwing`, `chainWave`, `chainFlap`, `faceTarget`, `walk`, `flap`, `swing`, `bob`, `moveBox`, `setRotateAngle`, `progressRotation*`, `progressPosition*`, `getMovementScale`/`setMovementScale`/`movementScale`, `transitionTo`, `calculateChain*`, `displayList`/`compiled`. Mortes mais **non triviales** : leur retrait déclenche une cascade dans des classes **vivantes** (ex. `CNAdvancedModelBox.calculateRotation`/`bob` appellent `model.getMovementScale()`). À traiter en nettoyage dédié + dédup `ModelBox`.
- **`RadiationOverlay` + `EasingHudOverlay`** : toujours commenté dans `HudRenderer.overlays` ; **`HelmetOverlay.setCoverage(...)` écrit dans le champ statique de `RadiationOverlay`, qui n'est lu par personne** — no-op silencieux (lié au §4 points 5 & 6 : décider entre supprimer `RadiationOverlay` ou réactiver et corriger le fade).
- **`SimpleMultiBlockPattern.test()`** : à vérifier — la classe ne semble plus exister (à confirmer/retirer si résidu).
- **`IrradiatedBiomes.monsters()`** : no-op appelé avec `(95, 5, 100)` silencieusement ignorés — à retirer ou implémenter (lié au §4 point 10, worldgen "irradié" template non terminé).

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
   `RadiationEffectHandler.apply` (fuite de tuyau) applique `MobEffectInstance(RADIATION,3,2,...)` à **tout** `LivingEntity` à proximité, sans vérifier `CNConfigs.server().radiation.enabledItemRadiation`, sans vérifier `CNTags.CNEntityTags.IRRADIATED_IMMUNE`, sans vérifier le spectateur, et **sans tenir compte de la résistance anti-radiation**. C'est un chemin indépendant des deux autres (`RadiationCapability`/`RadiationEffect`), qui eux respectent ces gardes — mais selon deux sémantiques *différentes*, à ne pas fusionner :

   - `RadiationEffect` (filtre de contagion de proximité, constructeur lignes 32-52) applique un **gate binaire** (immune tag, config, blacklist, spectateur, `résistance ≥ 1`) — mais ce gate est enfermé dans un lambda non réutilisable, et **reconstruit un `HashSet` + reparse les `ResourceLocation` de la blacklist à chaque appel** (cf. §5 point 5 / §6) — perf non corrigée en plus du problème de duplication.
   - `RadiationCapability.applyEffects` applique une **atténuation continue de dose** (`totalRadiation = totalRaw * (1 - resistance)`), pas un gate — cette logique est correcte et ne doit *pas* être déplacée dans un helper commun, elle reste propre à ce chemin (calcul de magnitude, pas d'éligibilité).

   *Conséquence* : une fuite de fluide irradiant un joueur en armure anti-radiation complète, ou un mob immunisé, même avec `enabledItemRadiation=false`.
   *Lien AUDIT_V1.md* : prolonge directement le constat §1.4 ("contrat IRadiationSource vs RadiationRegistry dupliqué sans règle de priorité") — ici un **troisième** mécanisme d'application apparaît, renforçant l'absence de propriétaire unique pour la feature radiation.
   *Recommandation* : extraire **uniquement le gate binaire** (pas l'atténuation continue) dans `RadiationCapability.canBeIrradiated(LivingEntity)`, avec la blacklist mise en cache statique au lieu d'être reconstruite par appel :

   ```java
    private static Set<EntityType<?>> entityBlacklistCache; // construit une fois, invalidé sur reload config/datapack

    public static boolean canBeIrradiated(LivingEntity entity) {
       if (entity.isSpectator()) return false;
       if (entity.getType().is(CNTags.CNEntityTags.IRRADIATED_IMMUNE.tag)) return false;
       if (!CNConfigs.server().radiation.enabledItemRadiation.get()) return false;
       if (getEntityBlacklist().contains(entity.getType())) return false;
       return getRadiationResistance(entity) < 1.0;
    }

    private static Set<EntityType<?>> getEntityBlacklist() {
      if (entityBlacklistCache == null) {
        entityBlacklistCache = new HashSet<>();
        ConfigValueResolver.loadValuesInSet(CNConfigs.server().radiation.configuredLists.getEntityBlackList(), entityBlacklistCache, ...);
      }
      return entityBlacklistCache;
    }
   ```

   ```java
    // RadiationEffectHandler.apply
    for (LivingEntity entity : entities) {
      if (!RadiationCapability.canBeIrradiated(entity)) continue;
      entity.addEffect(new MobEffectInstance(CNEffects.RADIATION.get(), 3, 2, false, false, false));
    }
    
    // RadiationEffect.RadiationEffect
    public RadiationEffect() {
      super(MobEffectCategory.HARMFUL, 15453236,
        amplifier -> 10,
        RadiationCapability::canBeIrradiated,   // au lieu de 20 lignes inline + HashSet reconstruit à chaque appel
        timer -> {},
        () -> new MobEffectInstance(CNEffects.RADIATION.get(), 300));
    }
   ```

   Utilisé en garde dans `RadiationEffectHandler.apply` (corrige le bug) et comme `filter` de `RadiationEffect` (remplace le lambda dupliqué, supprime la reconstruction de `HashSet` par tick/entité). `RadiationCapability.applyEffects` garde son atténuation continue propre, mais gagne un garde-fou minimal absent aujourd'hui : `if (player.isSpectator()) return;`.

5. **Deux overlays de "vision irradiée" actifs en parallèle**
   `IrradiatedOverlayRendererVision` (actif) et `RadiationOverlay` (commenté dans `HudRenderer` mais toujours alimenté via `HelmetOverlay.setCoverage`) sont **deux implémentations concurrentes du même effet visuel**. `AUDIT_V1.md` mentionnait `RadiationOverlay` comme mort sans relever explicitement la duplication fonctionnelle avec `IrradiatedOverlayRendererVision` — confirmé ici comme une vraie redondance architecturale (deux fichiers, deux mécanismes de fade, un seul réellement rendu).

6. **`RenderHelper.renderTextureOverlay` — `Math.round(alpha*coverage)` annule le fade**
   `RadiationOverlay.java:49` passe `Math.round(alpha * coverage)` (un `int` 0 ou 1) à un paramètre `float alpha ∈ [0,1]` — le fade progressif de `EasingHudOverlay` est **binarisé** (apparition/disparition instantanée). Non décrit explicitement dans `AUDIT_V1.md` (qui notait juste "RadiationOverlay:49 binarise l'alpha via Math.round" en passant, dans une remarque connexe sur `RenderHelper`) — confirmé ici comme un bug à part entière, mais **actuellement sans impact visible** puisque `RadiationOverlay` n'est pas rendu (cf. point 5).

### 🟠 Importants

7. **`RadiationCapability.lastBiomeLocation` — non persisté (correction : PAS du dead code)**
   ⚠️ Correction de l'audit : contrairement à ce qui était écrit, `lastBiomeLocation` **est bien lu pour une décision** (`RadiationCapability:89`, comparaison `!Objects.equals(biomeLoc, cap.getLastBiomeLocation())`). Ce n'est donc **pas** du dead code et il n'a pas été supprimé. Le seul point restant : `RadiationProvider.serializeNBT` ne le persiste pas → l'état est perdu au save/déconnexion (bug mineur, à corriger ou assumer).

8. **`ReactorSummaryDisplaySource` — sentinelle de taille de liste fragile + accès positionnel**
   `getComponents()` retourne une liste de taille **1** (pas de contrôleur) ou **6** (normal) ; les appelants testent `components.size() < 6` pour détecter le cas "pas de contrôleur", et `components.get(2).get(1)` accède positionnellement à la ligne "fuel". Tout ajout/réordonnancement futur de ligne casse silencieusement ces deux contrats implicites. `AUDIT_V1.md` ne détaillait pas `ReactorSummaryDisplaySource` à ce niveau.

9. **`ReactorSummaryDisplaySource.formatValue` — incohérence de mode entre résumé et displaySource individuel**
   En mode "normal" (mode 0), `HeatDisplaySource` affiche `"500 °C"` alors que `ReactorSummaryDisplaySource` affiche une **jauge** pour le heat dans le même mode (`gaugeOnNormal=true` pour heat uniquement) — incohérence visuelle entre les deux affichages pour un même mode utilisateur, non relevée dans `AUDIT_V1.md`.

10. **`IrradiatedBiomes` — contenu de worldgen visiblement copié d'un autre mod**
    `addDefaultIrradiatedOres`/`addDefaultSoftDisks` ajoutent `MiscOverworldPlacements.BLUE_ICE`, `Carvers.NETHER_CAVE`, `VOID_START_PLATFORM` — du contenu vanilla sans rapport avec un biome "irradié", et `monsters(...)` est un no-op appelé avec des paramètres `(95, 5, 100)` silencieusement ignorés. Combiné aux bugs B7/B19, le pipeline worldgen "irradié" semble être un **template non terminé/non re-thémé**, plus large que ce que `AUDIT_V1.md` avait documenté (qui se concentrait sur les surface rules).

11. **`CNNoiseGeneratorSettings.IRRADIATED`** définit `STEEL_BLOCK` comme bloc de remplissage par défaut du terrain (équivalent "stone"). Si jamais relié à une dimension, génèrerait un terrain massivement en acier — combiné aux surface rules cassées (B7/B19), point d'alerte non couvert par `AUDIT_V1.md`.

### 🟡 Mineurs

12. **`CreateNuclearJEI` — champ statique mutable `Categories` (nom non conventionnel)**, vidé/reconstruit à chaque appel de `registerCategories` — risque si JEI ré-appelle ce cycle (reload de ressources).
13. **`CNPonderReactorScenes.showReactorStructure`** — boucle triple (jusqu'à 11×13×13 ≈ 1859 itérations) avec 5 comparaisons positionnelles séquentielles par cellule ; remplaçable par une `Map` précalculée. Coût ponctuel (ouverture de ponder) mais signe de code à clarifier.
14. **`ReactorFrameDisplayManager.write`** persiste systématiquement les sentinelles `Integer.MAX_VALUE`/`MIN_VALUE` même quand `hasFrameColumn()` est faux — pollution NBT mineure.
15. **`ReactorBluePrintItemPacket.totalInit`** : champ `static double` partagé, lu dans `calculatePostgres()` avant d'être écrit par `write()` côté émission — état global non thread-safe, partiellement lié à B6 mais distinct.

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
- `ReactorControllerBlockEntity.clearLockIfAllInputsEmpty` : scan cubique `O(n³)` (`getBlockEntity` + capability lookup par cellule) alors que `inputFluidManager.getFuildHandlers(level)` donne déjà la liste exacte (le NPE de `findStructure`, lui, a été corrigé).
- **Nouveau** : `DefaultHeatCalculator.computeHeat` (package `reactorLogic`, non couvert par `AUDIT_V1.md`) — boucle imbriquée ~`57×81×4×57` avec désérialisation NBT répétée par cellule, exécutée à chaque tick de calcul de chaleur ; branche de proximité "cooler" manquante (asymétrie fuel/cooler).
- **Nouveau** : `NuclearExplosionEntity.tick()` — tri complet d'une pile de jusqu'à 11³=1331 `BlockPos` avec comparateur `distManhattan` coûteux, en un seul tick.

---

## 7. Plan d'action — priorités mises à jour

**Quick wins toujours en attente** (1 ligne, fort impact) : **B9** (mapping amplificateurs ambigu — décision produit).

**Nouveaux quick wins identifiés** :
- `RadiationEffectHandler` : ajouter les mêmes gardes (config/immunité/résistance) que les deux autres chemins.
- Enregistrer `IrradiatedCatCollarLayer` / implémenter `IrradiatedWoldCollarLayer.render`, ou retirer la mécanique de teinture de collier.
- Retirer `RenderHelper.renderTextureOverlay(..., Math.round(...))` → passer `alpha*coverage` brut (si `RadiationOverlay` est un jour réactivé).

**Priorité structurelle inchangée** : le scan géométrique `ReactorPattern`/`MultiblockHelpers` (point architecture #3) reste le risque de performance serveur le plus sérieux et n'a reçu **aucune** correction — c'est le chantier le plus rentable avant de poursuivre la décomposition de `ReactorControllerBlockEntity`.

**Nettoyage dead-code restant** :
- Méthodes d'animation mortes restantes dans `CNAdvancedEntityModel`/`CNAdvancedModelBox` + dédup `ModelBox` (cascade dans classes vivantes — nettoyage dédié).
- `RadiationOverlay`/`EasingHudOverlay` + `HelmetOverlay.setCoverage` no-op (décider : supprimer ou réactiver+corriger le fade — §4 points 5 & 6).
- `IrradiatedBiomes.monsters()` no-op et reliquat éventuel `SimpleMultiBlockPattern.test()`.
- Features à finir ou supprimer (pas du pur dead code, décision produit) : `IrradiatedWoldCollarLayer`/`IrradiatedCatCollarLayer`, `AntiRadiationArmorItem.getArmorTexture`/`ClothTagHelper.getArmorTexturePath`.
