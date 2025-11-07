package net.nuclearteam.createnuclear.foundation.events;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.chicken.IrradiatedChicken;

@Mod.EventBusSubscriber
public class CommentEvents {
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent event) {
        if (event.phase == Phase.START) return;
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof PathfinderMob mob) {
            EntityType<?> type = mob.getType();

            if (type == EntityType.CHICKEN)
                mob.goalSelector.addGoal(0, new AvoidEntityGoal<>(mob, IrradiatedChicken.class, 4.0F, 1.0F, 1.2F));

        }
    }
}
