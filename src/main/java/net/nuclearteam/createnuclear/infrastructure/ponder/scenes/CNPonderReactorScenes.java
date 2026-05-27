package net.nuclearteam.createnuclear.infrastructure.ponder.scenes;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.nuclearteam.createnuclear.CNItems;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlock;

import java.util.HashMap;
import java.util.Map;

public class CNPonderReactorScenes {

    // multiblock sizes and heights for tiers
    private static final int S_T1 = 5;
    private static final int H_T1 = 7;
    private static final int S_T2 = 7;
    private static final int H_T2 = 9;
    private static final int S_T3 = 9;
    private static final int H_T3 = 11;

    private static final int PEDESTAL_Y = 0; // couche de piedestal (snow/concrete)
    private static final int MULTIBLOCK_BASE_Y = 1; // multiblock starts at y=1
    private static final int MARGIN = 2; // Create margin
    private static int plateSizeFor(int multiblockSize) { return multiblockSize + MARGIN * 2; }

    /** * Conteneur simple pour stocker toutes les positions d'intérêt d'un multiblock. * Les BlockPos ici doivent être EN COORDONNÉES SCÈNE (0..plate-1 pour X/Z, 1..H pour Y). */
    private static class Positions {
        final BlockPos controller, input1, input2, liquidInput, alarm, output, firstCasing;
        Positions(BlockPos controller, BlockPos input1, BlockPos input2, BlockPos liquidInput, BlockPos alarm, BlockPos output, BlockPos firstCasing) {
            this.controller = controller;
            this.input1 = input1;
            this.input2 = input2;
            this.liquidInput = liquidInput;
            this.alarm = alarm;
            this.output = output;
            this.firstCasing = firstCasing;
        }
    }

    /** * Map statique – remplis/ajuste les coordonnées ici pour chaque taille (multiblockSize). * Exemple : clef = 5 pour S_T1 (5x5), 7 pour S_T2, 9 pour S_T3. * * Règle : x/z en 0..plate-1, y en 1..H ; adapte ces valeurs exactement comme dans ton NBT. */
    private static final Map<Integer, Positions> STATIC_POS = new HashMap<>();
    static {
        // Exemple pour T1 (multiblockSize = 5)
        // plate = 5 + 4 = 9 => x/z valides : 0..8 ; multiblock intérieur : 2..6
        STATIC_POS.put(5, new Positions(
                new BlockPos(4, 4, 6),  // controller
                new BlockPos(3, 2, 6),  // input1
                new BlockPos(3, 3, 6),  // input2
                new BlockPos(3, 4, 6),  // liquidInput
                new BlockPos(5, 4, 6),  // alarm
                new BlockPos(5, 2, 6),   // output
                new BlockPos(2, 1, 6)   // firstCasing
        ));

        // Exemple pour T2 (multiblockSize = 7)
        STATIC_POS.put(7, new Positions(
                new BlockPos(5, 5, 8),  // controller
                new BlockPos(3, 3, 8),  // input1
                new BlockPos(3, 4, 8),  // input2
                new BlockPos(3, 5, 8),  // liquidInput
                new BlockPos(7, 5, 8),  // alarm
                new BlockPos(7, 3, 8),   // output
                new BlockPos(2, 1, 8)   // firstCasing
        ));

        // Exemple pour T3 (multiblockSize = 9)
        STATIC_POS.put(9, new Positions(
                new BlockPos(6, 6, 10),  // controller
                new BlockPos(5, 4, 10), // input1
                new BlockPos(5, 5, 10), // input2
                new BlockPos(5, 6, 10),  // liquidInput
                new BlockPos(7, 6, 10),  // alarm
                new BlockPos(7, 4, 10),   // output
                new BlockPos(2, 1, 10)   // firstCasing
        ));
    }

    /** * Retourne les positions statiques pour la taille donnée. * Si aucune entrée statique n'existe, retourne des positions calculées "raisonnables". */
    private static Positions positionsFor(int multiblockSize, int height) {
        Positions p = STATIC_POS.get(multiblockSize);
        if (p != null) return p;

        // fallback : calcul simple centré (si tu veux éviter un NPE)
        int plate = plateSizeFor(multiblockSize);
        int offset = MARGIN;
        int cx = offset + (multiblockSize / 2);
        int cz = offset + (multiblockSize / 2);
        int cy = MULTIBLOCK_BASE_Y + (height / 2);
        return new Positions(
                new BlockPos(cx, cy, cz),               // controller
                new BlockPos(cx, MULTIBLOCK_BASE_Y+1, offset + multiblockSize - 1), // input1 devant
                new BlockPos(cx, Math.min(MULTIBLOCK_BASE_Y+height-1, MULTIBLOCK_BASE_Y+2), offset + multiblockSize - 1), // input2
                new BlockPos(cx, cy, offset),          // liquidInput derrière
                new BlockPos(Math.max(offset, cx-1), cy, cz), // alarm
                new BlockPos(offset + multiblockSize - 1, cy, cz), // output à droite
                new BlockPos(offset, MULTIBLOCK_BASE_Y, offset) // firstCasing (coin avant-gauche au sol)
        );
    }

