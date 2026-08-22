package net.nuclearteam.createnuclear.content.logistics;

import net.nuclearteam.createnuclear.api.ReactorFluidTypesValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.nuclearteam.createnuclear.api.multiblock.fluid.ReactorFluidType;

import javax.annotation.Nullable;
import java.util.Objects;

public class BigFluidStack {
    public static final int INF = 1_000_000_000;

    public FluidStack stack;
    public int amount;

    public BigFluidStack(FluidStack stack) {
        this(stack, 1);
    }

    public BigFluidStack(FluidStack stack, int amount) {
        this.stack = stack;
        this.amount = amount;
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.put("Fluid", stack.writeToNBT(new CompoundTag()));
        tag.putInt("Amount", amount);

        return tag;
    }

    public static BigFluidStack read(CompoundTag tag) {
        return new BigFluidStack(FluidStack.loadFluidStackFromNBT(tag.getCompound("Fluid")), tag.getInt("Amount"));
    }

    public void send(FriendlyByteBuf buffer) {
        buffer.writeFluidStack(stack);
        buffer.writeVarInt(amount);
    }

    public static BigFluidStack receive(FriendlyByteBuf buffer) {
        return new BigFluidStack(buffer.readFluidStack(), buffer.readVarInt());
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof BigFluidStack other)
            return Objects.equals(stack, other.stack) && amount == other.amount;
        return false;
    }

    @Override
    public int hashCode() {
        return (nullHash(stack) * 31) ^ Integer.hashCode(amount);
    }

    int nullHash(Object o) {
        return o == null ? 0 : o.hashCode();
    }

    @Override
    public String toString() {
        return "(" + stack.getDisplayName()
                .getString() + " x" + amount + ")";
    }

    public ReactorFluidType getFluidtype(@Nullable Level level) {
        if (level == null) {
            return ReactorFluidTypesValue.getReactorFluidType(this.stack.getFluid());
        }
        return ReactorFluidType.resolveReactorFluidType(this.stack.getFluid(), level);
    }
}
