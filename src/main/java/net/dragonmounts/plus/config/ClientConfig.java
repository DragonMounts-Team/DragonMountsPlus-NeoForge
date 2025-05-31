package net.dragonmounts.plus.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    public static final ClientConfig INSTANCE = new ClientConfig();
    private final ModConfigSpec.Builder builder;
    public final ModConfigSpec.BooleanValue debug;
    public final ModConfigSpec.ConfigValue<Double> camera_distance;
    public final ModConfigSpec.ConfigValue<Double> camera_offset;
    public final ModConfigSpec.BooleanValue converge_pitch_angle;
    public final ModConfigSpec.BooleanValue converge_yaw_angle;
    public final ModConfigSpec.BooleanValue hover_animation;
    public final ModConfigSpec.BooleanValue toggle_descending;
    public final ModConfigSpec.BooleanValue toggle_breathing;

    private ClientConfig() {
        var builder = new ModConfigSpec.Builder();
        this.debug = builder
                .comment("Debug mode. You need to restart Minecraft for the change to take effect. Unless you're a developer or are told to activate it, you don't want to set this to true.")
                .define("debug", false);
        this.camera_distance = builder
                .comment("Zoom out for third person 2 while riding the the dragon and dragon carriages DO NOT EXAGGERATE IF YOU DON'T WANT CORRUPTED WORLDS")
                .define("camera_distance", 20.0D);
        this.camera_offset = builder
                .comment("Third Person Camera Horizontal Offset")
                .define("camera_offset", 0.0D);
        this.converge_pitch_angle = builder
                .comment("Pitch Angle Convergence")
                .define("converge_pitch_angle", true);
        this.converge_yaw_angle = builder
                .comment("Yaw Angle Convergence")
                .define("converge_yaw_angle", true);
        this.hover_animation = builder
                .comment("Enables hover animation for dragons")
                .define("hover_animation", true);
        this.toggle_descending = builder
                .comment("Enables players to keep dragon descending")
                .define("toggle_descending", false);
        this.toggle_breathing = builder
                .comment("Enables players to keep dragon breathing")
                .define("toggle_breathing", false);
        this.builder = builder;
    }

    public void register(ModContainer mod) {
        mod.registerConfig(ModConfig.Type.CLIENT, this.builder.build());
    }
}
