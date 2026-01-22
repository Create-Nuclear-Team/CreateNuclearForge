package net.nuclearteam.createnuclear.content.particles;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CNParticleRegistry {
    public static final DeferredRegister<ParticleType<?>> DEF_REG;

    public static final RegistryObject<SimpleParticleType> NUCLEAR_MUSHROOM_CLOUD;

    private static ParticleType<BlockParticleOption> createBlockParticleType() {
        return new ParticleType<BlockParticleOption>(false, BlockParticleOption.DESERIALIZER) {
            public Codec<BlockParticleOption> codec() {
                return BlockParticleOption.codec(this);
            }
        };
    }

    private static ParticleType<ItemParticleOption> createItemParticleType() {
        return new ParticleType<ItemParticleOption>(false, ItemParticleOption.DESERIALIZER) {
            public Codec<ItemParticleOption> codec() {
                return ItemParticleOption.codec(this);
            }
        };
    }

    static {
        DEF_REG = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, "createnuclear");
        NUCLEAR_MUSHROOM_CLOUD = DEF_REG.register("nuclear_mushroom_cloud", () -> new SimpleParticleType(false));
    }

}
