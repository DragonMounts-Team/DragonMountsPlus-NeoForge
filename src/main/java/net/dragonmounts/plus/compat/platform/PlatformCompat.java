package net.dragonmounts.plus.compat.platform;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.loading.FMLLoader;

import java.util.function.Supplier;

public class PlatformCompat {
    public static boolean isClientSide() {
        return FMLLoader.getDist().isClient();
    }

    public static int sendSuccess(Object source, Supplier<Component> message) {
        if (source instanceof CommandSourceStack) {
            ((CommandSourceStack) source).sendSuccess(message, true);
        }
        return 1;
    }

    public static int sendFailure(Object source, Component message) {
        if (source instanceof CommandSourceStack) {
            ((CommandSourceStack) source).sendFailure(message);
        }
        return 0;
    }
}
