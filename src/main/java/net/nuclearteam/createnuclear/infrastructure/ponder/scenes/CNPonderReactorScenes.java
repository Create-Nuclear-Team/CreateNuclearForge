package net.nuclearteam.createnuclear.infrastructure.ponder.scenes;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.nuclearteam.createnuclear.CNItems;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlock;

public class CNPonderReactorScenes {

    // T1 (5x5x7)
    private static final BlockPos CONTROLLER_T1 = new BlockPos(5, 5, 6);
    private static final BlockPos OUTPUT_T1 = new BlockPos(6, 3, 6);
    private static final BlockPos INPUT1_T1 = new BlockPos(4, 4, 6);
    private static final BlockPos INPUT2_T1 = new BlockPos(4, 4, 2);
    private static final BlockPos LIQUID_INPUT_T1 = new BlockPos(4, 4, 2);
    private static final BlockPos ALARM_T1 = new BlockPos(4, 4, 2);

    // T2 (7x7x9)
    private static final BlockPos CONTROLLER_T2 = new BlockPos(6, 4, 3);
    private static final BlockPos OUTPUT_T2 = new BlockPos(4, 1, 4);
    private static final BlockPos INPUT1_T2 = new BlockPos(4, 4, 2);
    private static final BlockPos INPUT2_T2 = new BlockPos(4, 4, 2);
    private static final BlockPos LIQUID_INPUT_T2 = new BlockPos(4, 4, 2);
    private static final BlockPos ALARM_T2 = new BlockPos(4, 4, 2);

    // T3 (9x9x11)
    private static final BlockPos CONTROLLER_T3 = new BlockPos(8, 5, 4);
    private static final BlockPos OUTPUT_T3 = new BlockPos(4, 1, 4);
    private static final BlockPos INPUT1_T3 = new BlockPos(4, 4, 2);
    private static final BlockPos INPUT2_T3 = new BlockPos(4, 4, 2);
    private static final BlockPos LIQUID_INPUT_T3 = new BlockPos(4, 4, 2);
    private static final BlockPos ALARM_T3 = new BlockPos(4, 4, 2);

    // T1
    public static void t1(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("reactor_t1", "Construction du réacteur T1");
        scene.configureBasePlate(0, 0, 9);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.showBasePlate();
        scene.rotateCameraY(180);
        scene.scaleSceneView(0.7f);

        for (int y = 1; y < 8; y++) {
            scene.overlay().showText(10)
                    .text("Étage " + y)
                    .attachKeyFrame()
                    .placeNearTarget();

            for (int x = 1; x < 7; x++) {
                for (int z = 1; z < 7; z++) {
                    scene.world().showSection(util.select().position(x, y, z), Direction.NORTH);
                    scene.idle(5);
                    if (x == INPUT1_T1.getX() && y == INPUT1_T1.getY() && z == INPUT1_T1.getZ()) {
                        scene.overlay().showText(90)
                                .text("Input : Stocke uranium/graphite. Un par rod.")
                                .pointAt(util.vector().blockSurface(INPUT1_T1, Direction.UP))
                                .attachKeyFrame()
                                .placeNearTarget();
                        scene.idle(120);
                    }
                    if (x == OUTPUT_T1.getX() && y == OUTPUT_T1.getY() && z == OUTPUT_T1.getZ()) {
                        scene.overlay().showText(90)
                                .text("Output : Produit l'énergie (SU).")
                                .pointAt(util.vector().blockSurface(OUTPUT_T1, Direction.UP))
                                .attachKeyFrame()
                                .placeNearTarget();
                        scene.idle(120);
                    }
                    if (x == CONTROLLER_T1.getX() && y == CONTROLLER_T1.getY() && z == CONTROLLER_T1.getZ()) {
                        scene.overlay().showText(90)
                                .text("Contrôleur : Centre, gère l'énergie.")
                                .pointAt(util.vector().blockSurface(CONTROLLER_T1, Direction.DOWN))
                                .attachKeyFrame()
                                .placeNearTarget();
                        scene.idle(120);
                    }
                }
            }
        }

        scene.idle(30);
        scene.overlay()
                .showText(60)
                .text("Utilisez le blueprint pour activer.");
        Vec3 topSide = util.vector().blockSurface(CONTROLLER_T1, Direction.EAST);
        scene.overlay()
                .showControls(topSide, Pointing.UP, 60)
                .withItem(CNItems.REACTOR_BLUEPRINT.asStack())
                .rightClick();
        scene.world().modifyBlock(CONTROLLER_T1, s -> s.setValue(ReactorControllerBlock.ASSEMBLED, true), true);
    }

