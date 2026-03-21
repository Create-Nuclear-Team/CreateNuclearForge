package net.nuclearteam.createnuclear.content.effects.packet;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.nuclearteam.createnuclear.content.effects.ClientRadiationData;

public class RadiationSyncPacket extends SimplePacketBase {
    private final double radiation;

    public RadiationSyncPacket(double radiation) {
        this.radiation = radiation;
    }

    public RadiationSyncPacket(FriendlyByteBuf buffer) {
        this(buffer.readDouble());
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeDouble(radiation);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> new ClientRadiationData(radiation));
        return true;
    }
}
