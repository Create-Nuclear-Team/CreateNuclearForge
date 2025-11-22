package net.nuclearteam.createnuclear;

import com.simibubi.create.AllTags;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.decoration.encasing.EncasedCTBehaviour;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.Tags;
import net.nuclearteam.createnuclear.content.enriching.campfire.EnrichingCampfireBlock;
import net.nuclearteam.createnuclear.content.enriching.fire.EnrichingFireBlock;
import net.nuclearteam.createnuclear.content.equipment.cloth.ClothItem;
import net.nuclearteam.createnuclear.content.multiblock.casing.ReactorCasing;
import net.nuclearteam.createnuclear.CNTags.CNBlockTags;
import net.nuclearteam.createnuclear.CNTags.CNItemTags;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlock;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerGenerator;
import net.nuclearteam.createnuclear.content.multiblock.core.ReactorCore;
import net.nuclearteam.createnuclear.content.multiblock.frame.ReactorFrame;
import net.nuclearteam.createnuclear.content.multiblock.frame.ReactorframeItem;
import net.nuclearteam.createnuclear.content.multiblock.input.ReactorInput;
import net.nuclearteam.createnuclear.content.multiblock.input.ReactorInputGenerator;
import net.nuclearteam.createnuclear.content.multiblock.output.ReactorOutput;
import net.nuclearteam.createnuclear.content.multiblock.output.ReactorOutputGenerator;
import net.nuclearteam.createnuclear.content.multiblock.reactorCooler.ReactorCooler;
import net.nuclearteam.createnuclear.content.multiblock.reinforced.ReinforcedGlassBlock;
import net.nuclearteam.createnuclear.content.uraniumOre.UraniumOreBlock;

import static com.simibubi.create.foundation.data.CreateRegistrate.casingConnectivity;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

public class CNBlocks {

    public static final BlockEntry<ReactorCasing> REACTOR_CASING =
        CreateNuclear.REGISTRATE.block("reactor_casing", properties -> new ReactorCasing(properties, ReactorCasing.TypeBlock.CASING))
            .properties(p -> p.explosionResistance(3F)
                .destroyTime(4F))
            .blockstate((c,p) ->
                p.getVariantBuilder(c.getEntry()).forAllStates((state) -> ConfiguredModel.builder()
                    .modelFile(p.models().getExistingFile(p.modLoc("block/reactor/casing/block")))
                    .build()))
            .onRegister(CreateRegistrate.connectedTextures(() -> new EncasedCTBehaviour(CNSpriteShifts.REACTOR_CASING)))
            .onRegister(casingConnectivity((block,cc) -> cc.makeCasing(block, CNSpriteShifts.REACTOR_CASING)))
            .tag(BlockTags.NEEDS_DIAMOND_TOOL)
            .simpleItem()
            .transform(pickaxeOnly())
            .register();

