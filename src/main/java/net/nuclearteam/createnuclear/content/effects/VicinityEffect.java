package net.nuclearteam.createnuclear.content.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.nuclearteam.createnuclear.CreateNuclear;

import java.util.List;
import java.util.function.*;

public class VicinityEffect extends MobEffect {
    private final UnaryOperator<Integer> areaSize;
    private final Predicate<LivingEntity> filter;
    private final Supplier<MobEffectInstance>[] effects;

    private int cooldownTicks = 500;

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


        CreateNuclear.LOGGER.warn("Nearby Entities Count: {}, {}", nearbyEntities.size(), entity.getDisplayName().getString());
        for (Entity nearbyEntity : nearbyEntities) {
            LivingEntity nearby = (LivingEntity) nearbyEntity;

            if (nearby == entity) {
                continue;
            }

            for (Supplier<MobEffectInstance> effect : effects) {
                if (cooldownTicks == 0) {
                        nearby.addEffect(effect.get());
                    cooldownTicks = 500;
                } else {
                    cooldownTicks--;
                    CreateNuclear.LOGGER.warn("Test Duree: {}, entity: {}", cooldownTicks, nearby.getUUID());
                }
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 5 == 0;
    }
}





























