# Audit consolidé — CreateNuclearForge (branche V2-CorrectifAudit)

**Réorganisation du 2026-07-12 (HEAD `8b0428e3`).** Ce document a été restructuré pour ne conserver dans ses sections principales (§1 à §7) **que les problèmes encore ouverts**. Tout ce qui est corrigé, réfuté ou décidé (produit) a été déplacé en **§8 Mémoire**, avec le hash du commit correspondant quand il a pu être déterminé avec certitude par archéologie Git (`git log -S`, `git show`, `git blame`) — sinon c'est indiqué explicitement, sans hash inventé.

**Méthodologie de cette réorganisation** : nouvelle relecture complète du code actuel (`src/main/java`, **292 fichiers**, hors `src/generated`/`src/main/resources`), croisée avec `AUDIT_V1.md`, les versions précédentes de ce fichier, et l'historique Git (`git log`, 1437 commits au total sur le dépôt) — aucun des deux audits n'a été pris comme vérité absolue : plusieurs points ont été revérifiés directement dans le code et un point s'est avéré **périmé** (voir §0).

**Limite de couverture honnête** : les bugs mineurs/robustesse listés au §1 de l'historique (`B7, B10, B17, B18` et la liste « mineurs » non numérotée d'`AUDIT_V1.md`) n'ont pas été re-vérifiés faute de signal de changement — ni confirmés ni infirmés, à re-vérifier ponctuellement avant traitement. Beaucoup de corrections d'`AUDIT_V1.md` (B1, B2, B4, B5, B9, B13, B18, et l'essentiel des listes de dead code) sont antérieures à la fenêtre de commits explorée en détail (avant le 2026-06-24) ; l'archéologie Git ciblée sur ces items n'a pas abouti à un hash certain dans la majorité des cas — voir §8.4.

---

## 0. Constat global

Depuis la dernière consolidation (2026-06-28), le projet a continué d'évoluer : retrait de `IrradiatedBiomes.monsters()` (`8b0428e3`), blocage explicite (message "WIP") de l'apprivoisement des mobs irradiés (`261b26d8`), retrait complet de la mécanique de collier teignable jamais câblée (`17b29aa3`), correction de la tautologie `IHeat.isNotDanger` et ajout d'une hystérésis sur le heat appliqué aux sorties (`fb0897d3`, `a18038db`), guard NBT sur `ReactorFrameDisplayManager.write` (`381c7a5f`), et déduplication complète de `drawGauge` via `ReactorGaugeRenderer`/`AbstractReactorStatDisplaySource` (`35e474e3`, `d0da13d1`, `a8b6b23f`). Détail de chaque correction en §8.1.

**Point périmé trouvé lors de cette réorganisation** : l'item « Flicker vitesse de sortie en fin de ressources » était encore documenté en §1 comme un bug entièrement ouvert, proposant comme correction l'ajout d'un champ `lastAppliedOutputHeat` avec hystérésis sur `RPM_DIVIDER/2. **Cette correction a en réalité déjà été appliquée** par le commit `a18038db` (2026-07-05) — vérifié par lecture directe : `ReactorControllerBlockEntity.java:62` (champ `lastAppliedOutputHeat`), `:430-433` (comparaison au seuil et appel à `rotateOutputs` avec la valeur bufferisée). Le commit lui-même documente que le point reste partiellement ouvert : l'oscillateur `overHeat` sous-jacent (+1/-2 par tick) n'est pas supprimé, seul son symptôme visible (flicker RPM) est amorti, et le seuil de l'hystérésis n'a pas été validé en jeu. L'item est donc reclassé en §1 comme **partiellement résolu, validation en jeu restante** plutôt que comme bug non traité — voir §1.

**Nouveau point d'audit ajouté** : qualité et langue de la documentation du code (Javadoc, commentaires, noms de symboles) — voir §6. Un premier signal existait déjà dans `AUDIT_V1.md` §5 (« commentaires FR/EN mêlés ») mais n'avait jamais été centralisé ni suivi comme item d'audit à part entière.

La dette la plus structurelle reste **l'absence totale de tests** (`src/test` toujours vide, reconfirmé — 0 fichier `.java`) — précisément là où des bugs/incohérences silencieux (sur-extraction fluide, ex-tautologie `IHeat`, scan non court-circuité, doc devenue obsolète sans que personne ne le remarque) survivent à plusieurs passes d'audit. Voir §3 et §7.

---

## 1. 🐛 Bugs confirmés ouverts

| Réf. | Fichier:ligne | Gravité | Détail (revérifié 2026-07-12) |
|---|---|---|---|
| **ReactorInputFluidManager — sur-extraction** | `controller/manager/ReactorInputFluidManager.java:137-155` | 🟠 | `fluidNeeded` (paramètre de `extractFluids`) n'est **toujours jamais décrémenté** entre handlers (l.146-151) → chaque handler de la boucle tente d'extraire le besoin complet, sur-extraction possible avec plusieurs inputs. `if (toExtract > 1)` (l.147) ignore toujours les extractions de 1 unité. **Détail supplémentaire trouvé cette revue** : la garde `if (fluidNeeded <= 0) break;` (l.151) est **du code mort** — `fluidNeeded` n'étant jamais réassigné dans la boucle, cette condition ne peut être vraie que si le paramètre est déjà ≤ 0 en entrée. Toléré aujourd'hui (tank mono-slot en pratique), reste une violation de contrat latente. |
| **Flicker vitesse de sortie en fin de ressources — partiellement corrigé, validation en jeu restante** *(reclassé le 2026-07-12, code de correction déjà présent depuis `a18038db`, 2026-07-05)* | `content/multiblock/reactorLogic/DefaultOverheatController.java:13-30` + `controller/ReactorControllerBlockEntity.java:430-433` | 🟡 | `overHeat` continue de s'incrémenter de `+1`/`-2` à un rythme régulier, indépendamment du réapprovisionnement — l'oscillateur de base n'a **pas** été supprimé. Mais son symptôme (bascule de vitesse de sortie entre `0` et `X`) est désormais amorti : `ReactorControllerBlockEntity` ne réapplique `rotateOutputs` que si `\|heat - lastAppliedOutputHeat\| >= RPM_DIVIDER/2` (code vérifié présent). **Reste ouvert** : (1) valider en jeu que ce seuil élimine réellement la perception du flicker en fin de ressources (le commit d'origine le signale lui-même comme non validé) ; (2) décider si l'oscillateur `overHeat` doit être revu à la racine ou si l'hystérésis en aval suffit durablement. |

