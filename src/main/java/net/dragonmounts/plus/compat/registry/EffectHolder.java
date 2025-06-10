package net.dragonmounts.plus.compat.registry;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;

import static net.dragonmounts.plus.common.DragonMountsShared.makeKey;

public class EffectHolder<T extends MobEffect> extends DeferredHolder<T, MobEffect> {
    private static final ObjectArrayList<EffectHolder<?>> EFFECTS = new ObjectArrayList<>();

    public static <T extends MobEffect> EffectHolder<T> registerMobEffect(String name, T effect) {
        var holder = new EffectHolder<>(makeKey(Registries.MOB_EFFECT, name), effect);
        EFFECTS.add(holder);
        return holder;
    }

    static void registerEntries(Registry<MobEffect> registry) {
        for (var entity : EFFECTS) {
            entity.register(registry);
        }
    }

    public final T effect;

    public EffectHolder(ResourceKey<MobEffect> key, T effect) {
        super(key);
        this.effect = effect;
    }

    @Override
    protected T create() {
        return this.effect;
    }
}
