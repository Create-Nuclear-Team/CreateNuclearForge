package net.nuclearteam.createnuclear.foundation.data.recipe;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.data.recipe.CreateRecipeProvider;
import com.simibubi.create.foundation.utility.RegisteredObjects;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.nuclearteam.createnuclear.CNBlocks;
import net.nuclearteam.createnuclear.CNItems;
import net.nuclearteam.createnuclear.CNTags;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.content.equipment.armor.AntiRadiationArmorItem;
import net.nuclearteam.createnuclear.content.equipment.cloth.ClothItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class CNStandardRecipeGen extends CreateRecipeProvider {

    private String CRAFTING = enterFolder("crafting");
    GeneratedRecipe
        WHITE_CLOTH_FROM_STRING = create(ClothItem.Cloths.WHITE_CLOTH::getItem).unlockedBy(() -> Items.STRING)
            .viaShaped(b -> b
                    .define('#', Items.STRING)
                    .pattern("###")
                    .pattern("###")
                    .showNotification(true)
            ),

        WHITE_CLOTH_FROM_WOOL = create(ClothItem.Cloths.WHITE_CLOTH::getItem).returns(6).unlockedBy(() -> Items.WHITE_WOOL).withSuffix("_wool")
            .viaShaped(b -> b
                    .define('#', Blocks.WHITE_WOOL)
                    .pattern("###")
                    .pattern("###")
                    .showNotification(true)
            ),

        ENRICHED_SOUL_SOIL = create(CNBlocks.ENRICHED_SOUL_SOIL).unlockedBy(() -> Items.NETHER_STAR)
            .viaShaped(b -> b
                .define('S', Blocks.SOUL_SOIL)
                .define('O', Blocks.OBSIDIAN)
                .define('N', Items.NETHER_STAR)
                .pattern("SOS")
                .pattern("ONO")
                .pattern("SOS")
                .showNotification(true)
            ),

        ENRICHING_CAMPFIRE = create(CNBlocks.ENRICHING_CAMPFIRE).unlockedBy(CNBlocks.ENRICHED_SOUL_SOIL::get)
            .viaShaped(b -> b
                .define('E', CNBlocks.ENRICHED_SOUL_SOIL)
                .define('L', ItemTags.LOGS)
                .define('S', Tags.Items.RODS_WOODEN)
                .pattern(" S ")
                .pattern("SES")
                .pattern("LLL")
                .showNotification(true)
            ),



        LEAD_COMPACTING = metalCompacting(ImmutableList.of(CNItems.LEAD_NUGGET, CNItems.LEAD_INGOT, CNBlocks.LEAD_BLOCK),
            ImmutableList.of(() -> CNTags.forgeItemTag("nuggets/lead"), () -> CNTags.forgeItemTag("ingots/lead"), () -> CNTags.forgeItemTag("storage_blocks/lead"))),

        STEEL_COMPACTING = metalCompacting(ImmutableList.of(CNItems.STEEL_NUGGET, CNItems.STEEL_INGOT, CNBlocks.STEEL_BLOCK),
            ImmutableList.of(() -> CNTags.forgeItemTag("nuggets/steel"), () -> CNTags.forgeItemTag("ingots/steel"), () -> CNTags.forgeItemTag("storage_blocks/steel"))),

        RAW_LEAD_BLOCK = create(CNBlocks.RAW_LEAD_BLOCK).unlockedBy(CNItems.RAW_LEAD::get)
            .viaShaped(b -> b.define('R', CNItems.RAW_LEAD.get())
                .pattern("RRR")
                .pattern("RRR")
                .pattern("RRR")
                .showNotification(true)
            ),

        RAW_URANIUM_BLOCK = create(CNBlocks.RAW_URANIUM_BLOCK).unlockedBy(CNItems.RAW_URANIUM::get)
            .viaShaped(b -> b.define('R', CNItems.RAW_URANIUM.get())
                .pattern("RRR")
                .pattern("RRR")
                .pattern("RRR")
                .showNotification(true)
            ),

    REACTOR_BLUEPRINT_ITEM = create(CNItems.REACTOR_BLUEPRINT).unlockedBy(CNBlocks.REACTOR_CONTROLLER::get)
            .viaShaped(b -> b
                    .define('S', CNTags.forgeItemTag("ingots/steel"))
                    .define('D', AllBlocks.DISPLAY_BOARD)
                    .define('P', AllItems.PRECISION_MECHANISM)
                    .define('E', AllItems.EMPTY_SCHEMATIC)
                    .pattern("SDS")
                    .pattern("SPS")
                    .pattern("SES")
                    .showNotification(true)
            )
        ;


    private String CRAFTING_REACTOR = enterFolder("crafting/reactor");

    GeneratedRecipe
        REINFORCED_GLASS = create(CNBlocks.REINFORCED_GLASS).unlockedBy(CNBlocks.REACTOR_CASING::get)
            .viaShaped(b -> b
                    .define('G', Blocks.GLASS)
                    .define('S', CNItems.LEAD_INGOT)
                    .pattern("SGS")
                    .pattern("GSG")
                    .pattern("SGS")
                    .showNotification(true)
            )
    ;

    private String CRAFTING_ITEMS = enterFolder("crafting/items/armors");

    AntiRadiationArmorItem.DyeRecipArmorList
        ANTI_RADIATION_HELMET = new AntiRadiationArmorItem.DyeRecipArmorList(color -> create(CNItems.ANTI_RADIATION_HELMETS.get(color))
            .unlockedByTag(() -> CNTags.CNItemTags.CLOTH.tag)
            .withCategory(RecipeCategory.COMBAT)
            .viaShaped(i -> i
                    .define('X', CNTags.forgeItemTag("ingots/lead"))
                    .define('Y', ClothItem.Cloths.getByColor(color).get())
                    .define('Z', CNBlocks.REINFORCED_GLASS)
                    .pattern("YXY")
                    .pattern("XZX")
                    .showNotification(true)
            )
        ),


        ANTI_RADIATION_CHESTPLATES = new AntiRadiationArmorItem.DyeRecipArmorList(color -> create(CNItems.ANTI_RADIATION_CHESTPLATES.get(color))
            .unlockedByTag(() -> CNTags.CNItemTags.CLOTH.tag)
            .withCategory(RecipeCategory.COMBAT)
            .viaShaped(i -> i
            .define('X', CNTags.forgeItemTag("ingots/lead"))
            .define('Y', ClothItem.Cloths.getByColor(color).get())
            .define('Z', CNItems.GRAPHITE_ROD)
                    .pattern("Y Y")
                    .pattern("XXX")
                    .pattern("ZXZ")
                    .showNotification(true)
            )
        ),

        ANTI_RADIATION_LEGGINS = new AntiRadiationArmorItem.DyeRecipArmorList(color -> create(CNItems.ANTI_RADIATION_LEGGINGS.get(color))
                .unlockedByTag(() -> CNTags.CNItemTags.CLOTH.tag)
                .withCategory(RecipeCategory.COMBAT)
                .viaShaped(i -> i
                        .define('X', CNTags.forgeItemTag("ingots/lead"))
                        .define('Y', ClothItem.Cloths.getByColor(color).get())
                        .define('Z', CNBlocks.REINFORCED_GLASS)
                        .pattern("YXY")
                        .pattern("Z Z")
                        .pattern("X X")
                        .showNotification(true)
                )
        )
    ;

    GeneratedRecipe
        ANTI_RADIATION_BOOTS = create(CNItems.ANTI_RADIATION_BOOTS).unlockedByTag(() -> CNTags.CNItemTags.CLOTH.tag).withCategory(RecipeCategory.COMBAT)
            .viaShaped(b -> b
                    .define('X', CNTags.forgeItemTag("ingots/lead"))
                    .define('Y', ClothItem.Cloths.WHITE_CLOTH.getItem())
                    .pattern("Y Y")
                    .pattern("X X")
                    .showNotification(true)
            )
        ;

    private final String BLAST_FURNACE = enterFolder("blast_furnace");
    GeneratedRecipe
        URANIUM_ORE_TO_URANIUM_POWDER = blastFurnaceRecipeTags(() -> CNItems.RAW_URANIUM::get, () -> CNTags.CNItemTags.URANIUM_ORES.tag, "_for_uranium_ore", 4),
        RAW_LEAD_ORES = blastFurnaceRecipeTags(() -> CNItems.LEAD_INGOT::get, () -> CNTags.CNItemTags.LEAD_ORES.tag, "_for_lead_ore", 1),
        RAW_LEAD = blastFurnaceRecipe(CNItems.LEAD_INGOT::get, CNItems.RAW_LEAD::get, "_for_raw_lead", 1)
    ;




    String currentFolder = "";

    String enterFolder(String foldedr) {
        currentFolder = foldedr;
        return currentFolder;
    }

    GeneratedRecipeBuilder create(Supplier<ItemLike> result) {
        return new GeneratedRecipeBuilder(currentFolder, result);
    }

    GeneratedRecipeBuilder create(ResourceLocation result) {
        return new GeneratedRecipeBuilder(currentFolder, result);
    }

    GeneratedRecipeBuilder create(ItemProviderEntry<? extends  ItemLike> result) {
        return create(result::get);
    }

    GeneratedRecipe metalCompacting(List<ItemProviderEntry<? extends ItemLike>> variants,
                                    List<Supplier<TagKey<Item>>> ingredients) {
        GeneratedRecipe result = null;
        for (int i = 0; i + 1 < variants.size(); i++) {
            ItemProviderEntry<? extends ItemLike> currentEntry = variants.get(i);
            ItemProviderEntry<? extends ItemLike> nextEntry = variants.get(i + 1);
            Supplier<TagKey<Item>> currentIngredient = ingredients.get(i);
            Supplier<TagKey<Item>> nextIngredient = ingredients.get(i + 1);

            result = create(nextEntry).withSuffix("_from_compacting")
                    .unlockedBy(currentEntry::get)
                    .viaShaped(b -> b.pattern("###")
                            .pattern("###")
                            .pattern("###")
                            .define('#', currentIngredient.get()));
        }
        return result;
    }

    GeneratedRecipe blastFurnaceRecipe(Supplier<? extends ItemLike> result, Supplier<? extends ItemLike> ingredient, String suffix, int count) {
        return create(result::get).withSuffix(suffix)
                .returns(count)
                .viaCooking(ingredient::get)
                .rewardXP(.1f)
                .inBlastFurnace();
    }

    GeneratedRecipe blastFurnaceRecipeTags(Supplier<? extends ItemLike> result, Supplier<TagKey<Item>> ingredient, String suffix, int count) {
        return create(result::get).withSuffix(suffix)
                .returns(count)
                .viaCookingTag(ingredient)
                .rewardXP(.1f)
                .inBlastFurnace();
    }

    GeneratedRecipe smokerRecipe(Supplier<? extends ItemLike> result, Supplier<? extends ItemLike> ingredient, String suffix, int count) {
        return create(result::get).withSuffix(suffix)
                .returns(count)
                .viaCooking(ingredient::get)
                .rewardXP(.0f)
                .inSmoker();
    }

    GeneratedRecipe smokerRecipeTags(Supplier<? extends ItemLike> result, Supplier<TagKey<Item>> ingredient, String suffix, int count) {
        return create(result::get).withSuffix(suffix)
                .returns(count)
                .viaCookingTag(ingredient)
                .rewardXP(.0f)
                .inSmoker();
    }

    GeneratedRecipe furnaceRecipe(Supplier<? extends ItemLike> result, Supplier<? extends ItemLike> ingredient, String suffix, int count) {
        return create(result::get).withSuffix(suffix)
                .returns(count)
                .viaCooking(ingredient::get)
                .rewardXP(.1f)
                .inFurnace();
    }

    GeneratedRecipe furnaceRecipeTags(Supplier<? extends ItemLike> result, Supplier<TagKey<Item>> ingredient, String suffix, int count) {
        return create(result::get).withSuffix(suffix)
                .returns(count)
                .viaCookingTag(ingredient)
                .rewardXP(.1f)
                .inFurnace();
    }



    class GeneratedRecipeBuilder {

        private String path;
        private String suffix;
        private Supplier<? extends ItemLike> result;
        private ResourceLocation compatDatagenOutput;
        private RecipeCategory category;

        private Supplier<ItemPredicate> unlockedBy;
        private int amount;

        private GeneratedRecipeBuilder(String path) {
            this.path = path;
            this.suffix = "";
            this.amount = 1;
            this.category = RecipeCategory.MISC;
        }

        public GeneratedRecipeBuilder(String path, Supplier<? extends ItemLike> result) {
            this(path);
            this.result = result;
        }

        public GeneratedRecipeBuilder(String path, ResourceLocation result) {
            this(path);
            this.compatDatagenOutput = result;
        }

        GeneratedRecipeBuilder returns(int amount) {
            this.amount = amount;
            return this;
        }

        GeneratedRecipeBuilder unlockedBy(Supplier<? extends ItemLike> item) {
            this.unlockedBy = () -> ItemPredicate.Builder.item()
                    .of(item.get())
                    .build();
            return this;
        }

        GeneratedRecipeBuilder unlockedByTag(Supplier<TagKey<Item>> tag) {
            this.unlockedBy = () -> ItemPredicate.Builder.item()
                    .of(tag.get())
                    .build();
            return this;
        }


        GeneratedRecipeBuilder withSuffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        GeneratedRecipeBuilder withCategory(RecipeCategory category) {
            this.category = category;
            return this;
        }

        // FIXME 5.1 refactor - recipe categories as markers instead of sections?
        GeneratedRecipe viaShaped(UnaryOperator<ShapedRecipeBuilder> builder) {
            return register(consumer -> {
                ShapedRecipeBuilder b = builder.apply(ShapedRecipeBuilder.shaped(category, result.get(), amount));
                if (unlockedBy != null)
                    b.unlockedBy("has_item", inventoryTrigger(unlockedBy.get()));
                b.save(consumer, createSimpleLocation(path));
            });
        }

        GeneratedRecipe viaShapeless(UnaryOperator<ShapelessRecipeBuilder> builder) {
            return register(consumer -> {
                ShapelessRecipeBuilder b = builder.apply(ShapelessRecipeBuilder.shapeless(category, result.get(), amount));
                if (unlockedBy != null)
                    b.unlockedBy("has_item", inventoryTrigger(unlockedBy.get()));
                b.save(consumer, createSimpleLocation(path));
            });
        }

        GeneratedRecipe viaNetheriteSmithing(Supplier<? extends Item> base, Supplier<Ingredient> upgradeMaterial) {
            this.withCategory(RecipeCategory.COMBAT);
            return register(consumer -> {
                SmithingTransformRecipeBuilder b =
                        SmithingTransformRecipeBuilder.smithing(Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                                Ingredient.of(base.get()), upgradeMaterial.get(), category, result.get()
                                        .asItem());
                b.unlocks("has_item", inventoryTrigger(ItemPredicate.Builder.item()
                        .of(base.get())
                        .build()));
                b.save(consumer, createSimpleLocation(path));
            });
        }

        private ResourceLocation createSimpleLocation(String recipeType) {
            return CreateNuclear.asResource(recipeType + "/" + getRegistryName().getPath() + suffix);
        }

        private ResourceLocation createLocation(String recipeType) {
            return CreateNuclear.asResource(recipeType + "/" + path + "/" + getRegistryName().getPath() + suffix);
        }

        private ResourceLocation getRegistryName() {
            return compatDatagenOutput == null ? RegisteredObjects.getKeyOrThrow(result.get()
                    .asItem()) : compatDatagenOutput;
        }

        GeneratedRecipeBuilder.GeneratedCookingRecipeBuilder viaCooking(Supplier<? extends ItemLike> item) {
            return unlockedBy(item).viaCookingIngredient(() -> Ingredient.of(item.get()));
        }

        GeneratedRecipeBuilder.GeneratedCookingRecipeBuilder viaCookingTag(Supplier<TagKey<Item>> tag) {
            return unlockedByTag(tag).viaCookingIngredient(() -> Ingredient.of(tag.get()));
        }

        GeneratedRecipeBuilder.GeneratedCookingRecipeBuilder viaCookingIngredient(Supplier<Ingredient> ingredient) {
            return new GeneratedRecipeBuilder.GeneratedCookingRecipeBuilder(ingredient);
        }

        class GeneratedCookingRecipeBuilder {

            private Supplier<Ingredient> ingredient;
            private float exp;
            private int cookingTime;

            private final RecipeSerializer<? extends AbstractCookingRecipe> FURNACE = RecipeSerializer.SMELTING_RECIPE,
                    SMOKER = RecipeSerializer.SMOKING_RECIPE, BLAST = RecipeSerializer.BLASTING_RECIPE,
                    CAMPFIRE = RecipeSerializer.CAMPFIRE_COOKING_RECIPE;

            GeneratedCookingRecipeBuilder(Supplier<Ingredient> ingredient) {
                this.ingredient = ingredient;
                cookingTime = 200;
                exp = 0;
            }

            GeneratedRecipeBuilder.GeneratedCookingRecipeBuilder forDuration(int duration) {
                cookingTime = duration;
                return this;
            }

            GeneratedRecipeBuilder.GeneratedCookingRecipeBuilder rewardXP(float xp) {
                exp = xp;
                return this;
            }

            GeneratedRecipe inFurnace() {
                return inFurnace(b -> b);
            }

            GeneratedRecipe inFurnace(UnaryOperator<SimpleCookingRecipeBuilder> builder) {
                return create(FURNACE, builder, 1);
            }

            GeneratedRecipe inSmoker() {
                return inSmoker(b -> b);
            }

            GeneratedRecipe inSmoker(UnaryOperator<SimpleCookingRecipeBuilder> builder) {
                create(FURNACE, builder, 1);
                create(CAMPFIRE, builder, 3);
                return create(SMOKER, builder, .5f);
            }

            GeneratedRecipe inBlastFurnace() {
                return inBlastFurnace(b -> b);
            }

            GeneratedRecipe inBlastFurnace(UnaryOperator<SimpleCookingRecipeBuilder> builder) {
                create(FURNACE, builder, 1);
                return create(BLAST, builder, .5f);
            }

            private GeneratedRecipe create(RecipeSerializer<? extends AbstractCookingRecipe> serializer,
                                           UnaryOperator<SimpleCookingRecipeBuilder> builder, float cookingTimeModifier) {
                return register(consumer -> {
                    boolean isOtherMod = compatDatagenOutput != null;

                    SimpleCookingRecipeBuilder b = builder.apply(SimpleCookingRecipeBuilder.generic(ingredient.get(),
                            RecipeCategory.MISC, isOtherMod ? Items.DIRT : result.get(), exp,
                            (int) (cookingTime * cookingTimeModifier), serializer));

                    if (unlockedBy != null)
                        b.unlockedBy("has_item", inventoryTrigger(unlockedBy.get()));

                    b.save(result -> {
                        consumer.accept(
                                isOtherMod ? new ModdedCookingRecipeResult(result, compatDatagenOutput, null )
                                        : result);
                    }, createSimpleLocation(RegisteredObjects.getKeyOrThrow(serializer)
                            .getPath()));
                });
            }
        }
    }

    public CNStandardRecipeGen(PackOutput output) {
        super(output);
    }

    private record ModdedCookingRecipeResult(FinishedRecipe wrapped, ResourceLocation outputOverride, List<ICondition> conditions) implements FinishedRecipe {
        @Override
        public ResourceLocation getId() {
            return wrapped.getId();
        }

        @Override
        public RecipeSerializer<?> getType() {
            return wrapped.getType();
        }

        @Override
        public JsonObject serializeAdvancement() {
            return wrapped.serializeAdvancement();
        }

        @Override
        public ResourceLocation getAdvancementId() {
            return wrapped.getAdvancementId();
        }

        @Override
        public void serializeRecipeData(JsonObject object) {
            wrapped.serializeRecipeData(object);
            object.addProperty("result", outputOverride.toString());

            JsonArray conds = new JsonArray();
            conditions.forEach(c -> conds.add(CraftingHelper.serialize(c)));
            object.add("conditions", conds);
        }
    }


}
