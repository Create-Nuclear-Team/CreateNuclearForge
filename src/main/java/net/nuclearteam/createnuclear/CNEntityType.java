package net.nuclearteam.createnuclear;

import com.tterrag.registrate.util.entry.EntityEntry;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.CNModelLayers;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.cat.IrradiatedCat;
import net.nuclearteam.createnuclear.CNTags.CNEntityTags;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.cat.IrradiatedCatModel;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.cat.IrradiatedCatRenderer;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.chicken.IrradiatedChicken;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.chicken.IrradiatedChickenModel;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.chicken.IrradiatedChickenRenderer;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.cow.IrradiatedCow;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.cow.IrradiatedCowModel;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.cow.IrradiatedCowRenderer;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.wolf.IrradiatedWolf;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.wolf.IrradiatedWolfModel;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.wolf.IrradiatedWolfRenderer;
import net.nuclearteam.createnuclear.content.explosion.NuclearExplosionEntity;
import net.nuclearteam.createnuclear.content.equipment.armor.AntiRadiationArmorModel;

public class CNEntityType {

   public static final EntityEntry<IrradiatedCat> IRRADIATED_CAT = CreateNuclear.REGISTRATE
        .entity("irradiated_cat", IrradiatedCat::new, MobCategory.CREATURE)
        .loot((tb, e) -> tb.add(e, LootTable.lootTable()))
        .tag(CNEntityTags.IRRADIATED_IMMUNE.tag)
        .properties(p -> p.sized(0.6f, 0.7f))
        .lang("Irradiated Cat")
        .renderer(() -> IrradiatedCatRenderer::new)
        .attributes(IrradiatedCat::createAttributes)
        .register();

    public static final EntityEntry<IrradiatedChicken> IRRADIATED_CHICKEN = CreateNuclear.REGISTRATE
        .entity("irradiated_chicken", IrradiatedChicken::new, MobCategory.CREATURE)
        .loot((tb, e) -> tb.add(e, LootTable.lootTable()))
        .tag(CNEntityTags.IRRADIATED_IMMUNE.tag)
        .properties(p -> p.sized(0.6f, 0.7f))
        .lang("Irradiated Chicken")
        .renderer(() -> IrradiatedChickenRenderer::new)
        .attributes(IrradiatedChicken::createAttributes)
        .register();

   public static final EntityEntry<IrradiatedWolf> IRRADIATED_WOLF = CreateNuclear.REGISTRATE
        .entity("irradiated_wolf", IrradiatedWolf::new, MobCategory.CREATURE)
        .loot((tb, e) -> tb.add(e, LootTable.lootTable()))
        .tag(CNEntityTags.IRRADIATED_IMMUNE.tag)
        .properties(p -> p.sized(0.6f, 0.85f))
        .lang("Irradiated Wolf")
        .renderer(() -> IrradiatedWolfRenderer::new)
        .attributes(IrradiatedWolf::createAttributes)
        .register();

   public static final EntityEntry<IrradiatedCow> IRRADIATED_COW = CreateNuclear.REGISTRATE
    .entity("irradiated_cow", IrradiatedCow::new, MobCategory.CREATURE)
    .loot((tb, e) -> tb.add(e, LootTable.lootTable()))
    .tag(CNEntityTags.IRRADIATED_IMMUNE.tag)
    .properties(p -> p.sized(0.6f, 0.85f))
    .lang("Irradiated Cow")
    .renderer(() -> IrradiatedCowRenderer::new)
    .attributes(IrradiatedCow::createAttributes)
    .register();

    public static final EntityEntry<Entity> NUCLEAR_EXPLOSION =
        CreateNuclear.REGISTRATE.entity("nuclear_explosion", NuclearExplosionEntity::new, MobCategory.MISC)
            .properties(builder -> builder
                .sized(0.2F, 0.2F)
                .setTrackingRange(128)
                .setUpdateInterval(1)
                .setShouldReceiveVelocityUpdates(true)
            )
            // Utilise le renderer vide par défaut de Minecraft
            .renderer(() -> NoopRenderer::new)
            .register();

   public static void registerModelLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(CNModelLayers.IRRADIATED_CAT, IrradiatedCatModel::createBodyLayer);
        event.registerLayerDefinition(CNModelLayers.IRRADIATED_CHICKEN, IrradiatedChickenModel::createBodyLayer);
        event.registerLayerDefinition(CNModelLayers.IRRADIATED_WOLF, IrradiatedWolfModel::createBodyLayer);
        event.registerLayerDefinition(CNModelLayers.IRRADIATED_COW, IrradiatedCowModel::createBodyLayer);

        event.registerLayerDefinition(CNModelLayers.ANTI_IRRADIATION_ARMOR, AntiRadiationArmorModel::createBodyLayer);

   }

    public static void register() {}
}
