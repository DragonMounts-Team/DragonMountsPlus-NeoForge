package net.dragonmounts.plus.compat.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
public abstract class DeferredHolder<V extends T, T> implements Supplier<V> {
    public final ResourceKey<T> key;
    private Holder<T> holder;
    private V value;

    public DeferredHolder(ResourceKey<T> key) {
        this.key = key;
    }

    public final Holder<T> wrap() {
        return Objects.requireNonNull(this.holder);
    }

    @Override
    public final V get() {
        return Objects.requireNonNull(this.value);
    }

    public final boolean is(@Nullable T other) {
        return this.value == other;
    }

    protected abstract V create();

    protected final void register(Registry<T> registry) {
        this.holder = Registry.registerForHolder(registry, this.key, this.value = this.create());
    }

    @Override
    public final boolean equals(Object other) {
        return this == other || (
                other instanceof DeferredHolder<?, ?> that && Objects.equals(this.key, that.key)
        );
    }

    @Override
    public final int hashCode() {
        return this.key.hashCode();
    }
}
