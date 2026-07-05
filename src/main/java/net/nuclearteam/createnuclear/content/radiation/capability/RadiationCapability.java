package net.nuclearteam.createnuclear.content.radiation.capability;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.nuclearteam.createnuclear.CNAttributes;
import net.nuclearteam.createnuclear.CNEffects;
import net.nuclearteam.createnuclear.CNTags;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.api.radiation.IRadiationSource;
import net.nuclearteam.createnuclear.api.radiation.RadiationRegistry;
import net.nuclearteam.createnuclear.foundation.utility.ConfigValueResolver;
import net.nuclearteam.createnuclear.foundation.utility.InventoryHashUtil;
import net.nuclearteam.createnuclear.infrastructure.config.CNConfigs;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class RadiationCapability implements IRadiationCapability {
    private double radiation;
    private long inventoryHash;
    private ResourceLocation lastBiomeLocation;
    private double contagionDose;
    private int contagionTicks;

    @Override
    public double getRadiation() {
        return this.radiation;
    }

    @Override
    public void setRadiation(double value) {
        this.radiation = value;
    }

    @Override
    public long getInventoryHash() {
        return this.inventoryHash;
    }

    @Override
    public void setInventoryHash(long hash) {
        this.inventoryHash = hash;
    }

    @Override
    public ResourceLocation getLastBiomeLocation() {
        return this.lastBiomeLocation;
    }

    @Override
    public void setLastBiomeLocation(ResourceLocation location) {
        this.lastBiomeLocation = location;
    }

    @Override
    public double getContagionDose() {
        return this.contagionDose;
    }

    @Override
    public void setContagionDose(double dose) {
        this.contagionDose = dose;
    }

    @Override
    public int getContagionTicks() {
        return this.contagionTicks;
    }

    @Override
    public void setContagionTicks(int ticks) {
        this.contagionTicks = ticks;
    }

    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity) {
            event.addCapability(CreateNuclear.asResource("irradiated_resistance"), new RadiationProvider());
        }
    }

    public static void onLivingEntityTick(LivingTickEvent event) {
        tickRadiation(event.getEntity());
    }

    public static void applyContagion(LivingEntity entity, double doseValue, int durationTicks) {
        entity.getCapability(RadiationProvider.CAP).ifPresent(cap -> {
            cap.setContagionDose(doseValue);
            cap.setContagionTicks(durationTicks);
        });
    }

    public static void tickRadiation(LivingEntity entity) {
        Level level = entity.level();
        if (level.isClientSide) return;

        entity.getCapability(RadiationProvider.CAP).ifPresent(cap -> {
            if (entity instanceof Player player) {
                long newHash = InventoryHashUtil.compute(player);
                if (newHash != cap.getInventoryHash()) {
                    cap.setInventoryHash(newHash);
                    cap.setRadiation(Math.max(0, computeItemRadiation(player)));
                }
            } else {
                cap.setRadiation(Math.max(0, computeItemRadiation(entity)));
            }

            ResourceKey<Biome> biomeKey = level.getBiome(entity.blockPosition()).unwrapKey().orElse(null);
            ResourceLocation biomeLoc = biomeKey != null ? biomeKey.location() : null;
            if (!Objects.equals(biomeLoc, cap.getLastBiomeLocation())) {
                cap.setLastBiomeLocation(biomeLoc);
            }

            if (!canBeIrradiated(entity)) return;

            if (cap.getContagionTicks() > 0) {
                cap.setContagionTicks(cap.getContagionTicks() - 1);
            }

            double contagionDose = cap.getContagionTicks() > 0 ? cap.getContagionDose() : 0;

            double totalRaw = cap.getRadiation() + getRawBiomeRadiation(biomeKey) + contagionDose;
            double resistance = getRadiationResistance(entity);
            double totalRadiation = totalRaw * (1.0 - resistance);

            applyEffects(entity, totalRadiation);
        });
    }

    private static double computeItemRadiation(Player player) {
        double radiation = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof IRadiationSource source)
                radiation += source.getRadiation(stack, player);
            radiation += RadiationRegistry.getRadiation(stack, player);
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.getItem() instanceof IRadiationSource source)
                radiation += source.getRadiation(stack, player);
            radiation += RadiationRegistry.getRadiation(stack, player);
        }
        return radiation;
    }

    private static double getStackRadiation(ItemStack stack, LivingEntity entity) {
        double radiation = 0;
        if (stack.getItem() instanceof IRadiationSource source) radiation += source.getRadiation(stack, entity);
        radiation += RadiationRegistry.getRadiation(stack, entity);

        return radiation;
    }

    private static double computeItemRadiation(LivingEntity entity) {
        double radiation = 0;
        for (ItemStack stack : entity.getArmorSlots()) {
            radiation += getStackRadiation(stack, entity);
        }
        radiation += getStackRadiation(entity.getMainHandItem(), entity);
        radiation += getStackRadiation(entity.getOffhandItem(), entity);
        return radiation;
    }

    private static double getRawBiomeRadiation(ResourceKey<Biome> biomeKey) {
        if (biomeKey == null) return 0;
        return RadiationRegistry.get(biomeKey);
    }

    /**
     * Single source of truth for radiation eligibility, shared by every application path
     * (item/biome tick, vicinity effect, open-pipe leak). Covers <em>eligibility</em> only —
     * the continuous attenuation by resistance lives in {@link #applyEffects} / the effect tick.
     */
    public static boolean canBeIrradiated(LivingEntity entity) {
        if (entity.isSpectator()) return false;
        if (entity.getType().is(CNTags.CNEntityTags.IRRADIATED_IMMUNE.tag)) return false;
        if (!CNConfigs.server().radiation.enabledItemRadiation.get()) return false;
        if (getEntityBlacklist().contains(entity.getType())) return false;
        return getRadiationResistance(entity) < 1.0;
    }

    // Cached, parsed entity blacklist. Rebuilt only when the underlying config list instance
    // changes (ForgeConfigSpec returns a fresh list on reload), so no per-tick parsing.
    private static List<? extends String> cachedBlacklistSource;
    private static Set<EntityType<?>> cachedBlacklist = Set.of();

    private static Set<EntityType<?>> getEntityBlacklist() {
        List<? extends String> source = CNConfigs.server().radiation.configuredLists.getEntityBlackList();
        if (source != cachedBlacklistSource) {
            Set<EntityType<?>> resolved = new HashSet<>();
            ConfigValueResolver.loadValuesInSet(source, resolved,
                    entry -> BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.tryParse(entry)));
            cachedBlacklist = resolved;
            cachedBlacklistSource = source;
        }
        return cachedBlacklist;
    }

    public static double getRadiationResistance(LivingEntity entity) {
        double resistance = 0d;

        AttributeInstance attribute = entity.getAttribute(CNAttributes.IRRADIATED_RESISTANCE.get());

        if (attribute != null) resistance += attribute.getValue();

        return Mth.clamp(resistance, 0.0, 1.0);
    }

    private static void applyEffects(LivingEntity entity, double radiation) {
        final double radiation_desactive = 0;
        MobEffect radiationEffect = CNEffects.RADIATION.get();

        if (radiation <= radiation_desactive) return;

        int amp;
        if (radiation < CNConfigs.server().radiation.radiationLevel1.get()) amp = CNConfigs.server().radiation.amplifierLevel0.get();
        else if (radiation < CNConfigs.server().radiation.radiationLevel2.get()) amp = CNConfigs.server().radiation.amplifierLevel1.get();
        else if (radiation < CNConfigs.server().radiation.radiationLevel3.get()) amp = CNConfigs.server().radiation.amplifierLevel2.get();
        else amp = CNConfigs.server().radiation.amplifierLevel2.get();

        MobEffectInstance current = entity.getEffect(radiationEffect);

        if (current != null && current.getAmplifier() != amp) {
            entity.removeEffect(radiationEffect);
            current = null;
        }

        if (current == null || current.getDuration() <= 40) {
            entity.addEffect(new MobEffectInstance(CNEffects.RADIATION.get(), 100, amp, true, true));
        }
    }
}
