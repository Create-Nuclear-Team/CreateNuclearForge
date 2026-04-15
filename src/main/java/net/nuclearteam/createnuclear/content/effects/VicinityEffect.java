package net.nuclearteam.createnuclear.content.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.nuclearteam.createnuclear.CreateNuclear;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.*;

public class VicinityEffect extends MobEffect {
    private final UnaryOperator<Integer> areaSize;
    private final Predicate<LivingEntity> filter;
    private final Supplier<MobEffectInstance>[] effects;

    private final Map<UUID, Integer> cooldowns = new HashMap<>();

    @SafeVarargs
    protected VicinityEffect(MobEffectCategory category, int color, UnaryOperator<Integer> areaSize, Predicate<LivingEntity> filter, Consumer<Integer> timer, Supplier<MobEffectInstance>... effects) {
        super(category, color);

        this.areaSize = areaSize;
        this.filter = filter;
        this.effects = effects;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        List<Entity> nearbyEntities = entity.level().getEntities(entity, entity.getBoundingBox().inflate(areaSize.apply(amplifier)), e -> e instanceof LivingEntity target && filter.test(target));

        for (Entity nearbyEntity : nearbyEntities) {
            LivingEntity nearby = (LivingEntity) nearbyEntity;

            for (Supplier<MobEffectInstance> effect : effects) {
                int cooldownTicks = 0;
                if (cooldownTicks == 0) {
                        nearby.addEffect(effect.get());
                    cooldownTicks = 500;
                } else {
                    cooldownTicks--;
                    //CreateNuclear.LOGGER.warn("Test Duree: {}, entity: {}", cooldownTicks, nearby.getUUID());
                }
            }
        }
    }

    private int getCooldown(LivingEntity entity) {
        return cooldowns.getOrDefault(entity.getUUID(), 0);
    }

    private void setCooldown(LivingEntity entity, int ticks) {
        cooldowns.put(entity.getUUID(), ticks);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 5 == 0;
    }
}





























