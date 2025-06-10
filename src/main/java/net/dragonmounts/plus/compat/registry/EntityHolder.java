package net.dragonmounts.plus.compat.registry;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.dragonmounts.plus.common.DragonMountsShared.makeKey;

public class EntityHolder<T extends Entity> extends DeferredHolder<EntityType<T>, EntityType<?>> {
    private static final Object2ObjectOpenHashMap<EntityHolder<? extends LivingEntity>, Supplier<AttributeSupplier.Builder>> ATTRIBUTES = new Object2ObjectOpenHashMap<>();
    private static final ObjectArrayList<EntityHolder<?>> ENTITIES = new ObjectArrayList<>();

    public static <T extends Entity> EntityHolder<T> registerEntity(
            String name,
            MobCategory category,
            EntityType.EntityFactory<T> factory,
            Consumer<EntityType.Builder<T>> init
    ) {
        var builder = EntityType.Builder.of(factory, category);
        init.accept(builder);
        var holder = new EntityHolder<>(makeKey(Registries.ENTITY_TYPE, name), builder);
        ENTITIES.add(holder);
        return holder;
    }

    public static <T extends LivingEntity> EntityHolder<T> registerLivingEntity(
            String name,
            MobCategory category,
            EntityType.EntityFactory<T> factory,
            Supplier<AttributeSupplier.Builder> supplier,
            Consumer<EntityType.Builder<T>> init
    ) {
        var holder = registerEntity(name, category, factory, init);
        ATTRIBUTES.put(holder, supplier);
        return holder;
    }

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        for (var entry : ATTRIBUTES.object2ObjectEntrySet()) {
            event.put(entry.getKey().value(), entry.getValue().get().build());
        }
    }

    static void registerEntries(Registry<EntityType<?>> registry) {
        for (var entity : ENTITIES) {
            entity.register(registry);
        }
    }

    private final Function<ResourceKey<EntityType<?>>, EntityType<T>> factory;

    public EntityHolder(ResourceKey<EntityType<?>> key, EntityType.Builder<T> builder) {
        super(key);
        this.factory = builder::build;
    }

    @Override
    protected EntityType<T> create() {
        return this.factory.apply(this.key);
    }

    @SuppressWarnings("unchecked")
    public final <R extends T> EntityType<R> cast() {
        return (EntityType<R>) this.value();
    }
}
