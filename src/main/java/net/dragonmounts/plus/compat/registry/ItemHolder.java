package net.dragonmounts.plus.compat.registry;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static net.dragonmounts.plus.common.DragonMountsShared.makeKey;

public class ItemHolder<T extends Item> extends DeferredHolder<T, Item> implements ItemLike {
    private static final ObjectArrayList<ItemHolder<?>> ITEMS = new ObjectArrayList<>();

    public static <T extends Item> ItemHolder<T> registerItem(String name, Function<Item.Properties, T> factory) {
        var holder = new ItemHolder<>(makeKey(Registries.ITEM, name), factory);
        ITEMS.add(holder);
        return holder;
    }

    static void registerEntries(Registry<Item> registry) {
        for (var entity : ITEMS) {
            entity.register(registry);
        }
        DragonScaleArmorSuit.registerEntries(registry);
        BlockItemHolder.registerEntries(registry);
    }

    private final Function<Item.Properties, T> factory;

    public ItemHolder(ResourceKey<Item> key, Function<Item.Properties, T> factory) {
        super(key);
        this.factory = factory;
    }

    public boolean is(ItemStack stack) {
        return this.value() == stack.getItem();
    }

    @Override
    protected T create() {
        return this.factory.apply(new Item.Properties().setId(this.key));
    }

    @Override
    public @NotNull Item asItem() {
        return this.value();
    }
}
