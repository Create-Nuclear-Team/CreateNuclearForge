# Réévaluation indépendante — CreateNuclearForge (V2-CorrectifAudit)

**Méthodologie** : 7 audits indépendants couvrant la totalité de `src/main/java` (hors `src/generated` et `src/main/resources`), réalisés à partir de l'état actuel du code uniquement, puis recoupés avec `AUDIT_V1.md` et vérifiés par greps ciblés.

> Ce document ne liste plus que **ce qui reste à corriger**. Les bugs déjà résolus en sont retirés.

---

## 0. Constat global

Le code est en **refactor V2 actif** (extraction en cours de `ReactorInputSnapshotBuilder`, `ReactorGoggleTooltipRenderer`, `ReactorDisplayState`). La **grande majorité des bugs** de `AUDIT_V1.md` restent **présents**, et plusieurs **nouveaux problèmes** ont émergé dans les zones où les refactors V2 sont en cours (managers `controller/`, displaySources, radiation). Le dead code a été en grande partie nettoyé ; il reste un résidu non trivial (voir §2).

---

## 1. 🐛 Bugs importants toujours présents

| Réf. | Fichier:ligne | État | Détail |
|---|---|---|---|
| **B15** | `content/explosion/NuclearExplosionEntity.java` (~224) | **Toujours présent** | `try { onBlockExploded } catch(Exception){ destroyBlock(...,true) }` — exception comme branchement, avale les vrais bugs, sémantiques de drop différentes entre les deux branches. |

### Bugs mineurs confirmés toujours présents (liste condensée)
- `ReactorInputFluidManager.extractFluids/getBlocksPosition` : toujours `getFluidInTank(getTanks())` (off-by-one), `fluidNeeded` jamais décrémenté, `toExtract>1` ignore les extractions de 1 unité.
- `InventoryHashUtil` : toujours `h = 31*h + stack.getDamageValue()` → resync radiation à chaque variation de durabilité.
- `ReactorSizeDisplaySource` : toujours `tier*100/3` comme "%" — désormais creusé en détail (voir §3, nouveau finding lié).

---

## 2. 🧹 Dead code — reste à faire

> **⚠️ Ne pas supprimer ces éléments — code vivant** (faux positifs écartés) :
> - `CNTabulaModelRenderUtils` **n'est PAS du dead code** : il est utilisé par `CNAdvancedModelBox.cubeList`/`doRender`, sur le chemin de rendu **vivant** du champignon atomique (`NuclearMushroomCloudModel`). Conservé. La duplication `ModelBox` reste une opportunité de *refactor*, pas une suppression.
> - `ReactorOutputEntity.outputPos` **n'est PAS mort** : il est lu/écrit dans `read()`/`write()` (sérialisation NBT). Laissé en place.

Restant (grep global, zéro référence externe sauf mention) :

- **Méthodes d'animation mortes restantes** dans `CNAdvancedEntityModel`/`CNAdvancedModelBox` : `chainSwing`, `chainWave`, `chainFlap`, `faceTarget`, `walk`, `flap`, `swing`, `bob`, `moveBox`, `progressRotation*`, `progressPosition*`, `getMovementScale`/`setMovementScale`/`movementScale`, `transitionTo`, `calculateChain*`, `displayList`/`compiled`. Mortes mais **non triviales** : leur retrait déclenche une cascade dans des classes **vivantes** (ex. `CNAdvancedModelBox.calculateRotation`/`bob` appellent `model.getMovementScale()`). À traiter en nettoyage dédié + dédup `ModelBox`. **⚠️ `setRotateAngle` n'est PAS mort** : appelé ~10× par `NuclearMushroomCloudModel` (sous-classe vivante) — à conserver.
- **`RadiationOverlay` + `EasingHudOverlay`** : toujours commenté dans `HudRenderer.overlays` ; **`HelmetOverlay.setCoverage(...)` écrit dans le champ statique de `RadiationOverlay`, qui n'est lu par personne** — no-op silencieux (lié au §3 points 4 & 5 : décider entre supprimer `RadiationOverlay` ou réactiver et corriger le fade).
- **`SimpleMultiBlockPattern.test()`** : classe et méthode **toujours présentes** (`lib/multiblock/SimpleMultiBlockPattern.java:72`), instanciée via `IMultiBlockPatternBuilder` mais `test()` n'a **aucun appelant** — dead code résiduel à retirer.
- **`IrradiatedBiomes.monsters()`** : no-op appelé avec `(95, 5, 100)` silencieusement ignorés — à retirer ou implémenter (lié au §3 point 9, worldgen "irradié" template non terminé).

