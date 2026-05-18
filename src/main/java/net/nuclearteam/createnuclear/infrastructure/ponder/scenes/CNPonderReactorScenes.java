package net.nuclearteam.createnuclear.infrastructure.ponder.scenes;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.nuclearteam.createnuclear.CNItems;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlock;
import net.nuclearteam.createnuclear.foundation.utility.CreateNuclearLang;

public class CNPonderReactorScenes {

    /*
     Approach:
     - Create adds a base plate leaving 2 blocks margin on every horizontal side.
       So displayed plate size = multiblockSize + 4.
     - Multiblock vertical region starts at layer 1 (layer 0 = base plate).
     - Controller is centered horizontally: offset + floor(multiblockSize/2).
     - All special blocks (input, input2, liquid input, alarm, output) are computed
       relative to controller / multiblock bounds so they're consistent for all tiers.
     - If vertical placement (y) is off in-game, tweak the verticalOffset variable.
    */

    // multiblock sizes and heights for tiers
    private static final int S_T1 = 5;
    private static final int H_T1 = 7;
    private static final int S_T2 = 7;
    private static final int H_T2 = 9;
    private static final int S_T3 = 9;
    private static final int H_T3 = 11;

    // helper accessors
    private static int plateSizeFor(int multiblockSize) { return multiblockSize + 4; }
    private static int multiblockOffset() { return 2; } // margin on each side

    private static BlockPos controllerFor(int multiblockSize, int height) {
        int plate = plateSizeFor(multiblockSize);
        int offset = multiblockOffset();
        int centerX = offset + (multiblockSize / 2);
        int centerZ = offset + (multiblockSize / 2);
        int verticalOffset = 1; // multiblock starts at layer 1 (0 = base plate)
        int centerY = verticalOffset + (height / 2); // adjust if needed
        return new BlockPos(centerX, centerY, centerZ);
    }

    private static BlockPos outputFor(int multiblockSize, int height) {
        // put output on the positive X edge, middle height
        int offset = multiblockOffset();
        int x = offset + multiblockSize - 1; // right edge inside frame
        int z = offset + (multiblockSize / 2);
        int y = 1 + (height / 2);
        return new BlockPos(x, y, z);
    }

    private static BlockPos input1For(int multiblockSize, int height) {
        // left edge, centered
        int offset = multiblockOffset();
        int x = offset; // left edge inside frame
        int z = offset + (multiblockSize / 2);
        int y = 1 + (height / 2);
        return new BlockPos(x, y, z);
    }

    private static BlockPos input2For(int multiblockSize, int height) {
        // back edge (negative Z), centered
        int offset = multiblockOffset();
        int x = offset + (multiblockSize / 2);
        int z = offset; // front/back edge inside frame
        int y = 1 + (height / 2);
        return new BlockPos(x, y, z);
    }

    private static BlockPos liquidInputFor(int multiblockSize, int height) {
        // front edge (positive Z), centered (different from input2)
        int offset = multiblockOffset();
        int x = offset + (multiblockSize / 2);
        int z = offset + multiblockSize - 1;
        int y = 1 + (height / 2);
        return new BlockPos(x, y, z);
    }

    private static BlockPos alarmFor(int multiblockSize, int height) {
        // place alarm on top edge (negative X), centered vertically
        int offset = multiblockOffset();
        int x = offset; // left edge
        int z = offset; // corner (user said edges allowed)
        int y = 1 + (height / 2);
        return new BlockPos(x, y, z);
    }

