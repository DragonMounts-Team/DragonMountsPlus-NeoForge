package net.dragonmounts.plus.config;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.function.BiFunction;
import java.util.function.Predicate;

public class ServerConfig {
    public static ArgumentBuilder<CommandSourceStack, ?> buildCommand(Predicate<CommandSourceStack> permission) {
        return Commands.literal("config").requires(permission)
                .then(buildCommand(INSTANCE.debug, BoolArgumentType.bool(), BoolArgumentType::getBool));
    }

    public static <T> ArgumentBuilder<CommandSourceStack, ?> buildCommand(
            ModConfigSpec.ConfigValue<T> config,
            ArgumentType<T> type,
            BiFunction<CommandContext<CommandSourceStack>, String, T> function
    ) {
        var name = String.join(".", config.getPath());
        return Commands.literal(name)
                .executes(context -> {
                    context.getSource().sendSuccess(() -> Component.translatable("commands.dragonmounts.plus.config.query", name, config.get()), true);
                    return 1;
                })
                .then(Commands.argument("value", type).executes(context -> {
                    T value = function.apply(context, "value");
                    config.set(value);
                    context.getSource().sendSuccess(() -> Component.translatable("commands.dragonmounts.plus.config.set", name, value), true);
                    return 1;
                }));
    }

    public static final ServerConfig INSTANCE = new ServerConfig();
    public final ModConfigSpec.BooleanValue debug;
    private final ModConfigSpec.Builder builder;

    private ServerConfig() {
        var builder = new ModConfigSpec.Builder();
        this.debug = builder
                .comment("Debug mode. You need to restart Minecraft for the change to take effect. Unless you're a developer or are told to activate it, you don't want to set this to true.")
                .worldRestart()
                .define("debug", false);
        this.builder = builder;
    }

    public void register(ModContainer mod) {
        mod.registerConfig(ModConfig.Type.SERVER, this.builder.build());
    }
}
