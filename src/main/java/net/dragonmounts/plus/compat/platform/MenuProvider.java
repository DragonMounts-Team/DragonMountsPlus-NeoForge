package net.dragonmounts.plus.compat.platform;

import net.minecraft.server.level.ServerPlayer;

public interface MenuProvider<D> extends net.minecraft.world.MenuProvider {
    /// unused
    D getScreenOpeningData(ServerPlayer player);
}