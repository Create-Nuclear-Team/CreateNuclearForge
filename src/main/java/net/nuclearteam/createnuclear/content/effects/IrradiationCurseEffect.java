package net.nuclearteam.createnuclear.content.effects;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
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
                e -> { return
                        !e.getType().is(CNTags.CNEntityTags.IRRADIATED_IMMUNE.tag)
                        && !e.hasEffect(CNEffects.RADIATION.get())
                        && (e instanceof Player || (e instanceof Animal a
                         && !a.hasCustomName()
                         && (!(a instanceof TamableAnimal ta) || !ta.isTame())
                         && (!(a instanceof Bucketable b) || !b.fromBucket())));
                },
                () -> new MobEffectInstance(CNEffects.RADIATION.get(), 300));
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return super.isDurationEffectTick(duration, amplifier)/* && SZConfig.INSTANCE.zombiesCurseZombification.get()*/;
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }
}
