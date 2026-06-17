package net.nuclearteam.createnuclear.content.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.*;

public class VicinityEffect extends MobEffect {
    private final UnaryOperator<Integer> areaSize;
    private final Predicate<LivingEntity> filter;
    private final Supplier<MobEffectInstance>[] effects;

    // Stores the GameTime tick when the cooldown expires for each entity
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    @SafeVarargs
    protected VicinityEffect(MobEffectCategory category, int color, UnaryOperator<Integer> areaSize, Predicate<LivingEntity> filter, Consumer<Integer> timer, Supplier<MobEffectInstance>... effects) {
        super(category, color);

        this.areaSize = areaSize;
        this.filter = filter;
        this.effects = effects;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        long currentTime = entity.level().getGameTime();

        List<Entity> nearbyEntities = entity.level().getEntities(
                entity,
                entity.getBoundingBox().inflate(areaSize.apply(amplifier)),
                e -> e instanceof LivingEntity target && filter.test(target)
        );

        for (Entity nearbyEntity : nearbyEntities) {
            LivingEntity nearby = (LivingEntity) nearbyEntity;
            UUID entityUuid = nearby.getUUID();

            // Check if the cooldown has expired
            if (currentTime >= cooldowns.getOrDefault(entityUuid, 0L)) {

                for (Supplier<MobEffectInstance> effect : effects) {
                    nearby.addEffect(effect.get());
                }

                // Set a 100-tick (5 seconds) cooldown before refreshing the effect again
                cooldowns.put(entityUuid, currentTime + 100);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 5 == 0;
    }
}