package net.nuclearteam.createnuclear.infrastructure.data;

import com.simibubi.create.foundation.data.TagGen.CreateTagsProvider;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.Tags;
import net.nuclearteam.createnuclear.CNBlocks;
import net.nuclearteam.createnuclear.CNEntityType;
import net.nuclearteam.createnuclear.CNTags;
import net.nuclearteam.createnuclear.CNTags.CNFluidTags;
import net.nuclearteam.createnuclear.CNTags.CNItemTags;
import net.nuclearteam.createnuclear.CNTags.CNBlockTags;
import net.nuclearteam.createnuclear.CNTags.CNEntityTags;
import net.nuclearteam.createnuclear.CreateNuclear;

public class CreateNuclearRegistrateTags {
    public static void addGenerators() {
        CreateNuclear.REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, CreateNuclearRegistrateTags::genBlockTags);
        CreateNuclear.REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, CreateNuclearRegistrateTags::genItemTags);
        CreateNuclear.REGISTRATE.addDataGenerator(ProviderType.FLUID_TAGS, CreateNuclearRegistrateTags::genFluidTags);
        CreateNuclear.REGISTRATE.addDataGenerator(ProviderType.ENTITY_TAGS, CreateNuclearRegistrateTags::genEntityTags);
    }

    private static void genBlockTags(RegistrateTagsProvider<Block> provIn) {
        CreateTagsProvider<Block> prov = new CreateTagsProvider<>(provIn, Block::builtInRegistryHolder);

        prov.tag(BlockTags.CAMPFIRES)
                .add(CNBlocks.ENRICHING_CAMPFIRE.get())
        ;

        prov.tag(BlockTags.FIRE)
                .add(CNBlocks.ENRICHING_FIRE.get())
        ;

        prov.tag(BlockTags.DRAGON_TRANSPARENT)
                .add(CNBlocks.ENRICHING_FIRE.get())
        ;

        for (CNBlockTags tag : CNBlockTags.values()) {
            if (tag.alwaysDatagen) {
                prov.getOrCreateRawBuilder(tag.tag);
            }
        }
    }

    private static void genItemTags(RegistrateTagsProvider<Item> provIn) {
        CreateTagsProvider<Item> prov = new CreateTagsProvider<>(provIn, Item::builtInRegistryHolder);

        for (CNItemTags tag : CNItemTags.values()) {
            if (tag.alwaysDatagen) {
                prov.getOrCreateRawBuilder(tag.tag);
            }
        }
    }

    private static void genFluidTags(RegistrateTagsProvider<Fluid> provIn) {
        CreateTagsProvider<Fluid> prov = new CreateTagsProvider<>(provIn, Fluid::builtInRegistryHolder);

        prov.tag(CNTags.forgeFluidTag("uranium"))
                .addTag(CNFluidTags.URANIUM.tag)
        ;

        prov.tag(FluidTags.LAVA)
                .addTag(CNFluidTags.URANIUM.tag)
        ;


        for (CNFluidTags tag : CNFluidTags.values()) {
            if (tag.alwaysDatagen) {
                prov.getOrCreateRawBuilder(tag.tag);
            }
        }
    }

    private static void genEntityTags(RegistrateTagsProvider<EntityType<?>> provIn) {
        CreateTagsProvider<EntityType<?>> prov = new CreateTagsProvider<>(provIn, EntityType::builtInRegistryHolder);

        prov.tag(EntityTypeTags.FALL_DAMAGE_IMMUNE)
                .add(CNEntityType.IRRADIATED_CAT.get())
                .add(CNEntityType.IRRADIATED_CHICKEN.get())
        ;

        for (CNEntityTags tag : CNEntityTags.values()) {
            if (tag.alwaysDatagen) {
                prov.getOrCreateRawBuilder(tag.tag);
            }
        }
    }
}
