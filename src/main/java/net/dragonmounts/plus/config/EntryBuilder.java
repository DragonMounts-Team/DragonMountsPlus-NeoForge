package net.dragonmounts.plus.config;

import com.google.common.collect.HashBiMap;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.function.BiFunction;

public record EntryBuilder(HashBiMap<ModConfigSpec.ConfigValue<?>, Integer> entries, ModConfigSpec.Builder builder) {
    public static String formatName(ModConfigSpec.ConfigValue<?> entry) {
        return String.join(".", entry.getPath());
    }

    public static String translate(String key) {
        return "options.dragonmounts.plus." + key;
    }

    public static ModConfigSpec.BooleanValue config(
            ModConfigSpec.Builder builder,
            String key,
            boolean fallback,
            String desc
    ) {
        return builder.translation(translate(key)).comment(desc).define(key, fallback);
    }

    public static ModConfigSpec.DoubleValue config(
            ModConfigSpec.Builder builder,
            String key,
            double fallback,
            double min,
            double max,
            String desc
    ) {
        return builder.translation(translate(key)).comment(desc).defineInRange(key, fallback, min, max);
    }

    public <T extends ModConfigSpec.ConfigValue<?>> T config(String key, BiFunction<ModConfigSpec.Builder, String, T> factory) {
        var entry = factory.apply(this.builder.translation(translate(key)), key);
        this.entries.put(entry, this.entries.size());
        return entry;
    }
}
