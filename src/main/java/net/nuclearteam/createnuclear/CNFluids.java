package net.nuclearteam.createnuclear;

import com.simibubi.create.AllFluids;
import com.tterrag.registrate.builders.FluidBuilder.FluidTypeFactory;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.createmod.catnip.theme.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fluids.FluidInteractionRegistry;
import net.minecraftforge.fluids.FluidInteractionRegistry.InteractionInformation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.nuclearteam.createnuclear.content.decoration.palettes.CNPaletteStoneTypes;
import net.nuclearteam.createnuclear.foundation.advancement.CNAdvancement;
import net.nuclearteam.createnuclear.content.radiation.RadiationBucketItem;

import org.joml.Vector3f;
import net.nuclearteam.createnuclear.CNTags.CNFluidTags;

import java.util.List;
import java.util.function.Supplier;

public class CNFluids {
    public static final FluidEntry<ForgeFlowingFluid.Flowing> URANIUM =
        CreateNuclear.REGISTRATE.standardFluid("uranium", SolidRenderedPlaceableFluidtype.create(0x38FF08, () -> 1f / 32f))
            .lang("Liquid Uranium")
            .tag(CNFluidTags.URANIUM.tag)
            .properties(p -> p.viscosity(2500)
                .density(1600)
                .canSwim(false)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
                .canDrown(false)
            )
            .fluidProperties(f -> f.levelDecreasePerBlock(2)
                .tickRate(15)
                .slopeFindDistance(6)
                .explosionResistance(100f)
            )
            .source(ForgeFlowingFluid.Source::new)
            .bucket((s, p) -> new RadiationBucketItem(s, p, 20))
            .onRegister(CNFluids::registerFluidDispenseBehavior)
            .tag(CNTags.forgeItemTag("buckets/uranium"))
            .lang("Uranium Bucket")
            .build()
            .register();

    public static final FluidEntry<ForgeFlowingFluid.Flowing> THORIUM =
            CreateNuclear.REGISTRATE.standardFluid("thorium", SolidRenderedPlaceableFluidtype.create(0x38f9ff, () -> 1f / 32f))
                    .lang("Liquid Thorium")
                    .tag(CNFluidTags.THORIUM.tag)
                    .properties(p -> p.viscosity(200)
                            .density(100)
                            .canSwim(false)
                            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
                            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
                            .canDrown(false)
                    )
                    .fluidProperties(f -> f.levelDecreasePerBlock(2)
                            .tickRate(15)
                            .slopeFindDistance(6)
                            .explosionResistance(100f)
                    )
                    .source(ForgeFlowingFluid.Source::new)
                    .bucket()
                    .onRegister(CNFluids::registerFluidDispenseBehavior)
                    .tag(CNTags.forgeItemTag("buckets/thorium"))
                    .lang("Thorium Bucket")
                    .build()
                    .register();

