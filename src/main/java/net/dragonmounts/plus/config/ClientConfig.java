package net.dragonmounts.plus.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import static net.dragonmounts.plus.config.EntryBuilder.config;

public class ClientConfig {
    public static final ClientConfig INSTANCE = new ClientConfig();
    public final ModConfigSpec spec;
    public final ModConfigSpec.BooleanValue debug;
    public final ModConfigSpec.DoubleValue cameraDistance;
    public final ModConfigSpec.DoubleValue cameraOffset;
    public final ModConfigSpec.BooleanValue convergePitchAngle;
    public final ModConfigSpec.BooleanValue convergeYawAngle;
    public final ModConfigSpec.BooleanValue hoverState;
    public final ModConfigSpec.BooleanValue toggleDescending;
    public final ModConfigSpec.BooleanValue toggleBreathing;
    public final ModConfigSpec.BooleanValue pauseOnWhistle;

    private ClientConfig() {
        var builder = new ModConfigSpec.Builder();
        this.debug = config(builder.gameRestart(), "debug", false, "Debug mode. You need to restart Minecraft for the change to take effect. Unless you're a developer or are told to activate it, you don't want to set this to true.");
        this.cameraDistance = config(builder, "cameraDistance", 20.0, 0.0, 64.0, "Zoom out for third person 2 while riding the the dragon and dragon carriages DO NOT EXAGGERATE IF YOU DON'T WANT CORRUPTED WORLDS");
        this.cameraOffset = config(builder, "cameraOffset", 0.0, -32.0, 32.0, "Third Person Camera Horizontal Offset");
        this.convergePitchAngle = config(builder, "convergePitchAngle", true, "Pitch Angle Convergence");
        this.convergeYawAngle = config(builder, "convergeYawAngle", true, "Yaw Angle Convergence");
        this.hoverState = config(builder, "hoverState", true, "Enables hover state for dragons");
        this.toggleDescending = builder.translation("key.dragonmounts.plus.descend")
                .comment("Enables players to keep dragon descending")
                .define("toggleDescending", false);
        this.toggleBreathing = builder.translation("key.dragonmounts.plus.breathe")
                .comment("Enables players to keep dragon breathing")
                .define("toggleBreathing", false);
        this.pauseOnWhistle = config(builder, "pauseOnWhistle", true, "Whether to try to pause the game when using whistle");
        this.spec = builder.build();
    }

    public void register(ModContainer mod) {
        mod.registerConfig(ModConfig.Type.CLIENT, this.spec);
    }
}
