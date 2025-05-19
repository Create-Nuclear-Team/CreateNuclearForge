package net.nuclearteam.createnuclear.content.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.nuclearteam.createnuclear.CNEffects;
import net.nuclearteam.createnuclear.CNTags;
import net.nuclearteam.createnuclear.content.equipment.armor.AntiRadiationArmorItem;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class RadiationEffect extends MobEffect {
    public RadiationEffect() {

        super(MobEffectCategory.HARMFUL, 15453236);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, "648D7064-6A60-4F59-8ABE-C2C23A6DD7A9", -0.2D, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(Attributes.ATTACK_SPEED, "55FCED67-E92A-486E-9800-B47F202C4386", -0.2D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * If the player wears the anti_radiation_suit armor then he does not take damage
     */
    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        livingEntity.getArmorSlots().forEach(e -> {
            if (livingEntity.hasEffect(CNEffects.RADIATION.get()) && AntiRadiationArmorItem.Armor.isArmored2(e)) {
                livingEntity.hurt(livingEntity.damageSources().magic(), 0);
            }
            else if (livingEntity.getType().is(CNTags.CNEntityTags.IRRADIATED_IMMUNE.tag)) {
                livingEntity.removeEffect(this);
            }
            else {
                livingEntity.hurt(livingEntity.damageSources().magic(), 1 << amplifier);
            }
        });
    }
}
