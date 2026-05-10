package net.nuclearteam.createnuclear.infrastructure.config;

import net.createmod.catnip.config.ConfigBase;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.common.ForgeConfigSpec;
import net.nuclearteam.createnuclear.CreateNuclear;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
public class CRadiation extends ConfigBase {
    public final ConfigBool enabledItemRadiation = b(true, "enabledItemRadiation", Comments.enabled);
    public final ConfiguredList list = nested(0, ConfiguredList::new);
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
    }

    public static class ConfiguredList extends ConfigBase {
        private static ForgeConfigSpec.ConfigValue<List<? extends String>> ENTITY_BLACKLIST = null;

        @Override
        public void registerAll(ForgeConfigSpec.Builder builder) {
            ENTITY_BLACKLIST = builder
                    .comment("")
                    .defineListAllowEmpty(
                            List.of("entityBlackList"),
                            () -> List.of(
                                    "minecraft:armor_stand",
                                    "minecraft:item_frame",
                                    "minecraft:glow_item_frame"
                            ),
                            obj -> obj instanceof String
                    );

            builder.pop();
        }

        @Override
        public String getName() {
            return "listValue";
        }

        public List<? extends String> getEntityBlackList() {
            return ENTITY_BLACKLIST.get();
        }

        public static <T> void loadValueInList(List<? extends String> valuesIds, Set<T> targetSet, Function<String, T> func) {
            targetSet.clear();
            for (String valueId : valuesIds) {
                try {
                    T value = func.apply(valueId);

                    if (value != null) {
                        targetSet.add(value);
                    } else {
                        CreateNuclear.LOGGER.error("Unknown entity type in config: {}", value);
                    }
                } catch (Exception e) {
                    CreateNuclear.LOGGER.error("Unknown entity type in config: {} - {}", valueId, e.getMessage());
                }
            }
        }
    }
}
