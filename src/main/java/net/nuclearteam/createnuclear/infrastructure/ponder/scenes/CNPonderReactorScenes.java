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
                scene.idle(30);
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
    }

    /**
     * Shows the explanatory callout for the special block at (x,y,z), if any. Shared by both reveal
     * modes. When {@code reduced} is true (T3), these block-explanation texts run 15% shorter
     * (90->77, 110->94, hold 80->68); the other tiers keep the original durations.
     */
    private static void tryShowCallout(SceneBuilder scene, SceneBuildingUtil util, Positions pos, int x, int y, int z, boolean reduced) {
        BlockPos input1 = pos.input1, input2 = pos.input2, liquidInput = pos.liquidInput,
                alarm = pos.alarm, output = pos.output, controller = pos.controller;

        if (x == input1.getX() && y == input1.getY() && z == input1.getZ()) {
            scene.overlay().showText(reduced ? 77 : 90)
                    .text("Input: stores one stack of rod (free placing : can replace any casing not in the edge)")
                    .pointAt(util.vector().blockSurface(input1, Direction.UP))
                    .attachKeyFrame()
                    .placeNearTarget();
            scene.idle(reduced ? 68 : 80);
        }
        if (x == input2.getX() && y == input2.getY() && z == input2.getZ()) {
            scene.overlay().showText(reduced ? 94 : 110)
                    .text("Second input: needed because you will likely have one input " +
                            "of fuel rod and one for coolant (free placing)")
                    .pointAt(util.vector().blockSurface(input2, Direction.UP))
                    .attachKeyFrame()
                    .placeNearTarget();
            scene.idle(reduced ? 68 : 80);
        }
        if (x == liquidInput.getX() && y == liquidInput.getY() && z == liquidInput.getZ()) {
            scene.overlay().showText(reduced ? 77 : 90)
                    .text("Liquid Input: cooling fluid inlet (free placing)")
                    .pointAt(util.vector().blockSurface(liquidInput, Direction.UP))
                    .attachKeyFrame()
                    .placeNearTarget();
            scene.idle(reduced ? 68 : 80);
        }
        if (x == alarm.getX() && y == alarm.getY() && z == alarm.getZ()) {
            scene.overlay().showText(reduced ? 77 : 90)
                    .text("Alarm: will sound if reactor overheats (free placing)")
                    .pointAt(util.vector().blockSurface(alarm, Direction.UP))
                    .attachKeyFrame()
                    .placeNearTarget();
            scene.idle(reduced ? 68 : 80);
        }
        if (x == output.getX() && y == output.getY() && z == output.getZ()) {
            scene.overlay().showText(reduced ? 77 : 90)
                    .text("Output: where the generated power is extracted (free placing)")
                    .pointAt(util.vector().blockSurface(output, Direction.UP))
                    .attachKeyFrame()
                    .placeNearTarget();
            scene.idle(reduced ? 68 : 80);
        }
        if (x == controller.getX() && y == controller.getY() && z == controller.getZ()) {
            scene.overlay().showText(reduced ? 94 : 110)
                    .text("Controller: brain of the reactor, and the place where the blueprint goes to start it")
                    .pointAt(util.vector().blockSurface(controller, Direction.DOWN))
                    .attachKeyFrame()
                    .placeNearTarget();
            scene.idle(reduced ? 68 : 80);
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
        scene.scaleSceneView(0.45f);
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
        scene.scaleSceneView(0.35f);
        // T3 reveals floor-by-floor (section-based) so the largest tier doesn't lag; T1/T2 stay block-by-block.
        showReactorStructure(scene, util, S, H, plate, true);
    }
}