    public static final FluidEntry<ForgeFlowingFluid.Flowing> LIQUID_NITROGEN =
        CreateNuclear.REGISTRATE.standardFluid("nitrogen", SolidRenderedPlaceableFluidtype.create(0x23ECD5, () -> 1f / 16f))
            .lang("Liquid Nitrogen")
            .tag(CNFluidTags.NITROGEN.tag)
            .properties(p -> p.viscosity(1000)
                .density(1000)
                .canSwim(true)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_AXOLOTL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_FISH)
                .canDrown(false)
            )
            .fluidProperties(f -> f.levelDecreasePerBlock(5)
                .tickRate(10)
                .slopeFindDistance(6)
                .explosionResistance(100f)
            )
            .source(ForgeFlowingFluid.Source::new)
            //.onRegister(ReactorFluidTypesValue.setReactorFluidTypeInfos(8196, 100))
            .bucket()
            .onRegister(CNFluids::registerFluidDispenseBehavior)
            .lang("Nitrogen Bucket")
            .tag(CNTags.forgeItemTag("buckets/nitrogen"))
            .build()
            .register();


    public static void register() {}

    public static void handleFluidEffect(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.isAlive() || entity.isSpectator()) return;

        Level level = entity.level();

        // 1. EFFET DE L'URANIUM (Radiation)
        if (entity.isInFluidType(URANIUM.getType())) {
            if (entity.tickCount % 20 == 0) {
                entity.addEffect(new MobEffectInstance(CNEffects.RADIATION.get(), 100, 0));
            }
        }

        // 2. EFFET DE L'AZOTE LIQUIDE (Gel type Neige Poudreuse)
        else if (entity.isInFluidType(LIQUID_NITROGEN.getType())) {
            // PROBLÈME 1 RÉSOLU : Si l'entité est en feu, on l'éteint immédiatement
            if (entity.isOnFire()) {
                entity.clearFire();
            }

            if (entity instanceof  Player player && player.isSwimming()) {
                CNAdvancement.CRYOGENIC_BAPTISM.awardTo(player);
            }

            int currentTicks = entity.getTicksFrozen();
            int maxTicks = entity.getTicksRequiredToFreeze();
            int freezeSpeed = 3;

            if (level.isClientSide) {
                // On vise maxTicks + 1 pour que la baisse de -1 du client ramène pile à maxTicks
                entity.setTicksFrozen(Math.min(maxTicks + 1, currentTicks + freezeSpeed + 1));
            } else {
                // On vise maxTicks + 2 pour que la baisse de -2 du serveur ramène pile à maxTicks
                entity.setTicksFrozen(Math.min(maxTicks + 2, currentTicks + freezeSpeed + 2));

                // PROBLÈME 2 RÉSOLU : On vérifie le gel APRÈS modification ou avec la compensation
                if (entity.getTicksFrozen() >= maxTicks && entity.tickCount % 10 == 0) {
                    entity.hurt(entity.damageSources().freeze(), 2.0F);
                }

                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, true, false, false));
            }
        }

        // 3. EFFET DU THORIUM (Brûlure)
        else if (entity.isInFluidType(THORIUM.getType())) {
            entity.lavaHurt();
        }
    }

    public static void registerFluidInteractions() {
        // Supplier for the common BlockState to return (Autunite)
        Supplier<BlockState> autuniteState = () -> CNPaletteStoneTypes.AUTUNITE.getBaseBlock().get().defaultBlockState();

        // The FluidType that all interactions will target (uranium)
        FluidType uraniumType = URANIUM.get().getFluidType();

        // List of source FluidTypes we want to register (lava and water)
        List<FluidType> sourceFluids = List.of(
            ForgeMod.LAVA_TYPE.get(),
            ForgeMod.WATER_TYPE.get()
        );

        // Loop over each source fluid and register the interaction
        for (FluidType source : sourceFluids) {
            FluidInteractionRegistry.addInteraction(source, new InteractionInformation(uraniumType, fs -> autuniteState.get()));
        }
    }

    private static class SolidRenderedPlaceableFluidtype extends AllFluids.TintedFluidType {

        private Vector3f fogColor;
        private int fogIntColor;
        private Supplier<Float> fogDistance;

        public static FluidTypeFactory create(int fogColor, Supplier<Float> fogDistance) {
            return (p, s, f) -> {
                SolidRenderedPlaceableFluidtype fluidtype = new SolidRenderedPlaceableFluidtype(p,s,f);
                fluidtype.fogColor = new Color(fogColor, false).asVectorF();
                fluidtype.fogIntColor = fogColor;
                fluidtype.fogDistance = fogDistance;
                return fluidtype;
            };
        }


        private SolidRenderedPlaceableFluidtype(Properties properties, ResourceLocation stillTecture, ResourceLocation flowingTexture) {
            super(properties, stillTecture, flowingTexture);
        }

        @Override
        protected int getTintColor(FluidStack stack) {
            return NO_TINT;
        }

        @Override
        protected int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
            return fogIntColor;
        }

        @Override
        protected Vector3f getCustomFogColor() {
            return fogColor;
        }

        @Override
        protected float getFogDistanceModifier() {
            return fogDistance.get();
        }
    }

    private static final DispenseItemBehavior DEFAULT = new DefaultDispenseItemBehavior();
    private static final DispenseItemBehavior DISPENSE_FLUID = new DefaultDispenseItemBehavior(){
        @Override
        protected ItemStack execute(BlockSource pSource, ItemStack pStack) {
            DispensibleContainerItem dispensibleContainerItem = (DispensibleContainerItem) pStack.getItem();
            BlockPos pos = pSource.getPos().relative(pSource.getBlockState().getValue(DispenserBlock.FACING));
            Level level = pSource.getLevel();
            if (dispensibleContainerItem.emptyContents(null, level, pos, null, pStack)) {
                return new ItemStack(Items.BUCKET);
            }
            return DEFAULT.dispense(pSource, pStack);
        }
    };

    private static void registerFluidDispenseBehavior(BucketItem bucket) {
        DispenserBlock.registerBehavior(bucket, DISPENSE_FLUID);
    }
}
