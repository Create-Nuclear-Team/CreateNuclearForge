package net.nuclearteam.createnuclear.content.redstone.displayLink.source;

import java.util.List;
import java.util.Optional;
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
import net.nuclearteam.createnuclear.content.multiblock.IHeat;
import net.nuclearteam.createnuclear.content.multiblock.MultiblockHelpers;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;
import net.nuclearteam.createnuclear.foundation.utility.CreateNuclearLang;
import joptsimple.internal.Strings;

public class ReactorSummaryDisplaySource extends DisplaySource {

    // --- CONSTANTES D'ERREUR (COMME LE BOILER) ---
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
        // Le réacteur a besoin de 6 lignes pour tout afficher proprement
        if (stats.maxRows() < 2) return notEnoughSpaceSingle;
        else if (stats.maxRows() < 6) return notEnoughSpaceDouble;

        if (context.getTargetBlockEntity() instanceof LecternBlockEntity) {
            Stream<MutableComponent> componentList = getComponents(context, false).map(components -> {
                return components.stream().reduce(MutableComponent::append).orElse(EMPTY_LINE);
            });
            return List.of(componentList.reduce((c1, c2) -> c1.append(Component.literal("\n")).append(c2)).orElse(EMPTY_LINE));
        }

        return getComponents(context, false).map(components -> {
            return components.stream().reduce(MutableComponent::append).orElse(EMPTY_LINE);
        }).toList();
    }

    @Override
    public List<List<MutableComponent>> provideFlapDisplayText(DisplayLinkContext context, DisplayTargetStats stats) {
        if (stats.maxRows() < 6) {
            context.flapDisplayContext = Boolean.FALSE;
            return notEnoughSpaceFlap;
        }

        List<List<MutableComponent>> components = getComponents(context, true).toList();

        // Vérification de la largeur horizontale (calculée comme le Boiler)
        if (stats.maxColumns() * FlapDisplaySection.MONOSPACE < 6 * FlapDisplaySection.MONOSPACE + components.get(1)
                .get(1).getString().length() * FlapDisplaySection.WIDE_MONOSPACE) {
            context.flapDisplayContext = Boolean.FALSE;
            return notEnoughSpaceFlap;
        }

        return components;
    }

    @Override
    public void loadFlapDisplayLayout(DisplayLinkContext context, FlapDisplayBlockEntity flapDisplay, FlapDisplayLayout layout, int lineIndex) {
        // Si erreur d'espace, on remet le layout par défaut
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

    private Stream<List<MutableComponent>> getComponents(DisplayLinkContext context, boolean forFlapDisplay) {
        ReactorControllerBlockEntity controller = MultiblockHelpers.getControllerForPart(context.level(), context.getSourcePos());
        if (controller == null) return Stream.of(EMPTY);

        // MODES : 0: Normal, 1: Value, 2: Percent, 3: Gauge
        int mode = context.sourceConfig().getInt("display_mode");

        int heat = (int) controller.getConfiguredPattern().getOrCreateTag().getDouble("heat");
        int fuel = controller.getBigFuelItem().count;
        int cooler = controller.getBigCoolerItem().count;
        int size = controller.getMultiblockSize();
        int fluid = controller.getBigFluidStack().isEmpty() ? 0 : (int) controller.getBigFluidStack().get(0).amount;

        MutableComponent statusLabel = labelOf("status");
        MutableComponent sizeLabel = labelOf("size");
        MutableComponent fuelLabel = labelOf("fuel");
        MutableComponent coolerLabel = labelOf("cooler");
        MutableComponent fluidLabel = labelOf("fluid");
        MutableComponent heatLabel = labelOf("heat");

        int lw = labelWidth();
        if (forFlapDisplay) {
            statusLabel = Component.literal(Strings.repeat(' ', lw - labelWidthOf("status"))).append(statusLabel);
            sizeLabel = Component.literal(Strings.repeat(' ', lw - labelWidthOf("size"))).append(sizeLabel);
            fuelLabel = Component.literal(Strings.repeat(' ', lw - labelWidthOf("fuel"))).append(fuelLabel);
            coolerLabel = Component.literal(Strings.repeat(' ', lw - labelWidthOf("cooler"))).append(coolerLabel);
            fluidLabel = Component.literal(Strings.repeat(' ', lw - labelWidthOf("fluid"))).append(fluidLabel);
            heatLabel = Component.literal(Strings.repeat(' ', lw - labelWidthOf("heat"))).append(heatLabel);
        }

        return Stream.of(
                // Statut
                List.of(statusLabel, controller.isAssembled() ?
                        CreateNuclearLang.translateDirect("display_source.reactor.active").withStyle(ChatFormatting.GOLD) :
                        CreateNuclearLang.translateDirect("display_source.reactor.idle").withStyle(ChatFormatting.GRAY)),
                // Taille
                List.of(sizeLabel, formatSize(size)),
                // Fuel
                List.of(fuelLabel, formatValue(fuel, 64, mode, false, ChatFormatting.GREEN)),
                // Cooler
                List.of(coolerLabel, formatValue(cooler, 64, mode, false, ChatFormatting.AQUA)),
                // Fluid (mB)
                List.of(fluidLabel, formatFluid(fluid, 16000, mode, ChatFormatting.BLUE)),
                // Chaleur
                List.of(heatLabel, formatValue(heat, 1000, mode, true, IHeat.HeatLevel.of(heat).getTextColor()))
        );
    }

    private MutableComponent formatSize(int size) {
        String key = size <= 5 ? "small" : size <= 7 ? "medium" : "large";
        return CreateNuclearLang.translateDirect("display_source.reactor.size." + key).withStyle(ChatFormatting.BLUE);
    }

    private MutableComponent formatFluid(int current, int max, int mode, ChatFormatting color) {
        if (mode == 0 || mode == 3) return drawGauge(current, max, color);
        if (mode == 2) return Component.literal((current * 100 / max) + "%").withStyle(color);

        return Component.literal(String.valueOf(current))
                .append(" ")
                .append(CreateNuclearLang.translateDirect("generic.unit.fluid.value"))
                .withStyle(color);
    }

    private MutableComponent formatValue(int current, int max, int mode, boolean gaugeOnNormal, ChatFormatting color) {
        if (mode == 3 || (mode == 0 && gaugeOnNormal)) return drawGauge(current, max, color);
        if (mode == 2) return Component.literal((current * 100 / max) + "%").withStyle(color);
        return Component.literal(String.valueOf(current)).withStyle(color);
    }

    private MutableComponent drawGauge(int current, int max, ChatFormatting color) {
        int width = 10;
        int filled = (int) (Mth.clamp((float) current / max, 0, 1) * width);
        return Component.literal("█".repeat(filled) + "░".repeat(width - filled)).withStyle(color);
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