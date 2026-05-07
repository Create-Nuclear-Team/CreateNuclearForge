package net.nuclearteam.createnuclear.content.effects.capability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.network.PacketDistributor;
import net.nuclearteam.createnuclear.CNAttributes;
import net.nuclearteam.createnuclear.CNEffects;
import net.nuclearteam.createnuclear.CNPackets;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.api.radiation.IRadiationSource;
import net.nuclearteam.createnuclear.foundation.networking.radiation.RadiationSyncPacket;
import net.nuclearteam.createnuclear.content.equipment.armor.AntiRadiationArmorItem;
import net.nuclearteam.createnuclear.foundation.utility.InventoryHashUtil;

public class RadiationCapability implements IRadiationCapability {
    private double radiation;
    private long inventoryHash;

    @Override
    public double getRadiation() {
        return this.radiation;
    }

    @Override
    public void setRadiation(double value) {
        this.radiation = value;
    }

    @Override
    public long getInventoryHash() {
        return this.inventoryHash;
    }

    @Override
    public void setInventoryHash(long hash) {
        this.inventoryHash = hash;
    }

    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(CreateNuclear.asResource("irradiated_resistance"), new RadiationProvider());
        }
    }

    public static void onPlayerTick(PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;
        Level level = player.level();

        if (level.isClientSide) return;

        player.getCapability(RadiationProvider.CAP).ifPresent(cap -> {
            long newHash = InventoryHashUtil.compute(player);

            if (newHash != cap.getInventoryHash()) {
                cap.setInventoryHash(newHash);

                double radiation = 0;

                for (ItemStack stack : player.getInventory().items) {
                    if (stack.getItem() instanceof IRadiationSource source) {
                        radiation += source.getRadiation(stack, player);
                    }
                }

                for (ItemStack stack : player.getInventory().offhand) {
                    if (stack.getItem() instanceof IRadiationSource source) {
                        radiation += source.getRadiation(stack, player);
                    }
                }

                double resistance = player.getAttributeValue(CNAttributes.IRRADIATED_RESISTANCE.get());

                for (ItemStack stack : player.getArmorSlots()) {
                    if (stack.getItem() instanceof ArmorItem armorItem && armorItem instanceof AntiRadiationArmorItem.Helmet) {
                        float ratio = 1.0f;
                        if (stack.isDamageableItem()) {
                            ratio = 1f - ((float) stack.getDamageValue() / (float) stack.getMaxDamage());
                            ratio = Math.max(0f, ratio);
                        }
                        resistance *= ratio;
                    }
                }

                CreateNuclear.LOGGER.warn("onPlayerTick::resistance: {}, radiation: {}, newHash: {}", resistance, radiation, newHash);
                resistance = Mth.clamp(resistance, 0.0, 1.0);
                radiation *= (1.0 - resistance);

                CreateNuclear.LOGGER.warn("onPlayerTick::radiation2: {}", radiation);


                cap.setRadiation(radiation);

                CNPackets.getChannel().send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new RadiationSyncPacket(radiation));
            }

            applyEffects(player, cap.getRadiation());
        });
    }

    private static void applyEffects(Player player, double radiation) {
        int amp;

        if (radiation < 10) return;
        else if (radiation < 25) amp = 0;
        else if (radiation < 50) amp = 1;
        else amp = 2;

        player.addEffect(new MobEffectInstance(CNEffects.RADIATION.get(), 100, amp, true, true));
    }
}
