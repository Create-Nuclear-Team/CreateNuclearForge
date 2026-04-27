package net.nuclearteam.createnuclear.content.redstone.displayLink.source;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;
import net.nuclearteam.createnuclear.content.multiblock.IHeat;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;
import net.nuclearteam.createnuclear.content.multiblock.MultiblockHelpers;
import net.nuclearteam.createnuclear.foundation.utility.CreateNuclearLang;

import java.util.ArrayList;
import java.util.List;

public class ReactorSummaryDisplaySource extends DisplaySource {

    @Override
    public List<MutableComponent> provideText(DisplayLinkContext context, DisplayTargetStats stats) {
        // Utilisation de ton helper
        ReactorControllerBlockEntity controller = MultiblockHelpers.getControllerForPart(context.level(), context.getSourcePos());

        if (controller == null) {
            return List.of(CreateNuclearLang.translate("display_source.reactor_summary.no_controller")
                    .component()
                    .withStyle(ChatFormatting.RED));
        }

        List<MutableComponent> lines = new ArrayList<>();

        // 1. LIGNE ÉTAT (Status: ACTIVE/IDLE)
        boolean assembled = controller.isAssembled();
        MutableComponent statusLabel = CreateNuclearLang.translate("display_source.reactor_summary.status").component();
        MutableComponent statusValue = assembled ?
                CreateNuclearLang.translate("display_source.reactor_summary.active").component().withStyle(ChatFormatting.GOLD) :
                CreateNuclearLang.translate("display_source.reactor_summary.idle").component().withStyle(ChatFormatting.GRAY);

        lines.add(statusLabel.append(" ").append(statusValue));

        // 2. LIGNE TEMPÉRATURE
        int heat = (int) controller.getConfiguredPattern().getOrCreateTag().getDouble("heat");
        IHeat.HeatLevel heatLevel = IHeat.HeatLevel.of(heat);

        lines.add(CreateNuclearLang.translate("display_source.reactor_summary.temperature")
                .space()
                .add(CreateNuclearLang.number(heat).component().withStyle(heatLevel.getTextColor()))
                .space()
                .translate("generic.unit.heat.value")
                .component());

        // 3. LIGNE CARBURANT
        int fuelRods = controller.getBigFuelItem().count;
        lines.add(CreateNuclearLang.translate("display_source.reactor_summary.fuel")
                .space()
                .add(CreateNuclearLang.number(fuelRods).component().withStyle(ChatFormatting.GREEN))
                .component());

        // 4. LIGNE LIQUIDE (si présent)
        if (!controller.getBigFluidStack().isEmpty()) {
            var fluid = controller.getBigFluidStack().get(0);
            lines.add(CreateNuclearLang.translate("display_source.reactor_summary.coolant")
                    .space()
                    .add(CreateNuclearLang.number(fluid.amount).component().withStyle(ChatFormatting.AQUA))
                    .space()
                    .translate("generic.unit.fluid.value")
                    .component());
        }

        return lines;
    }

    @Override
    protected String getTranslationKey() {
        return "reactor_summary";
    }
}