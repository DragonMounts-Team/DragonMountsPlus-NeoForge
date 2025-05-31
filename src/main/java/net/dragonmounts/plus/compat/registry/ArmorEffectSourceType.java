package net.dragonmounts.plus.compat.registry;

import com.mojang.serialization.MapCodec;
import net.dragonmounts.plus.common.api.ArmorEffectSource;
import net.dragonmounts.plus.common.component.ListBasedArmorEffectSource;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegistryBuilder;

import static net.dragonmounts.plus.common.DragonMountsShared.ARMOR_EFFECT_SOURCE;
import static net.minecraft.resources.ResourceLocation.withDefaultNamespace;

public record ArmorEffectSourceType<T extends ArmorEffectSource>(MapCodec<T> codec) {
    public static final ResourceLocation DEFAULT = withDefaultNamespace("component");
    public static final Registry<ArmorEffectSourceType<?>> REGISTRY
            = new RegistryBuilder<>(ARMOR_EFFECT_SOURCE).sync(true).defaultKey(DEFAULT).create();
    public static final ArmorEffectSourceType<ListBasedArmorEffectSource> COMPONENT =
            new ArmorEffectSourceType<>(ListBasedArmorEffectSource.CODEC);
    public static final ArmorEffectSourceType<ArmorEffectSource> BUILTIN =
            new ArmorEffectSourceType<>(MapCodec.unit(ListBasedArmorEffectSource.EMPTY));
}