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

public class CoolerDisplaySource extends NumericSingleLineDisplaySource {

    @Override
    protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        ReactorControllerBlockEntity controller = MultiblockHelpers.getControllerForPart(context.level(), context.getSourcePos());
        if (controller == null || controller.isRemoved()) return ZERO.copy();

        // Label + Espace
        MutableComponent label = CreateNuclearLang.translateDirect("display_source.reactor.cooler").append(" ");

        int mode = context.sourceConfig().getInt("display_mode");
        int cooler = 0;
        if (controller.getDisplayState() != null && controller.getDisplayState().items() != null) {
            for (var entry : controller.getDisplayState().items().entrySet()) {
                if (net.nuclearteam.createnuclear.api.multiblock.rods.RodType.TypeRodPredicate.IS_COOLED.test(entry.getKey().getDefaultInstance())) {
                    cooler += entry.getValue();
                }
            }
        }
        int maxCooler = 64;

        return label.append(switch (mode) {
            case 1 -> Component.literal((cooler * 100 / maxCooler) + "%").withStyle(ChatFormatting.AQUA);
            case 2 -> {
                int gaugeWidth = 6;
                yield drawGauge(cooler, maxCooler, ChatFormatting.AQUA, gaugeWidth);
            }
            default -> Component.literal(String.valueOf(cooler)).withStyle(ChatFormatting.AQUA);
        });
    }

    private MutableComponent drawGauge(int current, int max, ChatFormatting color, int width) {
        int filled = (int) (Mth.clamp((float) current / max, 0, 1) * width);
        return Component.literal("█".repeat(filled) + "▒".repeat(Math.max(0, width - filled))).withStyle(color);
    }

    @Override protected String getTranslationKey() { return "cooler"; }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
        if (isFirstLine) return;
        builder.addSelectionScrollInput(0, 100, (selectionScrollInput, l) -> selectionScrollInput
                .forOptions(CreateNuclearLang.translatedOptions("display_source.reactor.mode", "value", "percent", "gauge")), "display_mode");
    }

    @Override protected boolean allowsLabeling(DisplayLinkContext context) { return true; }
}