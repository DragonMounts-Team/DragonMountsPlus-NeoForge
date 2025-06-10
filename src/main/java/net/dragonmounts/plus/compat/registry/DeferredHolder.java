package net.dragonmounts.plus.compat.registry;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public abstract class DeferredHolder<V extends T, T> implements Holder<T> {
    public final ResourceKey<T> key;
    private Holder<T> holder;
    private V value;

    public DeferredHolder(ResourceKey<T> key) {
        this.key = key;
    }

    protected abstract V create();

    protected final void register(Registry<T> registry) {
        this.holder = Registry.registerForHolder(registry, this.key, this.value = this.create());
    }

    public final boolean is(@Nullable T other) {
        return this.value == other;
    }

    @Override
    public final boolean equals(@Nullable Object other) {
        return this == other || (
                other instanceof Holder<?> that
                        && that.kind() == Kind.REFERENCE
                        && Objects.equals(this.key, that.getKey())
        );
    }

    @Override
    public final int hashCode() {
        return this.key.hashCode();
    }

    @Override
    public final V value() {
        return Objects.requireNonNull(this.value);
    }

    @Override
    public boolean isBound() {
        return this.holder != null && this.holder.isBound();
    }

    @Override
    public boolean is(ResourceLocation location) {
        return this.key.location().equals(location);
    }

    @Override
    public boolean is(ResourceKey<T> key) {
        return key == this.key;
    }

    @Override
    public boolean is(Predicate<ResourceKey<T>> predicate) {
        return predicate.test(this.key);
    }

    @Override
    public boolean is(TagKey<T> tag) {
        return this.holder != null && this.holder.is(tag);
    }

    @Override
    public boolean is(Holder<T> holder) {
        return holder.is(this.key);
    }

    @Override
    public Stream<TagKey<T>> tags() {
        return this.holder == null ? Stream.empty() : this.holder.tags();
    }

    @Override
    public Either<ResourceKey<T>, T> unwrap() {
        return Either.left(this.key);
    }

    @Override
    public Optional<ResourceKey<T>> unwrapKey() {
        return Optional.of(this.key);
    }

    @Override
    public Kind kind() {
        return Kind.REFERENCE;
    }

    @Override
    public boolean canSerializeIn(HolderOwner<T> owner) {
        return this.holder != null && this.holder.canSerializeIn(owner);
    }

    @Override
    public Holder<T> getDelegate() {
        return this.holder != null ? this.holder.getDelegate() : this;
    }
}
