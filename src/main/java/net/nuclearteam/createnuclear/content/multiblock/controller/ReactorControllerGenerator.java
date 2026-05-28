package net.nuclearteam.createnuclear.content.multiblock.controller;

import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
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
        // Sécurité si l'état est foireux au tout début du scan de Minecraft
        if (state == null || !state.hasProperty(ReactorControllerBlock.ASSEMBLED) || !state.hasProperty(ReactorControllerBlock.ACTIVE)) {
            return prov.models().cubeAll("block/reactor/controller/reactor_controller_off", prov.modLoc("block/reactor/controller/controller_panel_off"));
        }

        // Récupération de nos deux booléens natifs
        boolean assembled = state.getValue(ReactorControllerBlock.ASSEMBLED);
        boolean active = state.getValue(ReactorControllerBlock.ACTIVE);

        // Déduction de l'état visuel
        String suffix = "off";
        if (assembled) {
            suffix = active ? "on" : "standby";
        }

        // Génération automatique des fichiers comme avant
        String modelName = "block/reactor/controller/reactor_controller_" + suffix;
        String texturePath = "block/reactor/controller/controller_panel_" + suffix;

        return prov.models().cubeAll(modelName, prov.modLoc(texturePath));
    }
}