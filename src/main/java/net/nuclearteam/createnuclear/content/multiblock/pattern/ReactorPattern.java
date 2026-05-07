package net.nuclearteam.createnuclear.content.multiblock.pattern;

import lib.multiblock.SimpleMultiBlockAislePatternBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.nuclearteam.createnuclear.CNBlocks;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlock;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class ReactorPattern {
    //public BlockPos VerifyPattern(char character) {}

    private static final Predicate<BlockInWorld> blockInWorldAPredicate = state ->
        stateIs(CNBlocks.REACTOR_CASING.get()).test(state)
                || stateIs(CNBlocks.REACTOR_OUTPUT.get()).test(state)
                || stateIs(CNBlocks.REACTOR_INPUT.get()).test(state)
                || stateIs(CNBlocks.REACTOR_LIQUID_INPUT.get()).test(state)
                || stateIs(CNBlocks.REACTOR_ALARM.get()).test(state)
    ;

    private static Predicate<BlockInWorld> stateIs(Block block) {
        return a -> {
            BlockState state = a.getState();
            return state != null && state.is(block);
        };
    }

    public BlockPos VerifyPattern5x5(char character) {
        return SimpleMultiBlockAislePatternBuilder.start()
                .aisle("OOOOO", "OAAAO", "OAAAO", "OAAAO", "OOOOO")
                .aisle("OABAO", "ODDDO", "BDCDB", "ODDDO", "OABAO")
                .aisle("OABAO", "ODDDO", "BDCDB", "ODDDO", "OABAO")
                .aisle("OABAO", "ODDDO", "BDCDB", "ODDDO", "OA*AO")
                .aisle("OABAO", "ODDDO", "BDCDB", "ODDDO", "OABAO")
                .aisle("OABAO", "ODDDO", "BDCDB", "ODDDO", "OABAO")
                .aisle("OOOOO", "OAAAO", "OAAAO", "OAAAO", "OOOOO")
                .where('A', blockInWorldAPredicate)
                .where('B', a -> a.getState().is(CNBlocks.REACTOR_FRAME.get()))
                .where('C', a -> a.getState().is(CNBlocks.REACTOR_CORE.get()))
                .where('D', a -> a.getState().is(CNBlocks.REACTOR_COOLER.get()))
                .where('*', a -> a.getState().is(CNBlocks.REACTOR_CONTROLLER.get()))
                .where('O', a -> a.getState().is(CNBlocks.REACTOR_CASING.get()))
                .getDistanceController(character);
    }

    public BlockPos VerifyPattern7x7(char character) {
        return SimpleMultiBlockAislePatternBuilder.start()
            .aisle("OOOOOOO", "OAAAAAO", "OAAAAAO", "OAAAAAO", "OAAAAAO", "OAAAAAO", "OOOOOOO")
            .aisle("OABABAO", "ADDDDDA", "BDCDCDB", "ADDDDDA", "BDCDCDB", "ADDDDDA", "OABABAO")
            .aisle("OABABAO", "ADDDDDA", "BDCDCDB", "ADDDDDA", "BDCDCDB", "ADDDDDA", "OABABAO")
            .aisle("OABABAO", "ADDDDDA", "BDCDCDB", "ADDDDDA", "BDCDCDB", "ADDDDDA", "OABABAO")
            .aisle("OABABAO", "ADDDDDA", "BDCDCDB", "ADDDDDA", "BDCDCDB", "ADDDDDA", "OAB*BAO")
            .aisle("OABABAO", "ADDDDDA", "BDCDCDB", "ADDDDDA", "BDCDCDB", "ADDDDDA", "OABABAO")
            .aisle("OABABAO", "ADDDDDA", "BDCDCDB", "ADDDDDA", "BDCDCDB", "ADDDDDA", "OABABAO")
            .aisle("OABABAO", "ADDDDDA", "BDCDCDB", "ADDDDDA", "BDCDCDB", "ADDDDDA", "OABABAO")
            .aisle("OOOOOOO", "OAAAAAO", "OAAAAAO", "OAAAAAO", "OAAAAAO", "OAAAAAO", "OOOOOOO")
            .where('A', blockInWorldAPredicate)
            .where('B', a -> a.getState().is(CNBlocks.REACTOR_FRAME.get()))
            .where('C', a -> a.getState().is(CNBlocks.REACTOR_CORE.get()))
            .where('D', a -> a.getState().is(CNBlocks.REACTOR_COOLER.get()))
            .where('*', a -> a.getState().is(CNBlocks.REACTOR_CONTROLLER.get()))
            .where('O', a -> a.getState().is(CNBlocks.REACTOR_CASING.get()))
            .getDistanceController(character);
    }

    public BlockPos VerifyPattern9x9(char character) {
        return SimpleMultiBlockAislePatternBuilder.start()
            .aisle("OOOOOOOOO", "OAAAAAAAO", "OAAAAAAAO", "OAAAAAAAO", "OAAAAAAAO", "OAAAAAAAO", "OAAAAAAAO", "OAAAAAAAO", "OOOOOOOOO")
            .aisle("OBAABAABO", "BDDDDDDDB", "ADCDCDCDA", "ADDDDDDDA", "BDCDCDCDB", "ADDDDDDDA", "ADCDCDCDA", "BDDDDDDDB", "OBAABAABO")
            .aisle("OBAABAABO", "BDDDDDDDB", "ADCDCDCDA", "ADDDDDDDA", "BDCDCDCDB", "ADDDDDDDA", "ADCDCDCDA", "BDDDDDDDB", "OBAABAABO")
            .aisle("OBAABAABO", "BDDDDDDDB", "ADCDCDCDA", "ADDDDDDDA", "BDCDCDCDB", "ADDDDDDDA", "ADCDCDCDA", "BDDDDDDDB", "OBAABAABO")
            .aisle("OBAABAABO", "BDDDDDDDB", "ADCDCDCDA", "ADDDDDDDA", "BDCDCDCDB", "ADDDDDDDA", "ADCDCDCDA", "BDDDDDDDB", "OBAABAABO")
            .aisle("OBAABAABO", "BDDDDDDDB", "ADCDCDCDA", "ADDDDDDDA", "BDCDCDCDB", "ADDDDDDDA", "ADCDCDCDA", "BDDDDDDDB", "OBAA*AABO")
            .aisle("OBAABAABO", "BDDDDDDDB", "ADCDCDCDA", "ADDDDDDDA", "BDCDCDCDB", "ADDDDDDDA", "ADCDCDCDA", "BDDDDDDDB", "OBAABAABO")
            .aisle("OBAABAABO", "BDDDDDDDB", "ADCDCDCDA", "ADDDDDDDA", "BDCDCDCDB", "ADDDDDDDA", "ADCDCDCDA", "BDDDDDDDB", "OBAABAABO")
            .aisle("OBAABAABO", "BDDDDDDDB", "ADCDCDCDA", "ADDDDDDDA", "BDCDCDCDB", "ADDDDDDDA", "ADCDCDCDA", "BDDDDDDDB", "OBAABAABO")
            .aisle("OBAABAABO", "BDDDDDDDB", "ADCDCDCDA", "ADDDDDDDA", "BDCDCDCDB", "ADDDDDDDA", "ADCDCDCDA", "BDDDDDDDB", "OBAABAABO")
            .aisle("OOOOOOOOO", "OAAAAAAAO", "OAAAAAAAO", "OAAAAAAAO", "OAAAAAAAO", "OAAAAAAAO", "OAAAAAAAO", "OAAAAAAAO", "OOOOOOOOO")
            .where('A', blockInWorldAPredicate)
            .where('B', a -> a.getState().is(CNBlocks.REACTOR_FRAME.get()))
            .where('C', a -> a.getState().is(CNBlocks.REACTOR_CORE.get()))
            .where('D', a -> a.getState().is(CNBlocks.REACTOR_COOLER.get()))
            .where('*', a -> a.getState().is(CNBlocks.REACTOR_CONTROLLER.get()))
            .where('O', a -> a.getState().is(CNBlocks.REACTOR_CASING.get()))
            .getDistanceController(character);
    }

    public void FindController(BlockPos blockPos, Level level, List<? extends Player> players, boolean first){ // Function that checks the surrounding blocks in order
        BlockPos newBlock;                                                   // to find the controller and verify the pattern
        Vec3i pos = new Vec3i(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        for (int y = pos.getY()-5; y != pos.getY()+6; y+=1) {
            for (int x = pos.getX()-9; x != pos.getX()+10; x+=1) {
                for (int z = pos.getZ()-9; z != pos.getZ()+10; z+=1) {
                    newBlock = new BlockPos(x, y, z);
                    if (level.getBlockState(newBlock).is(CNBlocks.REACTOR_CONTROLLER.get())) { // verifying the pattern
                        ReactorControllerBlock controller = (ReactorControllerBlock) level.getBlockState(newBlock).getBlock();
                        controller.Verify(level.getBlockState(newBlock), newBlock, level, players, first);
                    }
                }
            }
        }
    }

    public BlockPos findControllerPos(BlockPos blockPos, Level level, List<? extends Player> players, boolean first){
        BlockPos newBlock;                                                   // to find the controller and verify the pattern
        Vec3i pos = new Vec3i(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        for (int y = pos.getY()-5; y != pos.getY()+6; y+=1) {
            for (int x = pos.getX()-9; x != pos.getX()+10; x+=1) {
                for (int z = pos.getZ()-9; z != pos.getZ()+10; z+=1) {
                    newBlock = new BlockPos(x, y, z);
                    if (level.getBlockState(newBlock).is(CNBlocks.REACTOR_CONTROLLER.get())) { // verifying the pattern
                        ReactorControllerBlock controller = (ReactorControllerBlock) level.getBlockState(newBlock).getBlock();
                        ReactorControllerBlockEntity entity = controller.getBlockEntity(level, newBlock);
                        if  (entity != null) {
                            if (isInReactorRange(entity.getMultiblockPos(), blockPos)) {
                                return newBlock;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public boolean isInReactorRange(int []reactorPos, BlockPos blockPos) {
        //[xMin, xMax, yMin, yMax, zMin, zMax]
        if (reactorPos == null || reactorPos.length < 6) return false;
        return blockPos.getX() >= reactorPos[0] && blockPos.getX() <= reactorPos[1]
                && blockPos.getY() >= reactorPos[2] && blockPos.getY() <= reactorPos[3]
                && blockPos.getZ() >= reactorPos[4] && blockPos.getZ() <= reactorPos[5];
    }
}