    public static final BlockEntry<ReactorCore> REACTOR_CORE =
        CreateNuclear.REGISTRATE.block("reactor_core", ReactorCore::new)
            .properties(p -> p.explosionResistance(6F))
            .properties(p -> p.destroyTime(4F))
            .tag(BlockTags.NEEDS_DIAMOND_TOOL)
            .blockstate((c, p) ->
                p.getVariantBuilder(c.getEntry())
                    .forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(p.models().getExistingFile(p.modLoc("block/reactor/core/block")))
                        .uvLock(false)
                        .build()
                    )
            )
            .transform(pickaxeOnly())
            .simpleItem()
            .register();

    public static final BlockEntry<ReactorFrame> REACTOR_FRAME =
        CreateNuclear.REGISTRATE.block("reactor_frame", ReactorFrame::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.explosionResistance(3F).destroyTime(2F))
            .addLayer(() -> RenderType::cutoutMipped)
            .transform(pickaxeOnly())
            .tag(BlockTags.NEEDS_DIAMOND_TOOL)
            .blockstate((c, p) ->
                p.getVariantBuilder(c.getEntry())
                .forAllStatesExcept(state -> {
                    ReactorFrame.Part part = state.getValue(ReactorFrame.PART);
                    String baseFile = "block/reactor/frame/frame_";
                    ModelFile start = p.models().getExistingFile(p.modLoc(baseFile + "top"));
                    ModelFile middle = p.models().getExistingFile(p.modLoc(baseFile + "middle"));
                    ModelFile bottom = p.models().getExistingFile(p.modLoc(baseFile + "bottom"));
                    ModelFile none = p.models().getExistingFile(p.modLoc(baseFile + "none"));
                    return ConfiguredModel.builder().modelFile(switch (part) {
                        case START -> start;
                        case MIDDLE -> middle;
                        case END -> bottom;
                        default -> none;
                    })
                    .uvLock(false)
                    .build();
                })
            )
            .item(ReactorframeItem::new)
            .model(AssetLookup.customBlockItemModel("reactor", "frame", "item"))
            .build()
            .register();

    public static final BlockEntry<ReactorCooler> REACTOR_COOLER =
        CreateNuclear.REGISTRATE.block("reactor_cooler", ReactorCooler::new)
            .properties(p -> p.explosionResistance(3F)
                    .destroyTime(4F))
            .blockstate((c,p) ->
                    p.getVariantBuilder(c.getEntry()).forAllStates((state) -> ConfiguredModel.builder()
                            .modelFile(p.models().getExistingFile(p.modLoc("block/reactor/cooler/block")))
                            .build()))
            .tag(BlockTags.NEEDS_DIAMOND_TOOL)
            .simpleItem()
            .transform(pickaxeOnly())
            .register();


    public static final BlockEntry<ReactorInput> REACTOR_INPUT =
        CreateNuclear.REGISTRATE.block("reactor_input", ReactorInput::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.explosionResistance(6F))
            .properties(p -> p.destroyTime(2F))
            .addLayer(() -> RenderType::cutoutMipped)
            .transform(pickaxeOnly())
            .tag(BlockTags.NEEDS_DIAMOND_TOOL)
            .blockstate(new ReactorInputGenerator()::generate)
            .item()
            .transform(customItemModel("reactor", "input", "item"))
            .register();

    public static final BlockEntry<ReactorOutput> REACTOR_OUTPUT =
        CreateNuclear.REGISTRATE.block("reactor_output", ReactorOutput::new)
            .properties(p -> p.explosionResistance(6F).destroyTime(4F))
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_PURPLE).forceSolidOn())
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag, BlockTags.NEEDS_DIAMOND_TOOL)
            .transform(pickaxeOnly())
            .blockstate(new ReactorOutputGenerator()::generate)
            .onRegister(block -> BlockStressValues.CAPACITIES.register(block, () -> 10240.0))
            .item()
            .transform(customItemModel("reactor", "output", "item"))
            .register();

    public static final BlockEntry<ReactorControllerBlock> REACTOR_CONTROLLER =
        CreateNuclear.REGISTRATE.block("reactor_controller", ReactorControllerBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.explosionResistance(6F))
            .properties(p -> p.destroyTime(4F))
            .transform(pickaxeOnly())
            .tag(BlockTags.NEEDS_DIAMOND_TOOL)
            .blockstate(new ReactorControllerGenerator()::generate)
            .item()
            .transform(customItemModel("reactor", "controller", "item"))
            .register();

    public static final BlockEntry<ReinforcedGlassBlock> REINFORCED_GLASS = CreateNuclear.REGISTRATE
        .block("reinforced_glass", ReinforcedGlassBlock::new)
        .initialProperties(() -> Blocks.GLASS)
        .properties(p -> p.explosionResistance(7.0F).destroyTime(2F))
        .onRegister(CreateRegistrate.connectedTextures(() -> new EncasedCTBehaviour(CNSpriteShifts.REACTOR_GLASS)))
        .onRegister(casingConnectivity((block,cc) -> cc.makeCasing(block, CNSpriteShifts.REACTOR_GLASS)))
        .loot(RegistrateBlockLootTables::dropWhenSilkTouch)
        .tag(CNTags.forgeBlockTag("glass"), BlockTags.IMPERMEABLE)
        .recipe((c, p) -> ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, c.get())
            .unlockedBy("has_reactor_casing", RegistrateRecipeProvider.has(CNBlocks.REACTOR_CASING.get()))
            .define('G', CNTags.forgeItemTag("glass"))
            .define('S', CNTags.forgeItemTag("ingots/lead"))
            .pattern("SGS")
            .pattern("GSG")
            .pattern("SGS")
            .showNotification(true)
            .save(p, CreateNuclear.asResource("crafting/reactor/" + c.getName())))
        .blockstate((c, p) -> p.getVariantBuilder(c.getEntry())
            .forAllStates(state -> ConfiguredModel.builder().modelFile(p.models()
                .withExistingParent("reinforced_glass",new ResourceLocation("block/cube_all"))
                .texture("all", p.modLoc("block/reactor/reinforced/glass"))
                .texture("particle", p.modLoc("block/reactor/reinforced/glass"))
            )
            .build())
        )
        .addLayer(() -> RenderType::translucent)
        .item()
        .tag(CNTags.forgeItemTag("glass"))
        .build()
        .register();



    public static final BlockEntry<EnrichingFireBlock> ENRICHING_FIRE =
        CreateNuclear.REGISTRATE.block("enriching_fire", properties -> new EnrichingFireBlock(properties, 3.0f))
            .initialProperties(() -> Blocks.FIRE)
            .properties(Properties::replaceable)
            .properties(Properties::noCollission)
            .properties(Properties::noOcclusion)
            .properties(EnrichingFireBlock.getLight())
            .tag(CNBlockTags.FAN_PROCESSING_CATALYSTS_ENRICHED.tag)
            .loot((lt, b) -> lt.add(b, BlockLootSubProvider.noDrop()))
            .addLayer(() -> RenderType::cutout)
            .blockstate((c, p) -> {
                String baseFolder = "block/enriching/fire/";
                ModelFile Floor0 = p.models().getExistingFile(p.modLoc(baseFolder + "floor0"));
                ModelFile Floor1 = p.models().getExistingFile(p.modLoc(baseFolder + "floor1"));
                ModelFile Side0 = p.models().getExistingFile(p.modLoc(baseFolder + "side0"));
                ModelFile Side1 = p.models().getExistingFile(p.modLoc(baseFolder + "side1"));
                ModelFile SideAlt0 = p.models().getExistingFile(p.modLoc(baseFolder + "side_alt0"));
                ModelFile SideAlt1 = p.models().getExistingFile(p.modLoc(baseFolder + "side_alt1"));

                p.getMultipartBuilder(c.get())
                        .part()
                        .modelFile(Floor0)
                        .nextModel()
                        .modelFile(Floor1)
                        .addModel()
                        .end()
                        .part()
                        .modelFile(Side0)
                        .nextModel()
                        .modelFile(Side1)
                        .nextModel()
                        .modelFile(SideAlt0)
                        .nextModel()
                        .modelFile(SideAlt1)
                        .addModel()
                        .end()
                        .part()
                        .modelFile(Side0).rotationY(90).nextModel()
                        .modelFile(Side1).rotationY(90).nextModel()
                        .modelFile(SideAlt0).rotationY(90).nextModel()
                        .modelFile(SideAlt1).rotationY(90)
                        .addModel()
                        .end()
                        .part()
                        .modelFile(Side0).rotationY(180).nextModel()
                        .modelFile(Side1).rotationY(180).nextModel()
                        .modelFile(SideAlt0).rotationY(180).nextModel()
                        .modelFile(SideAlt1).rotationY(180)
                        .addModel()
                        .end()
                        .part()
                        .modelFile(Side0).rotationY(270).nextModel()
                        .modelFile(Side1).rotationY(270).nextModel()
                        .modelFile(SideAlt0).rotationY(270).nextModel()
                        .modelFile(SideAlt1).rotationY(270)
                        .addModel()
                        .end();
            })
            .register()
    ;

    public static final BlockEntry<EnrichingCampfireBlock> ENRICHING_CAMPFIRE =
        CreateNuclear.REGISTRATE.block("enriching_campfire", properties -> new EnrichingCampfireBlock(properties, true, 5))
            .properties(p -> p.mapColor(MapColor.PODZOL)
                .instrument(NoteBlockInstrument.BASS)
                .strength(2.0F)
                .sound(SoundType.WOOD)
                .lightLevel(EnrichingCampfireBlock::getLight))
            .properties(Properties::noOcclusion)
            .properties(Properties::ignitedByLava)
            .addLayer(() -> RenderType::cutoutMipped)
            .transform(axeOrPickaxe())
            .tag(CNBlockTags.ALL_CAMPFIRES.tag)
            .recipe((c, p) -> ShapedRecipeBuilder.shaped(RecipeCategory.MISC, c.get())
                .unlockedBy("has_enriched_soul_soil", RegistrateRecipeProvider.has(CNBlocks.ENRICHED_SOUL_SOIL.get()))
                .define('E', CNBlocks.ENRICHED_SOUL_SOIL)
                .define('L', ItemTags.LOGS)
                .define('S', Tags.Items.RODS_WOODEN)
                .pattern(" S ")
                .pattern("SES")
                .pattern("LLL")
                .showNotification(true)
                .save(p, CreateNuclear.asResource("crafting/" + c.getName())))
            .loot((lt, b) -> lt.add(b, RegistrateBlockLootTables.createSilkTouchDispatchTable(b, lt.applyExplosionDecay(b, LootItem.lootTableItem(CNBlocks.ENRICHED_SOUL_SOIL)))))
            .blockstate((c, p) ->
                p.getVariantBuilder(c.getEntry()).forAllStatesExcept(state -> {
                    Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                    return ConfiguredModel.builder()
                        .modelFile(p.models().getExistingFile(p.modLoc("block/enriching/campfire/" + (state.getValue(EnrichingCampfireBlock.LIT) ? "block" : "block_off"))))
                        .uvLock(false)
                        .rotationY(switch (facing) {
                            case NORTH -> 180;
                            case WEST -> 90;
                            case EAST -> 270;
                            default -> 0;
                        })
                        .build();
                    }, BlockStateProperties.SIGNAL_FIRE, BlockStateProperties.WATERLOGGED
                )
            )
            .item()
            .model((c, p) ->
                p.withExistingParent(c.getName(), new ResourceLocation("item/generated"))
                    .texture("layer0", p.modLoc("item/enriched/campfire"))
            )
            .build()
            .tag(CNBlockTags.FAN_PROCESSING_CATALYSTS_ENRICHED.tag)
            .register();

    public static final BlockEntry<Block> DEEPSLATE_LEAD_ORE =
        CreateNuclear.REGISTRATE.block("deepslate_lead_ore", Block::new)
            .initialProperties(() -> Blocks.DIAMOND_ORE)
            .simpleItem()
            .transform(pickaxeOnly())
            .loot((lt, b) -> lt.add(b,
                RegistrateBlockLootTables.createSilkTouchDispatchTable(b,
                    lt.applyExplosionDecay(b, LootItem.lootTableItem(CNItems.RAW_LEAD)
                        .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))
            ))))
            .tag(BlockTags.NEEDS_IRON_TOOL,
                CNTags.forgeBlockTag("ores"),
                CNTags.forgeBlockTag("ores_in_ground/deepslate"),
                CNTags.forgeBlockTag("ores/lead"),
                CNBlockTags.LEAD_ORES.tag
            )
            .item()
            .tag(CNItemTags.LEAD_ORES.tag,
                CNTags.forgeItemTag("ores/lead"))
            .build()
            .register();

    public static final BlockEntry<Block> LEAD_ORE =
        CreateNuclear.REGISTRATE.block("lead_ore", Block::new)
            .initialProperties(SharedProperties::stone)
            .simpleItem()
            .transform(pickaxeOnly())
            .loot((lt, b) -> lt.add(b,
                RegistrateBlockLootTables.createSilkTouchDispatchTable(b,
                    lt.applyExplosionDecay(b, LootItem.lootTableItem(CNItems.RAW_LEAD)
                        .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))
            ))))
            .tag(BlockTags.NEEDS_IRON_TOOL,
                CNTags.forgeBlockTag("ores"),
                CNTags.forgeBlockTag("ores_in_ground/stone"),
                    CNTags.forgeBlockTag("ores/lead"),
                CNBlockTags.LEAD_ORES.tag
            )
            .item()
            .tag(CNItemTags.LEAD_ORES.tag,
                CNTags.forgeItemTag("ores/lead"))
            .build()
            .register();

    public static final BlockEntry<Block> RAW_URANIUM_BLOCK =
        CreateNuclear.REGISTRATE.block("raw_uranium_block", Block::new)
            .initialProperties(SharedProperties::stone)
            .transform(pickaxeOnly())
            .tag(BlockTags.NEEDS_DIAMOND_TOOL,
                CNTags.forgeBlockTag("storage_blocks/raw_uranium"))
            .recipe((c, p) -> ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, c.get())
                .unlockedBy("has_raw_materials_uranium", RegistrateRecipeProvider.has(CNTags.forgeItemTag("raw_materials/uranium")))
                .define('R', CNTags.forgeItemTag("raw_materials/uranium"))
                .pattern("RRR")
                .pattern("RRR")
                .pattern("RRR")
                .save(p, CreateNuclear.asResource("crafting/" + c.getName())))
            .item()
            .tag(CNTags.forgeItemTag("storage_blocks/raw_uranium"))
            .build()
            .register();

    public static final BlockEntry<Block> RAW_LEAD_BLOCK =
        CreateNuclear.REGISTRATE.block("raw_lead_block", Block::new)
            .initialProperties(SharedProperties::stone)
            .transform(pickaxeOnly())
            .tag(CNTags.forgeBlockTag("storage_blocks/raw_lead"))
            .recipe((c, p) -> ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, c.get())
                    .unlockedBy("has_raw_materials_lead", RegistrateRecipeProvider.has(CNTags.forgeItemTag("raw_materials/lead")))
                    .define('R', CNTags.forgeItemTag("raw_materials/lead"))
                    .pattern("RRR")
                    .pattern("RRR")
                    .pattern("RRR")
                    .save(p, CreateNuclear.asResource("crafting/" + c.getName())))
            .item()
            .tag(CNTags.forgeItemTag("storage_blocks/raw_lead"))
            .build()
            .register();

    public static final BlockEntry<Block> LEAD_BLOCK =
        CreateNuclear.REGISTRATE.block("lead_block", Block::new)
            .initialProperties(SharedProperties::stone)
            .transform(pickaxeOnly())
            .tag(CNTags.forgeBlockTag("storage_blocks/lead"))
            .item()
            .tag(CNTags.forgeItemTag("storage_blocks/lead"))
            .build()
            .register();

    public static final BlockEntry<Block> ENRICHED_SOUL_SOIL =
        CreateNuclear.REGISTRATE.block("enriched_soul_soil", Block::new)
            .initialProperties(() -> Blocks.SOUL_SOIL)
            .recipe((c, p) -> ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, c.get())
                .unlockedBy("has_nether_star", RegistrateRecipeProvider.has(Items.NETHER_STAR))
                .define('S', Blocks.SOUL_SOIL)
                .define('O', Blocks.OBSIDIAN)
                .define('N', Items.NETHER_STAR)
                .pattern("SOS")
                .pattern("ONO")
                .pattern("SOS")
                .showNotification(true)
                .save(p, CreateNuclear.asResource("crafting/" + c.getName()))
            )
            .simpleItem()
            .tag(BlockTags.MINEABLE_WITH_SHOVEL)
            .tag(CNBlockTags.ENRICHING_FIRE_BASE_BLOCKS.tag, BlockTags.NEEDS_DIAMOND_TOOL)
            .register();

    public static final BlockEntry<UraniumOreBlock> DEEPSLATE_URANIUM_ORE =
        CreateNuclear.REGISTRATE.block("deepslate_uranium_ore", UraniumOreBlock::new)
            .initialProperties(() -> Blocks.DIAMOND_ORE)
            .properties(UraniumOreBlock.litBlockEmission())
            .transform(pickaxeOnly())
            .loot((lt, b) -> lt.add(b,
                RegistrateBlockLootTables.createSilkTouchDispatchTable(b,
                    lt.applyExplosionDecay(b, LootItem.lootTableItem(CNItems.RAW_URANIUM)
                        .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))
            ))))
            .tag(BlockTags.NEEDS_DIAMOND_TOOL,
                BlockTags.NEEDS_IRON_TOOL,
                CNTags.forgeBlockTag("ores"),
                CNTags.forgeBlockTag("ores_in_ground/deepslate"),
                    CNTags.forgeBlockTag("ores/uranium"),
                CNBlockTags.URANIUM_ORES.tag
            )
            .item()
            .tag(CNItemTags.URANIUM_ORES.tag,
                    CNTags.forgeItemTag("ores/uranium"))
            .build()
            .register();

    public static final BlockEntry<UraniumOreBlock> URANIUM_ORE =
        CreateNuclear.REGISTRATE.block("uranium_ore", UraniumOreBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(UraniumOreBlock.litBlockEmission())
            .simpleItem()
            .transform(pickaxeOnly())
            .loot((lt, b) -> lt.add(b,
                RegistrateBlockLootTables.createSilkTouchDispatchTable(b,
                    lt.applyExplosionDecay(b, LootItem.lootTableItem(CNItems.RAW_URANIUM)
                        .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))
            ))))
            .tag(BlockTags.NEEDS_DIAMOND_TOOL,
                BlockTags.NEEDS_IRON_TOOL,
                CNTags.forgeBlockTag("ores"),
                CNTags.forgeBlockTag("ores_in_ground/stone"),
                CNTags.forgeBlockTag("ores/uranium"),
                CNBlockTags.URANIUM_ORES.tag
            )
            .item()
            .tag(CNItemTags.URANIUM_ORES.tag,
                CNTags.forgeItemTag("ores/uranium"))
            .build()
            .register();

    public static final BlockEntry<Block> STEEL_BLOCK =
        CreateNuclear.REGISTRATE.block("steel_block", Block::new)
            .initialProperties(SharedProperties::stone)
            .transform(pickaxeOnly())
            .tag(CNTags.forgeBlockTag("storage_blocks/steel"))
            .item()
            .tag(CNTags.forgeItemTag("storage_blocks/steel"))
            .build()
            .register();

    /*public static final BlockEntry<EventTriggerBlock> TEST_EVENT_TRIGGER_BLOCK = CreateNuclear.REGISTRATE.block("test_event_trigger_block", EventTriggerBlock::new)
            .defaultBlockstate()
            .defaultLang()
            .simpleItem()
            .register();*/


    public static void register() {
        CreateNuclear.LOGGER.info("Registering ModBlocks for " + CreateNuclear.MOD_ID);
    }
}
