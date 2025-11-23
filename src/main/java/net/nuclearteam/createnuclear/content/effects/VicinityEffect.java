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

            int couldown = getCooldown(nearby);
            if (couldown <= 0) {
                for (Supplier<MobEffectInstance> effect : effects) {
                    nearby.addEffect(effect.get());
                }

                setCooldown(nearby, 500);

            } else {
                setCooldown(nearby, couldown - 1);
                CreateNuclear.LOGGER.warn("Test Duree: {}, entity: {}", couldown, nearby.getUUID());
            }
        }


        // Clean up cooldowns only on the server: remove entries whose entity is gone or marked removed.
        if (entity.level() instanceof ServerLevel serverLevel) {
            cooldowns.entrySet().removeIf(entry -> {
                Entity e = serverLevel.getEntity(entry.getKey());
                return e == null || e.isRemoved();
            });
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





























