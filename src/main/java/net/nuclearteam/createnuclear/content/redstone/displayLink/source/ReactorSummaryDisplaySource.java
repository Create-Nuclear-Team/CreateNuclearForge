package net.nuclearteam.createnuclear.content.redstone.displayLink.source;

import java.util.List;
import java.util.stream.Stream;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.content.trains.display.FlapDisplayBlockEntity;
import com.simibubi.create.content.trains.display.FlapDisplayLayout;
import com.simibubi.create.content.trains.display.FlapDisplaySection;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.nuclearteam.createnuclear.api.multiblock.rods.RodType.TypeRodPredicate;
import net.nuclearteam.createnuclear.content.multiblock.IHeat;
import net.nuclearteam.createnuclear.content.multiblock.MultiblockHelpers;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;
import net.nuclearteam.createnuclear.foundation.utility.CreateNuclearLang;
import joptsimple.internal.Strings;

public class ReactorSummaryDisplaySource extends DisplaySource {

    public static final List<MutableComponent> notEnoughSpaceSingle =
            List.of(CreateNuclearLang.translateDirect("display_source.reactor.not_enough_space")
                    .append(CreateNuclearLang.translateDirect("display_source.reactor.for_reactor_status")));

    public static final List<MutableComponent> notEnoughSpaceDouble =
            List.of(CreateNuclearLang.translateDirect("display_source.reactor.not_enough_space"),
                    CreateNuclearLang.translateDirect("display_source.reactor.for_reactor_status"));

    public static final List<List<MutableComponent>> notEnoughSpaceFlap =
            List.of(List.of(CreateNuclearLang.translateDirect("display_source.reactor.not_enough_space")),
                    List.of(CreateNuclearLang.translateDirect("display_source.reactor.for_reactor_status")));

    @Override
    public List<MutableComponent> provideText(DisplayLinkContext context, DisplayTargetStats stats) {
        if (stats.maxRows() < 2) return notEnoughSpaceSingle;
        if (stats.maxRows() < 6) return notEnoughSpaceDouble;

        int gaugeWidth = (stats.maxColumns() >= 80) ? 10 : 6;

        List<List<MutableComponent>> components = getComponents(context, false, gaugeWidth).toList();

        // Sécurité : Si le contrôleur est absent, on affiche juste le message d'erreur (index 0)
        if (components.size() < 6) {
            return List.of(components.get(0).stream().reduce(MutableComponent::append).orElse(EMPTY_LINE));
        }

        if (context.getTargetBlockEntity() instanceof LecternBlockEntity) {
            Stream<MutableComponent> componentList = components.stream().map(list -> list.stream().reduce(MutableComponent::append).orElse(EMPTY_LINE));
            return List.of(componentList.reduce((c1, c2) -> c1.append(Component.literal("\n")).append(c2)).orElse(EMPTY_LINE));
        }

        return components.stream().map(list -> list.stream().reduce(MutableComponent::append).orElse(EMPTY_LINE)).toList();
    }

    @Override
    public List<List<MutableComponent>> provideFlapDisplayText(DisplayLinkContext context, DisplayTargetStats stats) {
        if (stats.maxRows() < 6) {
            context.flapDisplayContext = Boolean.FALSE;
            return notEnoughSpaceFlap;
        }

        int gaugeWidth = 6;
        List<List<MutableComponent>> components = getComponents(context, true, gaugeWidth).toList();

        // --- FIX DU CRASH ---
        // On vérifie que le contrôleur est bien présent (liste complète de 6 éléments)
        // avant de tenter le calcul de largeur sur l'index 2.
        if (components.size() < 6) {
            return components;
        }

        if (stats.maxColumns() * FlapDisplaySection.MONOSPACE < 6 * FlapDisplaySection.MONOSPACE + components.get(2)
                .get(1).getString().length() * FlapDisplaySection.WIDE_MONOSPACE) {
            context.flapDisplayContext = Boolean.FALSE;
            return notEnoughSpaceFlap;
        }

        return components;
    }

    @Override
    public void loadFlapDisplayLayout(DisplayLinkContext context, FlapDisplayBlockEntity flapDisplay, FlapDisplayLayout layout, int lineIndex) {
        if (lineIndex == 0 || context.flapDisplayContext instanceof Boolean b && !b) {
            if (layout.isLayout("Default")) return;
            layout.loadDefault(flapDisplay.getMaxCharCount());
            return;
        }

        String layoutKey = "Reactor";
        if (layout.isLayout(layoutKey)) return;

        int lw = labelWidth();
        int labelLength = (int) (lw * FlapDisplaySection.MONOSPACE);
        float maxSpace = flapDisplay.getMaxCharCount(1) * FlapDisplaySection.MONOSPACE;

        FlapDisplaySection label = new FlapDisplaySection(labelLength, "alphabet", false, true);
        FlapDisplaySection symbols = new FlapDisplaySection(maxSpace - labelLength, "pixel", false, false).wideFlaps();

        layout.configure(layoutKey, List.of(label, symbols));
    }

