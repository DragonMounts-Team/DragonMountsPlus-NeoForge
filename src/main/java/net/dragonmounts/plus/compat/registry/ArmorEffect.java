package net.dragonmounts.plus.compat.registry;

import net.dragonmounts.plus.common.capability.ArmorEffectManager;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.registries.RegistryBuilder;

import static net.dragonmounts.plus.common.DragonMountsShared.ARMOR_EFFECT;

public interface ArmorEffect {
    Registry<ArmorEffect> REGISTRY = new RegistryBuilder<>(ARMOR_EFFECT).sync(true).create();

    boolean activate(ArmorEffectManager manager, Player player, int level);
}