    // T2
    public static void t2(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("reactor_t2", "Construction du réacteur T2");
        scene.configureBasePlate(0, 0, 11);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.showBasePlate();
        scene.rotateCameraY(180);
        scene.scaleSceneView(0.55f);

        for (int y = 1; y < 10; y++) {
            scene.overlay().showText(10)
                    .text("Étage " + y)
                    .attachKeyFrame()
                    .placeNearTarget();

            for (int x = 0; x < 7; x++) {
                for (int z = 0; z < 7; z++) {
                    scene.world().showSection(util.select().position(x, y, z), Direction.NORTH);
                    scene.idle(5);
                    if (x == INPUT1_T2.getX() && y == INPUT1_T2.getY() && z == INPUT1_T2.getZ()) {
                        scene.overlay().showText(90)
                                .text("Input : Capacité accrue.")
                                .pointAt(util.vector().blockSurface(INPUT1_T2, Direction.UP))
                                .attachKeyFrame()
                                .placeNearTarget();
                        scene.idle(120);
                    }
                    if (x == OUTPUT_T2.getX() && y == OUTPUT_T2.getY() && z == OUTPUT_T2.getZ()) {
                        scene.overlay().showText(90)
                                .text("Output : Énergie supérieure.")
                                .pointAt(util.vector().blockSurface(OUTPUT_T2, Direction.UP))
                                .attachKeyFrame()
                                .placeNearTarget();
                        scene.idle(120);
                    }
                    if (x == CONTROLLER_T2.getX() && y == CONTROLLER_T2.getY() && z == CONTROLLER_T2.getZ()) {
                        scene.overlay().showText(90)
                                .text("Contrôleur : Optimisé pour T2.")
                                .pointAt(util.vector().blockSurface(CONTROLLER_T2, Direction.DOWN))
                                .attachKeyFrame()
                                .placeNearTarget();
                        scene.idle(120);
                    }
                }
            }
        }

        scene.idle(30);
        scene.overlay()
                .showText(60)
                .text("Activez avec le blueprint.");
        Vec3 topSide = util.vector().blockSurface(CONTROLLER_T2, Direction.EAST);
        scene.overlay()
                .showControls(topSide, Pointing.UP, 60)
                .withItem(CNItems.REACTOR_BLUEPRINT.asStack())
                .rightClick();
        scene.world().modifyBlock(CONTROLLER_T2, s -> s.setValue(ReactorControllerBlock.ASSEMBLED, true), true);
    }

    // T3
    public static void t3(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("reactor_t3", "Construction du réacteur T3");
        scene.configureBasePlate(0, 0, 13);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.showBasePlate();
        scene.rotateCameraY(180);
        scene.scaleSceneView(0.4f);

        for (int y = 1; y < 12; y++) {
            scene.overlay().showText(10)
                    .text("Étage " + y)
                    .attachKeyFrame()
                    .placeNearTarget();

            for (int x = 0; x < 9; x++) {
                for (int z = 0; z < 9; z++) {
                    scene.world().showSection(util.select().position(x, y, z), Direction.NORTH);
                    scene.idle(5);
                    if (x == INPUT1_T3.getX() && y == INPUT1_T3.getY() && z == INPUT1_T3.getZ()) {
                        scene.overlay().showText(90)
                                .text("Input : Grande capacité.")
                                .pointAt(util.vector().blockSurface(INPUT1_T3, Direction.UP))
                                .attachKeyFrame()
                                .placeNearTarget();
                        scene.idle(120);
                    }
                    if (x == OUTPUT_T3.getX() && y == OUTPUT_T3.getY() && z == OUTPUT_T3.getZ()) {
                        scene.overlay().showText(90)
                                .text("Output : Puissance maximale.")
                                .pointAt(util.vector().blockSurface(OUTPUT_T3, Direction.UP))
                                .attachKeyFrame()
                                .placeNearTarget();
                        scene.idle(120);
                    }
                    if (x == CONTROLLER_T3.getX() && y == CONTROLLER_T3.getY() && z == CONTROLLER_T3.getZ()) {
                        scene.overlay().showText(90)
                                .text("Contrôleur : Gestion avancée.")
                                .pointAt(util.vector().blockSurface(CONTROLLER_T3, Direction.DOWN))
                                .attachKeyFrame()
                                .placeNearTarget();
                        scene.idle(120);
                    }
                }
            }
        }

        scene.idle(30);
        scene.overlay()
                .showText(60)
                .text("Activez avec le blueprint. Surveillez les alarmes !");
        Vec3 topSide = util.vector().blockSurface(CONTROLLER_T3, Direction.EAST);
        scene.overlay()
                .showControls(topSide, Pointing.UP, 60)
                .withItem(CNItems.REACTOR_BLUEPRINT.asStack())
                .rightClick();
        scene.world().modifyBlock(CONTROLLER_T3, s -> s.setValue(ReactorControllerBlock.ASSEMBLED, true), true);
    }
}