package net.dragonmounts.plus.config;

import com.google.common.collect.HashBiMap;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dragonmounts.plus.compat.platform.ServerNetworkHandler;
import net.dragonmounts.plus.config.network.S2CBooleanConfigPayload;
import net.dragonmounts.plus.config.network.S2CDoubleConfigPayload;
import net.dragonmounts.plus.config.network.S2CSyncConfigPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static net.dragonmounts.plus.config.EntryBuilder.formatName;

public class ServerConfig {
    public static final ServerConfig INSTANCE = new ServerConfig();
    public final ModConfigSpec server;
    public final ModConfigSpec startup;
    public final ModConfigSpec.BooleanValue debug;
    public final ModConfigSpec.BooleanValue isEggPushable;
    public final ModConfigSpec.BooleanValue isEggOverridden;
    public final ModConfigSpec.BooleanValue ignitingBreath;
    public final ModConfigSpec.BooleanValue destructiveBreath;
    public final ModConfigSpec.BooleanValue smeltingBreath;
    public final ModConfigSpec.BooleanValue quenchingBreath;
    public final ModConfigSpec.BooleanValue frostyBreath;
    public final ModConfigSpec.DoubleValue baseHealth;
    public final ModConfigSpec.DoubleValue baseDamage;
    public final ModConfigSpec.DoubleValue baseArmor;
    protected final HashBiMap<ModConfigSpec.ConfigValue<?>, Integer> entries;

    private ServerConfig() {
        var entries = HashBiMap.<ModConfigSpec.ConfigValue<?>, Integer>create();
        var server = new EntryBuilder(entries, new ModConfigSpec.Builder());
        var startup = new EntryBuilder(entries, new ModConfigSpec.Builder());
        this.debug = server.config("debug", (builder, key) -> builder
                .comment("Debug mode. You need to restart Minecraft for the change to take effect. Unless you're a developer or are told to activate it, you don't want to set this to true.")
                .worldRestart()
                .define(key, false)
        );
        this.isEggPushable = server.config("isEggPushable", (builder, key) -> builder
                .comment("Whether an egg is pushable on collision")
                .define(key, false)
        );
        this.isEggOverridden = server.config("isEggOverridden", (builder, key) -> builder
                .comment("Whether interaction hook about vanilla dragon egg is enabled")
                .define(key, true)
        );
        this.ignitingBreath = server.config("ignitingBreath", (builder, key) -> builder
                .comment("Whether fire-like dragon breath can ignite the hit blocks")
                .define(key, false)
        );
        this.destructiveBreath = server.config("destructiveBreath", (builder, key) -> builder
                .comment("Whether airflow-like dragon breath can destroy the hit blocks")
                .define(key, true)
        );
        this.smeltingBreath = server.config("smeltingBreath", (builder, key) -> builder
                .comment("Whether fire-like dragon breath can smelt the hit blocks")
                .define(key, false)
        );
        this.quenchingBreath = server.config("quenchingBreath", (builder, key) -> builder
                .comment("Whether mist-like dragon breath can put out fire and solidify lava")
                .define(key, true)
        );
        this.frostyBreath = server.config("frostyBreath", (builder, key) -> builder
                .comment("Whether blizzard-like dragon breath can leave snow on ground")
                .define(key, false)
        );
        this.baseHealth = startup.config("baseHealth", (builder, key) -> builder
                .comment("The base health of a newly spawned dragon at adulthood")
                .defineInRange(key, 90.0, 1.0, 1024.0)
        );
        this.baseDamage = startup.config("baseDamage", (builder, key) -> builder
                .comment("The base damage of a newly spawned dragon at adulthood")
                .defineInRange(key, 12.0, 0.0, 2048.0)
        );
        this.baseArmor = startup.config("baseArmor", (builder, key) -> builder
                .comment("The base armor of a newly spawned dragon at adulthood")
                .defineInRange(key, 8.0, 0.0, 30.0)
        );
        this.server = server.builder().build();
        this.startup = startup.builder().build();
        this.entries = entries;
    }

    public ModConfigSpec.ConfigValue<?> getEntry(int id) {
        return this.entries.inverse().get(id);
    }

    public Collection<ModConfigSpec.ConfigValue<?>> getEntries() {
        return this.entries.keySet();
    }

