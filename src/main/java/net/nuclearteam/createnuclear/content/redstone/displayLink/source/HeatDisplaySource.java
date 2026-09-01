package net.nuclearteam.createnuclear.content.redstone.displayLink.source;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import net.minecraft.ChatFormatting;
import net.nuclearteam.createnuclear.content.multiblock.IHeat;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;

public class HeatDisplaySource extends AbstractReactorStatDisplaySource {

    @Override
    protected String getLabelKey() {
        return "display_source.reactor.heat";
    }

    @Override
    protected int getMax() {
        ReactorControllerBlockEntity controller = getControllerBlock();
        if (controller == null) {
            return ReactorDisplayConstants.MAX_HEAT;
        }

        return ReactorDisplayConstants.maxHeatForSize(controller.getMultiblockSize());
    }

    @Override
    protected ChatFormatting getColor(int value, ReactorControllerBlockEntity controller) {
        return IHeat.HeatLevel.of(value, controller.getMultiblockSize()).getTextColor();
    }

    @Override
    protected int computeValue(ReactorControllerBlockEntity controller, DisplayLinkContext context) {
        return (int) controller.getConfiguredPattern().getOrCreateTag().getDouble("heat");
    }

    @Override
    protected String getTranslationKey() {
        return "heat";
    }
}