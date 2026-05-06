package net.nuclearteam.createnuclear.content.redstone.displayLink.source;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.NumericSingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.nuclearteam.createnuclear.content.multiblock.MultiblockHelpers;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;
import net.nuclearteam.createnuclear.foundation.utility.CreateNuclearLang;

public class FuelDisplaySource extends NumericSingleLineDisplaySource {

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        ReactorControllerBlockEntity controller = MultiblockHelpers.getControllerForPart(context.level(), context.getSourcePos());
        if (controller == null || controller.isRemoved()) return ZERO.copy();

        // Label + Espace
        MutableComponent label = CreateNuclearLang.translateDirect("display_source.reactor.fuel").append(" ");

        int mode = context.sourceConfig().getInt("display_mode");
        var fuelStack = controller.getBigFuelItem();
        int fuel = (fuelStack != null) ? fuelStack.count : 0;
        int maxFuel = 64;

        return label.append(switch (mode) {
            case 1 -> Component.literal((fuel * 100 / maxFuel) + "%").withStyle(ChatFormatting.GREEN);
            case 2 -> {
                int gaugeWidth = 6;
                yield drawGauge(fuel, maxFuel, ChatFormatting.GREEN, gaugeWidth);
            }
            default -> Component.literal(String.valueOf(fuel)).withStyle(ChatFormatting.GREEN);
        });
    }

    private MutableComponent drawGauge(int current, int max, ChatFormatting color, int width) {
        int filled = (int) (Mth.clamp((float) current / max, 0, 1) * width);
        return Component.literal("█".repeat(filled) + "▒".repeat(Math.max(0, width - filled))).withStyle(color);
    }

    @Override protected String getTranslationKey() { return "fuel"; }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
        if (isFirstLine) return;
        builder.addSelectionScrollInput(0, 100, (selectionScrollInput, l) -> selectionScrollInput
                .forOptions(CreateNuclearLang.translatedOptions("display_source.reactor.mode", "value", "percent", "gauge")), "display_mode");
    }

    @Override protected boolean allowsLabeling(DisplayLinkContext context) { return true; }
}