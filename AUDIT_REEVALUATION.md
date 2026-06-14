# 🔍 Réévaluation indépendante — Create Nuclear (branche `V2-CorrectifAudit`)

> Nouvelle analyse complète du code (`src/main/java`, hors `src/generated` et `src/main/resources`), réalisée indépendamment de `AUDIT.md`, puis comparée à ce dernier. 4 revues par zone (multiblock/réacteur, radiation/effets/équipement/explosion, events/utility/worldgen/config, package racine/recettes) + vérifications ciblées par grep/lecture.

---

## 0. Constat général

Le projet a visiblement subi un **gros travail de nettoyage depuis l'audit** (commits `d0325da2`, `42565d89`, `cedf05cd`, `01d5eb55`, `62305467`, `b99b60c1`, `67798bdf`, `b4948109`...). Plusieurs problèmes structurels majeurs de `AUDIT.md` sont **réglés**, mais une bonne partie des bugs fonctionnels « quick win » identifiés sont **toujours présents tels quels**, et de **nouveaux bugs** apparaissent dans le code récemment extrait (managers, packets de blueprint).

---

## 1. Points de `AUDIT.md` **toujours valides**

### Bugs critiques (§2)

| # | Statut | Détail vérifié |
|---|---|---|
| **B1** `EventTextOverlay.isActive() = timer>0 && false` | ✅ Toujours présent | `foundation/events/overlay/EventTextOverlay.java:40`. `triggerEvent()` n'a aucun appelant — overlay 100% mort. |
| **B3** `VicinityEffect` cooldown déclaré dans la boucle | ✅ Toujours présent (Critique confirmé) | `content/effects/VicinityEffect.java`. `cooldowns`/`getCooldown`/`setCooldown` toujours inutilisés. `RadiationEffect` réapplique l'effet à chaque tick de durée à toutes les entités proches au lieu de toutes les 500 ticks. |
| **B4** `THORIUM_BLOCK` drop `RAW_URANIUM` + tags d'ore | ✅ Toujours présent | `CNBlocks.java:599-618`. Toujours copié-collé d'un minerai d'uranium, tags `ores*`/`THORIUM_ORES` conservés. |
| **B5** `BiomeTagRule.apply()` retourne `null` | ✅ Toujours présent, mais **dormance accrue** | `infrastructure/worldgen/biome/surfacerule/BiomeTagRule.java:21`. Le seul appelant (`IS_IRRADIATED_PLAIN`/`IS_HIGHLANDS`) est lui-même mort → bug latent mais inatteignable. |
| **B6** Packet blueprint — argument uranium perdu | ✅ Toujours présent (variante) | `ReactorBluePrintItemScreen.java` : args 5/6 inversés à l'appel **et** `ReactorBluePrintItemPacket` envoie `countGraphiteRod` deux fois → `countUraniumRod` jamais transmis. |
| **B7** `CNNoiseData.bootstrapRegistries()` vide, `EROSION` jamais enregistré | ✅ Toujours présent — **risque accru** | `EROSION` est désormais consommé par la `DEFAULT_RULE` qui, elle, est **active** (alors que l'audit la jugeait dormante). À vérifier en jeu : possible échec de génération du monde irradié. |
| **B9** `RadiationCapability.applyEffects` deux branches → `amplifierLevel0` | ✅ Toujours présent | `content/radiation/capability/RadiationCapability.java:154-157` (déplacé depuis `content/effects/capability`). Le palier `radiationLevel1` est toujours inopérant. |
| **B10** `IrradiatedOverlayRendererVision` sans null-check `mc.player` | ✅ Toujours présent | `foundation/events/overlay/IrradiatedOverlayRendererVision.java:24-26`. |
| **B12** `AnimalUtil.isFood` teste le tag NBT `"Ingredient"` jamais écrit | ✅ Toujours présent | Vérifié par grep global : aucun writer de `"Ingredient"` dans tout `src/main/java`. Le mécanisme d'élevage/soin du poulet irradié via `isFood` reste cassé. |
| **B13** Armure anti-radiation, bras invisibles en mode CHEST | ✅ Toujours présent | `AntiRadiationArmorClientExtensions.java:39-40` utilise toujours `model.rightArm`/`leftArm` (champs hérités non rendus) au lieu de `right_arm`/`left_arm`. |
| **B15** `try/catch(Exception)` comme branchement dans `NuclearExplosionEntity` | ✅ Toujours présent | Ligne ~226, avale toute exception silencieusement. |
| **B16** `ModdedCookingRecipeResult` construit avec `conditions=null` puis `.forEach` | ✅ Toujours présent | `CNStandardRecipeGen.java:351/391`. NPE datagen latent confirmé tel quel. |
| **B18** `CNCClient.screenShaking` réutilise la clé TOML `nuclearBombFlash` | ✅ Toujours présent (Critique) | `infrastructure/config/CNCClient.java:8-9`. |
| **B19** `IS_HIGHLANDS = biome()` (varargs vide → toujours faux) | ✅ Toujours présent | Fichier renommé `IrradiatedSurfaceRules` (v2 fusionné/renommé), branches `MOON_DIRT`/secondaire toujours mortes. |

### Dead code (§3) toujours présent

- **Framework d'animation keyframe** (`CNModelAnimator`, `CNAnimation`, `CNIAnimatedEntity`, `CNAdvancedEntityModel`/`CNBasicEntityModel`/`CNTabulaModelRenderUtils`/`CNTextureOffset`/`CNTransform`, etc., 10 fichiers) dans `content/explosion` — toujours **100% mort**, zéro appelant.
- **`CreateNuclearDamageSources`** vs **`CNDamageSources`** — toujours dupliqués à l'identique, 2 sites d'appel toujours sur la mauvaise classe.
- **`CommentEventClients.java`** — coquille vide toujours présente.
- **`foundation/events/possible code`** — fichier brouillon sans extension, toujours présent.
- **`CNShapelessRecipeGen`** — toujours mort (registration commentée dans `CreateNuclearDatagen`), duplique `GeneratedRecipeBuilder` de `CNStandardRecipeGen`.
- **`RadiationOverlay`/`EasingHudOverlay`** — `RadiationOverlay` toujours non-instanciée comme overlay (commentée dans `HudRenderer`), mais ⚠️ nuance : `RadiationOverlay.setCoverage(...)` est appelé statiquement par `HelmetOverlay:69` → pas du tout-mort, juste **partiellement mort** (l'overlay lui-même ne s'affiche jamais, mais une de ses méthodes statiques est utilisée ailleurs).
- **`VerifyPattern5x5/7x7/9x9`** (`ReactorPattern`) — toujours mortes, et ont **divergé** des vraies définitions de pattern dans `CNMultiblock` (ex: `"ODDDO"` vs `"ADDDA"`).
- **`saveData2`** (`ReactorBluePrintMenu:160-179`) — toujours mort et toujours buggé (pas de reset).
- **`VicinityEffect.cooldowns`/`getCooldown`/`setCooldown`**, **`RadiationCapability.getArmorResistance`**, **`VirtualReactorInputFluid.removeFluid/getAmount`**, **`MultiblockMatchResult`/`matchesWithResult`/`contruct`** (lib), **`CNAdvancement.START=null`**, **`CExplode`** (config jamais branché), **`registerAndWrap`** (`CNDensityFunctions`), **`IrradiatedBiomes.monsters()`** no-op, **`PaletteBlockPattern.VANILLA_RANGE`** (doublon de `STANDARD_RANGE`), **`CNFanProcessingTypes.parseLegacy/ofLegacyName/LEGACY_NAME_MAP`** — tous **toujours présents et morts**.
- **Imports doublons dans `CNBlocks.java`** — toujours présents malgré le commit `cedf05cd` (de nouveaux doublons ou ratés sur ce fichier précisément).

### Design (§6)

- **Package racine fourre-tout** (~25 classes `CN*` à plat) — **toujours tel quel**, aucune sous-décomposition.
- **`run/` versionné** — toujours suivi par git (19 fichiers, dont des `hs_err_pid*.log`, un jar désactivé, `imgui.ini`).
- **Aucun test** — `src/test` toujours vide/absent.
- **Duplication `drawGauge`** dans les 5 `DisplaySource` — toujours présent à l'identique, plus `ReactorSummaryDisplaySource` qui réimplique sa propre version + magic numbers (64/64/16000/1000) toujours dupliqués.
- **`GeneratedRecipeBuilder` dupliqué** entre `CNStandardRecipeGen`/`CNShapelessRecipeGen`, et **`moddedCompacting`/`moddedPaths` dupliqués** entre `EnrichedRecipeGen`/`SnowPowderRecipeGen` (et `moddedPaths` toujours `return null` après une boucle dont le résultat est jeté) — **toujours présents, toujours morts**.
- **Commentaires FR/EN mêlés** un peu partout (`ClientEvents`, `CNFluids`, mixins, explosion/particles, équipement irradié) — toujours présent.
- **Logs de debug en prod** — `"EXPLOSIOOOOOON…"` toujours présent dans `NuclearMushroomCloudParticle`.

---

## 2. Points **corrigés**

| Point AUDIT | Vérification | Conclusion |
|---|---|---|
| **B2** — `RadiationSyncPacket` no-op | `find` global : aucune classe `*RadiationSync*` n'existe plus | ✅ **Corrigé** (disparu lors de la consolidation radiation, commit `42565d89`) |
| **B11** — `LOGGER...coreList.toString()` avant null-check (`SimpleMultiBlockAislePatternBuilder`) | Code actuel : le null-check précède bien le message d'erreur, avec ternaire `coreList != null ? coreList.size() : 0` | ✅ **Corrigé** |
| **B17** — `RodsTooltipHandler` condition de namespace inversée | Agent confirme la classe « correctly wired and free of obvious bugs » après lecture complète | ✅ **Corrigé** (probablement supprimé/réécrit) |
| **Inversion de dépendance `api/`** — `MultiBlockManagerBeta`, `RodType`, `ReactorFluidType` important `content.*` | Grep des imports : **aucun** des trois n'importe quoi que ce soit de `content.*` | ✅ **Corrigé** — la couche `api/` n'a plus de dépendance vers `content/` |
| **Deux frameworks multiblock concurrents** (`lib.multiblock.manager.*`, `IBetterPattern`, builder non-aisle) | `find` confirme : `lib/multiblock/manager` n'existe plus, `IBetterPattern`/`MultiBlockManager`/`MultiBlockCache`/`SimpleMultiBlockPatternBuilder` (non-aisle) n'existent plus | ✅ **Corrigé** — seuls les éléments réellement utilisés de `lib.multiblock` (`SimpleMultiBlockAislePatternBuilder`, `SimpleMultiBlockPattern`) subsistent |
| **Feature radiation éparpillée sur 4 couches** | `foundation/networking/radiation` et `foundation/item/radiation` n'existent plus ; tout est dans `content/radiation`, `api/radiation`, `content/effects` | ✅ **Largement corrigé** (3 packages au lieu de 4, cohérent — reste un découpage en 3 plutôt qu'1 mais c'est raisonnable) |
| **Seam `ConfigValueResolver` non utilisé** | Désormais utilisé par `RadiationEffect` (`ConfigValueResolver.loadValuesInSet`) | ✅ **Corrigé** (au moins pour ce cas d'usage) |
| **`foundation/util` vs `foundation/utility` redondants** | Seul `foundation/utility` existe désormais | ✅ **Corrigé** |
| **God class `ReactorControllerBlockEntity` (886 lignes)** | Désormais **718 lignes** ; logique extraite vers `manager/`, `service/DefaultHeatService`+`HeatManager`, `consumable/ConsumptionCycleManager`, `ReactorMeltdownExecutor`, `ReactorFrameDisplayManager`, `display/ReactorDisplayState`+`ReactorGoggleTooltipRenderer`, `ReactorDebugDiagnostics` | 🟡 **Partiellement corrigé** — réduction réelle (-168 lignes) et architecture manager/service/display bien amorcée, mais reste loin de la cible « <300 lignes » et `tick()`/`handleAssembledState()` restent de gros méthodes multi-responsabilités |
| **B14 — `FluidLockManager` static map non keyée par dimension** | Le problème de clé `BlockPos` sans dimension est confirmé toujours présent | 🟡 **Partiellement** — le sous-point « 2 systèmes de verrou parallèles (`PersistentFluidLocks`) » n'a pas été re-confirmé dans cette passe, possiblement réduit |

---

## 3. Points **devenus obsolètes** (zone/fichier supprimé, finding non-applicable)

- **`IrradiatedSurfaceRules` v1** vs v2 — il n'y a plus qu'**un seul** fichier `IrradiatedSurfaceRules` (le « v2 » a été renommé/fusionné, commit `90ad3849`/`d0325da2`). Le point « doublon v1/v2 » disparaît, mais **le bug `IS_HIGHLANDS = biome()` subsiste dans le fichier survivant** (cf. §1, B19).
- **Audit B8** (`clearLockIfAllInputsEmpty` NPE par `findStructure(...).data().getSize()` sans null-check) — la méthode existe toujours (`controller/ReactorControllerBlockEntity.java:685-718`) mais la nouvelle revue ne relève plus de risque NPE direct ; en revanche elle relève un **nouveau problème de performance** (re-scan complet de la structure à chaque vidage de fluide). Le risque a changé de nature plutôt que disparu — je le classe « reformulé » plutôt que strictement obsolète.
- **`ReactorFluidTypesValue.java`** (cité dans « imports morts ») — n'apparaît plus dans `api/multiblock/fluid/` (seul `ReactorFluidType.java` existe) → ce fichier/finding est obsolète.
- **Couplage `MultiBlockManagerBeta` ↔ `ReactorControllerBlockEntity` (effet de bord `setMultiblockFacing`)** — non re-confirmé par la revue actuelle ; la classe ne dépend plus de `content.*` du tout (cf. §2), donc soit ce couplage a été supprimé, soit il a été restructuré. À vérifier ponctuellement si pertinent, mais le problème **tel que décrit** (import direct) n'existe plus.

---

## 4. Points **non confirmés / nécessitent vérification ciblée** (ni confirmés ni infirmés dans cette passe)

- Perf : scan ~3971 blocs par `ReactorPattern.findController` sans garde client (§4 audit) — non re-vérifié explicitement.
- Perf : `ReactorAlarmEntity.tickServer()` sans garde `!isClientSide` — non re-vérifié.
- Perf : `ReactorFluidType.getTypeForFluid` double-boucle — non re-vérifié (la revue a trouvé un *autre* problème sur ce fichier, voir nouveaux problèmes).
- `HelmetOverlay` double rendu hotbar — non re-vérifié.
- `InventoryHashUtil` incluant `getDamageValue()` dans le hash — non re-vérifié.
- `ReactorOutput.requireNonNull`+`assert` — confirmé présent par la nouvelle revue (voir §5), donc **toujours valide** en fait.
- `RodsTooltipHandler`/`TextUtils`/`Mods`/`SoundEntryBuilder`/`BigFluidStack` helpers morts — `Mods.getBlock/getItem` confirmés encore morts (Mineur), le reste non re-vérifié individuellement.

---

## 5. **Nouveaux problèmes** non mentionnés dans `AUDIT.md`

Ces points ressortent de la nouvelle revue et concernent essentiellement le **code récemment extrait** lors de la décomposition (managers, packets, services) — ce qui est cohérent : le refactor a déplacé le code mais introduit de nouvelles régressions ponctuelles.

| # | Fichier:ligne | Problème | Gravité | Conséquence | Recommandation |
|---|---|---|---|---|---|
| N1 | `content/multiblock/IHeat.java:73-75` | `isNotDanger(heat)` = `of(heat) != DANGER \|\| of(heat) != NONE` — **tautologie toujours vraie** | 🔴 Critique | La garde `if (IHeat.HeatLevel.isNotDanger(heat))` à `ReactorControllerBlockEntity:530` ne filtre jamais l'état DANGER — la suppression de la rotation de sortie en cas de surchauffe ne se déclenche jamais | `return of(heat) != DANGER && of(heat) != NONE;` |
| N2 | `manager/ReactorInputFluidManager.java:128-129, 147-148` | `getFluidInTank(getTanks())` — off-by-one (index = count au lieu de count-1), et `extractFluids` ne décrémente jamais `fluidNeeded` | 🔴 Critique | Comptage/extraction de fluide d'entrée potentiellement faussé/cassé à l'exécution | Itérer `for (int t=0; t<getTanks(); t++)` comme `ReactorFrameDisplayManager` le fait correctement |
| N3 | `bluePrintItem/ReactorBluePrintItemPacket.java:20, 80-91` | `private static double totalInit` (statique, jamais initialisé → 0.0) ; `Double.isNaN(totalInit)` ne se déclenche jamais (0.0 ≠ NaN), et le champ **statique est partagé entre tous les joueurs/items** | 🟠 Majeur | Division par 0 → `Infinity`/`NaN`, contamination de la progression entre différents items blueprint de joueurs différents | Champ d'instance, init correcte, revoir `Math.pow(3600/5000, count)` (overflow pour count≥4-5) |
| N4 | `output/ReactorOutputEntity.java` | `speed` n'est jamais mis à une valeur ≠ 0 (seulement `tick()` le met à 0) ; `determineSpeed()` jamais appelé avec un résultat exploité | 🟠 Majeur (à vérifier en jeu) | L'arbre de sortie du réacteur pourrait ne jamais tourner / ne jamais générer de stress | Vérifier le câblage avec `ReactorControllerBlockEntity.rotate()` |
| N5 | `output/ReactorOutput.java:63-67` | `Objects.requireNonNull(...)` puis `assert entity != null` (désactivé en prod) avant `entity.getAssembled()` | 🟠 Majeur | Crash NPE possible à l'interaction si le bloc 3 au-dessus n'est pas le bon type (race pendant désassemblage) | Remplacer `assert` par un vrai null-check → `InteractionResult.FAIL` |
| N6 | `output/ReactorOutput.java:65`, `output/ReactorOutputEntity.java:119` | Offset codé en dur `pos.above(3)` pour trouver le contrôleur, indépendant de la taille réelle du multiblock (5/7/9) | 🟠 Majeur | Cassure silencieuse si la géométrie de pattern change selon la taille | Router via `ReactorPattern`/helper multiblock plutôt qu'un offset fixe |
| N7 | `input/fluid/ReactorLiquidInput.java:85-88` | `use()` exige `player.isCreative()` avant de déléguer au remplissage/vidange par seau | 🟠 Majeur (à vérifier) | Les joueurs en survie ne pourraient pas remplir/vider l'entrée fluide du réacteur au seau | Lever/retirer la garde `isCreative()` si non intentionnelle |
| N8 | `controller/ReactorControllerBlockEntity.java` — `liquidLife`/`updateLiquidTimers()` | Mécanisme de "progression liquide" mort, persisté en NBT, supplanté par `fluidBuffer` ; appel commenté à une méthode `calculateLiquidProgress()` inexistante | 🟡 Mineur | I/O NBT inutile, confusion (2 systèmes parallèles dont 1 mort) | Supprimer `liquidLife` et `updateLiquidTimers()` |
| N9 | `controller/manager/ReactorOutputManager.java` + interface | `distributeSU` (47 lignes) + méthode d'interface + 26 lignes d'exemple commenté, jamais appelé | 🟡 Mineur (mort, ~73 lignes) | Code mort issu de l'extraction récente | Câbler dans le tick de sortie ou supprimer |
| N10 | `manager/ReactorInputFluidManager.java:95` | `LOGGER.warn("getBlocksPosition: {} {}", ...)` appelé chaque tick | 🟡 Mineur | Spam de logs niveau WARN en jeu normal | Retirer |
| N11 | `controller/consumable/FluidConsumable.java` + `IConsumable` | Classe entière (49 lignes) construite seulement par sa propre `deserializeNBT`, `consume()` ne fait jamais rien, rien ne sérialise de tag `"fluid"` | 🟡 Mineur (mort) | Fonctionnalité moitié-implémentée laissée dans le code extrait | Supprimer ou implémenter |
| N12 | `controller/consumable/ItemConsumable.java:43` `deserializeNBT` | Construit `new ItemConsumable(itemName, null)` — `rodType=null`, alors que `computeTimer()` appelle `rodType.rodTimer()` | 🟡 Mineur (NPE latent) | NPE si `computeTimer()` est appelé après désérialisation | Résoudre `rodType` depuis le registre au lieu de `null` |
| N13 | `output/ReactorOutputEntity.java:55-59, 195-229` | `ReactorOutputValue` (35 lignes, impl complète de `ValueBoxTransform.Sided`) jamais instanciée, seule référence commentée | 🟡 Mineur (mort) | Scaffolding de comportement de scroll-value mort | Réactiver ou supprimer |
| N14 | `controller/ReactorControllerBlock.java:123` | `state.setValue(ASSEMBLED, false)` — résultat jeté, pas de `setBlockAndUpdate` | 🟡 Mineur | Instruction trompeuse, pas de changement d'état réel | Supprimer ou corriger |
| N15 | `reactorLogic/DefaultHeatCalculator.computeHeat` | Boucle imbriquée O(grille×voisins) re-scannant la liste d'items à chaque voisin | 🟡 Mineur perf | Calcul de chaleur coûteux (périodique, pas par tick) | `Map<Integer,ItemStack>` indexé par slot |
| N16 | `content/contraptions/irradiated/IrradiatedAnimal.startConverting` | `Math.min(difficulty.getId()-1, 0)` au lieu de `Math.max` → NORMAL/HARD = même amplificateur que EASY, PEACEFUL → -1 | 🟠 Majeur | Le bonus de dégâts de conversion ne scale jamais avec la difficulté ; valeur négative possible en PEACEFUL | `Math.max(difficulty.getId()-1, 0)` |
| N17 | `content/contraptions/irradiated/wolf/IrradiatedWolfModel.headParts()/bodyParts()` | Retourne `null` au lieu de `ImmutableList.of(...)` (contrairement à Cat/Chicken/Cow) | 🟠 Majeur | Violation de contrat `Iterable<ModelPart>`, NPE latent si une logique parent itère ces listes | Retourner `ImmutableList.of(...)` comme les autres modèles irradiés |
| N18 | `content/equipment/cloth/ClothItem.java` (`@SuppressWarnings("unused")`, enum `Cloths`) | Enum 16 `DyeColor` + helpers jamais utilisés en dehors de sa propre déclaration, duplique `CNItems.CLOTHS` | 🟡 Mineur (mort/duplication) | Risque de divergence avec `CNItems.CLOTHS` | Supprimer si confirmé inutile |
| N19 | `content/particles/SmallNuclearExplosionParticle.java` | 13 classes Factory quasi-identiques (Nuke/Mine/Underzealot/Raygun/...) ; override `getQuadSize` no-op ; littéral caractère `'＀'` (U+FF00) utilisé comme masque binaire au lieu de `0xFF00` | 🟡 Mineur | Duplication ×13, lisibilité | Factoriser via constructeur paramétré ; remplacer le littéral par `0xFF00` |
| N20 | `content/explosion/CNNuclearExplosionSound.java:26` | `this.volume = 20` alors que la logique de fade attend `[0,1]` | 🟡 Mineur | Valeur initiale incohérente (masquée par le 1er fade-in) | Initialiser à `0F`/`1F` |
| N21 | `content/contraptions/irradiated/wolf/IrradiatedWoldCollarLayer.java` | Faute de frappe "Wold"→"Wolf" dans le nom de classe/fichier, `render()` vide, non enregistrée | 🟡 Mineur (mort) | Scaffolding inachevé pour une fonctionnalité de collier | Finir ou supprimer |
| N22 | `content/redstone/displayLink/source/ReactorSummaryDisplaySource.java:25` | Import de `joptsimple.internal.Strings` (classe interne d'une lib tierce non liée) juste pour `repeat(' ', n)` | 🟡 Mineur | Dépendance fragile à une classe "internal" | Remplacer par `" ".repeat(n)` (Java 11+) |
| N23 | `content/logistics/BigFluidStack.java:74` | `nullHash(Object)` réinvente `Objects.hashCode`/`Objects.hash` | 🟡 Mineur | Code superflu | Utiliser `Objects.hash(stack, amount)` |
| N24 | `CNSoundEvents.java:118-120` + chemins `"reacteur/casing"` | Typo `"GeiGer Medium"` (casse incohérente vs "Geiger High/Low") ; chemins de sons en français (`reacteur`) vs anglais ailleurs (`reactor`) | 🟡 Mineur | Incohérence visible (sous-titres) et dans l'organisation des assets | Corriger la casse ; renommer les chemins (impacte les assets, hors périmètre resources) |
| N25 | `content/multiblock/ReactorAssembler.java:134-138` | `getPlayersInRadius` `@Deprecated`, 0 appelant | 🟡 Mineur (mort) | — | Supprimer |
| N26 | `input/item/ReactorInputEntity.java` / `output/ReactorOutputEntity.java` — champ `controller` typé `ReactorControllerBlock` | Type = singleton Block partagé (pas un état positionnel), jamais lu utilement dans `ReactorInputEntity` | 🟡 Mineur (design, repris de l'audit §6 mais avec confirmation que le champ est désormais carrément mort dans Input) | Confusion, état mort | Supprimer ou remplacer par `BlockPos`/référence BE |
| N27 | `casing/ReactorCasingEntity.java:25` | `setController` calcule un offset fixe `+4/+4`, indépendant de la taille (5/7/9) — semble appelée nulle part | 🟡 Mineur | Offset faux si jamais exercé pour 7x7/9x9 | Supprimer ou paramétrer par taille |
| N28 | `casing/ReactorCasing.java:73-75 onWrenched` | `context.getPlayer()` (nullable) déréférencé sans null-check | 🟡 Mineur | NPE si un déclencheur non-joueur (distributeur) actionne la clé | `if (player == null) return PASS;` |
| N29 | Managers Input/InputFluid/Output/Alarm — `getBlocksPosition(Level)` | 3 des 4 managers n'ont pas la garde `level == null` présente dans `ReactorAlarmManager` ; sérialisation x/y/z dupliquée ×3 vs `BlockPos.asLong()` dans Output | 🟡 Mineur (duplication/incohérence) | NPE potentiel, format de sauvegarde incohérent entre managers | Extraire `AbstractReactorIOManager` avec garde + sérialisation commune |
| N30 | `controller/ReactorControllerBlockEntity.java:357` | Lecture `configuredPattern.getOrCreateTag().getDouble("heat")` chaque tick sans la garde `isEmptyConfiguredPattern()` utilisée ailleurs (L412/519/554/560) | 🟡 Mineur | `getOrCreateTag()` mute `ItemStack.EMPTY` chaque tick — incohérence de garde | Ajouter la garde manquante |
| N31 | `manager/ReactorFrameDisplayManager.java:50-59` | Le ratio agrège la capacité/quantité de **tous** les tanks, mais `frameFluidCache` ne stocke que le **premier** fluide non-vide trouvé | 🟡 Mineur | Affichage trompeur si plusieurs fluides distincts présents dans le cadre | Restreindre à un seul type de fluide ou agréger par type |
| N32 | `api/multiblock/fluid/ReactorFluidType.java:196-213` `maxHeat()/efficiency()` | Retournent en dur `12`, lecture de config réelle commentée (`//CNConfigs.server()...`) | 🟡 Mineur | Bug fonctionnel potentiel si un `ReactorFluidType` a `useConfig=true` | Vérifier l'usage de `useConfig` ; restaurer la lecture config ou supprimer le chemin mort |
| N33 | `infrastructure/worldgen/biome/IrradiatedBiomes.java:17-18` `monsters(...)` | Méthode appelée avec de vrais paramètres (`95, 5, 100`) mais **corps totalement vide** | 🟠 Majeur | Le biome irradié n'a probablement **aucun spawn de mob configuré** malgré l'appel qui le suggère | Implémenter `SpawnerData` ou retirer l'appel trompeur |

---

## 6. Tableau de synthèse global (par rapport à `AUDIT.md`)

| Catégorie audit | Statut global |
|---|---|
| Bugs critiques B1,B3,B4,B5,B6,B7,B9,B10,B12,B13,B15,B16,B18,B19 | **Toujours valides** (14/19 hors B2/B11/B17 corrigés, B8/B14 reformulés/partiels) |
| Inversion `api/` ↔ `content/` | **Corrigé** ✅ |
| Deux frameworks multiblock | **Corrigé** ✅ (lib.multiblock réduit à ce qui est réellement utilisé) |
| Radiation sur 4 couches | **Largement corrigé** ✅ (3 couches cohérentes) |
| God class `ReactorControllerBlockEntity` | **Réduit (886→718 lignes), décomposition amorcée mais incomplète** 🟡 |
| `ConfigValueResolver` non utilisé | **Corrigé** ✅ |
| `foundation/util` vs `utility` | **Corrigé** ✅ |
| Package racine fourre-tout, `run/` versionné, absence de tests | **Toujours valides** |
| Dead code (keyframe, CreateNuclearDamageSources, CNShapelessRecipeGen, possible code, CommentEventClients, VerifyPattern*, saveData2, etc.) | **Toujours valides** (sauf IrradiatedSurfaceRules v1, fusionné) |
| Duplication drawGauge / recipe gen | **Toujours valides** |
| **Nouveaux problèmes** (N1-N33) | Concentrés dans le code récemment extrait (managers, packets blueprint, services, output) — cohérent avec un refactor en cours qui a déplacé sans toujours corriger |

---

## 7. Recommandations priorisées (indépendantes de l'audit)

1. **Quick wins très haute valeur** (1-3 lignes chacun, à corriger en premier) : **N1** (`IHeat.isNotDanger` tautologie), **B9** (palier radiation), **B18** (clé config dupliquée), **B3** (cooldown VicinityEffect), **B4** (loot thorium), **B1** (`&& false`), **N2** (off-by-one fluide).
2. **Bugs fonctionnels potentiellement bloquants pour le gameplay** : **N4** (vitesse de sortie réacteur jamais non-nulle), **N7** (remplissage fluide réservé à la créative ?), **N3** (progression blueprint cross-contaminée), **B6** (uranium jamais transmis).
3. **Nettoyage à risque faible, gain élevé** (continuer la dynamique déjà engagée) : supprimer le framework d'animation mort (~10 fichiers), `CreateNuclearDamageSources`, `possible code`, `CommentEventClients`, `CNShapelessRecipeGen`, `VerifyPattern*`, `saveData2`, `FluidConsumable`/`distributeSU`, `ReactorOutputValue`.
4. **Continuer la décomposition de `ReactorControllerBlockEntity`** (`tick()`/`handleAssembledState()` restent les derniers gros morceaux) et **harmoniser les 4 managers I/O** (null-check `level`, sérialisation position commune) — c'est exactement la trajectoire déjà entamée, à finir.
5. **Cohésion finale** : `run/` → `.gitignore`, ajouter au moins des tests unitaires sur la logique de pattern/heat (zéro test actuellement), réorganiser le package racine en sous-package `registration/`.
