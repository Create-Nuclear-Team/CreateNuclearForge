package net.nuclearteam.createnuclear;

import static net.nuclearteam.createnuclear.content.equipment.armor.AntiRadiationArmorItem.Boot;
import static net.nuclearteam.createnuclear.content.equipment.armor.AntiRadiationArmorItem.Chestplate;
import static net.nuclearteam.createnuclear.content.equipment.armor.AntiRadiationArmorItem.Helmet;
import static net.nuclearteam.createnuclear.content.equipment.armor.AntiRadiationArmorItem.Leggings;

import static net.nuclearteam.createnuclear.CNTags.CNItemTags;

import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.nuclearteam.createnuclear.content.equipment.armor.AntiRadiationArmorItem;
import net.nuclearteam.createnuclear.content.equipment.cloth.ClothItem;
import net.nuclearteam.createnuclear.content.equipment.cloth.ClothItem.DyeItemList;
import net.nuclearteam.createnuclear.content.multiblock.bluePrintItem.ReactorBluePrintItem;
import net.nuclearteam.createnuclear.foundation.utility.TextUtils;

import java.util.function.Supplier;

public class CNItems {

    public static final ItemEntry<Item>
        YELLOWCAKE = CreateNuclear.REGISTRATE
            .item("yellowcake", Item::new)
            .register(),

        RAW_LEAD = CreateNuclear.REGISTRATE
                .item("raw_lead", Item::new)
                .tag(CNTags.forgeItemTag("raw_ores"), CNTags.forgeItemTag("raw_materials"), CNTags.forgeItemTag("raw_materials/lead"))
                .register(),

        RAW_URANIUM = CreateNuclear.REGISTRATE
                .item("raw_uranium", Item::new)
                .tag(CNTags.forgeItemTag("raw_ores"), CNTags.forgeItemTag("raw_materials"), CNTags.forgeItemTag("raw_materials/uranium"))
                .register(),

        URANIUM_POWDER = CreateNuclear.REGISTRATE
                .item("uranium_powder", Item::new)
                .register(),

        STEEL_INGOT = CreateNuclear.REGISTRATE
                .item("steel_ingot", Item::new)
                .tag(CNTags.forgeItemTag("ingots"), CNTags.forgeItemTag("ingots/steel"))
                .register(),

        COAL_DUST = CreateNuclear.REGISTRATE
                .item("coal_dust", Item::new)
                .tag(CNTags.forgeItemTag("dusts"), CNTags.forgeItemTag("coal_dusts"))
                .register(),

        GRAPHITE_ROD = CreateNuclear.REGISTRATE
                .item("graphite_rod", Item::new)
                .tag(CNTags.forgeItemTag("rods"), CNItemTags.COOLER.tag)
                .register(),

        LEAD_INGOT = CreateNuclear.REGISTRATE
                .item("lead_ingot", Item::new)
                .tag(CNTags.forgeItemTag("ingots"), CNTags.forgeItemTag("ingots/lead"))
                .register(),

        STEEL_NUGGET = CreateNuclear.REGISTRATE
                .item("steel_nugget", Item::new)
                .tag(CNTags.forgeItemTag("nuggets"), CNTags.forgeItemTag("nuggets/steel"))
                .register(),

    URANIUM_ROD = CreateNuclear.REGISTRATE
            .item("uranium_rod", Item::new)
            .tag(CNTags.forgeItemTag("rods"), CNItemTags.FUEL.tag)
            .register(),

    LEAD_NUGGET = CreateNuclear.REGISTRATE
            .item("lead_nugget", Item::new)
            .tag(CNTags.forgeItemTag("nuggets"), CNTags.forgeItemTag("nuggets/lead"))
            .register(),

    GRAPHENE = CreateNuclear.REGISTRATE
            .item("graphene", Item::new)
            .register(),

    ENRICHED_YELLOWCAKE = CreateNuclear.REGISTRATE
            .item("enriched_yellowcake", Item::new)
            .register()
    ;

    public static final Helmet.DyeItemHelmetList<Helmet> ANTI_RADIATION_HELMETS = new Helmet.DyeItemHelmetList<>(color -> {
        String colorName = color.getSerializedName();
        TagKey<Item> tag = !colorName.equals("white")
                ? CNItemTags.ANTI_RADIATION_HELMET_DYE.tag
                : CNItemTags.ANTI_RADIATION_ARMOR.tag;
        return CreateNuclear.REGISTRATE.item(colorName + "_anti_radiation_helmet", p -> new Helmet(p, color))
                .tag(
                        CNTags.forgeItemTag("helmets"),
                        CNTags.forgeItemTag("armors"),
                        tag,
                        CNItemTags.ALL_ANTI_RADIATION_ARMORS.tag,
                        CNItemTags.ANTI_RADIATION_HELMET_FULL_DYE.tag
                )
                .lang(TextUtils.titleCaseConversion(color.getName()) +" Anti Radiation Helmet")
                .model((c, p) -> p.generated(c, CreateNuclear.asResource("item/armors/helmets/" + colorName + "_anti_radiation_helmet")))
                .register();

    });

