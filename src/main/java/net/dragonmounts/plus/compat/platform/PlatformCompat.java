package net.dragonmounts.plus.compat.platform;

import net.neoforged.fml.loading.FMLLoader;

public class PlatformCompat {
    public static boolean isClientSide() {
        return FMLLoader.getDist().isClient();
    }
}