    private Stream<List<MutableComponent>> getComponents(DisplayLinkContext context, boolean forFlapDisplay, int gaugeWidth) {
        ReactorControllerBlockEntity controller = MultiblockHelpers.getControllerForPart(context.level(), context.getSourcePos());

        // Sécurité Triple-Check : null, removed ou non assemblé
        if (controller == null || controller.isRemoved()) {
            return Stream.of(List.of(CreateNuclearLang.translateDirect("display_source.reactor.no_controller")));
        }

        int mode = context.sourceConfig().getInt("display_mode");

        int heat = (int) controller.getConfiguredPattern().getOrCreateTag().getDouble("heat");
        int fuel = 0;
        int cooler = 0;
        if (controller.getDisplayState() != null && controller.getDisplayState().items() != null) {
            for (var entry : controller.getDisplayState().items().entrySet()) {
                if (TypeRodPredicate.isFuel(entry.getKey().getDefaultInstance(), context.level())) {
                    fuel += entry.getValue();
                } else if (TypeRodPredicate.isCooled(entry.getKey().getDefaultInstance(), context.level())) {
                    cooler += entry.getValue();
                }
            }
        }
        int size = controller.getMultiblockSize();
        var fluidList = controller.getBigFluidStack();
        int fluid = (fluidList != null && !fluidList.isEmpty() && fluidList.get(0) != null) ? (int) fluidList.get(0).amount : 0;

        int lw = labelWidth();
        MutableComponent lStatus = padLabel("status", lw).append(" ");
        MutableComponent lSize   = padLabel("size", lw).append(" ");
        MutableComponent lFuel   = padLabel("fuel", lw).append(" ");
        MutableComponent lCooler = padLabel("cooler", lw).append(" ");
        MutableComponent lFluid  = padLabel("fluid", lw).append(" ");
        MutableComponent lHeat   = padLabel("heat", lw).append(" ");

        return Stream.of(
                List.of(lStatus, controller.isAssembled() ?
                        CreateNuclearLang.translateDirect("display_source.reactor.active").withStyle(ChatFormatting.GOLD) :
                        CreateNuclearLang.translateDirect("display_source.reactor.idle").withStyle(ChatFormatting.GRAY)),
                List.of(lSize, formatSize(size)),
                List.of(lFuel,   formatValue(fuel, 64, mode, false, ChatFormatting.GREEN, gaugeWidth)),
                List.of(lCooler, formatValue(cooler, 64, mode, false, ChatFormatting.AQUA, gaugeWidth)),
                List.of(lFluid,  formatFluid(fluid, 16000, mode, ChatFormatting.BLUE, gaugeWidth)),
                List.of(lHeat,   formatValue(heat, 1000, mode, true, IHeat.HeatLevel.of(heat, controller.getMultiblockSize()).getTextColor(), gaugeWidth))
        );
    }

    private MutableComponent padLabel(String key, int lw) {
        return Component.literal(Strings.repeat(' ', lw - labelWidthOf(key))).append(labelOf(key));
    }

    private MutableComponent formatSize(int size) {
        String key = size <= 5 ? "small" : size <= 7 ? "medium" : "large";
        return CreateNuclearLang.translateDirect("display_source.reactor.size." + key).withStyle(ChatFormatting.BLUE);
    }

    private MutableComponent formatFluid(int current, int max, int mode, ChatFormatting color, int width) {
        if (mode == 0 || mode == 3) return drawGauge(current, max, color, width);
        if (mode == 2) return Component.literal((current * 100 / max) + "%").withStyle(color);
        return Component.literal(String.valueOf(current)).append(" ").append(CreateNuclearLang.translateDirect("generic.unit.fluid.value")).withStyle(color);
    }

    private MutableComponent formatValue(int current, int max, int mode, boolean gaugeOnNormal, ChatFormatting color, int width) {
        if (mode == 3 || (mode == 0 && gaugeOnNormal)) return drawGauge(current, max, color, width);
        if (mode == 2) return Component.literal((current * 100 / max) + "%").withStyle(color);
        return Component.literal(String.valueOf(current)).withStyle(color);
    }

    private MutableComponent drawGauge(int current, int max, ChatFormatting color, int width) {
        int filled = (int) (Mth.clamp((float) current / max, 0, 1) * width);
        return Component.literal("█".repeat(filled) + "▒".repeat(Math.max(0, width - filled))).withStyle(color);
    }

    private int labelWidth() {
        return Stream.of("status", "size", "fuel", "cooler", "fluid", "heat").mapToInt(this::labelWidthOf).max().orElse(0);
    }

    private int labelWidthOf(String label) {
        return labelOf(label).getString().length();
    }

    private MutableComponent labelOf(String label) {
        return CreateNuclearLang.translateDirect("display_source.reactor." + label);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
        if (isFirstLine) return;
        builder.addSelectionScrollInput(0, 100, (selectionScrollInput, label) -> selectionScrollInput
                .forOptions(CreateNuclearLang.translatedOptions("display_source.reactor.mode", "normal", "value", "percent", "gauge")), "display_mode");
    }

    @Override protected String getTranslationKey() { return "reactor_summary"; }
}