    private static void CheckStopPoints(SceneBuilder scene, SceneBuildingUtil util, int S, int H, int plate) {
        BlockPos controller = controllerFor(S, H);
        BlockPos output = outputFor(S, H);
        BlockPos input1 = input1For(S, H);
        BlockPos input2 = input2For(S, H);
        BlockPos liquidInput = liquidInputFor(S, H);
        BlockPos alarm = alarmFor(S, H);

        for (int y = 1; y <= H; y++) {
            scene.overlay().showText(10)
                    .text(CreateNuclearLang.translate("ponder.reactor.floor").string())
                    .attachKeyFrame()
                    .placeNearTarget();

            for (int x = 0; x < plate; x++) {
                for (int z = 0; z < plate; z++) {
                    scene.world().showSection(util.select().position(x, y, z), Direction.NORTH);
                    scene.idle(4);

                    // overlays for points of interest
                    if (x == input1.getX() && y == input1.getY() && z == input1.getZ()) {
                        scene.overlay().showText(90)
                                .text(CreateNuclearLang.translate("ponder.reactor.input1").toString())
                                .pointAt(util.vector().blockSurface(input1, Direction.UP))
                                .attachKeyFrame()
                                .placeNearTarget();
                        scene.idle(80);
                    }
                    if (x == input2.getX() && y == input2.getY() && z == input2.getZ()) {
                        scene.overlay().showText(90)
                                .text(CreateNuclearLang.translate("ponder.reactor.input2").string())
                                .pointAt(util.vector().blockSurface(input2, Direction.UP))
                                .attachKeyFrame()
                                .placeNearTarget();
                        scene.idle(80);
                    }
                    if (x == liquidInput.getX() && y == liquidInput.getY() && z == liquidInput.getZ()) {
                        scene.overlay().showText(90)
                                .text(CreateNuclearLang.translate("ponder.reactor.liquid_input").string())
                                .pointAt(util.vector().blockSurface(liquidInput, Direction.UP))
                                .attachKeyFrame()
                                .placeNearTarget();
                        scene.idle(80);
                    }
                    if (x == alarm.getX() && y == alarm.getY() && z == alarm.getZ()) {
                        scene.overlay().showText(90)
                                .text(CreateNuclearLang.translate("ponder.reactor.alarm").string())
                                .pointAt(util.vector().blockSurface(alarm, Direction.UP))
                                .attachKeyFrame()
                                .placeNearTarget();
                        scene.idle(80);
                    }
                    if (x == output.getX() && y == output.getY() && z == output.getZ()) {
                        scene.overlay().showText(90)
                                .text(CreateNuclearLang.translate("ponder.reactor.output").string())
                                .pointAt(util.vector().blockSurface(output, Direction.UP))
                                .attachKeyFrame()
                                .placeNearTarget();
                        scene.idle(80);
                    }
                    if (x == controller.getX() && y == controller.getY() && z == controller.getZ()) {
                        scene.overlay().showText(90)
                                .text(CreateNuclearLang.translate("ponder.reactor.controller").string())
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
                .showText(60)
                .text(CreateNuclearLang.translate("ponder.reactor.use_blueprint").string());
        Vec3 topSide = util.vector().blockSurface(controller, Direction.EAST);
        scene.overlay()
                .showControls(topSide, Pointing.UP, 60)
                .withItem(CNItems.REACTOR_BLUEPRINT.asStack())
                .rightClick();
        scene.world().modifyBlock(controller, s -> s.setValue(ReactorControllerBlock.ASSEMBLED, true), true);
    }

    // --- T1 scene (5x5x7) ---
    public static void t1(SceneBuilder scene, SceneBuildingUtil util) {
        int S = S_T1, H = H_T1;
        int plate = plateSizeFor(S);
        scene.title("reactor_t1", CreateNuclearLang.translate("ponder.reactor.title.t1").string());
        scene.configureBasePlate(0, 0, plate);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.showBasePlate();
        scene.rotateCameraY(180);
        scene.scaleSceneView(0.6f);

        CheckStopPoints(scene, util, S, H, plate);
    }

    // --- T2 scene (7x7x9) ---
    public static void t2(SceneBuilder scene, SceneBuildingUtil util) {
        int S = S_T2, H = H_T2;
        int plate = plateSizeFor(S);
        scene.title("reactor_t2", CreateNuclearLang.translate("ponder.reactor.title.t2").string());
        scene.configureBasePlate(0, 0, plate);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.showBasePlate();
        scene.rotateCameraY(180);
        scene.scaleSceneView(0.45f);

        CheckStopPoints(scene, util, S, H, plate);
    }

    // --- T3 scene (9x9x11) ---
    public static void t3(SceneBuilder scene, SceneBuildingUtil util) {
        int S = S_T3, H = H_T3;
        int plate = plateSizeFor(S);
        scene.title("reactor_t3", CreateNuclearLang.translate("ponder.reactor.title.t3").string());
        scene.configureBasePlate(0, 0, plate);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.showBasePlate();
        scene.rotateCameraY(180);
        scene.scaleSceneView(0.3f);

        CheckStopPoints(scene, util, S, H, plate);
    }
}