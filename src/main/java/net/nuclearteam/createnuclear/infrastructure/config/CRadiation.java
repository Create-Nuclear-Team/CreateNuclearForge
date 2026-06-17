package net.nuclearteam.createnuclear.infrastructure.config;

import net.createmod.catnip.config.ConfigBase;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

@MethodsReturnNonnullByDefault
public class CRadiation extends ConfigBase {
    public final ConfigBool enabledItemRadiation = b(true, "enabledItemRadiation", Comments.enabled);
    public final ConfiguredLists configuredLists = nested(0, ConfiguredLists::new, Comments.list);
    public final ConfigInt radiationLevel1 = i(10, 0, 50, "radiationLevel1", Comments.radiationLevel1);
    public final ConfigInt radiationLevel2 = i(25, 0, 50, "radiationLevel2", Comments.radiationLevel2);
    public final ConfigInt radiationLevel3 = i(50, 0, 50, "radiationLevel3", Comments.radiationLevel3);
    public final ConfigInt amplifierLevel0 = i(0, 0, 10, "amplifierLevel0", Comments.amplifierLevel0);
    public final ConfigInt amplifierLevel1 = i(1, 0, 10, "amplifierLevel1", Comments.amplifierLevel1);
    public final ConfigInt amplifierLevel2 = i(2, 0, 10, "amplifierLevel2", Comments.amplifierLevel2);

    @Override
    public String getName() {
        return "Radiation";
    }

    private static class Comments {
        static String enabled = "When enabled, certain items may emit radiation that affects the player and nearby entities. "
            + "Disable this to neutralize radiation effects without removing the items.";
        static String radiationLevel1 = "Minimum radiation value required to apply the Radiation I effect. "
                + "Below this value, no radiation effect is applied.";
        static String radiationLevel2 = "Minimum radiation value required to upgrade the effect to Radiation II. "
                + "This value should be greater than radiationLevel1.";
        static String radiationLevel3 = "Minimum radiation value required to upgrade the effect to Radiation III. "
                + "This value should be greater than radiationLevel2.";
        static String amplifierLevel0 = "Mob effect amplifier used for the first radiation tier. "
                + "In Minecraft, amplifier 0 means Radiation I.";
        static String amplifierLevel1 = "Mob effect amplifier used for the second radiation tier. "
                + "In Minecraft, amplifier 1 means Radiation II.";
        static String amplifierLevel2 = "Mob effect amplifier used for the third radiation tier. "
                + "In Minecraft, amplifier 2 means Radiation III.";
        static String list = "Contains configurable lists related to radiation, such as the entity blacklist.\n" +
                "\n" +
                "The entity blacklist is mainly used to prevent certain non-living entities (like item frames or armor stands)\n" +
                "from transmitting radiation effects. This avoids unwanted side effects where decorative or technical entities\n" +
                "would otherwise be affected by or propagate radiation.";
        static String blackListEntity = "List of entity IDs that are excluded from radiation effects.\n" +
                "Add the registry names (e.g., \"minecraft:armor_stand\") of entities you want to ignore for radiation.\n" +
                "This is useful for entities like item frames or armor stands that should not be affected.\n" +
                "\n" +
                "How to fill:\n" +
                "- Each entry must be a valid entity ID in the format namespace:entity_name.\n" +
                "- To add a new entity, simply add its ID as a new string in the list.\n" +
                "- Example: \"minecraft:bat\" will prevent bats from being affected by radiation.\n" +
                "- To remove an entity from the blacklist, delete its line from the list.\n" +
                "\n" +
                "This style of configuration is standard in many mods: simply list the entity IDs you want to exclude.\n" +
                "If you are unsure of an entity's ID, check the mod's documentation or use the /summon command in-game to see the correct ID.";
    }

    public static class ConfiguredLists extends ConfigBase {
        private static ForgeConfigSpec.ConfigValue<List<? extends String>> ENTITY_BLACKLIST = null;

        @Override
        public void registerAll(ForgeConfigSpec.Builder builder) {

            ENTITY_BLACKLIST = builder
                    .comment(Comments.blackListEntity)
                    .defineListAllowEmpty(
                            List.of("entityBlackList"),
                            () -> List.of(
                                    "minecraft:armor_stand",
                                    "minecraft:item_frame",
                                    "minecraft:glow_item_frame"
                            ),
                            obj -> obj instanceof String
                    );
        }

        @Override
        public String getName() {
            return "lists";
        }

        public List<? extends String> getEntityBlackList() {
            return ENTITY_BLACKLIST.get();
        }
    }
}
