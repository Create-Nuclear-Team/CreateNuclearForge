package net.nuclearteam.createnuclear.content.effects;

import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.nuclearteam.createnuclear.CNEffects;
import net.nuclearteam.createnuclear.CNTags;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.content.equipment.armor.AntiRadiationArmorItem;

public class RadiationEffect extends MobEffect {
    public RadiationEffect() {
        super(MobEffectCategory.HARMFUL, 15453236);
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
