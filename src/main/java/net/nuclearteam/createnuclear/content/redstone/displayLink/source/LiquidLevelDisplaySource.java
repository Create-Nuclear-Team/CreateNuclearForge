package net.nuclearteam.createnuclear.content.redstone.displayLink.source;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.NumericSingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.network.chat.MutableComponent;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;
import net.nuclearteam.createnuclear.foundation.utility.CreateNuclearLang;

public class LiquidLevelDisplaySource extends NumericSingleLineDisplaySource {
    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (!(context.getSourceBlockEntity() instanceof ReactorControllerBlockEntity controller)) return ZERO.copy();

        return CreateNuclearLang.translate("generic.unit.liquid").component();
    }

    @Override
    protected String getTranslationKey() {
        return "liquid";
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }
}
