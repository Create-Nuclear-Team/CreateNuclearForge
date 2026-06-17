package net.nuclearteam.createnuclear.content.redstone.displayLink.source;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.NumericSingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.nuclearteam.createnuclear.content.multiblock.IHeat;
import net.nuclearteam.createnuclear.content.multiblock.MultiblockHelpers;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;
import net.nuclearteam.createnuclear.foundation.utility.CreateNuclearLang;

public class HeatDisplaySource extends NumericSingleLineDisplaySource {

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        ReactorControllerBlockEntity controller = MultiblockHelpers.getControllerForPart(context.level(), context.getSourcePos());
        if (controller == null || controller.isRemoved()) return ZERO.copy();

        // Ajout du Label + Espace
        MutableComponent label = CreateNuclearLang.translateDirect("display_source.reactor.heat").append(" ");

        int mode = context.sourceConfig().getInt("display_mode");
        int heat = (int) controller.getConfiguredPattern().getOrCreateTag().getDouble("heat");
        int maxHeat = 1000;

        return label.append(switch (mode) {
            case 1 -> Component.literal((heat * 100 / maxHeat) + "%")
                    .withStyle(IHeat.HeatLevel.of(heat, controller.getMultiblockSize()).getTextColor());

            case 2 -> {
                // Largeur réduite pour laisser de la place au label
                int gaugeWidth = 6;
                yield drawGauge(heat, maxHeat, IHeat.HeatLevel.of(heat, controller.getMultiblockSize()).getTextColor(), gaugeWidth);
            }

            default -> Component.literal(String.valueOf(heat))
                    .append(CreateNuclearLang.translateDirect("generic.unit.heat.value"))
                    .withStyle(IHeat.HeatLevel.of(heat, controller.getMultiblockSize()).getTextColor());
        });
    }

    private MutableComponent drawGauge(int current, int max, net.minecraft.ChatFormatting color, int width) {
        int filled = (int) (Mth.clamp((float) current / max, 0, 1) * width);
        int empty = Math.max(0, width - filled);
        return Component.literal("█".repeat(filled) + "▒".repeat(empty)).withStyle(color);
    }

    @Override protected String getTranslationKey() { return "heat"; }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
        if (isFirstLine) return;
        builder.addSelectionScrollInput(0, 100, (selectionScrollInput, l) -> selectionScrollInput
                .forOptions(CreateNuclearLang.translatedOptions("display_source.reactor.mode", "value", "percent", "gauge")), "display_mode");
    }

    @Override protected boolean allowsLabeling(DisplayLinkContext context) { return true; }
}