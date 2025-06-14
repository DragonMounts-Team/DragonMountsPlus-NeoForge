package net.dragonmounts.plus.client;

import net.dragonmounts.plus.common.client.gui.DoubleRange;
import net.dragonmounts.plus.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.ModConfigSpec;

import static net.dragonmounts.plus.config.EntryBuilder.formatName;
import static net.dragonmounts.plus.config.EntryBuilder.translate;
import static net.minecraft.client.OptionInstance.BOOLEAN_TO_STRING;
import static net.minecraft.client.OptionInstance.BOOLEAN_VALUES;

/// @see net.neoforged.neoforge.client.gui.ConfigurationScreen TODO
public class DMConfigScreen extends OptionsSubScreen {

    public static final OptionInstance.CaptionBasedToString<Double> X_2F_STRINGIFIER = (component, config) ->
            Options.genericValueLabel(component, Component.literal(String.format("%.2f", config)));
    public static final OptionInstance.CaptionBasedToString<Boolean> TOGGLE_STRINGIFIER;

    public static OptionInstance<Boolean> option(ModConfigSpec.BooleanValue entry) {
        return option(entry, BOOLEAN_TO_STRING, BOOLEAN_VALUES);
    }

    public static OptionInstance<Boolean> toggle(ModConfigSpec.BooleanValue entry) {
        return option(entry, TOGGLE_STRINGIFIER, BOOLEAN_VALUES);
    }

    public static OptionInstance<Double> slider(ModConfigSpec.DoubleValue entry, DoubleRange range) {
        return option(entry, X_2F_STRINGIFIER, range);
    }

    public static <T> OptionInstance<T> option(
            ModConfigSpec.ConfigValue<T> entry,
            OptionInstance.CaptionBasedToString<T> stringifier,
            OptionInstance.ValueSet<T> values
    ) {
        var prefix = translate(formatName(entry));
        var tooltip = Tooltip.create(Component.translatable(prefix + ".tooltip"));
        var defined = entry.getSpec().getTranslationKey();
        return new OptionInstance<>(defined == null ? prefix : defined, ignored -> tooltip, stringifier, values, entry.get(), entry::set);
    }

    public DMConfigScreen(ModContainer ignored, Screen lastScreen) {
        super(lastScreen, Minecraft.getInstance().options, Component.translatable("options.dragonmounts.plus.config"));
    }

    @Override
    protected void addOptions() {
        assert this.list != null;
        var client = ClientConfig.INSTANCE;
        this.list.addBig(slider(client.cameraDistance, new DoubleRange(0.0F, 64.0F, 0.25F)));
        this.list.addBig(slider(client.cameraOffset, new DoubleRange(-16.0F, 16.0F, 0.25F)));
        this.list.addSmall(
                option(client.debug),
                option(client.pauseOnWhistle),
                toggle(client.toggleDescending),
                //option(client.convergePitchAngle),
                toggle(client.toggleBreathing)
                //option(client.convergeYawAngle),
                //option(client.hoverState)
        );
    }

    @Override
    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.setScreen(this.lastScreen);
        ClientConfig.INSTANCE.spec.save();
    }

    @Override
    protected void setInitialFocus() {}

    static {
        var toggle = Component.translatable("options.key.toggle");
        var hold = Component.translatable("options.key.hold");
        TOGGLE_STRINGIFIER = ($, config) -> config ? toggle : hold;
    }
}
