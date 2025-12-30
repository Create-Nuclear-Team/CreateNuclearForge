package net.nuclearteam.createnuclear.content.redstone.displayLink.source;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.NumericSingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;
import net.nuclearteam.createnuclear.foundation.utility.CreateNuclearLang;

public class PatternCountRodsDisplaySource extends NumericSingleLineDisplaySource {
    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (!(context.getSourceBlockEntity() instanceof ReactorControllerBlockEntity controller)) return ZERO.copy();

        boolean typeRod = context.sourceConfig().getInt("typeRod") == 0;

        int countRod = typeRod ? controller.countUraniumRod : controller.countGraphiteRod;

        return CreateNuclearLang
                .number(countRod)
                .space()
                .translate("generic.unit.count.rod", typeRod ? "Fuel" : "Cooled")
                .component();
    }

    @Override
    protected String getTranslationKey() {
        return "reactor_count_rod";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
        super.initConfigurationWidgets(context, builder, isFirstLine);
        if (isFirstLine) return;

        builder.addSelectionScrollInput(0, 95, (selectionScrollInput, label) -> selectionScrollInput
                .forOptions(CreateNuclearLang.translatedOptions("display_source.reactor_controller_count_rod", "fuel", "cooled")), "typeRod");
    }

    @Override
    protected boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }
}
