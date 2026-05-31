package net.nuclearteam.createnuclear.content.effects.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.network.PacketDistributor;
import net.nuclearteam.createnuclear.CNAttributes;
import net.nuclearteam.createnuclear.CNEffects;
import net.nuclearteam.createnuclear.CNPackets;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.api.radiation.IRadiationSource;
import net.nuclearteam.createnuclear.api.radiation.RadiationRegistry;
import net.nuclearteam.createnuclear.content.uraniumOre.UraniumOreBlock;
import net.nuclearteam.createnuclear.foundation.networking.radiation.RadiationSyncPacket;
import net.nuclearteam.createnuclear.content.equipment.armor.AntiRadiationArmorItem;
import static net.nuclearteam.createnuclear.content.equipment.armor.AntiRadiationArmorItem.RADIATION_VALUE;
import net.nuclearteam.createnuclear.foundation.utility.InventoryHashUtil;
import net.nuclearteam.createnuclear.infrastructure.config.CNConfigs;

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

                if (CNConfigs.server().radiation.enabledItemRadiation.get()) {
                    for (ItemStack stack : player.getInventory().items) {
                        if (stack.getItem() instanceof IRadiationSource source) {
                            radiation += source.getRadiation(stack, player);
                        }
                        radiation += RadiationRegistry.getRadiation(stack, player);
                    }

                    for (ItemStack stack : player.getInventory().offhand) {
                        if (stack.getItem() instanceof IRadiationSource source) {
                            radiation += source.getRadiation(stack, player);
                        }
                        radiation += RadiationRegistry.getRadiation(stack, player);
                    }
                }

                double resistance = getRadiationResistance(player);
//                CreateNuclear.LOGGER.warn("Radiation: {}, resistance: {}, calcule: {}", radiation, resistance, (1.0 - resistance));
                radiation *= (1.0 - resistance);

                cap.setRadiation(Math.max(0, radiation));

                CNPackets.getChannel().send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new RadiationSyncPacket(radiation));
            }

            applyEffects(player, cap.getRadiation());
        });
    }

    public static double getRadiationResistance(LivingEntity entity) {
        double resistance = 0d;

        AttributeInstance attribute = entity.getAttribute(CNAttributes.IRRADIATED_RESISTANCE.get());

        if (attribute != null) resistance += attribute.getValue();

//        resistance += getArmorResistance(entity.getArmorSlots());

        return Mth.clamp(resistance, 0.0,1.0);
    }

    private static double getArmorResistance(Iterable<ItemStack> stacks) {
        double resistance = 0d;

        for (ItemStack stack : stacks) {
            if (stack.getItem() instanceof AntiRadiationArmorItem) {
                resistance += RADIATION_VALUE;
            }
        }

        return resistance;
    }

    private static void applyEffects(Player player, double radiation) {
        final double radiation_desactive = 0;
        MobEffect radiationEffect = CNEffects.RADIATION.get();

        if (radiation <= radiation_desactive) return;

        int amp;
        if (radiation < CNConfigs.server().radiation.radiationLevel1.get()) amp = CNConfigs.server().radiation.amplifierLevel0.get();
        else if (radiation < CNConfigs.server().radiation.radiationLevel2.get()) amp = CNConfigs.server().radiation.amplifierLevel0.get();
        else if (radiation < CNConfigs.server().radiation.radiationLevel3.get()) amp = CNConfigs.server().radiation.amplifierLevel1.get();
        else amp = CNConfigs.server().radiation.amplifierLevel2.get();

        MobEffectInstance current = player.getEffect(radiationEffect);

        if (current != null && current.getAmplifier() != amp) {
            player.removeEffect(radiationEffect);
            current = null;
        }

        if (current == null || current.getDuration() <= 40) {
            player.addEffect(new MobEffectInstance(CNEffects.RADIATION.get(), 100, amp, true, true));
        }
    }
}
