package net.dragonmounts.plus.client;

import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;

import static net.minecraft.client.OptionInstance.BOOLEAN_VALUES;

public class LazyBooleanConfigOption extends AbstractLazyConfigOption<Boolean> {
    public static final OptionInstance.CaptionBasedToString<Boolean> TOGGLE_STRINGIFIER;

    static {
        Component toggle = Component.translatable("options.key.toggle");
        Component hold = Component.translatable("options.key.hold");
        TOGGLE_STRINGIFIER = ($, config) -> config ? toggle : hold;
    }

    public LazyBooleanConfigOption(String caption, ModConfigSpec.BooleanValue config, Component tooltip, OptionInstance.CaptionBasedToString<Boolean> stringifier) {
        super(caption, config, tooltip, stringifier);
    }

    @Override
    public OptionInstance<Boolean> makeInstance() {
        return new OptionInstance<>(this.caption, this, this.stringifier, BOOLEAN_VALUES, this.config.get(), this.config::set);
    }
}
