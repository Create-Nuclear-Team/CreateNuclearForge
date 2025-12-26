package net.nuclearteam.createnuclear.api;

import com.simibubi.create.Create;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.api.multiblock.rods.RodType;


public class CreateNuclearRegistries {
    public static final ResourceKey<Registry<RodType>> ROD_TYPE = key("rods/type");

    private static <T> ResourceKey<Registry<T>> key(String name) {
        return ResourceKey.createRegistryKey(CreateNuclear.asResource(name));
    }
}
