package net.nuclearteam.createnuclear.content.effects;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import net.nuclearteam.createnuclear.CNEffects;
import net.nuclearteam.createnuclear.CNTags;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.content.effects.capability.RadiationCapability;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.nuclearteam.createnuclear.foundation.damageTypes.CreateNuclearDamageSources;
import net.nuclearteam.createnuclear.infrastructure.config.CNConfigs;
import net.nuclearteam.createnuclear.infrastructure.config.CRadiation;

import java.util.HashSet;
import java.util.Set;

public class RadiationEffect extends VicinityEffect {

    /**
     * Constructs the RadiationEffect with harmful category and color.
     * Also applies attribute modifiers to reduce speed, attack damage, and attack speed.
     */
    public RadiationEffect() {
        super(MobEffectCategory.HARMFUL, 15453236,
                amplifier -> 10,
                e -> {
                    boolean isWearingAntiRadiationArmor = false;

                    double resistance = RadiationCapability.getRadiationResistance(e);
                    if (resistance >= 1) isWearingAntiRadiationArmor = true;

                    Set<EntityType<?>> target = new HashSet<>();

                    CRadiation.ConfiguredList.loadValueInList(CNConfigs.server().radiation.list.getEntityBlackList(), target, entity -> {
                        ResourceLocation location = ResourceLocation.tryParse(entity);
                        return BuiltInRegistries.ENTITY_TYPE.get(location);
                    });

                    return !e.getType().is(CNTags.CNEntityTags.IRRADIATED_IMMUNE.tag)
                        && !e.hasEffect(CNEffects.RADIATION.get())
                        && !isWearingAntiRadiationArmor
                        && CNConfigs.server().radiation.enabledItemRadiation.get()
                        && !target.contains(e.getType())
                    ;
                },
                timer -> {},
                () -> new MobEffectInstance(CNEffects.RADIATION.get(), 300)); // Custom color (hex value)

        // Reduces movement speed by 20%
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED,
                "91AEAA56-376B-4498-935B-2F7F68070635", -0.2D, AttributeModifier.Operation.MULTIPLY_TOTAL);

        // Reduces attack damage by 20%
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE,
                "648D7064-6A60-4F59-8ABE-C2C23A6DD7A9", -0.2D, AttributeModifier.Operation.MULTIPLY_TOTAL);

        // Reduces attack speed by 20%
        this.addAttributeModifier(Attributes.ATTACK_SPEED,
                "55FCED67-E92A-486E-9800-B47F202C4386", -0.2D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    /**
     * Determines if the effect should be applied this tick.
     * Returning true causes the effect to apply every tick.
     *
     * @param duration  The remaining duration of the effect.
     * @param amplifier The strength (level) of the effect.
     * @return true if the effect should apply on this tick.
     */
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);

        double resistance = RadiationCapability.getRadiationResistance(entity);

        float damage = (float) ((1 << amplifier) * (1.0 - resistance));

        if (damage <= 0.0f) {
            return;
        }

        entity.hurt(CreateNuclearDamageSources.radiation(entity.level()), damage);
    }
}