package net.dragonmounts.plus.data;

import net.dragonmounts.plus.common.DragonMountsShared;
import net.dragonmounts.plus.common.init.DMStructures;
import net.dragonmounts.plus.common.tag.DMStructureTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.StructureTagsProvider;

import java.util.concurrent.CompletableFuture;

public class DMStructureTagProvider extends StructureTagsProvider {
    public DMStructureTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider, DragonMountsShared.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        this.tag(DMStructureTags.DRAGON_NESTS)
                .add(DMStructures.AETHER_DRAGON_NEST)
                .add(DMStructures.ENCHANTED_DRAGON_NEST)
                .add(DMStructures.FIRE_DRAGON_NEST)
                .add(DMStructures.FOREST_DRAGON_NEST)
                .add(DMStructures.ICE_DRAGON_NEST)
                .add(DMStructures.MOONLIGHT_DRAGON_NEST)
                .add(DMStructures.NETHER_DRAGON_NEST)
                .add(DMStructures.SKELETON_DRAGON_NEST)
                .add(DMStructures.SUNLIGHT_DRAGON_NEST)
                .add(DMStructures.TERRA_DRAGON_NEST)
                .add(DMStructures.WATER_DRAGON_NEST)
                .add(DMStructures.ZOMBIE_DRAGON_NEST);
    }
}