    private static void showReactorStructure(SceneBuilder scene, SceneBuildingUtil util, int S, int H, int plate) {
        Positions pos = positionsFor(S, H);
        BlockPos controller = pos.controller;
        BlockPos output = pos.output;
        BlockPos input1 = pos.input1;
        BlockPos input2 = pos.input2;
        BlockPos liquidInput = pos.liquidInput;
        BlockPos alarm = pos.alarm;
        BlockPos firstCasing = pos.firstCasing;

        for (int y = 1; y <= H; y++) {
            scene.overlay().showText(8)
                    .text("Floor " + y)
                    .attachKeyFrame()
                    .placeNearTarget();

            for (int x = 0; x < plate; x++) {
                for (int z = 0; z < plate; z++) {
                    scene.world().showSection(util.select().position(x, y, z), Direction.NORTH);
                    scene.idle(4);

                    if (x == firstCasing.getX() && y == firstCasing.getY() && z == firstCasing.getZ()) {
                        scene.overlay().showText(90)
                                .text("Input, Liquid Input, Alarm and Output can be placed in any position (and number) of a casing as long as it is not in an edge")
                                .pointAt(util.vector().blockSurface(firstCasing, Direction.DOWN))
                                .attachKeyFrame()
                                .placeNearTarget();
                        scene.idle(80);
                    }

                    if (x == input1.getX() && y == input1.getY() && z == input1.getZ()) {
                        scene.overlay().showText(90)
                                .text("Input: stores one stack of rod (graphite/uranium/thorium)")
                                .pointAt(util.vector().blockSurface(input1, Direction.UP))
                                .attachKeyFrame()
                                .placeNearTarget();
                        scene.idle(80);
                    }
                    if (x == input2.getX() && y == input2.getY() && z == input2.getZ()) {
                        scene.overlay().showText(110)
                                .text("Second input: needed because you will likely have one input " +
                                        "of fuel rod (thorium/uranium) and one for coolant (graphite)")
                                .pointAt(util.vector().blockSurface(input2, Direction.UP))
                                .attachKeyFrame()
                                .placeNearTarget();
                        scene.idle(80);
                    }
                    if (x == liquidInput.getX() && y == liquidInput.getY() && z == liquidInput.getZ()) {
                        scene.overlay().showText(90)
                                .text("Liquid Input: cooling fluid inlet")
                                .pointAt(util.vector().blockSurface(liquidInput, Direction.UP))
                                .attachKeyFrame()
                                .placeNearTarget();
                        scene.idle(80);
                    }
                    if (x == alarm.getX() && y == alarm.getY() && z == alarm.getZ()) {
                        scene.overlay().showText(90)
                                .text("Alarm: will sound if reactor overheats")
                                .pointAt(util.vector().blockSurface(alarm, Direction.UP))
                                .attachKeyFrame()
                                .placeNearTarget();
                        scene.idle(80);
                    }
                    if (x == output.getX() && y == output.getY() && z == output.getZ()) {
                        scene.overlay().showText(90)
                                .text("Output: where the generated power is extracted")
                                .pointAt(util.vector().blockSurface(output, Direction.UP))
                                .attachKeyFrame()
                                .placeNearTarget();
                        scene.idle(80);
                    }
                    if (x == controller.getX() && y == controller.getY() && z == controller.getZ()) {
                        scene.overlay().showText(110)
                                .text("Controller: manages the reactor's operation, multiblock structure, interactions with items/fluids and the blueprint pattern")
                                .pointAt(util.vector().blockSurface(controller, Direction.DOWN))
                                .attachKeyFrame()
                                .placeNearTarget();
                        scene.idle(80);
                    }
                }
            }
        }

        scene.idle(20);
        scene.overlay()
                .showText(110)
                .text("To start the reactor you will need liquid and fuel, then right click the controller with the blueprint in hand");
        Vec3 topSide = util.vector().blockSurface(controller, Direction.EAST);
        scene.overlay()
                .showControls(topSide, Pointing.UP, 60)
                .withItem(CNItems.REACTOR_BLUEPRINT.asStack())
                .rightClick();
        scene.world().modifyBlock(controller, s -> s.setValue(ReactorControllerBlock.ASSEMBLED, true), true);
    }

    public static void t1(SceneBuilder scene, SceneBuildingUtil util) {
        int S = S_T1, H = H_T1;
        int plate = plateSizeFor(S);
        scene.title("reactor_t1","Reactor T1");
        scene.configureBasePlate(0, 0, plate);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.showBasePlate();
        scene.rotateCameraY(180);
        scene.scaleSceneView(0.55f);
        showReactorStructure(scene, util, S, H, plate);
    }

    public static void t2(SceneBuilder scene, SceneBuildingUtil util) {
        int S = S_T2, H = H_T2;
        int plate = plateSizeFor(S);
        scene.title("reactor_t2","Reactor T2");
        scene.configureBasePlate(0, 0, plate);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.showBasePlate();
        scene.rotateCameraY(180);
        scene.scaleSceneView(0.40f);
        showReactorStructure(scene, util, S, H, plate);
    }

    public static void t3(SceneBuilder scene, SceneBuildingUtil util) {
        int S = S_T3, H = H_T3;
        int plate = plateSizeFor(S);
        scene.title("reactor_t3","Reactor T3");
        scene.configureBasePlate(0, 0, plate);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.showBasePlate();
        scene.rotateCameraY(180);
        scene.scaleSceneView(0.25f);
        showReactorStructure(scene, util, S, H, plate);
    }
}