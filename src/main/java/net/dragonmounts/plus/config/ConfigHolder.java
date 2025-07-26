package net.dragonmounts.plus.config;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Collection;

public abstract class ConfigHolder<S> {
    public abstract ModConfigSpec getSpec();

    public abstract Collection<ConfigEntry<?>> getEntries();

    protected abstract <T> ArgumentBuilder<S, ?> buildCommand(ConfigEntry<T> entry);

    public <T extends ArgumentBuilder<S, T>> T appendCommands(T command) {
        for (var entry : this.getEntries()) {
            command.then(this.buildCommand(entry));
        }
        return command;
    }
}
