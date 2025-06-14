package net.dragonmounts.plus.compat.platform;

import net.dragonmounts.plus.common.api.CommandOutput;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.ClientCommandSourceStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class PlatformCompat {
    public static boolean isClientSide() {
        return FMLLoader.getDist().isClient();
    }

    public static @Nullable CommandOutput wrapAsOutput(Object object) {
        return object instanceof ClientCommandSourceStack source ? new CommandOutput() {
            @Override
            public void sendSuccess(Supplier<Component> message) {
                source.sendSuccess(message, true);
            }

            @Override
            public void sendFailure(Component message) {
                source.sendFailure(message);
            }
        } : null;
    }
}
