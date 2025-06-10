package net.dragonmounts.plus.data;

import net.dragonmounts.plus.common.DragonMountsShared;
import net.dragonmounts.plus.common.init.DMBlocks;
import net.dragonmounts.plus.common.tag.DMBlockTags;
import net.dragonmounts.plus.compat.registry.BlockHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.resources.ResourceLocation.fromNamespaceAndPath;

public class DMBlockTagProvider extends BlockTagsProvider {
    public static final TagKey<Block> TORCHES = TagKey.create(Registries.BLOCK, fromNamespaceAndPath("c", "torches"));

    public DMBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider, DragonMountsShared.NAMESPACE);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        this.tag(TORCHES)
                .add(Blocks.TORCH, Blocks.WALL_TORCH, Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH, Blocks.REDSTONE_TORCH, Blocks.REDSTONE_WALL_TORCH);
        this.tag(BlockTags.PIGLIN_REPELLENTS).add(DMBlocks.DRAGON_CORE.key);
        this.tag(BlockTags.DRAGON_IMMUNE).add(DMBlocks.DRAGON_CORE.key);
        this.tag(DMBlockTags.AIRFLOW_DESTRUCTIBLE)
                .addTag(BlockTags.LEAVES)
                .addTag(BlockTags.FLOWERS)
                .addTag(BlockTags.SAPLINGS)
                .addTag(BlockTags.CROPS)
                .addTag(BlockTags.CAVE_VINES)
                .addTag(BlockTags.FIRE)
                .addTag(BlockTags.SMELTS_TO_GLASS)
                .addTag(BlockTags.CONCRETE_POWDER)
                .addTag(BlockTags.SNOW)
                .addTag(Tags.Blocks.GLASS_PANES)
                .addTag(Tags.Blocks.SANDS)
                .addTag(TORCHES)
                .add(
                        // Overworld:
                        Blocks.SHORT_GRASS,
                        Blocks.FERN,
                        Blocks.DEAD_BUSH,
                        Blocks.VINE,
                        Blocks.GLOW_LICHEN,
                        Blocks.TALL_GRASS,
                        Blocks.LARGE_FERN,
                        Blocks.HANGING_ROOTS,
                        Blocks.BROWN_MUSHROOM,
                        Blocks.RED_MUSHROOM,
                        Blocks.SMALL_DRIPLEAF,
                        Blocks.BIG_DRIPLEAF,
                        Blocks.BIG_DRIPLEAF_STEM,
                        Blocks.COCOA,
                        Blocks.SWEET_BERRY_BUSH,
                        Blocks.LILY_PAD,
                        Blocks.MOSS_CARPET,
                        Blocks.PALE_MOSS_CARPET,
                        Blocks.SUGAR_CANE,
                        Blocks.CACTUS,
                        // Nether:
                        Blocks.NETHER_SPROUTS,
                        Blocks.NETHER_WART,
                        Blocks.CRIMSON_ROOTS,
                        Blocks.CRIMSON_FUNGUS,
                        Blocks.WARPED_ROOTS,
                        Blocks.WARPED_FUNGUS,
                        Blocks.TWISTING_VINES,
                        Blocks.TWISTING_VINES_PLANT,
                        Blocks.WEEPING_VINES,
                        Blocks.WEEPING_VINES_PLANT,
                        // Other:
                        Blocks.CHORUS_PLANT,
                        Blocks.COBWEB,
                        Blocks.TORCH,
                        Blocks.WALL_TORCH,
                        Blocks.SPONGE,
                        Blocks.WET_SPONGE
                );
        addAll(this.tag(DMBlockTags.DRAGON_EGGS).add(Blocks.DRAGON_EGG), DMBlocks.BUILTIN_DRAGON_EGGS);
        addAll(this.tag(DMBlockTags.DRAGON_SCALE_BLOCKS), DMBlocks.BUILTIN_DRAGON_SCALE_BLOCKS);
        this.tag(BlockTags.FEATURES_CANNOT_REPLACE).addTag(DMBlockTags.DRAGON_EGGS);
        this.tag(BlockTags.BEACON_BASE_BLOCKS).addTag(DMBlockTags.DRAGON_SCALE_BLOCKS);
    }

    static void addAll(TagAppender<Block> builder, Collection<? extends BlockHolder<?>> blocks) {
        for (var block : blocks) {
            builder.add(block.key);
        }
    }
}