---

## 3. 🆕 Nouveaux problèmes / régressions apparus depuis l'audit

Ces points n'apparaissaient pas (ou pas sous cette forme) dans `AUDIT_V1.md` — ils résultent soit de zones non couvertes par l'audit précédent (cat/wolf/cow, displaySources détaillés), soit de refactors V2 en cours.

### 🔴 Critiques / Majeurs

1. **`IrradiatedCatCollarLayer` / `IrradiatedWoldCollarLayer` — feature de teinture de collier non câblée**
   `IrradiatedWoldCollarLayer.render()` est un corps **vide** ; `IrradiatedCatCollarLayer` (implémentation complète) n'est **jamais ajouté** via `addLayer(...)` au renderer. Pourtant `IrradiatedCat` câble entièrement l'interaction `DyeItem` → `setCollarColor`/`getCollarColor` (`IrradiatedCat.java:333-342`). `IrradiatedWolf` contient même des lignes commentées (`DATA_COLLAR_COLOR`) montrant une tentative abandonnée.
   *Conséquence* : un joueur peut teindre le collier d'un chat/loup irradié apprivoisé, l'action est acceptée, mais **rien ne s'affiche jamais** — feature visible cassée, non documentée dans `AUDIT_V1.md` (qui ne couvrait pas `content/contraptions/irradiated/{cat,wolf,cow}`).
   *Recommandation* : enregistrer `IrradiatedCatCollarLayer` dans le renderer du chat, implémenter `IrradiatedWoldCollarLayer.render`, ou retirer toute la mécanique `setCollarColor`/interaction `DyeItem` si hors scope.

2. **`AntiRadiationArmorItem.getArmorTexture` — hook mort en Forge 1.20.1**
   L'override `getArmorTexture(ItemStack, Entity, EquipmentSlot, String)` (renvoyant un chemin de texture par couleur de tissu via `ClothTagHelper.getArmorTexturePath`) **n'est plus appelé par Forge 47.2.x** — ce hook a été retiré depuis longtemps ; le rendu passe désormais exclusivement par `IClientItemExtensions`/`AntiRadiationArmorClientExtensions`, qui n'utilise jamais la couleur de tissu. Conséquence : toute la mécanique `ClothTagHelper`/`SmithingTransformRecipeMixin` (NBT `ClothColor`) n'a **aucun effet visuel** — teindre l'armure anti-radiation via tissu ne change rien à son apparence. C'est un **bug de fonctionnalité silencieuse**, distinct de B13 (qui concerne la visibilité des bras), non couvert par `AUDIT_V1.md`.
   *Recommandation* : implémenter la sélection de texture colorée dans `AntiRadiationArmorClientExtensions`/`AntiRadiationArmorModel`, ou retirer `getArmorTexture` + `ClothTagHelper.getArmorTexturePath` (et documenter que la teinture de tissu est cosmétiquement inerte).

3. **`RadiationEffectHandler` — 3ᵉ chemin d'application de la radiation, sans aucune garde**
   `RadiationEffectHandler.apply` (fuite de tuyau) applique `MobEffectInstance(RADIATION,3,2,...)` à **tout** `LivingEntity` à proximité, sans vérifier `CNConfigs.server().radiation.enabledItemRadiation`, sans vérifier `CNTags.CNEntityTags.IRRADIATED_IMMUNE`, sans vérifier le spectateur, et **sans tenir compte de la résistance anti-radiation**. C'est un chemin indépendant des deux autres (`RadiationCapability`/`RadiationEffect`), qui eux respectent ces gardes — mais selon deux sémantiques *différentes*, à ne pas fusionner :

   - `RadiationEffect` (filtre de contagion de proximité, constructeur lignes 32-52) applique un **gate binaire** (immune tag, config, blacklist, spectateur, `résistance ≥ 1`) — mais ce gate est enfermé dans un lambda non réutilisable, et **reconstruit un `HashSet` + reparse les `ResourceLocation` de la blacklist à chaque appel** (cf. §4 point 5 / §5) — perf non corrigée en plus du problème de duplication.
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

