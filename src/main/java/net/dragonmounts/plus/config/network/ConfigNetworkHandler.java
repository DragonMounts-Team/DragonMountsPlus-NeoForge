package net.dragonmounts.plus.config.network;

import net.dragonmounts.plus.common.client.ClientUtil;
import net.dragonmounts.plus.config.ServerConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ConfigNetworkHandler {
    public static void handleSyncConfig(S2CSyncConfigPayload payload, IPayloadContext ignored) {
        if (ClientUtil.isRemoteServer()) {
            for (var config : payload.entries()) {
                switch (ServerConfig.INSTANCE.getEntry(config.id())) {
                    case ModConfigSpec.BooleanValue value -> value.set(config.value().getAsByte() != 0);
                    case ModConfigSpec.DoubleValue value -> value.set(config.value().getAsDouble());
                    case null, default -> {}
                }
            }
        }
    }

    public static void handleBooleanConfig(S2CBooleanConfigPayload payload, IPayloadContext context) {
        if (!context.connection().isMemoryConnection()
                && ServerConfig.INSTANCE.server.isLoaded()
                && ServerConfig.INSTANCE.getEntry(payload.id()) instanceof ModConfigSpec.BooleanValue entry
        ) {
            entry.set(payload.value());
        }
    }

    public static void handleDoubleConfig(S2CDoubleConfigPayload payload, IPayloadContext context) {
        if (!context.connection().isMemoryConnection()
                && ServerConfig.INSTANCE.server.isLoaded()
                && ServerConfig.INSTANCE.getEntry(payload.id()) instanceof ModConfigSpec.DoubleValue entry
        ) {
            entry.set(payload.value());
        }
    }
}
