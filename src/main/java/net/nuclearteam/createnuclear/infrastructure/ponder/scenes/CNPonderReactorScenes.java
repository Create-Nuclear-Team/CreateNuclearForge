package net.nuclearteam.createnuclear.infrastructure.ponder.scenes;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.nuclearteam.createnuclear.CNBlocks;
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
        final BlockPos controller, input1, input2, liquidInput, alarm, output;
        Positions(BlockPos controller, BlockPos input1, BlockPos input2, BlockPos liquidInput, BlockPos alarm, BlockPos output) {
            this.controller = controller;
            this.input1 = input1;
            this.input2 = input2;
            this.liquidInput = liquidInput;
            this.alarm = alarm;
            this.output = output;
        }

        /** All blocks that carry an explanatory callout, so a section-based reveal can show them per layer. */
        BlockPos[] specials() {
            return new BlockPos[] { controller, input1, input2, liquidInput, alarm, output };
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
                new BlockPos(5, 2, 6)   // output
        ));

        // Exemple pour T2 (multiblockSize = 7)
        STATIC_POS.put(7, new Positions(
                new BlockPos(5, 5, 8),  // controller
                new BlockPos(3, 3, 8),  // input1
                new BlockPos(3, 4, 8),  // input2
                new BlockPos(3, 5, 8),  // liquidInput
                new BlockPos(7, 5, 8),  // alarm
                new BlockPos(7, 3, 8)   // output
        ));

        // Exemple pour T3 (multiblockSize = 9)
        STATIC_POS.put(9, new Positions(
                new BlockPos(6, 6, 10),  // controller
                new BlockPos(5, 4, 10), // input1
                new BlockPos(5, 5, 10), // input2
                new BlockPos(5, 6, 10),  // liquidInput
                new BlockPos(7, 6, 10),  // alarm
                new BlockPos(7, 4, 10)   // output
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
                new BlockPos(offset + multiblockSize - 1, cy, cz) // output à droite
        );
    }

    private static void showReactorStructure(SceneBuilder scene, SceneBuildingUtil util, int S, int H, int plate) {
        showReactorStructure(scene, util, S, H, plate, false);
    }

    /**
     * @param bySections when true, each floor is revealed as a single render section instead of
     *        block-by-block. Block-by-block creates one {@code WorldSectionElement} per cell
     *        ({@code H * plate^2}, e.g. 1859 for T3) and they all keep rendering every frame, which
     *        is what makes the larger tiers lag past ~2/3 of the scene. T1/T2 stay block-by-block.
     */
    private static void showReactorStructure(SceneBuilder scene, SceneBuildingUtil util, int S, int H, int plate, boolean bySections) {
        Positions pos = positionsFor(S, H);

        int minX = MARGIN;
        int maxX = MARGIN + S - 1;
        int minZ = MARGIN;
        int maxZ = MARGIN + S - 1;
        int minY = 1;
        int maxY = H;

        // Remplacer toutes les faces (sauf les arêtes et le contrôleur) par du Casing
        // Cela permet de boucher tous les trous causés par les anciens blocs I/O renommés dans le NBT.
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    boolean onXFace = (x == minX || x == maxX);
                    boolean onYFace = (y == minY || y == maxY);
                    boolean onZFace = (z == minZ || z == maxZ);

                    int faceCount = (onXFace ? 1 : 0) + (onYFace ? 1 : 0) + (onZFace ? 1 : 0);

                    // faceCount == 1 signifie qu'on est sur une face plane (pas une arête)
                    if (faceCount == 1) {
                        BlockPos currentPos = new BlockPos(x, y, z);
                        if (!currentPos.equals(pos.controller)) {
                            scene.world().modifyBlock(currentPos, s -> CNBlocks.REACTOR_CASING.get().defaultBlockState(), false);
                        }
                    }
                }
            }
        }

        scene.idle(20);

        for (int y = 1; y <= H; y++) {
            scene.overlay().showText(bySections ? 25 : 8)
                    .text("Floor " + y)
                    .attachKeyFrame()
                    .placeNearTarget();

            if (bySections) {
                // One section for the whole floor: same visual build-up, a fraction of the draws.
                scene.world().showSection(util.select().layer(y), Direction.NORTH);
                // Block-by-block got its per-floor pacing from plate^2 * idle(4). With a single
                // section per floor we must idle explicitly, otherwise callout-free floors flash
                // by instantly and their "Floor N" labels overlap each other.
                scene.idle(50);
                for (BlockPos sp : pos.specials()) {
                    if (sp.getY() == y) {
                        tryShowCallout(scene, util, pos, sp.getX(), sp.getY(), sp.getZ(), bySections);
                    }
                }
            } else {
                for (int x = 0; x < plate; x++) {
                    for (int z = 0; z < plate; z++) {
                        scene.world().showSection(util.select().position(x, y, z), Direction.NORTH);
                        scene.idle(4);
                        tryShowCallout(scene, util, pos, x, y, z, bySections);
                    }
                }
            }
        }

        scene.idle(20);
        scene.overlay()
                .showText(110)
                .text("To start the reactor you will need liquid and rods corresponding to the pattern, then right click the controller with the blueprint in hand");
        Vec3 topSide = util.vector().blockSurface(pos.controller, Direction.EAST);
        scene.overlay()
                .showControls(topSide, Pointing.UP, 60)
                .withItem(CNItems.REACTOR_BLUEPRINT.asStack())
                .rightClick();
        scene.world().modifyBlock(pos.controller, s -> s.setValue(ReactorControllerBlock.ASSEMBLED, true), true);
        
        scene.idle(120);
        scene.overlay()
                .showText(120)
                .text("Note: Check page 4 for I/O and Alarm blocks (placement & function).")
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(130);
    }

    private static void tryShowCallout(SceneBuilder scene, SceneBuildingUtil util, Positions pos, int x, int y, int z, boolean reduced) {
        BlockPos controller = pos.controller;

        if (x == controller.getX() && y == controller.getY() && z == controller.getZ()) {
            scene.overlay().showText(reduced ? 110 : 130)
                    .text("Controller: brain of the reactor, and the place where the blueprint goes to start it")
                    .pointAt(util.vector().blockSurface(controller, Direction.DOWN))
                    .attachKeyFrame()
                    .placeNearTarget();
            scene.idle(reduced ? 120 : 140);
        }
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
        showReactorStructure(scene, util, S, H, plate, true);
    }

    public static void t2(SceneBuilder scene, SceneBuildingUtil util) {
        int S = S_T2, H = H_T2;
        int plate = plateSizeFor(S);
        scene.title("reactor_t2","Reactor T2");
        scene.configureBasePlate(0, 0, plate);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.showBasePlate();
        scene.rotateCameraY(180);
        scene.scaleSceneView(0.45f);
        showReactorStructure(scene, util, S, H, plate, true);
    }

    public static void t3(SceneBuilder scene, SceneBuildingUtil util) {
        int S = S_T3, H = H_T3;
        int plate = plateSizeFor(S);
        scene.title("reactor_t3","Reactor T3");
        scene.configureBasePlate(0, 0, plate);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.showBasePlate();
        scene.rotateCameraY(180);
        scene.scaleSceneView(0.35f);
        // T3 reveals floor-by-floor (section-based) so the largest tier doesn't lag; T1/T2 stay block-by-block.
        showReactorStructure(scene, util, S, H, plate, true);
    }

    public static void ioPlacement(SceneBuilder scene, SceneBuildingUtil util) {
        int S = S_T1, H = H_T1;
        int plate = plateSizeFor(S);
        Positions pos = positionsFor(S, H);
        
        scene.title("reactor_io_placement", "Reactor I/O: Function & Placement");
        scene.configureBasePlate(0, 0, plate);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.showBasePlate();
        scene.rotateCameraY(180);
        scene.scaleSceneView(0.55f);

        // Populate the correct I/O blocks at the beginning because the schematic has old broken IDs
        scene.world().modifyBlock(pos.input1, s -> CNBlocks.REACTOR_ROD_INPUT.get().defaultBlockState().setValue(BlockStateProperties.FACING, Direction.SOUTH), false);
        scene.world().modifyBlock(pos.input2, s -> CNBlocks.REACTOR_ROD_INPUT.get().defaultBlockState().setValue(BlockStateProperties.FACING, Direction.SOUTH), false);
        scene.world().modifyBlock(pos.liquidInput, s -> CNBlocks.REACTOR_FLUID_INPUT.get().defaultBlockState().setValue(BlockStateProperties.FACING, Direction.SOUTH), false);
        scene.world().modifyBlock(pos.alarm, s -> CNBlocks.REACTOR_ALARM.get().defaultBlockState(), false);
        scene.world().modifyBlock(pos.output, s -> CNBlocks.REACTOR_OUTPUT.get().defaultBlockState().setValue(BlockStateProperties.FACING, Direction.SOUTH), false);

        // Show the entire T1 structure instantly
        scene.world().showSection(util.select().layersFrom(1), Direction.UP);
        scene.idle(20);

        // Show Input 1
        scene.overlay().showText(150)
                .text("Rod Input: Stores Heating or Cooling Rods. We need one Heating Rod (fuel) to heat the reactor, and one Cooling Rod for stability.")
                .pointAt(util.vector().blockSurface(pos.input1, Direction.SOUTH))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(160);

        // Show Input 2
        scene.overlay().showText(150)
                .text("Second Rod Input: This is why we need at least two inputs! One for the Heating Rod and one for the Cooling Rod to prevent explosions.")
                .pointAt(util.vector().blockSurface(pos.input2, Direction.SOUTH))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(160);

        // Show Fluid Input
        scene.overlay().showText(150)
                .text("Fluid Input: Fluid is also required to cool the reactor and ensure it does not explode.")
                .pointAt(util.vector().blockSurface(pos.liquidInput, Direction.SOUTH))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(160);
        
        // Show Alarm
        scene.overlay().showText(150)
                .text("Alarm (Optional): Emits a sound when the reactor becomes unstable and risks exploding.")
                .pointAt(util.vector().blockSurface(pos.alarm, Direction.SOUTH))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(160);

        // Show Output
        scene.overlay().showText(160)
                .text("Output: Necessary to extract generated energy. You can place multiple outputs to dispatch the total energy between them.")
                .pointAt(util.vector().blockSurface(pos.output, Direction.SOUTH))
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(170);

        // --- Placement ---
        scene.overlay().showText(120)
                .text("These blocks can replace ANY Reactor Casing, as long as it is not on an edge.")
                .attachKeyFrame()
                .placeNearTarget();
        scene.idle(130);

        // Demonstrate moving the blocks to the roof (y=7 for T1)
        BlockPos newIn1 = new BlockPos(3, 7, 3);
        BlockPos newIn2 = new BlockPos(3, 7, 4);
        BlockPos newLiq = new BlockPos(3, 7, 5);
        BlockPos newAlm = new BlockPos(5, 7, 3);
        BlockPos newOut = new BlockPos(5, 7, 5);

        // Replace old pos with casing
        scene.world().modifyBlock(pos.input1, s -> CNBlocks.REACTOR_CASING.get().defaultBlockState(), true);
        scene.world().modifyBlock(pos.input2, s -> CNBlocks.REACTOR_CASING.get().defaultBlockState(), true);
        scene.world().modifyBlock(pos.liquidInput, s -> CNBlocks.REACTOR_CASING.get().defaultBlockState(), true);
        scene.world().modifyBlock(pos.alarm, s -> CNBlocks.REACTOR_CASING.get().defaultBlockState(), true);
        scene.world().modifyBlock(pos.output, s -> CNBlocks.REACTOR_CASING.get().defaultBlockState(), true);
        
        // Place I/O on roof
        scene.world().modifyBlock(newIn1, s -> CNBlocks.REACTOR_ROD_INPUT.get().defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP), true);
        scene.world().modifyBlock(newIn2, s -> CNBlocks.REACTOR_ROD_INPUT.get().defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP), true);
        scene.world().modifyBlock(newLiq, s -> CNBlocks.REACTOR_FLUID_INPUT.get().defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP), true);
        // Note: ReactorAlarm might not have FACING. Using default state.
        scene.world().modifyBlock(newAlm, s -> CNBlocks.REACTOR_ALARM.get().defaultBlockState(), true);
        scene.world().modifyBlock(newOut, s -> CNBlocks.REACTOR_OUTPUT.get().defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP), true);
        
        scene.overlay().showText(100)
                .text("They can be grouped together on the roof or any other flat surface.")
                .pointAt(util.vector().blockSurface(newIn2, Direction.UP))
                .placeNearTarget();
        scene.idle(110);
    }
}
