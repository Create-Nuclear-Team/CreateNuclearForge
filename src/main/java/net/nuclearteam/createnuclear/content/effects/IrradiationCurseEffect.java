package net.nuclearteam.createnuclear.content.effects;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.nuclearteam.createnuclear.CNEffects;
import net.nuclearteam.createnuclear.CNEntityType;
import net.nuclearteam.createnuclear.CNTags;

import java.util.List;

public class IrradiationCurseEffect extends VicinityEffect {
    public IrradiationCurseEffect(MobEffectCategory category, int color) {
        super(category, color,
                amplifier -> 15,
                e -> !e.getType().is(CNTags.CNEntityTags.IRRADIATED_IMMUNE.tag) && !e.hasEffect(CNEffects.RADIATION.get()),
                () -> new MobEffectInstance(MobEffects.ABSORPTION, 300));
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return super.isDurationEffectTick(duration, amplifier)/* && SZConfig.INSTANCE.zombiesCurseZombification.get()*/;
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide) {
//            if (entity instanceof Animal animal) {
//                EntityType<? extends Animal> conversionType = ZombifiedAnimal.VANILLA_TO_ZOMBIFIED.get(animal.getType());
//
//                if (conversionType != null && EventHooks.canLivingConvert(animal, conversionType, timer -> {})) {
//                    Mob convertedAnimal = animal.convertTo(conversionType, false);
//
//                    convertedAnimal.finalizeSpawn((ServerLevel) animal.level(), animal.level().getCurrentDifficultyAt(convertedAnimal.blockPosition()), MobSpawnType.CONVERSION, null, null);
//                    ((ZombifiedAnimal) convertedAnimal).readFromVanilla(animal);
//                    EventHooks.onLivingConvert(animal, convertedAnimal);
//
//                    if (!animal.isSilent())
//                        animal.level().levelEvent(null, LevelEvent.SOUND_ZOMBIE_INFECTED, animal.blockPosition(), 0);
//                }
//                else {
//                    entity.hurt(DamageSources. (entity.level().registryAccess()), Float.MAX_VALUE);
//                }
//            }
        }
    }
}
