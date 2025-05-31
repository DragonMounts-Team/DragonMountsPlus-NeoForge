package net.dragonmounts.plus.client;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractLazyConfigOption<V> implements OptionInstance.TooltipSupplier<V> {
    public final OptionInstance.CaptionBasedToString<V> stringifier;
    public final Component tooltip;
    public final String caption;
    public final ModConfigSpec.ConfigValue<V> config;

    public AbstractLazyConfigOption(String caption, ModConfigSpec.ConfigValue<V> config, Component tooltip, OptionInstance.CaptionBasedToString<V> stringifier) {
        this.caption = caption;
        this.config = config;
        this.tooltip = tooltip;
        this.stringifier = stringifier;
    }

    @Override
    public @Nullable Tooltip apply(@NotNull V object) {
        return this.tooltip == null ? null : Tooltip.create(this.tooltip);
    }

    public abstract OptionInstance<V> makeInstance();
}
