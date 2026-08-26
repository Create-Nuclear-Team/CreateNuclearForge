package net.nuclearteam.createnuclear.content.multiblock;

import lib.multiblock.SimpleMultiBlockAislePatternBuilder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.nuclearteam.createnuclear.CNBlocks;
import net.nuclearteam.createnuclear.api.multiblock.MultiBlockManagerBeta;
import net.nuclearteam.createnuclear.api.multiblock.TypeMultiblock;

import java.util.function.Predicate;

public class CNMultiblock {
    public static final MultiBlockManagerBeta<TypeMultiblock> REGISTRATE_MULTIBLOCK = new MultiBlockManagerBeta<>();
    public static final String AAAAA = "AAAAA";
    public static final String AABAA = "AABAA";
    public static final String ADADA = "ADADA";
    public static final String BACAB = "BACAB";
    public static final String AAIAA = "AAIAA";
    public static final String AAAA = "AA*AA";
    public static final String AAOAA = "AAOAA";

    static {
        REGISTRATE_MULTIBLOCK.register("createnuclear:reactor",
                TypeMultiblock.REACTOR,
                SimpleMultiBlockAislePatternBuilder.start()
                        .aisle(AAAAA, AAAAA, AAAAA, AAAAA, AAAAA)
                        .aisle(AABAA, ADADA, BACAB, ADADA, AABAA)
                        .aisle(AABAA, ADADA, BACAB, ADADA, AABAA)
                        .aisle(AAIAA, ADADA, BACAB, ADADA, AAAA)
                        .aisle(AABAA, ADADA, BACAB, ADADA, AABAA)
                        .aisle(AABAA, ADADA, BACAB, ADADA, AABAA)
                        .aisle(AAAAA, AAAAA, AAAAA, AAAAA, AAOAA)
                        .where('A', stateIs(CNBlocks.REACTOR_CASING.get()))
                        .where('B', stateIs(CNBlocks.REACTOR_FRAME.get()))
                        .where('C', stateIs(CNBlocks.REACTOR_CORE.get()))
                        .where('D', stateIs(CNBlocks.REACTOR_COOLER.get()))
                        .where('*', stateIs(CNBlocks.REACTOR_CONTROLLER.get()))
                        .where('O', stateIs(CNBlocks.REACTOR_OUTPUT.get()))
                        .where('I', stateIs(CNBlocks.REACTOR_INPUT.get()))
                        .build()
        );
    }

    private static Predicate<BlockInWorld> stateIs(Block block) {
        return a -> {
            BlockState state = a.getState();
            return state != null && state.is(block);
        };
    }
}
