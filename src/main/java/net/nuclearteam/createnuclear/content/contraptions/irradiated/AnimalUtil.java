package net.nuclearteam.createnuclear.content.contraptions.irradiated;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.nuclearteam.createnuclear.CNItems;

import java.util.function.Predicate;

public class AnimalUtil {
    private AnimalUtil() {}

    public static void tick(LivingEntity me) {
        if (!me.level().isClientSide && me.isAlive()) {
            IrradiatedAnimal irradiatedAnimal = (IrradiatedAnimal) me;

            if (irradiatedAnimal.isConverting()) {
                irradiatedAnimal.setConversionTime(irradiatedAnimal.getConversionTime() - irradiatedAnimal.getConversionProgress());

                if (irradiatedAnimal.getConversionTime() <= 0 && ForgeEventFactory.canLivingConvert(me, irradiatedAnimal.getNormalVariant(), irradiatedAnimal::setConversionTime))
                    irradiatedAnimal.finishConversion((ServerLevel) me.level());
            }
        }
    }

    public static InteractionResult mobInteract(LivingEntity me, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.is(CNItems.YELLOWCAKE.get())) {
            if (me.hasEffect(MobEffects.WEAKNESS)) {
                if (!player.getAbilities().instabuild)
                    stack.shrink(1);

                if (!me.level().isClientSide)
                    ((IrradiatedAnimal) me).startConverting(me.getRandom().nextInt(2401) + 3600);

                me.level().gameEvent(me, GameEvent.ENTITY_INTERACT, me.getEyePosition());
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    public static boolean handleEntityEvent(LivingEntity me, byte id) {
        if (id == EntityEvent.ZOMBIE_CONVERTING) {
            if (!me.isSilent())
                me.level().playLocalSound(me.position().x, me.getEyeY(), me.position().z, SoundEvents.ZOMBIE_VILLAGER_CURE, me.getSoundSource(), 1.0F + me.getRandom().nextFloat(), me.getRandom().nextFloat() * 0.7F + 0.3F, false);

            return true;
        }

        return false;
    }

    public static boolean isFood(ItemStack stack, Ingredient foodItems) {
        return isFood(stack, foodItems, $ -> true);
    }

    public static boolean isFood(ItemStack stack, Ingredient foodItems, Predicate<ItemStack> extraTest) {
        if (stack.is(CNItems.YELLOWCAKE.get()) && stack.hasTag() && stack.getTag().contains("Ingredient")) {
            CompoundTag ingredientTag = stack.getTag().getCompound("Ingredient");
            ItemStack ingredient = ItemStack.of(ingredientTag);

            return foodItems.test(ingredient) || extraTest.test(ingredient);
        }

        return false;
    }
}