---

## 2. 🆕 Autres problèmes ouverts (hors radiation, hors documentation)

### 🟠 Important — performance résiduelle du scan multiblock

1. **Scan géométrique du contrôleur : court-circuit incomplet (`findController`)** — **seul sous-point encore ouvert de ce chantier** (garde client-side et double scan à la casse : corrigés, voir §8.1).
   `ReactorPattern` n'a qu'**une seule** boucle de scan partagée (`scanControllerCandidates`, `ReactorPattern.java:40-56`) sur ~3971 positions (`y∈[-5,+5]`, `x,z∈[-9,+9]`), filtrée par `isInReactorRange`. Le court-circuit (`return true` dans le visiteur dès qu'un contrôleur pertinent est trouvé) n'existe que dans **`findControllerPos`** (`ReactorPattern.java:73-94`, utilisé par `MultiblockHelpers` → `ReactorOutput`/`ReactorRodInput`/`ReactorFluidInput`/`ReactorAlarm`). **`findController`** (`ReactorPattern.java:58-71`, revérifié 2026-07-12 : le visiteur retourne toujours `false` l.69), appelé **directement** par `ReactorCasing.java`, `ReactorCooler.java` et `ReactorFrame.java`, continue de parcourir les ~3971 positions restantes même après avoir traité le contrôleur pertinent. `ReactorCasing` étant le bloc le plus posé/cassé (coque du réacteur), c'est le chemin le plus chaud du projet et il n'est **toujours pas** optimisé.
   Fix trivial inchangé : faire retourner `inRange` au lieu de `false` dans le visiteur de `findController`, comme c'est déjà fait dans `findControllerPos`. **Reste la priorité n°1 du projet** (perf uniquement désormais, plus une question de correction — voir §7).
   - **Point résiduel mineur, non corrigé, non confirmé comme bug** : `ReactorOutput` (propriété `DIR`) et `ReactorFluidInput` (propriété `FACING`) ont une propriété mutable mais leur `onRemove` n'a pas la garde `!state.is(newState.getBlock())` présente sur `ReactorAlarm`/`ReactorFrame`. Aucun chemin connu ne change `DIR`/`FACING` via `setBlock` sans remplacer le bloc, mais à surveiller si une interaction clé/wrench déclenche un jour un retrait/réenregistrement non désiré.

### 🟠 Importants

2. **`DefaultHeatCalculator.computeHeat` — reste O(n×81) + asymétrie fuel/cooler** *(gravité réévaluée à la baisse lors d'un tour précédent, toujours d'actualité)*
   Le neighbor lookup est passé de O(n) à O(1) (map `actualRods`, déjà corrigé — voir §8.1), mais chaque rod continue de boucler sur toute la grille `formattedPattern` (9×9=81 cellules) pour retrouver sa propre position — non remplacée par une map slot→position précalculée (coût ~57×81 ≈ 4617 itérations/tick sur réacteur plein). **Recommandation** : précalculer une `Map<Integer slot, int[] position>` — gain mineur, risque nul, priorité basse.
   - **Asymétrie toujours présente** : l'examen des voisins ne se fait que si `"fuel".equals(currentRod)` ; un cooler ne déclenche jamais l'examen de ses voisins, et `heat += rod.baseRodHeat() / neighborRod.proximityRodHeat()` (division) côté fuel→cooler-voisin vs addition côté fuel→fuel-voisin. **Ne pas toucher sans confirmation balance** — c'est potentiellement voulu.

3. **`ReactorSummaryDisplaySource` — sentinelle de taille + accès positionnel fragiles** *(inchangé)*
   `getComponents()` retourne toujours une liste de taille 1 (pas de contrôleur) ou 6 (normal) ; les appelants gardent `if (components.size() < 6)` avant un accès positionnel `components.get(2).get(1)` (ligne « fuel »). Toujours fragile à tout réordonnancement de ligne.

4. **`ReactorSummaryDisplaySource.formatValue` — incohérence de mode** *(inchangé)*
   `HeatDisplaySource` affiche `"500 °C"` en mode normal alors que `ReactorSummaryDisplaySource` force une jauge pour le heat dans le même mode (`gaugeOnNormal=true` pour heat uniquement) — incohérence visuelle toujours présente.

### 🟡 Mineurs

5. **`CreateNuclearJEI`** — champ statique mutable `Categories`, vidé/reconstruit à chaque `registerCategories` ; risque si JEI ré-appelle le cycle (reload ressources). *(non revérifié ce tour, signal inchangé depuis plusieurs audits)*
6. **`CNPonderReactorScenes.showReactorStructure`** — boucle triple (~11×13×13) avec comparaisons positionnelles ; coût ponctuel (ouverture ponder), remplaçable par une `Map` précalculée.

---

## 3. 🏗️ Architecture — points encore ouverts

| Point | État (revérifié 2026-07-12) |
|---|---|
| **`src/test`** | ❌ **Toujours vide** — confirmé, 0 fichier `.java` sous `src/test`. Le point structurel le plus important du projet reste entier ; c'est le terrain commun à la quasi-totalité des bugs silencieux relevés depuis le premier audit (chaleur, pattern matching, verrouillage fluide, double-scan multiblock, et maintenant la doc obsolète repérée en §0). |
| **`RadiationCapability.tickRadiation` pour `LivingEntity` non-joueurs** | ⚠️ **Point à surveiller, pas un bug** — recalcule `computeItemRadiation` à chaque tick sans dirty-check (le hash d'inventaire existant, `InventoryHashUtil`, ne couvre que le joueur). Compromis assumé explicitement dans le commit `9ded502c` (« no equivalent inventory to diff against ») — à reconsidérer seulement si la densité de mobs équipés en zone irradiée augmente et qu'un problème de perf est mesuré. |

*(Les autres affirmations historiques de cette table — inversion `api/`, deux frameworks multiblock concurrents, god class `ReactorControllerBlockEntity`, coordinateur de verrou fluide dédié — ont toutes été réfutées ou closes ; voir §8.2.)*

---

## 4. 🧹 Dead code & features inachevées — reste à faire

> **⚠️ Faux positifs à NE PAS supprimer** : `CNTabulaModelRenderUtils` (rendu vivant du champignon atomique) ; `ReactorOutputEntity.outputPos` (lu/écrit en NBT) ; `setRotateAngle` (appelé par `NuclearMushroomCloudModel`).

**Code commencé puis abandonné (hygiène de fin de PR)**
- `content/multiblock/output/ReactorOutput.java:56,91` — propriété `SPEED` toujours entièrement commentée (pas supprimée). *(Lignes revérifiées 2026-07-12 : `//builder.add(SPEED);` l.56, `/*.setValue(SPEED, 0)*/` l.91 — décalage mineur par rapport aux anciens numéros de ligne l.43/52/87.)*
- `foundation/data/recipe/CNStandardRecipeGen.java:226` — `// FIXME 5.1 refactor - recipe categories as markers...` toujours présent, à trancher.

**Worldgen « irradié » — template non terminé** *(revérifié 2026-07-12, inchangé)*
- `IrradiatedBiomes.java:22-27` ajoute toujours du contenu vanilla sans rapport avec le biome irradié (`BLUE_ICE`, `NETHER_CAVE`, `VOID_START_PLATFORM`).
- `CNNoiseGeneratorSettings.java:25` utilise toujours `STEEL_BLOCK` comme bloc de remplissage par défaut du biome irradié.

*(Le dead code pur déjà retiré — `SimpleMultiBlockPattern.test()`, `IrradiatedBiomes.monsters()`, le nettoyage de `PlayerInteractReactorFluidInput`, les fichiers de debug `run/`, la mécanique de collier teignable — est en §8.1/§8.3 avec ses hash.)*

---

## 5. ⚡ Performance — synthèse des points ouverts

| Point | État | Réf. |
|---|---|---|
| `ReactorPattern.findController` (Casing/Cooler/Frame) : **ne court-circuite jamais**, scanne ~3971 positions à chaque pose/casse même après avoir trouvé le contrôleur | ❌ **non corrigé** — chemin le plus chaud, dernier point ouvert du chantier scan multiblock | §2.1 |
| `DefaultHeatCalculator.computeHeat` : neighbor lookup en O(1) (déjà corrigé), reste une boucle O(81) de recherche de position par rod | 🟡 **partiellement corrigé, priorité basse** | §2.2 |

*(`findControllerPos`, la garde `isClientSide`, le double scan à la casse, et `clearLockIfAllInputsEmpty` sont déjà sains/corrigés — voir §8.1/§8.2.)*

---

## 6. 📝 Documentation — langue et clarté du code (nouveau point d'audit, 2026-07-12)

**Périmètre** : `src/main/java` (292 fichiers), recherche des commentaires/Javadoc non rédigés en anglais et des cas ambigus/incomplets indépendamment de la langue. Méthodologie : recherche par caractères accentués + vocabulaire français typique des commentaires, puis lecture manuelle des occurrences trouvées.

**Décompte global** : **14 fichiers sur 292** (~5 %) contiennent du français détectable, pour environ **40 lignes** de commentaires/Javadoc concernées. **Aucun symbole de code** (nom de classe, méthode, champ) en français n'a été trouvé — le problème est **localisé aux commentaires et Javadoc**, pas à l'API elle-même. Le mélange est quasi systématique FR/EN **dans le même fichier**, parfois la même méthode — jamais un fichier intégralement en français.

**Cas les plus significatifs** :

*Javadoc public sur API/points d'extension — le plus grave car visible par tout futur contributeur :*
1. `content/effects/VicinityEffect.java:53-57` — Javadoc de `onContaminate(LivingEntity)`, point d'extension abstrait clé (`RadiationEffect` en dépend) : première phrase en anglais, puis bascule en français dans la même doc. Pire cas relevé : Javadoc public, méthode non triviale, bilingue au sein d'un seul bloc.
2. `infrastructure/ponder/scenes/CNPonderReactorScenes.java:32` — Javadoc de la classe interne `Positions` entièrement en français, documentant une convention critique non répétée ailleurs (« Les BlockPos ici doivent être EN COORDONNÉES SCÈNE »).
3. `infrastructure/ponder/scenes/CNPonderReactorScenes.java:50` — Javadoc de `STATIC_POS` rédigé comme une note à soi-même plutôt qu'une documentation (« adapte ces valeurs exactement comme dans ton NBT »).
4. `infrastructure/ponder/scenes/CNPonderReactorScenes.java:85` — Javadoc de `positionsFor` en français ; bloque la lecture pour un contributeur non francophone d'une scène Ponder (bibliothèque Create, publique par nature).

*Commentaires sur logique métier non triviale :*
5. `CNFluids.java:139,153,156,159` — bloc de commentaires français documentant une correction fine de désynchronisation client/serveur des ticks de gel/dégel (compensation d'offset). Logique fragile, documentée uniquement en français.
6. `ClientEvents.java:24,60-78` — logique de tremblement de caméra liée à l'explosion nucléaire, entièrement commentée en français, y compris la formule de puissance ; non documentée en anglais du tout.
7. `foundation/mixin/RadiationHeartMixin.java:13,21,32` — mixin overridant le rendu du cœur HUD vanilla, commentaires français expliquant le ciblage. Les mixins sont le code le plus fragile aux montées de version MC/Forge — mérite une documentation claire et bilingue a minima.
8. `foundation/mixin/CameraAccessor.java:9` — accessor mixin, commentaire français isolé sur le pattern « invoker » Mixin.
9. `content/redstone/displayLink/source/ReactorSummaryDisplaySource.java:51,75,114` — trois commentaires français de garde défensive (« Sécurité Triple-Check ») sur le chemin d'affichage, seule trace de documentation de cette logique de garde.
10. `content/explosion/NuclearExplosionEntity.java:45,50` — commentaire français expliquant un contournement de compatibilité addon (cast en `Object` pour éviter un crash `ClassNotFoundError` si Alex's Caves est absent) : information cruciale sur une dépendance optionnelle, uniquement en français, à l'endroit le plus délicat du fichier.
11. `content/decoration/palettes/PalettesVariantEntry.java:54` — commentaire français sur un changement d'architecture de rendu déjà effectué ailleurs, utile pour éviter une régression, en français uniquement.
12. `infrastructure/worldgen/biome/IrradiatedBiomes.java:39-48` — 4 commentaires français sur des choix esthétiques de biome ; mineur, mais dans un fichier déjà pointé par §4 comme « template non terminé ».
13. `content/equipment/armor/AntiRadiationArmorItem.java:48` — commentaire français sur `getArmorTexture` (override d'API Forge publique) ; trivial sur le fond, à harmoniser en anglais par cohérence.
14. `content/redstone/displayLink/source/ReactorSizeDisplaySource.java:33` — commentaire français isolé, sur une classe explicitement exclue du refactor de mutualisation `drawGauge` (§8.1) — bon candidat à documenter proprement au prochain passage sur ce fichier.
15. `foundation/mixin/GameRendererMixin.java:17` — commentaire français seul sur le mixin gérant l'assombrissement du ciel pendant l'explosion — même remarque que 7/8.

**Ambiguïtés/incomplétudes indépendantes de la langue relevées au passage** :
- `CNPonderReactorScenes.java:50,90` — formulations en forme de note de dev figée dans le code (« adapte ces valeurs... comme dans ton NBT », « si tu veux éviter un NPE ») plutôt qu'une documentation stable, à reformuler indépendamment de la traduction.
- `ReactorOutput.java` (§4) — propriété `SPEED` commentée sans décision documentée — ambiguïté fonctionnelle non résolue.
- `CNStandardRecipeGen.java:226` (§4) — `FIXME` en anglais mais sans contexte suffisant pour un tiers.

**Pattern récurrent** : le point « commentaires FR/EN mêlés » déjà signalé dans `AUDIT_V1.md` §5 (sans jamais être suivi comme item séparé) **reste d'actualité** et se concentre sur un noyau stable de fichiers : les mixins (`CameraAccessor`, `RadiationHeartMixin`, `GameRendererMixin`), le rendu Display Link (`ReactorSummaryDisplaySource`, `ReactorSizeDisplaySource`), les scènes Ponder (`CNPonderReactorScenes`), et `CNFluids`/`ClientEvents`. Aucun de ces fichiers n'a été assaini depuis. Point positif : le sous-ensemble concerné est petit et stable (14/292 fichiers, ~5 %) et n'a pas grossi récemment — les derniers commits (retrait de `monsters()`, retrait du collier teignable, sons Geiger) n'ont pas introduit de nouveau commentaire français.

**Pourquoi ça doit être suivi** : ces commentaires portent presque toujours l'explication du *pourquoi* d'un choix non trivial (compat cross-mod, compensation de désync réseau, convention de coordonnées Ponder, garde défensive) — c'est-à-dire exactement le contenu qu'un commentaire doit porter selon les conventions du projet. Le laisser uniquement en français limite la relecture/contribution à l'équipe francophone actuelle et complique tout audit ou onboarding futur non francophone (y compris par un outil ou un contributeur externe à la communauté Create).

**Priorité recommandée** : 🟢 **cosmétique, à faire au fil de l'eau — pas bloquant.** Aucun symbole d'API n'est en français, donc pas d'impact sur la lisibilité des signatures pour un contributeur externe. Traiter en priorité les 4 cas de Javadoc public (items 1-4 ci-dessus, notamment `VicinityEffect.java` et `CNPonderReactorScenes.java`, seuls vrais cas de Javadoc public bilingue) lors du prochain passage sur ces fichiers, plutôt que d'ouvrir un chantier de traduction dédié — le volume (~40 lignes) ne le justifie pas. Chaque futur commit touchant l'un des 14 fichiers devrait normaliser en anglais les commentaires qu'il modifie. **Suivi** : cet item reste en §6 jusqu'à ce que les 4 cas de Javadoc public soient traduits ; à re-scanner périodiquement (le pattern n'a pas grossi depuis le dernier audit, mais rien ne garantit que ça dure).

---

## 7. 🗺️ Feuille de route — priorités justifiées

Classement par catégorie et impact, du plus important au plus cosmétique. Chaque item renvoie à sa section de détail.

**1. Dette technique structurelle (racine des bugs silencieux récurrents)**
- Démarrer une couverture de tests sur `DefaultHeatCalculator`, le pattern matcher (`ReactorPattern`/`CNMultiblock`) et `PersistentFluidLocks` (§3). **Justification** : c'est la cause profonde identifiée à travers *tous* les tours d'audit successifs — sur-extraction fluide, tautologie `IHeat`, scan non court-circuité, et maintenant une section d'audit devenue obsolète sans que personne ne s'en aperçoive (§0) sont tous des symptômes du même problème : rien ne vérifie automatiquement le comportement. C'est l'investissement qui a le plus gros effet de levier sur la fiabilité future, mais c'est aussi le plus long — d'où la priorité 1 malgré l'absence d'urgence immédiate.

**2. Optimisation — chemin le plus chaud du projet**
- `ReactorPattern.findController` : faire retourner `inRange` au lieu de `false` dans le visiteur, pour court-circuiter comme `findControllerPos` le fait déjà (§2.1, §5). **Justification** : fix d'une ligne, risque quasi nul, et c'est le chemin appelé par le bloc le plus posé/cassé du jeu (`ReactorCasing`). Rapport gain/effort le plus élevé du projet — reste la priorité n°1 « quick win » malgré son classement en position 2 ici (juste après la dette de tests, qui est stratégiquement plus importante mais bien plus longue à traiter).

**3. Bug fonctionnel latent — robustesse d'un contrat interne**
- `ReactorInputFluidManager` : décrémenter `fluidNeeded` entre handlers, gérer `toExtract == 1`, retirer la garde morte `fluidNeeded <= 0` (§1). **Justification** : viole silencieusement le contrat d'extraction ; toléré aujourd'hui uniquement parce que la configuration actuelle (tank mono-slot) masque le symptôme — un futur ajout de multi-input fluide le ferait resurgir sans avertissement.

**4. Validation en jeu — correction déjà codée**
- Flicker de vitesse de sortie : valider en jeu que le seuil d'hystérésis (`RPM_DIVIDER/2`, déjà implémenté par `a18038db`) supprime bien la perception du flicker en fin de ressources (§1). **Justification** : le code est déjà en place, il ne reste qu'une validation de gameplay — priorité basse en effort mais à ne pas oublier, sinon le point restera indéfiniment « à moitié fait » dans la mémoire du projet.

**5. Amélioration de conception / cohérence UX**
- `ReactorSummaryDisplaySource.formatValue` : harmoniser le mode d'affichage du heat avec `HeatDisplaySource` (§2.4). **Justification** : incohérence visible par le joueur, mais purement cosmétique, aucun risque fonctionnel.
- `ReactorSummaryDisplaySource` : remplacer l'accès positionnel fragile (`components.get(2).get(1)`) par un accès nommé/typé (§2.3). **Justification** : dette de conception qui ne casse rien aujourd'hui mais rendra toute évolution de l'ordre des lignes affichées dangereuse sans le vouloir.

**6. Optimisation mineure (gain faible, risque nul)**
- `DefaultHeatCalculator` : précalculer une map slot→position pour éliminer la boucle O(81) restante (§2.2, §5). **Ne pas** toucher à l'asymétrie fuel/cooler sans validation balance.
- `CNPonderReactorScenes.showReactorStructure` : remplacer la boucle triple par une map précalculée (§2.6). Coût ponctuel (ouverture d'un ponder), gain marginal.

**7. Nettoyage / décisions produit à trancher**
- `ReactorOutput.SPEED` : retirer les commentaires ou décider de réactiver la propriété (§4).
- `CNStandardRecipeGen.java:226` : trancher le `FIXME` sur les catégories de recette (§4).
- Worldgen « irradié » : retirer le contenu vanilla sans rapport (`BLUE_ICE`, `NETHER_CAVE`, `VOID_START_PLATFORM`) et choisir un bloc de remplissage cohérent à la place de `STEEL_BLOCK` (§4). **Justification** : n'affecte que la cohérence thématique du biome, pas de risque technique.

**8. Documentation (nouveau, §6)**
- Traduire en anglais les 4 cas de Javadoc public bilingue (`VicinityEffect.java`, `CNPonderReactorScenes.java` ×3) en priorité, puis les commentaires métier non triviaux listés en §6, au fil des prochains passages sur ces fichiers. **Justification** : cosmétique et non bloquant (aucun symbole d'API en français), mais ce sont typiquement des commentaires qui expliquent un « pourquoi » non trivial (compat, désync réseau, convention Ponder) — les laisser en français limite l'audit/onboarding futur.

**9. À surveiller sans action immédiate**
- `RadiationCapability.tickRadiation` pour les `LivingEntity` non-joueurs : pas de dirty-check d'inventaire (§3). Compromis assumé, à reconsidérer seulement si la densité de mobs équipés irradiés pose un problème de perf mesuré.
- `ReactorOutput`/`ReactorFluidInput` : garde `!state.is(newState.getBlock())` absente dans `onRemove` malgré des propriétés mutables (§2.1). Pas un bug confirmé, à surveiller.
- `CreateNuclearJEI` : champ statique mutable `Categories`, non revérifié récemment (§2.5).

**Explicitement retiré du plan (ne plus y revenir sans nouvel élément)** — détail et justification en §8.2/§8.3 :
- Découpage supplémentaire de `ReactorControllerBlockEntity` (chantier clos, croissance maîtrisée).
- « Correction » de l'inversion de dépendance `api/` (n'a jamais existé dans le code observé).
- Fusion/suppression du pipeline multiblock (sain, un seul framework vivant).
- Coordinateur dédié au verrouillage fluide (non justifié, `PersistentFluidLocks` suffit).
- Tags `CNItemTags.FUEL`/`COOLER` désynchronisés de `RodType.type()` (terminé).

---

## 8. ✅ Mémoire — corrections, réfutations et décisions déjà actées

### 8.1 Corrections appliquées, avec commit identifié

| Correction | Hash(es) | Date | Détail |
|---|---|---|---|
| Retrait de `IrradiatedBiomes.monsters()` (corps vide, arguments ignorés) | `8b0428e3` | 2026-07-12 | Méthode et son unique site d'appel supprimés ; les réglages de spawn passent désormais uniquement par `MobSpawnSettings.Builder`. |
| Blocage explicite (message "WIP") de l'apprivoisement des mobs irradiés (`AnimalUtil.blockTamingWip`) | `261b26d8` | 2026-07-11 | Remplace un apprivoisement cassé/incohérent (chat : collier non fonctionnel ; loup : aucun chemin d'apprivoisement) par un message explicite "pas encore implémenté". Ce même commit a aussi retiré la mécanique de collier (§8.3). |
| Retrait de la mécanique de collier teignable jamais câblée (`IrradiatedCatCollarLayer`, `IrradiatedWoldCollarLayer`, `DATA_COLLAR_COLOR`) | `17b29aa3` | 2026-07-11 | Décision produit exécutée — voir §8.3 pour le contexte de la décision. |
| Guard NBT : `ReactorFrameDisplayManager.write()` n'écrit les sentinelles `MIN_Y`/`MAX_Y` que si `hasFrameColumn()` est vrai | `381c7a5f` | 2026-07-11 | Élimine une pollution NBT mineure ; `read()` protégeait déjà chaque champ par `compound.contains(...)`, donc sans risque de régression. |
| Déduplication `drawGauge` : nouvelle classe `ReactorGaugeRenderer` + template `AbstractReactorStatDisplaySource`, constantes centralisées dans `ReactorDisplayConstants` | `35e474e3` → `d0da13d1` → `a8b6b23f` → `e34adcc5` | 2026-07-11 (20:38 → 21:08, ordre chronologique confirmé) | 5 copies identiques de `drawGauge` (Fuel/Cooler/Heat/LiquidLevel/Summary) ramenées à une seule implémentation ; `ReactorSizeDisplaySource` volontairement exclue (jauge à segments discrets, sémantique différente). |
| `IHeat.HeatLevel.isNotDanger` : tautologie `!= DANGER \|\| != NONE` (toujours vraie) remplacée par `!= DANGER` | `fb0897d3` | 2026-07-05 | ⚠️ **Changement de comportement de balance réel** : avant, les sorties tournaient toujours ; désormais elles s'arrêtent en `DANGER`. À valider en jeu (un joueur peut se retrouver sans écoulement de sortie tant que le réacteur reste en surchauffe). |
| Hystérésis sur le heat appliqué à `rotateOutputs` (champ `lastAppliedOutputHeat`, seuil `RPM_DIVIDER/2`) | `a18038db` | 2026-07-05 | Corrige le flicker RPM en fin de ressources — voir §1 pour le statut résiduel (validation en jeu du seuil encore ouverte). |
| `PlayerInteractReactorFluidInput` : renommage (`PlayerInteracteReactorFluidInput` → sans le 'e' parasite) + retrait de 4 blocs de dead code (variable morte, bloc commenté référençant `ReactorLiquidInput` inexistant, bloc vide `TANK_TO_ITEM`, `instanceof` toujours vrai) | `f2490469` | 2026-07-03 | **Note de fiabilité documentaire** : ce commit avait, par erreur, laissé l'ancienne description du problème dans `AUDIT_ACTUEL.md` (et même l'avait détaillée davantage) au lieu de la marquer résolue — staleness corrigée le 2026-07-12. Exemple concret de pourquoi §3 recommande des tests plutôt qu'une documentation manuelle comme seul filet de sécurité. |
| Tags `CNItemTags.FUEL`/`COOLER` désynchronisés de `RodType.type()` : classification unifiée sur `RodType.resolveRodType(Level)` | `562ce99d` (5 des 6 sites) + `30de52a7` (2 sites manqués : `ReactorBluePrintMenu`, lookup voisin `DefaultHeatCalculator`) | 2026-06-28 | `Level` enfilé jusqu'à `IHeatCalculator.computeHeat`. Vérifié par grep : plus aucun site de classification ne lit `CNItemTags.FUEL`/`COOLER`. `CNItemTags.FUEL` reste sur `THORIUM_ROD` comme tag de craft, explicitement découplé de la classification. |
| Retrait de `SimpleMultiBlockPattern.test()` | `2aaddb4d` | 2026-06-28 | Aucun appelant, retrait sûr. |
| `InventoryHashUtil.stackHash` n'inclut plus `getDamageValue()` | `575a8ab8` | 2026-06-28 | Aucune implémentation `IRadiationSource`/`RadiationRegistry` ne lit la durabilité ; l'inclure ne faisait que déclencher un recalcul serveur (`RadiationCapability.computeItemRadiation`) à chaque variation de durabilité (minage, combat) sur le hot path que le cache devait protéger. |
| Garde `level.isClientSide` centralisée dans `ReactorAssembler.assemble`/`disassemble` + suppression des overrides `playerDestroy` redondants (généralisation du modèle `ReactorRodInput`) sur `ReactorCasing`, `ReactorCooler`, `ReactorFrame`, `ReactorAlarm`, `ReactorOutput`, `ReactorFluidInput` | `34d5706f` | 2026-06-28 | Point de convergence unique de toute la chaîne `onPlace/onRemove/playerDestroy → findController/findControllerPos → ReactorAssembler` ; `onRemove` se déclenche déjà systématiquement lors d'une casse joueur, donc retirer l'appel redondant dans `playerDestroy` élimine le double scan sans changer de comportement observable. |
| Retrait des 6 fichiers de debug/env trackés dans `run/` (`hs_err_pid*.log`, `imgui.ini`, `servers.dat*`, `Jade*.jar.disabled`) + `.gitignore` élargi | `9ddb5a2a` | 2026-06-28 | Les 10 `run/schematics/*.nbt` conservés (assets de gameplay légitimes). |
| Extension de la radiation à toutes les `LivingEntity` (pas seulement le joueur) | `9ded502c` | 2026-06-28 | Point de vérité unique `canBeIrradiated(LivingEntity)` respecté, hook via `LivingTickEvent` (pas de boucle manuelle sur les entités du monde). Compromis assumé : pas de dirty-check pour les non-joueurs — voir §3 (point à surveiller). |
| Centralisation de l'attribution des advancements de pose sur le contrôleur (au lieu de la pièce posée) | `19617118` | 2026-06-26 | `MultiblockHelpers.handleAdvancedPlacedBy` redirige vers le contrôleur ; `CNAdvancementBehaviour` vérifie l'état Minecraft natif (`isAlreadyAwardedTo`) au lieu d'une liste interne. Élimine un risque de double-attribution. |
| Retrait du `LOGGER.warn` redondant dans `ReactorAssembler.assemble` (spam de log) | `b309f231` | 2026-06-26 | Plus aucun `LOGGER.warn`/`info` dans le chemin de succès. |
| `NuclearExplosionEntity` : anti-pattern `try { onBlockExploded } catch(Exception) { destroyBlock(...) }` remplacé par une vérification explicite (`getBlockState(immutablePos).is(state.getBlock())`) | `11b1108a` | 2026-06-22 | Introduit par `5acde3db` (2026-03-04, compat Alex's Caves), corrigé par `11b1108a`. Antérieur à la fenêtre de commits habituellement explorée en détail par ce document, mais retrouvé avec certitude par `git log -S "immutablePos"`. |

**Commit à mention neutre (ni bug ni fix, clarification)** : `20c1df0b` (*"IO blocks are now natively Casings in NBT"*) — message trompeur, ne change **rien** à l'architecture : nettoyage cosmétique de 22 lignes d'une scène Ponder. Les blocs I/O (`ReactorOutput`, `ReactorRodInput`, `ReactorFluidInput`, `ReactorAlarm`) restent des classes `Block` séparées de `ReactorCasing`.

### 8.2 Réfutations / non-problèmes confirmés (architecture)

Ces affirmations historiques ont été vérifiées comme **fausses ou déjà closes** dans le code actuel — elles ne correspondent à aucun commit de correction isolé (soit rien n'a jamais été cassé, soit la résolution est le résultat cumulatif de nombreux commits non attribuables individuellement) :

- **Inversion de dépendance `api/`** — ❌ toujours réfuté : `MultiBlockManagerBeta`, `api/multiblock/rods/RodType`, `api/multiblock/fluid/ReactorFluidType` n'importent aucun `content.*` (revérifié par grep sur tout `api/`, 2026-07-12 : zéro résultat). *Pas de hash applicable — l'inversion alléguée par `AUDIT_V1.md` n'existe pas dans l'historique observé.*
- **Deux/trois frameworks multiblock concurrents** — ❌ toujours réfuté : `lib/multiblock/manager/*` et `IBetterPattern` n'existent pas (8 fichiers restants dans `lib/multiblock/`, tous utilisés). *Pas de hash applicable.*
- **God class `ReactorControllerBlockEntity`** — ✅ chantier clos, croissance maîtrisée : **533 lignes** (886 dans `AUDIT_V1.md`, 492 puis 529 aux tours intermédiaires), délégation à 34 fichiers de support (`service/` 14, `manager/` 12, `consumable/` 6, `display/` 2). *Pas de hash unique — résultat cumulatif de dizaines de commits d'extraction progressive vers `manager/service/consumable`, non attribuable avec certitude à un commit isolé.*
- **Coordinateur de verrou fluide dédié** — ✅ toujours non justifié : `PersistentFluidLocks` reste le seul système ; pas de `FluidLockManager` concurrent retrouvé (celui décrit en `AUDIT_V1.md` B14 a disparu, hash non déterminé — voir §8.4). `clearLockIfAllInputsEmpty` utilise `getFuildHandlers(level)` (pas de scan 3D), double itération mineure sans gravité.
- **`clearLockIfAllInputsEmpty` : pas de scan cubique O(n³)** — ✅ acceptable, confirmé sain.

### 8.3 Décisions produit exécutées

- **Collier teignable chat/loup** — décision prise : retrait complet de la mécanique plutôt que finir le câblage. `IrradiatedCatCollarLayer.java`/`IrradiatedWoldCollarLayer.java` supprimés (aucun appelant, confirmé par grep) ; `DATA_COLLAR_COLOR`, `getCollarColor()`/`setCollarColor()`, et le NBT associé retirés d'`IrradiatedCat.java` ; interaction `DyeItem` retirée de `mobInteract` (complète une suppression partielle laissée par une édition en cours qui rendait le code non compilable en état intermédiaire). `IrradiatedWolf` n'avait de toute façon jamais câblé cette mécanique. Note : `getBreedOffspring` (`IrradiatedCat.java:248-260`) appelle toujours `setCollarColor`/`getCollarColor`, mais sur des `Cat` **vanilla** (API Minecraft native), sans rapport avec la mécanique retirée — laissé tel quel à raison. Hash : `17b29aa3` (2026-07-11).
- **Apprivoisement des mobs irradiés bloqué avec message "WIP"** plutôt que laissé cassé/incohérent — décision produit exécutée en même temps que le retrait du collier. Hash : `261b26d8` (2026-07-11).

### 8.4 Corrections historiques d'`AUDIT_V1.md` — hash non déterminé avec certitude pour la majorité

L'audit courant indique depuis plusieurs tours que les bugs suivants d'`AUDIT_V1.md` sont **vérifiés résolus dans le code actuel** : **B2, B3, B4, B5, B6, B8, B9, B11, B13, B16, B19, B12, B14**, l'off-by-one `getFluidInTank`, l'icône d'item de l'armure anti-radiation teintée, et l'ensemble des problèmes radiation historiques (point de vérité unique `canBeIrradiated`). Cette réorganisation a tenté de retrouver un hash de commit pour chacun via `git log -S` sur des chaînes distinctives ; résultat :

- **B15 (anti-pattern try/catch `NuclearExplosionEntity`)** — ✅ hash déterminé avec certitude : `11b1108a` (voir §8.1).
- **B9 (palier radiation `amplifierLevel0` dupliqué)** — candidat trouvé mais **non confirmé avec certitude** : `3b84bac4` ("refactor: update IrradiatedSurfaceRules and RadiationCapability for improved biome handling and radiation amplification logic") est thématiquement plausible mais le diff n'a pas été vérifié en détail. À valider manuellement avant de le citer comme preuve.
- **B13 (bras invisibles de l'armure anti-radiation)** — candidat trouvé mais **non confirmé avec certitude** : `d9ba3956` (2026-06-26, "fix: adjust arm dimensions and positions in AntiRadiationArmor model for better alignment") correspond thématiquement mais ne mentionne pas explicitement le bug des bras invisibles dans son message. À valider manuellement.
- **B1, B2, B4, B5, B18** — **non déterminé**. Recherches `git log -S` ciblées effectuées sur les chaînes distinctives de chaque bug (`&& false`, `RadiationSyncPacket`, valeur de drop `THORIUM_BLOCK`, `BiomeTagRule`/`return null`, clé config `screenShaking`) sans résultat concluant. Ces corrections sont probablement antérieures à la fenêtre de commits explorée en détail par ce document (avant le 2026-06-24) ; les retrouver demanderait une revue manuelle complète de l'historique pré-fenêtre, disproportionnée par rapport au gain (le code actuel confirme déjà que le bug n'est plus présent, seule la traçabilité du commit manque).
- **Reste de la liste** (B3, B6, B8, B11, B12, B16, B19, off-by-one `getFluidInTank`, icône armure, historique radiation complet) — recherche non retentée par souci de proportionnalité : chaque item nécessiterait sa propre requête `git log -S` sur une chaîne distinctive extraite manuellement du code originel décrit par `AUDIT_V1.md`, pour un ensemble de plus de 10 items mineurs déjà revérifiés comme absents du code actuel à plusieurs tours d'audit successifs. **Si la traçabilité individuelle de ces corrections devient nécessaire (audit externe, obligation de conformité), prévoir une session dédiée à l'archéologie Git plutôt que de l'inclure au fil de l'eau d'un audit de code.**

**Dead code massif d'`AUDIT_V1.md`** (§3 de ce document historique : `lib.multiblock.manager.*`, framework d'animation keyframe, `CreateNuclearDamageSources`, `CommentEventClients`, fichier `possible code`, `CNShapelessRecipeGen`, `RadiationOverlay`/`EasingHudOverlay`, `IrradiatedSurfaceRules` v1, `VerifyPattern7x7`/`9x9`, `saveData2`, et la longue liste de membres/méthodes/imports morts §3) — non revérifié individuellement dans cette réorganisation (hors périmètre de la demande, qui portait sur la réorganisation d'`AUDIT_ACTUEL.md` et non sur un nouveau balayage complet du dead code d'`AUDIT_V1.md`). Signal indirect : `lib/multiblock/` ne contient plus que 8 fichiers, tous utilisés (revérifié §8.2), ce qui suggère que le plus gros bloc (`lib.multiblock.manager.*` + `IBetterPattern`) a bien été supprimé à un moment de l'historique — mais aucun hash précis n'a été recherché pour cette réorganisation.
