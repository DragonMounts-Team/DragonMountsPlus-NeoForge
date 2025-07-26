package net.dragonmounts.plus.config;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Collection;
import java.util.List;

import static net.dragonmounts.plus.config.EntryUtil.config;
import static net.dragonmounts.plus.config.EntryUtil.formatName;

public class ClientConfig extends ConfigHolder<CommandSourceStack> {
    public static final ClientConfig INSTANCE = new ClientConfig();
    protected final List<ConfigEntry<?>> entries;
    public final ModConfigSpec spec;
    public final BooleanEntry debug;
    public final DoubleEntry cameraDistance;
    public final DoubleEntry cameraOffset;
    public final BooleanEntry convergePitchAngle;
    public final BooleanEntry convergeYawAngle;
    public final BooleanEntry hoverState;
    public final BooleanEntry toggleDescending;
    public final BooleanEntry toggleBreathing;
    public final BooleanEntry pauseOnFluting;

    private ClientConfig() {
        var builder = new ModConfigSpec.Builder();
        this.entries = List.of(
                this.debug = config(builder.gameRestart(), "debug", false, "Debug mode. You need to restart Minecraft for the change to take effect. Unless you're a developer or are told to activate it, you don't want to set this to true."),
                this.cameraDistance = config(builder, "cameraDistance", 20.0, 0.0, 64.0, "Zoom out for third person 2 while riding the the dragon and dragon carriages DO NOT EXAGGERATE IF YOU DON'T WANT CORRUPTED WORLDS"),
                this.cameraOffset = config(builder, "cameraOffset", 0.0, -32.0, 32.0, "Third Person Camera Horizontal Offset"),
                this.convergePitchAngle = config(builder, "convergePitchAngle", true, "Pitch Angle Convergence"),
                this.convergeYawAngle = config(builder, "convergeYawAngle", true, "Yaw Angle Convergence"),
                this.hoverState = config(builder, "hoverState", true, "Enables hover state for dragons"),
                this.toggleDescending = config(builder, "toggleDescending", false, "key.dragonmounts.plus.descend", "Enables players to keep dragon descending"),
                this.toggleBreathing = config(builder, "toggleBreathing", false, "key.dragonmounts.plus.breathe", "Enables players to keep dragon breathing"),
                this.pauseOnFluting = config(builder, "pauseOnFluting", true, "Whether to try to pause the game when fluting")
        );
        this.spec = builder.build();
    }

    @Override
    public Collection<ConfigEntry<?>> getEntries() {
        return this.entries;
    }

    @Override
    protected <T> ArgumentBuilder<CommandSourceStack, ?> buildCommand(ConfigEntry<T> entry) {
        return Commands.literal(formatName(entry.host)).executes(context -> {
            context.getSource().sendSuccess(() -> Component.translatable("commands.dragonmounts.plus.config.query", entry.getDisplayName(), entry.getAsString()), true);
            return 1;
        }).then(Commands.argument("value", entry.getArgument()).executes(context -> {
            entry.set(entry.parse(context, "value"));
            this.spec.save();
            context.getSource().sendSuccess(() -> Component.translatable("commands.dragonmounts.plus.config.modify", entry.getDisplayName(), entry.getAsString()), true);
            return 1;
        }));
    }

    public void register(ModContainer mod) {
        mod.registerConfig(ModConfig.Type.CLIENT, this.spec);
    }

    @Override
    public ModConfigSpec getSpec() {
        return this.spec;
    }
}
