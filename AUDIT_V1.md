# 🔬 Audit Create Nuclear — Rapport détaillé

> Audit du code Java (`src/main/java`, 282 fichiers / ~21 000 lignes) réalisé par revue multi-agents : 11 agents (un par package) ont lu l'intégralité du code ; chaque finding *bug* / *dead code* a été repris par un agent sceptique chargé de le **réfuter** (grep global incluant `src/generated`, mixins, reflection) afin d'éliminer les faux positifs ; un agent final a synthétisé l'architecture.
>
> **Hors périmètre** : `src/generated/resources` (datagen), `src/main/resources` (textures/lang/sons/nbt).
>
> **Résultat brut** : 144 findings → **80 confirmés**, 11 réfutés (écartés), 6 incertains, 47 cleanup/design/perf non soumis à vérification adversariale.

| Catégorie | Total | Confirmés (≈) |
|---|---|---|
| 🐛 Bugs | 41 | 28 |
| 🧹 Dead code | 33 | 30 |
| ♻️ Cleanup | 33 | — |
| 🏗️ Design | 16 | 10 |
| ⚡ Perf | 7 | 4 |
| ✅ Points forts | 14 | — |

**Constat transversal** : le code est visiblement en **plein refactor V2**. La majorité des problèmes ne sont pas de la mauvaise écriture mais des **reliques d'une migration inachevée** — deux frameworks multiblock en parallèle, du code commenté un peu partout, des `&& false` de debug oubliés, des features câblées à moitié. C'est l'angle principal du nettoyage.

---

## 1. Vision architecturale

L'organisation **package-by-feature** est saine et idiomatique (spécifique à create) pour un addon Create : couche d'enregistrement racine (`CN*`), `content/` par domaine, `foundation/` + `infrastructure/` transverses, `api/` + `impl/`, `compat/` isolé. Cinq problèmes structurels ressortent :

1. **🔴 Inversion de dépendance dans `api/`** — `api.multiblock.MultiBlockManagerBeta` importe `content.multiblock.controller.ReactorControllerBlockEntity` ; `api.multiblock.rods.RodType` importe `content...CNRodTypes` ; idem `ReactorFluidType`. Le package censé être la fondation stable dépend du contenu concret → cycle d'intention, `api/` ne peut pas servir de point d'extension propre.

2. **🔴 God class** — `ReactorControllerBlockEntity` fait **886 lignes**, touche items / fluides / chaleur / explosion / advancement / biome / NBT / tooltips. Les sous-packages `manager/`, `service/`, `consumable/` ont été créés *pour le découper*, mais l'extraction est à moitié faite : la BlockEntity orchestre encore tout → l'abstraction ajoute de l'indirection **sans** retirer de responsabilité.

