package net.dragonmounts.plus.compat.registry;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public class DeferredBlockItem<B extends Block, I extends Item> extends DeferredHolder<I, Item> implements ItemLike {
    private static final ObjectArrayList<DeferredBlockItem<?, ?>> ITEMS = new ObjectArrayList<>();

    public static <B extends Block, I extends Item> DeferredBlockItem<B, I> registerItem(DeferredBlock<B> block, BiFunction<B, Item.Properties, I> factory) {
        var holder = new DeferredBlockItem<>(block, factory);
        ITEMS.add(holder);
        return holder;
    }

    static void registerEntries(RegisterEvent.RegisterHelper<Item> registry) {
        for (var entity : ITEMS) {
            entity.register(registry);
        }
    }

    private final BiFunction<B, Item.Properties, I> factory;
    public final DeferredBlock<B> block;

    public DeferredBlockItem(DeferredBlock<B> block, BiFunction<B, Item.Properties, I> factory) {
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