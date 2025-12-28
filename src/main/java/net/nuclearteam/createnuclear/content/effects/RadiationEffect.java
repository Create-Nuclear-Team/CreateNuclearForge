package net.nuclearteam.createnuclear.content.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.nuclearteam.createnuclear.CNEffects;
import net.nuclearteam.createnuclear.CNTags;
import net.nuclearteam.createnuclear.content.equipment.armor.AntiRadiationArmorItem;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

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
                    for (ItemStack armor : e.getArmorSlots()) {
                        if (AntiRadiationArmorItem.Armor.isArmored(armor)) {
                            isWearingAntiRadiationArmor = true;
                            break;
                        }
                    }

                    return !e.getType().is(CNTags.CNEntityTags.IRRADIATED_IMMUNE.tag)
                                && !e.hasEffect(CNEffects.RADIATION.get())
                                && !isWearingAntiRadiationArmor
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

        // Apply radiation damage (magic type), scaled by amplifier
        int damage = 1 << amplifier;
        entity.hurt(entity.damageSources().magic(), damage);
    }
}