3. **🔴 Deux frameworks multiblock concurrents** — le vivant `api.multiblock.MultiBlockManagerBeta` (utilisé par `CNMultiblock`), et tout `lib.multiblock.manager.*` + `impl.IBetterPattern` qui sont **morts** (ne se référencent qu'entre eux). Piège à temps pour tout nouveau contributeur.

4. **🟠 Feature radiation éparpillée sur 4 couches** — `api.radiation` + `foundation/item/radiation` + `content/effects/capability` + `foundation/networking/radiation` implémentent **une seule** feature, sans package propriétaire. Le contrat `IRadiationSource` vs `RadiationRegistry` est dupliqué sans règle de priorité.

5. **🟠 Couplage hub + accès infra direct** — la BlockEntity contrôleur est un hub de couplage (atteint explosion, advancement, biome, config, logistics, inputs, registres) ; et les classes `content/` importent directement `infrastructure.config.CNConfigs` / `CNBiomes`. Un seam `ConfigValueResolver` existe déjà dans `foundation/utility` mais n'est pas utilisé.

**Autres points structurels** : package racine fourre-tout (~25 classes `CN*` à plat, mélangées avec `@Mod`, le proxy client et `ClientEvents`) ; `foundation/util` vs `foundation/utility` redondants ; framework de modèle décompilé logé dans `content/explosion` (mêle rendu client réutilisable et logique serveur) ; **aucun test** (`src/test` vide) ; dossier **`run/` versionné** (mixin.out, saves, configs — à mettre en `.gitignore`).

---

## 2. 🐛 Bugs confirmés — par priorité

### 🔴 Critiques (cassent une fonctionnalité, fix simple)

| # | Fichier:ligne | Problème |
|---|---|---|
| B1 | `foundation/events/overlay/EventTextOverlay.java:40` | `isActive()` fait **`return timer > 0 && false;`** → l'overlay ne s'affiche **jamais**. Kill-switch de debug oublié. Retirer `&& false`. Un `LOGGER.warn` de debug traîne aussi (à retirer). |
| B2 | `foundation/networking/radiation/RadiationSyncPacket.java:26` | Le handler fait `new ClientRadiationData(radiation)` puis **jette l'instance** (record sans stockage statique ni effet de bord). Le serveur envoie la radiation, **rien ne la mémorise côté client** → packet no-op, sync radiation cassée. Fix : stocker dans un holder statique client lu par l'overlay. |
| B3 | `content/effects/VicinityEffect.java:40` | `int cooldownTicks = 0;` déclaré **à l'intérieur** de la boucle `for` → `if (cooldownTicks==0)` toujours vrai, tout le throttling à 500 ticks est mort (map `cooldowns` + get/setCooldown jamais utilisés). `RadiationEffect` override `isDurationEffectTick → true` donc l'effet est ré-appliqué **à chaque tick** sur toutes les entités proches. |
| B4 | `CNBlocks.java:593-610` | **`THORIUM_BLOCK`** (bloc de stockage) drope **`RAW_URANIUM`** au lieu de lui-même, et est taggé comme un **minerai** (`ores`, `ores/thorium`, `NEEDS_DIAMOND_TOOL`…). Copier-coller d'un minerai d'uranium. Confirmé dans le datagen (`thorium_block.json:41`). Fix : calquer sur `LEAD_BLOCK`/`STEEL_BLOCK` (loot par défaut, seul tag `storage_blocks/thorium`). |
| B5 | `infrastructure/worldgen/biome/surfacerule/BiomeTagRule.java:21` | `apply()` retourne **`null`** au lieu d'une `Condition` ; la classe interne `Predicate` qui contient la vraie logique n'est jamais instanciée → **NPE** à l'évaluation de toute surface rule basée dessus. Fix : `return new Predicate(context)`. |
| B6 | `content/multiblock/bluePrintItem/ReactorBluePrintItemScreen.java:70` | `new ReactorBluePrintItemPacket(..., countGraphiteRod, countGraphiteRod)` : le 6ᵉ argument répète le 5ᵉ → la valeur **uranium est perdue**, le calcul de progression est faussé. (Ligne 53 : `coef=0.1F` envoyé en place de `heat` → chaleur jamais transmise ; le packet tourne aussi à chaque tick sans condition de changement.) |

### 🟠 Importants

| # | Fichier:ligne | Problème |
|---|---|---|
| B7 | `infrastructure/worldgen/biome/CNNoiseData.java:18` | `bootstrapRegistries()` **entièrement commenté** → noise `EROSION` jamais enregistré, mais les surface rules font `noiseCondition(EROSION)` 3× (`IrradiatedSurfaceRules2:54/58/62`) → `getOrThrow` lèvera. **Dormant** tant qu'aucune dimension ne câble `irradiated_noise`, mais défaut d'intégrité de données réel. |
| B8 | `content/multiblock/controller/ReactorControllerBlockEntity.java:1020` | `clearLockIfAllInputsEmpty` enchaîne `findStructure(...).data().getSize()` sans null-check ; `findStructure` retourne `null` quand la structure n'est plus formée → **NPE serveur**. Les 2 autres appelants (`ReactorAssembler:37,62`) gardent bien le null — prouve l'oubli. Fix : stocker le résultat, null-check, fallback `reactorSize`. |
| B9 | `content/effects/capability/RadiationCapability.java:165` | `if (rad<level1) amp=amplifierLevel0; else if (rad<level2) amp=amplifierLevel0;` → les **deux** branches donnent `amplifierLevel0`, le palier `radiationLevel2` est inopérant. La config (`CRadiation` l.30-36) confirme : la 2ᵉ branche devrait être `amplifierLevel1`. Palier de radiation documenté qui ne marche pas. |
| B10 | `foundation/events/overlay/IrradiatedOverlayRendererVision.java:23-26` | `mc.player.hasEffect(...)` et `mc.gameMode.getPlayerMode()` sans null-check → **NPE** pendant chargement / respawn / déconnexion. Les autres overlays gèrent ce cas. Garder `mc.player` **et** `mc.gameMode`. |
| B11 | `lib/multiblock/SimpleMultiBlockAislePatternBuilder.java:75` | `LOGGER...coreList.toString()` **avant** le null-check ligne 76 → NPE qui masque le message d'erreur explicite prévu juste après. Idem `SimpleMultiBlockPatternBuilder:64`. |
| B12 | `content/contraptions/irradiated/AnimalUtil.java:69` | `isFood` teste un sous-tag NBT `"Ingredient"` (jamais écrit) au lieu du stack tenu → **aucun aliment n'est reconnu** → élevage / soin / apprivoisement via `isFood` cassés pour le poulet irradié. (Le `TemptGoal` qui utilise `FOOD_ITEMS` directement fonctionne encore.) |
| B13 | `content/equipment/armor/AntiRadiationArmorClientExtensions.java:39` | En mode `CHEST`, règle la visibilité sur `model.rightArm/leftArm` (champs hérités, **non rendus** par `renderToBuffer`) au lieu de `right_arm/left_arm` (custom, réellement rendus) → **bras invisibles** sur l'armure anti-radiation. |
| B14 | `content/multiblock/input/fluid/FluidLockManager.java:16` | `Map<BlockPos,Fluid>` **statique** : (1) pas de dimension dans la clé → collision inter-dimension, (2) **jamais purgée** (unload chunk/monde) → fuite mémoire, (3) doublonne `PersistentFluidLocks` (deux sources de vérité, aiguillage `level instanceof ServerLevel`). La suppression d'un contrôleur ne nettoie que `PersistentFluidLocks`. |
| B15 | `content/explosion/NuclearExplosionEntity.java:224` | `try { onBlockExploded } catch(Exception){ destroyBlock(...,true) }` : utilise une exception comme branchement (anti-pattern), **avale les vrais bugs**, et les deux branches ont des sémantiques de drop **différentes** (catch = drop forcé). Au minimum logger. |
| B16 | `foundation/data/recipe/CNStandardRecipeGen.java:391` | `ModdedCookingRecipeResult.serializeRecipeData` fait `conditions.forEach` alors que le record est construit avec `conditions=null` (l.351) → **NPE datagen** dès qu'une recette `isOtherMod` serait sérialisée. Latent (chemin non câblé) mais cassé. Fix : liste vide au lieu de null, ou garde. |
| B17 | `foundation/events/RodsTooltipHandler.java:26` | Condition de namespace **inversée** : n'applique `RodsStats` qu'aux items **non**-createnuclear (donc no-op, car les rods sont du mod) ; et `RodsStats` est déjà branché via Registrate → si corrigé, **double tooltip**. Code mort fonctionnel à risque de régression — probablement à supprimer entièrement. |
| B18 | `infrastructure/config/CNCClient.java:9` | `screenShaking` réutilise la **même clé TOML** `nuclearBombFlash` que la ligne 8 → les deux options pointent sur la même valeur config (lu dans `ClientEvents:60`). Fix : clé `screenShaking`. |
| B19 | `infrastructure/worldgen/biome/surfacerule/IrradiatedSurfaceRules2.java:23` | `IS_HIGHLANDS = biome()` (varargs vide → `Set.copyOf([])::contains` toujours faux) ne matche jamais → branches `MOON_DIRT` (l.35) / `LEAD_TURF` (l.39) **mortes**. |

### 🟡 Mineurs / robustesse

- `content/multiblock/output/ReactorOutputEntity.java:117` — `tick()` : pas de null-check `level`, 2-3 lookups world redondants/tick (client+serveur) pour une position fixe `above(3)`.
- `content/multiblock/input/fluid/ReactorInputFluidManager.java:129/148` — off-by-one `getFluidInTank(getTanks())` : toléré par le `SmartFluidTank` single-tank actuel (qui ignore l'index) mais **viole le contrat `IFluidHandler`** ; et `extractFluids` ne **décrémente jamais `fluidNeeded`** + ignore l'extraction quand `toExtract>1`.
- `content/multiblock/core/ReactorCoreEntity.java` — `tick()` vide.
- `content/multiblock/input/item/ReactorInput.java:65` — retourne `PASS` après ouverture de menu côté serveur → désync client(`SUCCESS`)/serveur. Fix : `CONSUME`/`SUCCESS`.
- `foundation/utility/RenderHelper.java:36` — champs `lastAlpha/lastCoverage/lastFirstPerson` écrits jamais lus (cache inexistant) ; branche `coverage!=1f` inerte (scale commenté + translate qui s'annulent). Connexe : `RadiationOverlay:49` binarise l'alpha via `Math.round`.
- `foundation/utility/InventoryHashUtil.java:91` — le hash inclut `getDamageValue()` → tout changement de durabilité déclenche un resync (rafales de `RadiationSyncPacket` au minage/combat).
- `content/multiblock/output/ReactorOutput.java:63` — `requireNonNull` + `assert entity!=null` (assert désactivé en prod) au lieu d'une garde gracieuse ; `DIR` 0..2 avec valeur 2 jamais utilisée.
- `content/redstone/displayLink/source/ReactorSizeDisplaySource.java:31` — `%` trompeur `tier*100/3` (33/66/100 % pour tier 1..3).

---

## 3. 🧹 Dead code à supprimer

### Gros blocs (forte valeur, risque faible)

- **Tout `lib.multiblock.manager.*`** (`MultiBlockManager`, `MultiBlockCache`, `RegisteredMultiBlockPattern`) + **`lib.multiblock.impl.IBetterPattern`** + `SimpleMultiBlockPatternBuilder` (variante non-aisle) → reliques V2 mortes (ne se référencent qu'entre elles). *(`MultiBlockCache.isCached` a même une sémantique inversée.)*
- **Framework d'animation keyframe** : `CNModelAnimator` + `CNAnimation` + `CNIAnimatedEntity` + surcharges `CNAdvancedEntityModel.rotate/rotateMinus` → jamais utilisé (le modèle réel anime directement via `animateParticle`). ⚠️ **Garder** `CNAdvancedEntityModel`/`CNBasicEntityModel`/`CNAdvancedModelBox` : `NuclearMushroomCloudModel` en hérite réellement.
- **`CreateNuclearDamageSources`** = doublon (quasi octet-près) de `CNDamageSources` → garder `CNDamageSources`, migrer les 2 références (`RadiationEffect:94`, `CNFanProcessingTypes:122`) ; les surcharges privées `source(Entity…)` sont aussi mortes dans les deux classes.
- **`foundation/events/CommentEventClients.java`** — coquille `@EventBusSubscriber` vide (aucun `@SubscribeEvent`) → supprimer.
- **`foundation/events/possible code`** — fichier sans extension `.java`, brouillon `com.tonpackage` (3 fragments : services META-INF, `HudOverlayRegistry`, 2ᵉ `CNClientEvent`) → sortir du dépôt.
- **`foundation/data/recipe/CNShapelessRecipeGen.java`** — provider qui n'émet aucune recette, enregistrement commenté (`CreateNuclearDatagen:42`).
- **`IrradiatedSurfaceRules` v1** — remplacé par v2 (`CNNoiseGeneratorSettings:29`), ne survit que via un import.
- **`RadiationOverlay` + `EasingHudOverlay`** — `RadiationOverlay` commenté dans `HudRenderer.overlays` → jamais instancié/rendu (et `IrradiatedOverlayRendererVision` fait déjà ce rendu).
- **`VerifyPattern7x7` / `VerifyPattern9x9`** (`ReactorPattern`) + `getBlockPosForReactor` (@Deprecated) — jamais appelés.
- **`saveData2`** (`ReactorBluePrintMenu:160`) — doublon mort de `saveData` (et logique de comptage subtilement fausse : pas de reset).

### Membres / méthodes morts

`ReactorOutputValue` + `outputPos` + `getStressConfigKey` trivial + addBehaviours commenté (`ReactorOutputEntity`) ; `inputPos`/`inputLevelKey` + override `getLevel` trivial (`ReactorInputEntity`) ; `ReactorCasingEntity.getController` + champ `controller` (offset magique `+4/+4`) ; `VicinityEffect.cooldowns`/`getCooldown`/`setCooldown` + param `timer` ; `getArmorResistance` (`RadiationCapability`) ; helpers `TextUtils` (`renderMultilineDebugText`, `renderDebugText`, `translateWithFormatting`, `formatInt`×2, `leftPad`) ; `Mods` (`getBlock`/`getItem`/`contains`/`rl`/`id`/`runIfInstalled`/`executeIfInstalled`) ; `SoundEntryBuilder` (`playItemPickup`, `playExisting`×4, `addVariant`×2, `noSubtitle`, setter `attenuationDistance`) ; `BigFluidStack` (`send`/`receive`/`read`/`isInfinite`/`duplicateWrappers`/`comparator`/`write`) ; `CNParticleRegistry.createBlockParticleType`/`createItemParticleType` ; `parseLegacy`/`ofLegacyName`/`LEGACY_NAME_MAP` (`CNFanProcessingTypes`) ; `registerAndWrap` + vars locales inutilisées (`CNDensityFunctions`) ; `CExplode` (config `size`/`type`/`time` jamais lue) ; `IrradiatedBiomes.monsters()` (corps commenté, no-op) ; `VANILLA_RANGE`/`cubeBottomTop` (`PaletteBlockPattern`) ; `getVariant()` (`CNPaletteStoneTypes`) ; `START=null` (`CNAdvancement`) ; surcharge `RadiationRegistry.getRadiation(biome,player)` ; `VirtualReactorInputFluid.removeFluid`/`getAmount` ; `matchesWithResult`/`construct`/`MultiblockMatchResult`/`getDistanceControllerTest` (lib) ; `ensureProperlyBuilt` (`SimpleMultiBlockPatternBuilder`).

### Imports morts / doublons

`CNBlocks.java` (`ResourceLocation`, `BlockTags`, `Enriching*Block` importés 2×) ; `AntiRadiationArmorItem.java` (`Util`/`Component`/`Attribute`/`world.item.*` en double, `TagKey`) ; `ReactorFluidTypesValue.java` (`reflect.Array`/`Field`, `Map`, `ForgeRegistries`) ; `CreateNuclearDatagen.java:18` (`CNShapelessRecipeGen`) ; `CNMixingRecipeGen.java:7` (`Fluids`) ; import dupliqué `IMultiBlockPatternBuilder` (`SimpleMultiBlockPatternBuilder:6-7`).

---

## 4. ⚡ Performance

| Fichier:ligne | Problème | Fix |
|---|---|---|
| `content/multiblock/pattern/ReactorPattern.java:96` | `findController` / `findControllerPos` scannent **~3971 blocs** (`getBlockState` + `getBlockEntity`) à **chaque pose/casse** de casing/frame/cooler/output — et **aussi côté client** (pas de garde `isClientSide`). Boucles avec `!=` comme condition d'arrêt et `new BlockPos` par itération. | Réduire la zone, `MutableBlockPos`, court-circuit dès trouvé, garde `isClientSide`, `<` au lieu de `!=`. |
| `content/effects/RadiationEffect.java:40` | Le predicate reconstruit un `HashSet<EntityType>` + reparse toutes les `ResourceLocation` de la blacklist **à chaque entité candidate** de la bounding box, **à chaque tick** (override `isDurationEffectTick → true`). | Cacher le `Set` (invalidé au reload config). |
| `content/multiblock/alarm/ReactorAlarmEntity.java:95` | `tickServer()` appelé inconditionnellement (y compris **client**, pas de garde `!isClientSide`), `awardPlayer` à chaque tick quand `POWERED`. | Garde `!isClientSide`, n'award qu'au front montant. |
| `api/multiblock/fluid/ReactorFluidType.java:55` | `getTypeForFluid` : double boucle sur le registry `FLUID_TYPE` + `ForgeRegistries.FLUIDS.getKey(fluid)` recalculé à chaque itération interne. | Calculer la clé une fois avant le filtre. |
| `foundation/events/overlay/HelmetOverlay.java:73` | `gui.renderHotbar(12f, graphics)` re-rendu depuis un overlay HELMET chaque frame (registerAbove → double rendu possible) + lookups inventaire répétés (`getArmor(HEAD)` 2×/frame). | Vérifier la nécessité, mémoriser le casque par frame. |

*Mineurs* : `NuclearMushroomCloudParticle:136` (`new PoseStack()` + `endBatch()` par frame), `ReactorSummaryDisplaySource:180` (`labelWidth()` recalcule des traductions par affichage).

---

## 5. ♻️ Cleanup notable (qualité)

- **Duplication `drawGauge`** copiée à l'identique dans 5 `DisplaySource` (Cooler/Fuel/Heat/LiquidLevel/Summary) + magic numbers `maxFuel=64`, `maxCooler=64`, `maxFluid=16000`, `maxHeat=1000` → extraire `AbstractReactorStatDisplaySource`.
- **`GeneratedRecipeBuilder` dupliqué** entre `CNStandardRecipeGen` et `CNShapelessRecipeGen` (+ typo param `enterFolder(String foldedr)`, `createLocation()` jamais appelé).
- **`moddedCompacting`/`moddedPaths` dupliqués** entre `EnrichedRecipeGen` et `SnowPowderRecipeGen` (et `moddedPaths` retourne toujours `null` avec une signature `GeneratedRecipe` trompeuse).
- **Framework de modèle décompilé** (`CNBasicModelPart`, `CNTabulaModelRenderUtils`, `CNAdvancedModelBox`) : noms de variables FernFlower (`lvt_10_1_`, `p_228305_1_`, `var9`), `ModelBox`/`TexturedQuad`/`PositionTextureVertex` dupliqués, shadowing de champs `cubeList`/`childModels`.
- **Logs de debug en prod** : `LOGGER.info("EXPLOSIOOOOOON…")` à chaque explosion (`NuclearMushroomCloudParticle:76`), `LOGGER.warn` par entité touchée (`NuclearExplosionEntity`).
- **Typos dans des noms publics** : `RadiationEffetcHandler` (→ Effect), `SolidRenderedPlaceableFluidtype` + param `stillTecture`, `caracter` (record `MultiBlockOffsetPos`), `contruct` (`IMultiBlockPattern`), `foldedr`.
- **`fireDamage`** non utilisé dans le constructeur `EnrichingCampfireBlock` (valeur 5 passée pour rien) ; dead store `i = ...get2DDataValue()` jamais relu (`EnrichingCampfireBlockEntity:27`) ; branches `isShiftKeyDown` identiques (`ReactorBluePrintItem:51`) ; `use()` redondant retournant `PASS` (`EnrichingCampfireBlock:61`).
- **Commentaires FR/EN mêlés** un peu partout, code commenté résiduel, double `;;` (`CNMixingRecipeGen`, `CNStandardRecipeGen:78`, `CNCClient`), en-têtes FernFlower (`Maths`, `SimplexNoise`).
- **`unlockedBy("has_storage_blocks_steel_nugget", ingots/thorium)`** — critère copié-collé incohérent pour `THORIUM_NUGGET` (`CNItems:186`).

---

## 6. 🏗️ Design

- **Inversion de couche `api/`** (cf. §1.1) — `api.multiblock`/`api.radiation` dépendent du `content` concret.
- **`MultiBlockManagerBeta` couplé à `ReactorControllerBlockEntity`** : la détection de pattern écrit `setMultiblockFacing` en **effet de bord** dans une BlockEntity concrète (mélange détection + mutation). Fix : retourner la `Direction`, laisser l'appelant l'appliquer.
- **Deux `DeferredRegister` concurrents** pour `ForgeRegistries.PARTICLE_TYPES` (`CNParticleRegistry` brut vs `CNParticleTypes` enum wrapper) — pas un bug (Forge le supporte) mais deux mécanismes/styles parallèles.
- **`controller` typé `ReactorControllerBlock`** (le Block singleton sans état positionnel) dans `ReactorInputEntity`/`ReactorOutputEntity` au lieu de la BlockEntity/BlockPos → champ qui n'apporte aucune info ; champ jamais lu dans `ReactorInputEntity`.
- **`RadiationRegistry` parallèle à `IRadiationSource`** sans règle de priorité → risque de double comptage latent (`RadiationCapability:116` additionne les deux inconditionnellement ; inoffensif aujourd'hui car aucun item n'est dans les deux).
- **`HudOverlay` sur-spécifiée** : `getPriority`/tri sans effet réel sur l'ordre Forge ; `HudRenderer` instancié comme champ alors qu'il est sans état.
- **`@Mod.EventBusSubscriber` sans modid** + `onServerTick()` no-op enregistré pour rien + nommage trompeur `CommentEvents`/`CommentEventClients`.
- **`api/` nommé `Beta`** : surface d'API provisoire promue dans le package censé être stable.
- **`ClothTagHelper`** : copie `base` puis l'écrase toujours (copie inutile dans le flux réel) ; `foundation/util` vs `foundation/utility` sans règle.

---

## 7. ✅ Le bon (à conserver)

- **Bootstrap idiomatique** : un `CreateRegistrate` + classes `CN*` par domaine appelées depuis `CreateNuclear.onInitialize()` → point unique prévisible ; wiring `DeferredRegister` bien séparé du forge-bus.
- **`api/` + `impl/` datapack registries** : `RodType`/`ReactorFluidType` data-driven avec Codecs + **Builder à validation explicite** (accumule les champs manquants, `IllegalStateException` précise, résolution config paresseuse au runtime) → réacteur extensible par datapack. Très bon design.
- **`RadiationCapability`** : recalcul de la radiation d'inventaire **gardé par un hash** (`InventoryHashUtil`), sync uniquement si `needsSync` → évite le travail par tick sur le hot path joueur.
- **`NuclearExplosionEntity`** : libération **symétrique** des chunks force-loadés (start tick / `remove()`), état `loadingChunks` persisté en NBT pour relâcher après reload.
- **Isolation compat** : `Mods.isLoaded` + pont `AlexscaveCompat` via cast `Object` → pas de `NoClassDefFoundError` quand Alex's Caves est absent. Record `SimpleMultiBlockPattern` immutable (`List.copyOf`/`Map.copyOf`).
- **`CNPackets`** (enum + `PacketType` à index auto) et **`CNTags`** (enum NameSpace factorisé, `matches()` typés) : peu de boilerplate, cohérent.
- **`CNAdvancementBehaviour`** : self-cleanup propre (retire les advancements obtenus, libère `playerId`), server-only, FakePlayer ignoré, persistance NBT.
- **`CNFanProcessingTypes`** : wrappers de recette statiques réutilisés (pas d'allocation par item sur le hot path).
- **`CNProcessingRecipeGen`** : agrégation des sous-générateurs derrière un `DataProvider` unique (`allOf` de `CompletableFuture`).
- **`NotifyUtil`** : gardes side serveur cohérentes, bons overloads `LangBuilder`/`MutableComponent`.
- Direction `controller/{manager,service,consumable}` (interfaces `I*` + impl `Default*`) : la bonne trajectoire stratégie/DI — **à terminer**.

---

## 8. 📋 Plan d'action priorisé

**Quick wins (≈1 ligne, fort impact)** — corriger d'abord : **B1** (`&& false`), **B2** (sync radiation), **B3** (cooldown), **B5** (`return null`), **B9** (palier radiation), **B18** (clé config) ; puis **B4** (loot thorium).

**🔴 Priorité 1 — nettoyage à risque faible**
1. Supprimer les reliques V2 mortes : `lib.multiblock.manager.*`, `IBetterPattern`, builders non-aisle, framework keyframe, `CommentEventClients`, `possible code`, `CreateNuclearDamageSources`, `CNShapelessRecipeGen`, `RadiationOverlay`/`EasingHudOverlay`, `IrradiatedSurfaceRules` v1. → **gain le plus élevé pour le moindre risque.**
2. Mettre `run/` dans `.gitignore` (et le dé-tracker du repo).

**🔴 Priorité 2 — bugs fonctionnels**
3. Traiter les bugs critiques + importants du §2.

**🟠 Priorité 3 — dette structurelle**
4. Corriger l'**inversion de couche `api/`** : `api.multiblock`/`api.radiation` ne définissent que des interfaces/types ; le `content` dépend de l'api (retourner un `IReactorController` au lieu de `ReactorControllerBlockEntity` ; sortir le wiring `RodType`/`ReactorFluidType` de `api`).
5. **Terminer la décomposition** de `ReactorControllerBlockEntity` (BlockEntity = coordinateur mince, cible < 300 lignes, délégation complète à `manager`/`service`/`consumable`) ; résoudre les **2 systèmes de verrou de fluide** et les **2 `saveData`** en un seul.

**🟡 Priorité 4 — cohésion**
6. Consolider la radiation en **un** module (api = `IRadiationSource` seul) ; extraire le framework de modèle hors de `content/explosion` ; créer un sous-package `registration/` ; fusionner `foundation/util` → `utility` ; router l'accès config via `ConfigValueResolver` ; renommer les symboles `Beta` ; **ajouter des tests** (`src/test` est vide).

---

*Note : les findings de type bug/dead-code ont été vérifiés de façon adversariale ; 11 ont été réfutés et écartés du rapport. Les sévérités intègrent les corrections des vérificateurs (ex. plusieurs bugs « high » initiaux sont dormants/latents tant qu'un chemin précis n'est pas câblé — signalé au cas par cas ci-dessus).*
