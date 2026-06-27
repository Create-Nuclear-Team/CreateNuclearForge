package net.nuclearteam.createnuclear.content.biome;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.nuclearteam.createnuclear.infrastructure.config.CNConfigs;
import net.nuclearteam.createnuclear.infrastructure.worldgen.biome.BiomeIrradiationService;

public class BiomeRestoreCellItem extends Item {
    public static final String TAG = "biome_restore";
    private static final int CHARGE_PER_CLICK = 1;

    public BiomeRestoreCellItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack stack = player.getItemInHand(interactionHand);

        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.success(stack);
        }

        if (getCharge(stack) >= getMaxCharge()) return InteractionResultHolder.pass(stack);

        boolean restored = BiomeIrradiationService.restoreArea(serverLevel, player.blockPosition());
        if (!restored) return InteractionResultHolder.pass(stack);

        addCharge(stack, CHARGE_PER_CLICK);

        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return CNConfigs.server().biomeRestore.alwaysShowBar.get() || getCharge(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * getCharge(stack) / getMaxCharge());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x4A90D9;
    }

    public static int getCharge(ItemStack stack) {
        return getChargeTag(stack, 0);
    }

    public static int getMaxCharge() {
        return CNConfigs.server().biomeRestore.maxCharge.get();
    }

    public static void addCharge(ItemStack stack, int amount) {
        int current = getCharge(stack);
        int next = Mth.clamp(current + amount, 0, getMaxCharge());
        stack.getOrCreateTag().putInt(TAG, next);
    }

    public static int getChargeTag(ItemStack stack, int defaultValue) {
        if (stack == null) return defaultValue;
        CompoundTag tag = stack.getTag();
        return (tag != null && tag.contains(TAG)) ? tag.getInt(TAG) : defaultValue;
    }
}
