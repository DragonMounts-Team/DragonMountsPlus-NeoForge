package net.dragonmounts.plus.data;

import net.dragonmounts.plus.common.DragonMountsShared;
import net.dragonmounts.plus.common.tag.DMBiomeTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class DMBiomeTagProvider extends BiomeTagsProvider {
    public DMBiomeTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider, DragonMountsShared.NAMESPACE);
    }

    @Override
    protected void addTags(@NotNull HolderLookup.Provider registries) {
        this.tag(DMBiomeTags.HAS_AETHER_DRAGON_NEST)
                .addTag(BiomeTags.IS_OCEAN);
        this.tag(DMBiomeTags.HAS_ENCHANTED_DRAGON_NEST)
                .addTag(BiomeTags.IS_END);
        this.tag(DMBiomeTags.HAS_FIRE_DRAGON_NEST)
                .addTag(BiomeTags.IS_MOUNTAIN);
        this.tag(DMBiomeTags.HAS_FOREST_DRAGON_NEST)
                .add(Biomes.PLAINS)
                .add(Biomes.MEADOW)
                .addTag(BiomeTags.IS_JUNGLE)
                .addTag(BiomeTags.IS_FOREST);
        this.tag(DMBiomeTags.HAS_ICE_DRAGON_NEST)
                .add(Biomes.ICE_SPIKES)
                .add(Biomes.SNOWY_BEACH)
                .add(Biomes.SNOWY_PLAINS)
                .add(Biomes.SNOWY_SLOPES)
                .add(Biomes.SNOWY_TAIGA);
        this.tag(DMBiomeTags.HAS_MOONLIGHT_DRAGON_NEST)
                .addTag(BiomeTags.IS_OCEAN);
        this.tag(DMBiomeTags.HAS_NETHER_DRAGON_NEST)
                .addTag(BiomeTags.IS_NETHER);
        this.tag(DMBiomeTags.HAS_SKELETON_DRAGON_NEST)
                .addTag(BiomeTags.IS_NETHER);
        this.tag(DMBiomeTags.HAS_SUNLIGHT_DRAGON_NEST)
                .add(Biomes.DESERT);
        this.tag(DMBiomeTags.HAS_TERRA_DRAGON_NEST)
                .addTag(BiomeTags.IS_BADLANDS);
        this.tag(DMBiomeTags.HAS_WATER_DRAGON_NEST)
                .add(Biomes.SWAMP)
                .addTag(BiomeTags.IS_OCEAN);
        this.tag(DMBiomeTags.HAS_ZOMBIE_DRAGON_NEST)
                .addTag(BiomeTags.IS_NETHER);
    }
}
