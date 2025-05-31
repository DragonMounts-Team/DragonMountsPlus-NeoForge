package net.dragonmounts.plus.compat.registry;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

public abstract class DeferredHolder<V extends T, T> implements Supplier<V> {
    public final ResourceKey<T> key;
    private V value;

    public DeferredHolder(ResourceKey<T> key) {
        this.key = key;
    }

    protected abstract V create();

    public final void register(RegisterEvent.RegisterHelper<T> helper) {
        helper.register(this.key, this.value = this.create());
    }

    public final boolean is(@Nullable T other) {
        return this.value == other;
    }

    public final boolean is(ItemStack stack) {
        return this.value == stack.getItem();
    }

    @Override
    public final V get() {
        if (this.value == null) throw new IllegalStateException();
        return this.value;
    }

    @Override
    public final boolean equals(Object other) {
        return this == other || (
                other instanceof DeferredHolder<?, ?> that && Objects.equals(key, that.key)
        );
    }

    @Override
    public final int hashCode() {
        return this.key.hashCode();
    }
}
