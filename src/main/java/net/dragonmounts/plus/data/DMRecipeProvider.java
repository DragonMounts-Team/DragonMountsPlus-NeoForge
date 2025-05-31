package net.dragonmounts.plus.data;

import net.dragonmounts.plus.common.init.DMBlocks;
import net.dragonmounts.plus.common.init.DMItems;
import net.dragonmounts.plus.common.item.*;
import net.dragonmounts.plus.common.tag.DMItemTags;
import net.dragonmounts.plus.common.util.DragonScaleArmorSuit;
import net.dragonmounts.plus.compat.registry.DragonType;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static net.dragonmounts.plus.common.DragonMountsShared.makeKey;
import static net.minecraft.data.recipes.SimpleCookingRecipeBuilder.blasting;
import static net.minecraft.data.recipes.SimpleCookingRecipeBuilder.smelting;

public class DMRecipeProvider extends RecipeProvider {
    protected DMRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    public void buildRecipes() {
        var output = this.output;
        var registry = Registries.RECIPE;
        smelting(Ingredient.of(DMItems.IRON_DRAGON_ARMOR), RecipeCategory.MISC, Items.IRON_INGOT, 1.0F, 200)
                .unlockedBy("has_armor", has(DMItems.IRON_DRAGON_ARMOR))
                .save(output, makeKey(registry, "iron_ingot_form_smelting"));
        smelting(Ingredient.of(DMItems.GOLDEN_DRAGON_ARMOR), RecipeCategory.MISC, Items.GOLD_INGOT, 1.0F, 200)
                .unlockedBy("has_armor", has(DMItems.GOLDEN_DRAGON_ARMOR))
                .save(output, makeKey(registry, "gold_ingot_form_smelting"));
        blasting(Ingredient.of(DMItems.IRON_DRAGON_ARMOR), RecipeCategory.MISC, Items.IRON_INGOT, 1.0F, 100)
                .unlockedBy("has_armor", has(DMItems.IRON_DRAGON_ARMOR))
                .save(output, makeKey(registry, "iron_ingot_form_blasting"));
        blasting(Ingredient.of(DMItems.GOLDEN_DRAGON_ARMOR), RecipeCategory.MISC, Items.GOLD_INGOT, 1.0F, 100)
                .unlockedBy("has_armor", has(DMItems.GOLDEN_DRAGON_ARMOR))
                .save(output, makeKey(registry, "gold_ingot_form_blasting"));
        dragonArmor(output, Tags.Items.INGOTS_IRON, Tags.Items.STORAGE_BLOCKS_IRON, DMItems.IRON_DRAGON_ARMOR);
        dragonArmor(output, Tags.Items.INGOTS_GOLD, Tags.Items.STORAGE_BLOCKS_GOLD, DMItems.GOLDEN_DRAGON_ARMOR);
        dragonArmor(output, Tags.Items.GEMS_EMERALD, Tags.Items.STORAGE_BLOCKS_EMERALD, DMItems.EMERALD_DRAGON_ARMOR);
        dragonArmor(output, Tags.Items.GEMS_DIAMOND, Tags.Items.STORAGE_BLOCKS_DIAMOND, DMItems.DIAMOND_DRAGON_ARMOR);
        for (DragonType type : DragonType.REGISTRY) {
            var scales = type.getInstance(DragonScalesItem.class, null);
            if (scales == null) continue;
            dragonScaleAxe(scales, output, type.getInstance(DragonScaleAxeItem.class, null));
            dragonScaleArmors(output, scales, type.getInstance(DragonScaleArmorSuit.class, null));
            dragonScaleBow(output, scales, type.getInstance(DragonScaleBowItem.class, null));
            dragonScaleHoe(output, scales, type.getInstance(DragonScaleHoeItem.class, null));
            dragonScalePickaxe(output, scales, type.getInstance(DragonScalePickaxeItem.class, null));
            dragonScaleShovel(output, scales, type.getInstance(DragonScaleShovelItem.class, null));
            dragonScaleShield(output, scales, type.getInstance(DragonScaleShieldItem.class, null));
            dragonScaleSword(output, scales, type.getInstance(DragonScaleSwordItem.class, null));
        }
        shaped(RecipeCategory.TOOLS, DMItems.DIAMOND_SHEARS)
                .define('X', Tags.Items.GEMS_DIAMOND)
                .pattern(" X")
                .pattern("X ")
                .unlockedBy("has_diamond", has(Tags.Items.GEMS_DIAMOND))
                .save(output);
        shaped(RecipeCategory.REDSTONE, Items.DISPENSER)
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('#', Tags.Items.COBBLESTONES)
                .define('X', DMItemTags.DRAGON_SCALE_BOWS)
                .pattern("###")
                .pattern("#X#")
                .pattern("#R#")
                .unlockedBy("has_bow", has(DMItemTags.DRAGON_SCALE_BOWS))
                .save(output, makeKey(registry, getItemName(Blocks.DISPENSER)));
        shaped(RecipeCategory.TOOLS, DMItems.AMULET)
                .define('#', Tags.Items.STRINGS)
                .define('Y', Tags.Items.COBBLESTONES)
                .define('X', Tags.Items.ENDER_PEARLS)
                .pattern(" Y ")
                .pattern("#X#")
                .pattern(" # ")
                .unlockedBy("has_pearls", has(Tags.Items.ENDER_PEARLS))
                .save(output);
        shaped(RecipeCategory.DECORATIONS, DMBlocks.DRAGON_NEST)
                .define('X', Tags.Items.RODS_WOODEN)
                .pattern("XXX")
                .pattern("XXX")
                .pattern("XXX")
                .unlockedBy("has_sticks", has(Tags.Items.RODS_WOODEN))
                .save(output);
        shaped(RecipeCategory.TOOLS, DMItems.WHISTLE)
                .define('P', Tags.Items.RODS_WOODEN)
                .define('#', Tags.Items.ENDER_PEARLS)
                .define('X', Tags.Items.STRINGS)
                .pattern("P#")
                .pattern("#X")
                .unlockedBy("has_pearls", has(Tags.Items.ENDER_PEARLS))
                .save(output);
        shaped(RecipeCategory.TOOLS, Items.SADDLE)
                .define('#', Tags.Items.INGOTS_IRON)
                .define('X', Tags.Items.LEATHERS)
                .pattern(" X ")
                .pattern("X#X")
                .unlockedBy("has_leather", has(Tags.Items.LEATHERS))
                .save(output, makeKey(registry, getItemName(Items.SADDLE)));
    }

