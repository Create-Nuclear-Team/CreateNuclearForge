Architecture cible
Le ReactorControllerBlockEntity deviendrait un orchestrateur mince :

ReactorControllerBlockEntity
 ├─ état persistant minimal (position/taille/facing, configuredPattern, inventory)
 ├─ managers (existants) : input/output/inputFluid/alarm/frameDisplay
 ├─ services (existants) : IHeatService, IPersistenceService
 │
 ├─ NOUVEAU: ReactorRuntimeOrchestrator (remplace le corps de tick()/handleAssembledState)
 │     ├─ ReactorInputSnapshotBuilder        (#10)
 │     ├─ ReactorHeatUpdateCoordinator       (#5)
 │     ├─ FluidConsumptionRateCalculator     (#6)
 │     ├─ ConsumptionCycleManager.update()   (#7, extension de l'existant)
 │     ├─ ReactorOutputDriver                (#8)
 │     ├─ ReactorAlarmCoordinator            (#1)
 │     └─ ReactorMeltdownMonitor             (#2, #11)
 │            └─ NuclearExplosionFactory     (#3)
 │                  └─ BiomeIrradiationService (#4, hors package controller)
 │
 ├─ NOUVEAU: ReactorDisplayState (DTO)        (#9, #10, #13)
 │     └─ ReactorGoggleTooltipRenderer        (#9)
 │
 └─ NOUVEAU: FluidLockCoordinator (IFluidLockService) (#12)
Le tick() du BE devient essentiellement :

@Override
public void tick() {
    super.tick();
    if (level.isClientSide || isExploding) return;

    MeltdownState meltdown = meltdownMonitor.tick(currentHeat());
    if (meltdown == MeltdownState.EXPLODE) {
        explosionFactory.trigger(...);
        isExploding = true;
        return;
    }

    resolveEntitiesIfNeeded();
    if (!isAssembled()) return;

    ReactorInputSnapshot snapshot = snapshotBuilder.build(level, inputManager, inputFluidManager);
    displayState.updateFrom(snapshot);

    runtimeOrchestrator.run(snapshot, ...); // heat, fluid buffer, consumption cycle, rotation
    alarmCoordinator.update(meltdownMonitor.isDanger());
    stateSynchronizer.updateVisibility(...);
}

#10 — Snapshot des intrants (ReactorInputSnapshotBuilder) → consolide la collecte déjà faite dans tick(), réutilisée par #5/#6/#9. C'est le pivot qui débloque le découpage du gros tick().

Construction de l'état d'affichage client (clientDisplay*)
Code concerné : tick() lignes 547-575 (remplissage clientDisplayItems, clientDisplayFluids, clientMaxFluidCapacity, bigFuelItem, bigCoolerItem, bigFluidStack).

Pourquoi l'extraire : c'est une étape de collecte de données (scan des handlers d'items/fluides) totalement séparable de la logique de simulation. Aujourd'hui mélangée au tick principal, elle gonfle tick() et duplique des accès aux managers.
Principe : SRP + cohésion — "collecter l'état des intrants pour affichage/calcul" est une étape nommée et isolée du pipeline.
Cible : ReactorInputSnapshotBuilder (service), méthode buildSnapshot(Level, ReactorInputManagerI, ReactorInputFluidManagerI) -> ReactorInputSnapshot (record contenant items, fluides, capacité max, bigFuel/bigCooler/bigFluidStack).
Justification : le snapshot devient l'unique source utilisée à la fois par le tooltip (#9), le calcul de chaleur (#5) et la consommation (#6/#7) — élimine la dispersion actuelle des mêmes données dans plusieurs champs mutables du BE.
Dépendances à injecter : Level, ReactorInputManagerI, ReactorInputFluidManagerI.

Avant toute proposition de modification, explique précisément les changements envisagés et les raisons de ces choix. Ne modifie aucun fichier directement et présente uniquement l’analyse, les recommandations et les éventuels exemples de code dans cette discussion.