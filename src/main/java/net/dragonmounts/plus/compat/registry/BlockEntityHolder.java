package net.dragonmounts.plus.compat.registry;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSets;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.Set;

import static net.dragonmounts.plus.common.DragonMountsShared.makeKey;

public class BlockEntityHolder<T extends BlockEntity> extends DeferredHolder<BlockEntityType<T>, BlockEntityType<?>> {
    private static final ObjectArrayList<BlockEntityHolder<?>> ENTITIES = new ObjectArrayList<>();

    public static <T extends BlockEntity> BlockEntityHolder<T> registerBlockEntity(String name, BlockEntityType.BlockEntitySupplier<T> factory, BlockHolder<?>... blocks) {
        var holder = new BlockEntityHolder<>(makeKey(Registries.BLOCK_ENTITY_TYPE, name), factory, blocks);
        ENTITIES.add(holder);
        return holder;
    }

    static void registerEntries(RegisterEvent.RegisterHelper<BlockEntityType<?>> registry) {
        for (var entity : ENTITIES) {
            entity.register(registry);
        }
    }

    public final Set<BlockHolder<?>> blocks;
    public final BlockEntityType.BlockEntitySupplier<? extends T> factory;

    public BlockEntityHolder(ResourceKey<BlockEntityType<?>> key, BlockEntityType.BlockEntitySupplier<? extends T> factory, BlockHolder<?>... blocks) {
        super(key);
        this.factory = factory;
        this.blocks = Set.of(blocks);
    }

    @Override
    protected BlockEntityType<T> create() {
        var set = new ReferenceOpenHashSet<Block>();
        for (var block : this.blocks) {
            set.add(block.get());
        }
        if (set.isEmpty()) throw new IllegalStateException();
        return new BlockEntityType<>(this.factory, ReferenceSets.unmodifiable(set));
    }
}