    void dragonArmor(RecipeOutput output, TagKey<Item> ingot, TagKey<Item> block, ItemLike result) {
        shaped(RecipeCategory.COMBAT, result).define('#', ingot).define('X', block).pattern("X #").pattern(" XX").pattern("## ").unlockedBy("has_ingot", has(ingot)).unlockedBy("has_block", has(block)).save(output);
    }

    void dragonScaleAxe(Item scales, RecipeOutput output, @Nullable Item result) {
        if (result == null) return;
        shaped(RecipeCategory.TOOLS, result)
                .define('#', Tags.Items.RODS_WOODEN)
                .define('X', scales)
                .pattern("XX")
                .pattern("X#")
                .pattern(" #")
                .unlockedBy("has_dragon_scales", has(scales))
                .save(output);
    }

    private void dragonScaleArmors(RecipeOutput consumer, Item scales, @Nullable DragonScaleArmorSuit suit) {
        if (suit == null) return;
        shaped(RecipeCategory.COMBAT, suit.helmet()).define('X', scales).pattern("XXX").pattern("X X").unlockedBy("has_dragon_scales", has(scales)).save(consumer);
        shaped(RecipeCategory.COMBAT, suit.chestplate()).define('X', scales).pattern("X X").pattern("XXX").pattern("XXX").unlockedBy("has_dragon_scales", has(scales)).save(consumer);
        shaped(RecipeCategory.COMBAT, suit.leggings()).define('X', scales).pattern("XXX").pattern("X X").pattern("X X").unlockedBy("has_dragon_scales", has(scales)).save(consumer);
        shaped(RecipeCategory.COMBAT, suit.boots()).define('X', scales).pattern("X X").pattern("X X").unlockedBy("has_dragon_scales", has(scales)).save(consumer);
    }

    private void dragonScaleBow(RecipeOutput consumer, Item scales, @Nullable Item result) {
        if (result == null) return;
        shaped(RecipeCategory.COMBAT, result).define('#', scales).define('X', Tags.Items.STRINGS).pattern(" #X").pattern("# X").pattern(" #X").unlockedBy("has_dragon_scales", has(scales)).save(consumer);
    }

    private void dragonScaleHoe(RecipeOutput consumer, Item scales, @Nullable Item result) {
        if (result == null) return;
        shaped(RecipeCategory.TOOLS, result).define('#', Tags.Items.RODS_WOODEN).define('X', scales).pattern("XX").pattern(" #").pattern(" #").unlockedBy("has_dragon_scales", has(scales)).save(consumer);
    }

    private void dragonScalePickaxe(RecipeOutput consumer, Item scales, @Nullable Item result) {
        if (result == null) return;
        shaped(RecipeCategory.TOOLS, result).define('#', Tags.Items.RODS_WOODEN).define('X', scales).pattern("XXX").pattern(" # ").pattern(" # ").unlockedBy("has_dragon_scales", has(scales)).save(consumer);
    }

    private void dragonScaleShield(RecipeOutput consumer, Item scales, @Nullable Item result) {
        if (result == null) return;
        shaped(RecipeCategory.COMBAT, result).define('X', Tags.Items.INGOTS_IRON).define('W', scales).pattern("WXW").pattern("WWW").pattern(" W ").unlockedBy("has_dragon_scales", has(scales)).save(consumer);
    }

    private void dragonScaleShovel(RecipeOutput consumer, Item scales, @Nullable Item result) {
        if (result == null) return;
        shaped(RecipeCategory.TOOLS, result).define('#', Tags.Items.RODS_WOODEN).define('X', scales).pattern("X").pattern("#").pattern("#").unlockedBy("has_dragon_scales", has(scales)).save(consumer);
    }

    private void dragonScaleSword(RecipeOutput consumer, Item scales, @Nullable Item result) {
        if (result == null) return;
        shaped(RecipeCategory.COMBAT, result).define('#', Tags.Items.RODS_WOODEN).define('X', scales).pattern("X").pattern("X").pattern("#").unlockedBy("has_dragon_scales", has(scales)).save(consumer);
    }

    public static final class Factory extends RecipeProvider.Runner {
        public Factory(PackOutput output, CompletableFuture<HolderLookup.Provider> future) {
            super(output, future);
        }

        @Override
        protected @NotNull RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new DMRecipeProvider(registries, output);
        }

        @Override
        public @NotNull String getName() {
            return "Dragon Mounts Recipes";
        }
    }
}
