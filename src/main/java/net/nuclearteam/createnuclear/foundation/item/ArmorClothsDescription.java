package net.nuclearteam.createnuclear.foundation.item;

import com.simibubi.create.foundation.item.TooltipModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.content.equipment.armor.AntiRadiationArmorItem;
import net.nuclearteam.createnuclear.foundation.utility.CreateNuclearLang;

import java.util.ArrayList;
import java.util.List;

public record ArmorClothsDescription(Item item) implements TooltipModifier {
    public static ArmorClothsDescription create(Item item) {
        return new ArmorClothsDescription(item);
    }

    @Override
    public void modify(ItemTooltipEvent context) {
        List<Component> description = getDescription(item);
        if (!description.isEmpty()) {
            List<Component> tooltip = context.getToolTip();
            tooltip.add(CommonComponents.EMPTY);
            tooltip.addAll(description);
        }
    }

    private static List<Component> getDescription(Item item) {
        List<Component> components = new ArrayList<>();

        if (item instanceof AntiRadiationArmorItem armor) {
            CompoundTag tag = new ItemStack(armor).getTag();
            CreateNuclear.LOGGER.warn("ArmorClothsDescription::getDescription tag: {}", tag);
            if (tag != null && tag.contains("Cloth")) {
                CreateNuclearLang.translate("tooltip.cloth.color")
                        .style(ChatFormatting.AQUA)
                        .addTo(components);
            }
        };

        return components;
    }
}
