package net.nuclearteam.createnuclear.content.radiation;

import com.simibubi.create.api.effect.OpenPipeEffectHandler;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidStack;
import net.nuclearteam.createnuclear.CNEffects;
import net.nuclearteam.createnuclear.content.radiation.capability.RadiationCapability;

import java.util.List;

public class RadiationEffectHandler implements OpenPipeEffectHandler {
    @Override
    public void apply(Level level, AABB area, FluidStack fluid) {
        if (level.getGameTime() % 5 != 0) return;

        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAffectedByPotions);
        for (LivingEntity entity : entities) {
            if (!RadiationCapability.canBeIrradiated(entity)) continue;
            entity.addEffect(new MobEffectInstance(CNEffects.RADIATION.get(), 3, 2, false, false, false));
        }
    }
}