    public void broadcast(@NotNull ModConfigSpec.ConfigValue<?> entry) {
        Integer id = this.entries.get(entry);
        if (id == null) return;
        switch (entry) {
            case ModConfigSpec.BooleanValue value ->
                    ServerNetworkHandler.sendToAll(null, new S2CBooleanConfigPayload(id, value.get()));
            case ModConfigSpec.DoubleValue value ->
                    ServerNetworkHandler.sendToAll(null, new S2CDoubleConfigPayload(id, value.get()));
            default -> {}
        }
    }

    public void sync(ServerPlayer player) {
        var entries = new ObjectArrayList<S2CSyncConfigPayload.Entry>();
        var values = this.startup.getValues();
        for (var entry : this.entries.entrySet()) {
            if (!values.contains(entry.getKey().getPath())) continue;
            switch (entry) {
                case ModConfigSpec.BooleanValue value ->
                        entries.add(new S2CSyncConfigPayload.Entry(entry.getValue(), ByteTag.valueOf(value.get())));
                case ModConfigSpec.DoubleValue value ->
                        entries.add(new S2CSyncConfigPayload.Entry(entry.getValue(), DoubleTag.valueOf(value.get())));
                default -> {}
            }
        }
        ServerNetworkHandler.sendTo(player, new S2CSyncConfigPayload(entries));
    }

    public <T extends ArgumentBuilder<CommandSourceStack, T>> T appendCommands(T command) {
        for (var entry : this.getEntries()) {
            switch (entry) {
                case ModConfigSpec.BooleanValue value -> command.then(buildCommand(value));
                case ModConfigSpec.DoubleValue value -> command.then(buildCommand(value));
                default -> {}
            }
        }
        return command;
    }

    public void register(ModContainer mod) {
        mod.registerConfig(ModConfig.Type.SERVER, this.server);
        mod.registerConfig(ModConfig.Type.STARTUP, this.startup);
    }

    public static String getAsString(ModConfigSpec.ConfigValue<?> entry) {
        return entry instanceof ModConfigSpec.BooleanValue ? String.valueOf(entry.get()) : entry.get().toString();
    }

    public ArgumentBuilder<CommandSourceStack, ?> buildCommand(ModConfigSpec.BooleanValue entry) {
        return Commands.literal(formatName(entry)).executes(context -> query(context, entry))
                .then(Commands.argument("value", BoolArgumentType.bool()).executes(context ->
                        modify(context, entry, BoolArgumentType.getBool(context, "value"))
                ));
    }

    public ArgumentBuilder<CommandSourceStack, ?> buildCommand(ModConfigSpec.DoubleValue entry) {
        ModConfigSpec.Range<Double> range = entry.getSpec().getRange();
        return Commands.literal(formatName(entry)).executes(context -> query(context, entry))
                .then(Commands.argument("value", range == null
                        ? DoubleArgumentType.doubleArg()
                        : DoubleArgumentType.doubleArg(range.getMin(), range.getMax())
                ).executes(context ->
                        modify(context, entry, DoubleArgumentType.getDouble(context, "value"))
                ));
    }

    public <T> int modify(CommandContext<CommandSourceStack> context, ModConfigSpec.ConfigValue<T> entry, T value) {
        entry.set(value);
        this.broadcast(entry);
        entry.save();
        context.getSource().sendSuccess(() -> Component.translatable(
                "commands.dragonmounts.plus.config.modify",
                getDisplayName(entry),
                getAsString(entry)
        ), true);
        return 1;
    }

    public static int query(CommandContext<CommandSourceStack> context, ModConfigSpec.ConfigValue<?> entry) {
        context.getSource().sendSuccess(() -> Component.translatable(
                "commands.dragonmounts.plus.config.query",
                getDisplayName(entry),
                getAsString(entry)
        ), true);
        return 1;
    }

    public static MutableComponent getDisplayName(ModConfigSpec.ConfigValue<?> entry) {
        var name = entry.getSpec().getTranslationKey();
        if (name == null) {
            return ComponentUtils.wrapInSquareBrackets(Component.literal(formatName(entry)));
        }
        return ComponentUtils.wrapInSquareBrackets(Component.translatable(name)).withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT, Component.translatable(name + ".tooltip"))
        ).withColor(ChatFormatting.GREEN));
    }
}
