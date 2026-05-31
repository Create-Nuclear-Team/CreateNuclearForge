package net.nuclearteam.createnuclear.content.multiblock.controller;

import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;

public class ReactorControllerGenerator extends SpecialBlockStateGen {

    @Override
    protected int getXRotation(BlockState state) {
        return 0;
    }

    @Override
    protected int getYRotation(BlockState state) {
        if (state == null || !state.hasProperty(ReactorControllerBlock.FACING)) {
            return 0;
        }
        return horizontalAngle(state.getValue(ReactorControllerBlock.FACING));
    }

    @Override
    public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov, BlockState state) {
        final String controllerPath = "block/reactor/controller/controller_panel_";

        // Safety fallback if the state is invalid at the start of Minecraft's block scan
        if (!state.hasProperty(ReactorControllerBlock.ASSEMBLED) || !state.hasProperty(ReactorControllerBlock.ACTIVE)) {
            return prov.models()
                    .withExistingParent("block/reactor/controller/" + ControllerVisualState.OFF.getSuffix(), prov.modLoc("block/reactor/controller/block"))
                    .texture("1", prov.modLoc(controllerPath + ControllerVisualState.OFF.getSuffix()))
                    .texture("particle", prov.modLoc(controllerPath + ControllerVisualState.OFF.getSuffix()));
        }

        boolean isAssembled = state.getValue(ReactorControllerBlock.ASSEMBLED);
        boolean isActive = state.getValue(ReactorControllerBlock.ACTIVE);

        ControllerVisualState controllerState = ControllerVisualState.OFF;
        if (isAssembled) {
            controllerState = isActive ? ControllerVisualState.ON : ControllerVisualState.STANDBY;
        }

        return prov.models()
                .withExistingParent("block/reactor/controller/" + controllerState.getSuffix(), prov.modLoc("block/reactor/controller/block"))
                .texture("1", prov.modLoc(controllerPath + controllerState.getSuffix()))
                .texture("particle", prov.modLoc(controllerPath + controllerState.getSuffix()));
    }

    enum ControllerVisualState {
        OFF("off"),
        ON("on"),
        STANDBY("standby")
        ;

        private final String suffix;

        ControllerVisualState(String suffix) {
            this.suffix = suffix;
        }

        public String getSuffix() {
            return this.suffix;
        }
    }
}