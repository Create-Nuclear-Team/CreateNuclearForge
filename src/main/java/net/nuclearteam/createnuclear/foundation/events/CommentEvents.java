package net.nuclearteam.createnuclear.foundation.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.nuclearteam.createnuclear.CNEffects;
import net.nuclearteam.createnuclear.CNEntityType;
import net.nuclearteam.createnuclear.CNItems;
import net.nuclearteam.createnuclear.CNTags;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.IrradiatedAnimal;
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

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Entity entity = event.getTarget();
        Player player = event.getEntity();

        if (entity instanceof Animal animal && IrradiatedAnimal.VANILLA_TO_IRRADIATED.containsKey(animal.getType())) {
            ItemStack stack = player.getItemInHand(event.getHand());

            if (stack.is(CNItems.URANIUM_POWDER.get()) && !animal.hasEffect(CNEffects.TEST.get())) {
                animal.addEffect(new MobEffectInstance(CNEffects.TEST.get(), TimeUtil.rangeOfSeconds(50, 70).sample(animal.getRandom())));

                if (!player.getAbilities().instabuild)
                    stack.shrink(1);

                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();

//        if (player.getItemInHand(event.getHand()).is(Items.LEAD))
//            SuspiciousRitual.maybeSendInfoMessages(null, event.getLevel(), event.getPos(), player);
    }

    @SubscribeEvent
    public static void onEntityConvert(LivingConversionEvent.Pre event) {
        if (event.getEntity() instanceof IrradiatedChicken && event.getOutcome() == EntityType.ZOMBIFIED_PIGLIN)
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLivingSetAttackTarget(LivingChangeTargetEvent event) {
        if (event.getNewTarget() != null && event.getNewTarget().hasEffect(CNEffects.TEST.get()) && event.getEntity().getType().is(CNTags.CNEntityTags.IRRADIATED_IMMUNE.tag))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity livingEntity = event.getEntity();
        Entity killer = event.getSource().getEntity();
        Level level = livingEntity.level();

        if (!level.isClientSide && (level.getDifficulty() == Difficulty.NORMAL || level.getDifficulty() == Difficulty.HARD) && killer instanceof IrradiatedAnimal zombifiedAnimal) {
            EntityType<? extends Mob> conversionType = (EntityType<? extends Mob>) killer.getType();

            if (livingEntity instanceof Animal killedEntity && killedEntity.getType() == zombifiedAnimal.getNormalVariant() && ForgeEventFactory.canLivingConvert(livingEntity, conversionType, timer -> {})) {
                if (level.getDifficulty() != Difficulty.HARD && level.random.nextBoolean())
                    return;

                Mob convertedAnimal = killedEntity.convertTo(conversionType, false);

                if (convertedAnimal != null) {
                    convertedAnimal.finalizeSpawn((ServerLevel) level, level.getCurrentDifficultyAt(convertedAnimal.blockPosition()), MobSpawnType.CONVERSION, null, null);
                    ((IrradiatedAnimal) convertedAnimal).readFromVanilla(killedEntity);
                    ForgeEventFactory.onLivingConvert(livingEntity, convertedAnimal);

                    if (!killer.isSilent())
                        level.levelEvent(null, LevelEvent.SOUND_ZOMBIE_INFECTED, killer.blockPosition(), 0);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getOriginal().hasEffect(CNEffects.TEST.get()))
            event.getEntity().addEffect(new MobEffectInstance(CNEffects.TEST.get(), Integer.MAX_VALUE));
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity().hasEffect(CNEffects.TEST.get()) && event.getDistance() > 3.0F) {
            MobEffectInstance cushionEffect = event.getEntity().getEffect(CNEffects.TEST.get());

            event.setDamageMultiplier(Math.max(event.getDamageMultiplier() * (0.3F - cushionEffect.getAmplifier() * 0.2F), 0));
            event.getEntity().playSound(SoundEvents.SLIME_SQUISH, 1.0F, 1.5F);
        }
    }

    @SubscribeEvent
    public static void onKnockback(LivingKnockBackEvent event) {
        if (event.getEntity().hasEffect(CNEffects.TEST.get())) {
            MobEffectInstance cushionEffect = event.getEntity().getEffect(CNEffects.TEST.get());

            event.setStrength(Math.max(event.getOriginalStrength() * (0.3F - cushionEffect.getAmplifier() * 0.2F), 0));
            event.getEntity().playSound(SoundEvents.SLIME_SQUISH, 1.0F, 1.5F);
        }
    }
}