    public static final Chestplate.DyeItemChestplateList<Chestplate> ANTI_RADIATION_CHESTPLATES = new Chestplate.DyeItemChestplateList<>(color -> {
        String colorName = color.getSerializedName();

        TagKey<Item> tag = !colorName.equals("white")
                ? CNItemTags.ANTI_RADIATION_CHESTPLATE_DYE.tag
                : CNItemTags.ANTI_RADIATION_ARMOR.tag;
        return CreateNuclear.REGISTRATE.item(colorName + "_anti_radiation_chestplate",  p -> new Chestplate(p, color))
                .tag(
                        CNTags.forgeItemTag("chestplates"),
                        CNTags.forgeItemTag("armors"),
                        tag,
                        CNItemTags.ALL_ANTI_RADIATION_ARMORS.tag,
                        CNItemTags.ANTI_RADIATION_CHESTPLATE_FULL_DYE.tag
                )
                .lang(TextUtils.titleCaseConversion(color.getName()) +" Anti Radiation Chestplate")
                .model((c, p) -> p.generated(c, CreateNuclear.asResource("item/armors/chestplates/" + colorName + "_anti_radiation_chestplate")))
                .register();

    });

    public static final Leggings.DyeItemLeggingsList<Leggings> ANTI_RADIATION_LEGGINGS = new Leggings.DyeItemLeggingsList<>(color -> {

        String colorName = color.getSerializedName();
        TagKey<Item> tag = !colorName.equals("white")
                ? CNItemTags.ANTI_RADIATION_LEGGINGS_DYE.tag
                : CNItemTags.ANTI_RADIATION_ARMOR.tag;
        return CreateNuclear.REGISTRATE.item(colorName + "_anti_radiation_leggings",  p -> new Leggings(p, color))
                .tag(
                        CNTags.forgeItemTag("leggings"),
                        CNTags.forgeItemTag("armors"),
                        tag,
                        CNItemTags.ALL_ANTI_RADIATION_ARMORS.tag,
                        CNItemTags.ANTI_RADIATION_LEGGINGS_FULL_DYE.tag
                )
                .lang(TextUtils.titleCaseConversion(color.getName()) +" Anti Radiation Leggings")
                .model((c, p) -> p.generated(c, CreateNuclear.asResource("item/armors/leggings/" + colorName + "_anti_radiation_leggings")))
                .register();

    });

    public static final ItemEntry<? extends AntiRadiationArmorItem.Boot>
            ANTI_RADIATION_BOOTS = CreateNuclear.REGISTRATE.item("anti_radiation_boots", Boot::new)
            .tag(CNTags.forgeItemTag("boots"), CNTags.forgeItemTag("armors"), CNItemTags.ANTI_RADIATION_BOOTS_DYE.tag, CNItemTags.ANTI_RADIATION_ARMOR.tag, CNItemTags.ALL_ANTI_RADIATION_ARMORS.tag)
            .lang("Anti Radiation Boots")
            .model((c, p) -> p.generated(c, CreateNuclear.asResource("item/armors/anti_radiation_boots")))
            .register();

    public static final DyeItemList<ClothItem> CLOTHS = new ClothItem.DyeItemList<>(color -> {
        String colorName = color.getSerializedName();
        return CreateNuclear.REGISTRATE.item(colorName+ "_cloth", p -> new ClothItem(p, color))
                .tag(CNItemTags.CLOTH.tag)
                .lang(TextUtils.titleCaseConversion(color.getName()) + " Cloth")
                .model((c, p) -> p.generated(c, CreateNuclear.asResource("item/cloth/" + colorName + "_cloth")))
                .register();
    });

    public static final ItemEntry<ForgeSpawnEggItem> SPAWN_WOLF = registerSpawnEgg("wolf_irradiated_spawn_egg", CNEntityType.IRRADIATED_WOLF, 0x42452B,0x4C422B, "Irradiated Wolf Spawn Egg");
    public static final ItemEntry<ForgeSpawnEggItem> SPAWN_CAT = registerSpawnEgg("cat_irradiated_spawn_egg", CNEntityType.IRRADIATED_CAT, 0x382C19, 0x742728, "Irradiated Cat Spawn Egg");
    public static final ItemEntry<ForgeSpawnEggItem> SPAWN_CHICKEN = registerSpawnEgg("chicken_irradiated_spawn_egg", CNEntityType.IRRADIATED_CHICKEN, 0x6B9455, 0x95393C, "Irradiated Chicken Spawn Egg");

    public static final ItemEntry<ReactorBluePrintItem> REACTOR_BLUEPRINT = CreateNuclear.REGISTRATE
            .item("reactor_blueprint_item", ReactorBluePrintItem::new)
            .lang("Reactor Blueprint")
            .model((c, p) -> p.generated(c, CreateNuclear.asResource("item/reactor_blueprint")))
            .properties(p -> p.stacksTo(1))
            .register();


    private static ItemEntry<ForgeSpawnEggItem> registerSpawnEgg(String name, Supplier<? extends EntityType<? extends Mob>> entity, int backgroundColor, int highlightColor, String nameItems) {
        return CreateNuclear.REGISTRATE
                .item(name, p -> new ForgeSpawnEggItem(entity, backgroundColor, highlightColor, p))
                .lang(nameItems)
                .model((c, p) -> p.withExistingParent(c.getName(), new ResourceLocation("item/template_spawn_egg")))
                .register();

    }

    public static void register() {}


}
