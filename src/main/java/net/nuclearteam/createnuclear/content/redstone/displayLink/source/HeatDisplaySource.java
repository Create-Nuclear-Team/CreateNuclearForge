package net.nuclearteam.createnuclear.content.redstone.displayLink.source;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.NumericSingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.nuclearteam.createnuclear.content.multiblock.IHeat;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;
import net.nuclearteam.createnuclear.foundation.utility.CreateNuclearLang;

public class HeatDisplaySource extends NumericSingleLineDisplaySource {
    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (!(context.getSourceBlockEntity() instanceof ReactorControllerBlockEntity controller)) return ZERO.copy();

        boolean heatOriginal = context.sourceConfig().getInt("heat") == 0;
        boolean heatValue = context.sourceConfig().getInt("heat") == 1;
        boolean heatType = context.sourceConfig().getInt("heat") == 2;

        if (heatOriginal) {
            return CreateNuclearLang.number(controller.heat)
                .space()
                .translate("generic.unit.heat.value")
                .component();
        } else if (heatValue) {
            return CreateNuclearLang.number(Math.abs(controller.heat /4))
                .space()
                .translate("generic.unit.heat.rotation")
                .component();
        } else if (heatType) {
            return CreateNuclearLang
                .translateDirect("generic.unit.heat.type", IHeat.HeatLevel.of(controller.heat))
                .withStyle(IHeat.HeatLevel.of(controller.heat).getTextColor());
        } else {
            return null;
        }

    }

    @Override
    protected String getTranslationKey() {
        return "heat";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
        super.initConfigurationWidgets(context, builder, isFirstLine);
        if (isFirstLine) return;

        builder.addSelectionScrollInput(0, 95, (selectionScrollInput, label) -> selectionScrollInput
                .forOptions(CreateNuclearLang.translatedOptions("display_source.heat", "value", "rotation", "type")), "heat");
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }
}
