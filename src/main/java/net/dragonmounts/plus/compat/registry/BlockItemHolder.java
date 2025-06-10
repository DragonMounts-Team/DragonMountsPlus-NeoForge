package net.dragonmounts.plus.compat.registry;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public class BlockItemHolder<B extends Block, I extends Item> extends DeferredHolder<I, Item> implements ItemLike {
    private static final ObjectArrayList<BlockItemHolder<?, ?>> ITEMS = new ObjectArrayList<>();

    public static <B extends Block, I extends Item> BlockItemHolder<B, I> registerItem(BlockHolder<B> block, BiFunction<B, Item.Properties, I> factory) {
        var holder = new BlockItemHolder<>(block, factory);
        ITEMS.add(holder);
        return holder;
    }

    static void registerEntries(Registry<Item> registry) {
        for (var entity : ITEMS) {
            entity.register(registry);
        }
    }

    private final BiFunction<B, Item.Properties, I> factory;
    public final BlockHolder<B> block;

    public BlockItemHolder(BlockHolder<B> block, BiFunction<B, Item.Properties, I> factory) {
        super(ResourceKey.create(Registries.ITEM, block.key.location()));
        this.factory = factory;
        this.block = block;
    }

    @Override
    protected I create() {
        return this.factory.apply(this.block.get(), new Item.Properties().setId(this.key).useBlockDescriptionPrefix());
    }

    @Override
    public @NotNull Item asItem() {
        return this.get();
    }
}