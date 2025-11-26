package net.nuclearteam.createnuclear.content.multiblock.pattern;

import lib.multiblock.SimpleMultiBlockAislePatternBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.nuclearteam.createnuclear.CNBlocks;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlock;
import net.nuclearteam.createnuclear.content.multiblock.output.ReactorOutput;

import java.util.List;

public class ReactorPattern {
    //public BlockPos VerifyPattern(char character) {}

    public BlockPos VerifyPattern(char character) {
        return SimpleMultiBlockAislePatternBuilder.start()
                .aisle("OOOOO", "OAAAO", "OAAAO", "OAAAO", "OOOOO")
                .aisle("OABAO", "ODDDO", "BDCDB", "ODDDO", "OABAO")
                .aisle("OABAO", "ODDDO", "BDCDB", "ODDDO", "OABAO")
                .aisle("OABAO", "ODDDO", "BDCDB", "ODDDO", "OA*AO")
                .aisle("OABAO", "ODDDO", "BDCDB", "ODDDO", "OABAO")
                .aisle("OABAO", "ODDDO", "BDCDB", "ODDDO", "OABAO")
                .aisle("OOOOO", "OAAAO", "OAAAO", "OAAAO", "OOAOO")
                .where('A', a -> a.getState().is(CNBlocks.REACTOR_CASING.get())
                        || a.getState().is(CNBlocks.REACTOR_OUTPUT.get())
                        || a.getState().is(CNBlocks.REACTOR_INPUT.get())
                )
                .where('B', a -> a.getState().is(CNBlocks.REACTOR_FRAME.get()))
                .where('C', a -> a.getState().is(CNBlocks.REACTOR_CORE.get()))
                .where('D', a -> a.getState().is(CNBlocks.REACTOR_COOLER.get()))
                .where('*', a -> a.getState().is(CNBlocks.REACTOR_CONTROLLER.get()))
                .where('O', a -> a.getState().is(CNBlocks.REACTOR_CASING.get()))
                .where('I', a -> a.getState().is(CNBlocks.REACTOR_OUTPUT.get()))
                .getDistanceController(character);
    }

    public ReactorControllerBlock FindController(BlockPos blockPos, Level level, List<? extends Player> players, boolean first){ // Function that checks the surrounding blocks in order
        BlockPos newBlock;                                                   // to find the controller and verify the pattern
        Vec3i pos = new Vec3i(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        for (int y = pos.getY()-3; y != pos.getY()+4; y+=1) {
            for (int x = pos.getX()-5; x != pos.getX()+5; x+=1) {
                for (int z = pos.getZ()-5; z != pos.getZ()+5; z+=1) {
                    newBlock = new BlockPos(x, y, z);
                    if (level.getBlockState(newBlock).is(CNBlocks.REACTOR_CONTROLLER.get())) { // verifying the pattern
                        ReactorControllerBlock controller = (ReactorControllerBlock) level.getBlockState(newBlock).getBlock();
                        controller.Verify(level.getBlockState(newBlock), newBlock, level, players, first);
                        return controller;
                    }
                }
            }
        }
        return null;
    }

    public BlockPos FindOutputPos(BlockPos blockPos, Level level, List<? extends Player> players, boolean first){ // Function that checks the surrounding blocks in order
        Vec3i pos = new Vec3i(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        for (int y = pos.getY()-3; y != pos.getY()+4; y+=1) {
            for (int x = pos.getX()-5; x != pos.getX()+5; x+=1) {
                for (int z = pos.getZ()-5; z != pos.getZ()+5; z+=1) {
                    return new BlockPos(x, y, z);

                }
            }
        }
        return null;
    }
}