4. **Deux overlays de "vision irradiée" actifs en parallèle**
   `IrradiatedOverlayRendererVision` (actif) et `RadiationOverlay` (commenté dans `HudRenderer` mais toujours alimenté via `HelmetOverlay.setCoverage`) sont **deux implémentations concurrentes du même effet visuel**. `AUDIT_V1.md` mentionnait `RadiationOverlay` comme mort sans relever explicitement la duplication fonctionnelle avec `IrradiatedOverlayRendererVision` — confirmé ici comme une vraie redondance architecturale (deux fichiers, deux mécanismes de fade, un seul réellement rendu).

5. **`RenderHelper.renderTextureOverlay` — `Math.round(alpha*coverage)` annule le fade**
   `RadiationOverlay.java:49` passe `Math.round(alpha * coverage)` (un `int` 0 ou 1) à un paramètre `float alpha ∈ [0,1]` — le fade progressif de `EasingHudOverlay` est **binarisé** (apparition/disparition instantanée). Non décrit explicitement dans `AUDIT_V1.md` (qui notait juste "RadiationOverlay:49 binarise l'alpha via Math.round" en passant, dans une remarque connexe sur `RenderHelper`) — confirmé ici comme un bug à part entière, mais **actuellement sans impact visible** puisque `RadiationOverlay` n'est pas rendu (cf. point 4).

### 🟠 Importants

6. **`RadiationCapability.lastBiomeLocation` — non persisté**
   `lastBiomeLocation` **est bien lu pour une décision** (`RadiationCapability:89`, comparaison `!Objects.equals(biomeLoc, cap.getLastBiomeLocation())`) — ce n'est donc **pas** du dead code. Le point restant : `RadiationProvider.serializeNBT` ne le persiste pas → l'état est perdu au save/déconnexion (bug mineur, à corriger ou assumer).

7. **`ReactorSummaryDisplaySource` — sentinelle de taille de liste fragile + accès positionnel**
   `getComponents()` retourne une liste de taille **1** (pas de contrôleur) ou **6** (normal) ; les appelants testent `components.size() < 6` pour détecter le cas "pas de contrôleur", et `components.get(2).get(1)` accède positionnellement à la ligne "fuel". Tout ajout/réordonnancement futur de ligne casse silencieusement ces deux contrats implicites. `AUDIT_V1.md` ne détaillait pas `ReactorSummaryDisplaySource` à ce niveau.

8. **`ReactorSummaryDisplaySource.formatValue` — incohérence de mode entre résumé et displaySource individuel**
   En mode "normal" (mode 0), `HeatDisplaySource` affiche `"500 °C"` alors que `ReactorSummaryDisplaySource` affiche une **jauge** pour le heat dans le même mode (`gaugeOnNormal=true` pour heat uniquement) — incohérence visuelle entre les deux affichages pour un même mode utilisateur, non relevée dans `AUDIT_V1.md`.

9. **`IrradiatedBiomes` — contenu de worldgen visiblement copié d'un autre mod**
    `addDefaultIrradiatedOres`/`addDefaultSoftDisks` ajoutent `MiscOverworldPlacements.BLUE_ICE`, `Carvers.NETHER_CAVE`, `VOID_START_PLATFORM` — du contenu vanilla sans rapport avec un biome "irradié", et `monsters(...)` est un no-op appelé avec des paramètres `(95, 5, 100)` silencieusement ignorés. le pipeline worldgen "irradié" reste un **template non terminé/non re-thémé** sur ces autres points, plus large que ce que `AUDIT_V1.md` avait documenté (qui se concentrait sur les surface rules).

