package net.nuclearteam.createnuclear;


import com.mojang.logging.LogUtils;
import com.simibubi.create.CreateBuildInfo;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import net.nuclearteam.createnuclear.content.kinetics.fan.processing.CNFanProcessingTypes;
import net.nuclearteam.createnuclear.infrastructure.data.CreateNuclearDatagen;
import org.slf4j.Logger;

@Mod(CreateNuclear.MOD_ID)
public class CreateNuclear {
    public static final String MOD_ID = "createnuclear";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID)
           .defaultCreativeTab((ResourceKey<CreativeModeTab>) null);

    static {
        REGISTRATE.setTooltipModifierFactory(item -> new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                .andThen(TooltipModifier.mapNull(KineticStats.create(item))));
    }

    public CreateNuclear() {
        onInitialize();
    }

    public static void onInitialize() {
        LOGGER.info("{} {} initializing! Commit hash: {}", MOD_ID, CreateBuildInfo.VERSION, CreateBuildInfo.GIT_COMMIT);

        ModLoadingContext modLoadingContext = ModLoadingContext.get();

        IEventBus modEventBus = FMLJavaModLoadingContext.get()
                .getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

        REGISTRATE.registerEventListeners(modEventBus);


        CNTags.init();
        CNBlocks.register();
        CNBlockEntityTypes.register();
        CNItems.register();
        CNPackets.registerPackets();
        CNMenus.register();
        CNFluids.register();
        CNEntityType.register();

        CNCreativeModeTabs.register(modEventBus);
        CNEffects.register(modEventBus);
        CNPotions.register(modEventBus);
        CNRecipeTypes.register(modEventBus);

        modEventBus.addListener(CreateNuclear::init);
        modEventBus.addListener(CreateNuclear::onRegister);
        modEventBus.addListener(EventPriority.LOWEST, CreateNuclearDatagen::gatherData);
        forgeEventBus.addListener(CNFluids::handleFluidEffect);


        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> CreateNuclearClient.onCtorClient(modEventBus, forgeEventBus));
    }

    public static void init(final FMLCommonSetupEvent event) {
        event.enqueueWork(CNPotions::registerPotionsRecipes);
    }

    public static void onRegister(final RegisterEvent event) {
        CNFanProcessingTypes.register();

    }


    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

}