10. **`CNNoiseGeneratorSettings.IRRADIATED`** définit `STEEL_BLOCK` comme bloc de remplissage par défaut du terrain (équivalent "stone"). Si jamais relié à une dimension, génèrerait un terrain massivement en acier — point d'alerte non couvert par `AUDIT_V1.md`, indépendant des surface rules (B19, désormais corrigées).

11. **`IHeat.HeatLevel.isNotDanger` — condition tautologique, toujours `true`**
    `IHeat.java:88-90` :
    ```java
    public static boolean isNotDanger(int heat, int reactorSize) {
        return of(heat, reactorSize) != DANGER || of(heat, reactorSize) != NONE;
    }
    ```
    `X != DANGER || X != NONE` est une tautologie : un `HeatLevel` ne peut pas être simultanément égal à `DANGER` et à `NONE`, donc l'une des deux comparaisons est toujours vraie — la méthode retourne **toujours `true`**, quel que soit `heat`. Conséquence concrète dans son unique appelant, `ReactorControllerBlockEntity.handleAssembledState()` (ligne 454) :
    ```java
    if (IHeat.HeatLevel.isNotDanger(heat, getMultiblockSize()) && !outputManager.getBlocksPosition(level).isEmpty()) {
        outputManager.rotateOutputs(getLevel(), getAssembled(), heat);
    }
    ```
    la garde "ne pas faire tourner les sorties si le réacteur est en `DANGER`" est **inopérante** : les sorties continuent de tourner même en `HeatLevel.DANGER`. Non détecté dans `AUDIT_V1.md` (ni le fichier ni la méthode n'y sont mentionnés). **Aucun correctif appliqué pour l'instant — documentation uniquement**, à corriger dans un commit dédié (probablement `!= DANGER` seul, à confirmer avec le comportement voulu).

### 🟡 Mineurs

12. **`CreateNuclearJEI` — champ statique mutable `Categories` (nom non conventionnel)**, vidé/reconstruit à chaque appel de `registerCategories` — risque si JEI ré-appelle ce cycle (reload de ressources).
13. **`CNPonderReactorScenes.showReactorStructure`** — boucle triple (jusqu'à 11×13×13 ≈ 1859 itérations) avec 6 comparaisons positionnelles séquentielles par cellule ; remplaçable par une `Map` précalculée. Coût ponctuel (ouverture de ponder) mais signe de code à clarifier.
14. **`ReactorFrameDisplayManager.write`** persiste systématiquement les sentinelles `Integer.MAX_VALUE`/`MIN_VALUE` même quand `hasFrameColumn()` est faux — pollution NBT mineure.

---

## 4. 🏗️ Architecture — état actuel (synthèse)

Les 5 problèmes structurels de `AUDIT_V1.md` §1 restent **tous valides** :

1. **Inversion `api/`** : `api.multiblock.MultiBlockManagerBeta`/`RodType`/`ReactorFluidType` dépendent toujours de `content.*` — confirmé.
2. **God class `ReactorControllerBlockEntity`** : toujours ~680 lignes, hub pour 5 managers, verrous fluides (`tryLockFluid`/`canAcceptFluid`/`clearLock`/`clearLockIfAllInputsEmpty`, ~65 lignes), rotation/output (`rotate`, ~35 lignes), tooltips. La décomposition progresse (managers/services/snapshot extraits) mais la BE reste le hub central — **non terminé**, conforme au plan d'action §8.5 de `AUDIT_V1.md`.
3. **Deux frameworks multiblock concurrents** : le ménage a réduit `lib/multiblock` (suppression de `MultiBlockManager`/`MultiBlockCache`/`IBetterPattern`), mais **trois couches coexistent toujours** pour la détection de contrôleur — `lib/multiblock` (matching générique), `api.multiblock.MultiBlockManagerBeta` (vérification de structure), et `content.multiblock.pattern.ReactorPattern`/`MultiblockHelpers` (scan géométrique brut, ~3971 blocs, **toujours** appelé en double — `findController` + `findControllerPos` — à chaque pose/casse, y compris côté client sans garde `isClientSide`). C'est le point de performance/architecture le plus critique et **entièrement non corrigé**.
4. **Radiation sur 4+ couches** : toujours vrai, et **aggravé** par le 3ᵉ chemin `RadiationEffectHandler` (§3 point 3) découvert dans cette analyse.
5. **Couplage hub + accès infra direct** : `ConfigValueResolver` existe toujours dans `foundation/utility` mais reste sous-utilisé (ex: `RadiationEffect` l'utilise pour la blacklist mais reconstruit un `HashSet` + reparse les `ResourceLocation` à **chaque tick par entité** — perf non corrigée, `AUDIT_V1.md` §4 ligne 2).

---

## 5. ⚡ Performance — état actuel

Tous les points perf de `AUDIT_V1.md` §4 restent **non corrigés** :
- `ReactorPattern.findController`/`findControllerPos` : scan ~3971 blocs ×2 par placement/casse, sans garde client — confirmé, **aggravé** par `MultiBlockManagerBeta.findStructure` qui peut ajouter jusqu'à 4×3×729 lookups supplémentaires.
- `RadiationEffect` : `HashSet<EntityType>` + parsing reconstruits à chaque entité/tick — confirmé.
- `HelmetOverlay.renderHotbar` + 3× `getArmor(HEAD)`/frame — confirmé.
- `ReactorControllerBlockEntity.clearLockIfAllInputsEmpty` : scan cubique `O(n³)` (`getBlockEntity` + capability lookup par cellule) alors que `inputFluidManager.getFuildHandlers(level)` donne déjà la liste exacte (le NPE de `findStructure`, lui, a été corrigé).
- **Nouveau** : `DefaultHeatCalculator.computeHeat` (package `reactorLogic`, non couvert par `AUDIT_V1.md`) — boucle imbriquée ~`57×81×4×57` avec désérialisation NBT répétée par cellule, exécutée à chaque tick de calcul de chaleur ; branche de proximité "cooler" manquante (asymétrie fuel/cooler).
- **Nouveau** : `NuclearExplosionEntity.tick()` — tri complet d'une pile de jusqu'à 11³=1331 `BlockPos` avec comparateur `distManhattan` coûteux, en un seul tick.

---

## 6. Plan d'action — priorités mises à jour

**Nouveaux quick wins identifiés** :
- `RadiationEffectHandler` : ajouter les mêmes gardes (config/immunité/résistance) que les deux autres chemins.
- Enregistrer `IrradiatedCatCollarLayer` / implémenter `IrradiatedWoldCollarLayer.render`, ou retirer la mécanique de teinture de collier.
- Retirer `RenderHelper.renderTextureOverlay(..., Math.round(...))` → passer `alpha*coverage` brut (si `RadiationOverlay` est un jour réactivé).

**Priorité structurelle inchangée** : le scan géométrique `ReactorPattern`/`MultiblockHelpers` (point architecture §4.3) reste le risque de performance serveur le plus sérieux et n'a reçu **aucune** correction — c'est le chantier le plus rentable avant de poursuivre la décomposition de `ReactorControllerBlockEntity`.

**Nettoyage dead-code restant** :
- Méthodes d'animation mortes restantes dans `CNAdvancedEntityModel`/`CNAdvancedModelBox` + dédup `ModelBox` (cascade dans classes vivantes — nettoyage dédié).
- `RadiationOverlay`/`EasingHudOverlay` + `HelmetOverlay.setCoverage` no-op (décider : supprimer ou réactiver+corriger le fade — §3 points 4 & 5).
- `IrradiatedBiomes.monsters()` no-op et reliquat éventuel `SimpleMultiBlockPattern.test()`.
- Features à finir ou supprimer (pas du pur dead code, décision produit) : `IrradiatedWoldCollarLayer`/`IrradiatedCatCollarLayer`, `AntiRadiationArmorItem.getArmorTexture`/`ClothTagHelper.getArmorTexturePath`.
