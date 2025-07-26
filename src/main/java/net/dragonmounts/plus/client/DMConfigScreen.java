package net.dragonmounts.plus.client;

import net.dragonmounts.plus.common.client.gui.DoubleRange;
import net.dragonmounts.plus.config.BooleanEntry;
import net.dragonmounts.plus.config.ClientConfig;
import net.dragonmounts.plus.config.ConfigEntry;
import net.dragonmounts.plus.config.DoubleEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;

import static net.dragonmounts.plus.config.EntryUtil.formatName;
import static net.dragonmounts.plus.config.EntryUtil.translate;
import static net.minecraft.client.OptionInstance.BOOLEAN_TO_STRING;
import static net.minecraft.client.OptionInstance.BOOLEAN_VALUES;

/// @see net.neoforged.neoforge.client.gui.ConfigurationScreen TODO
public class DMConfigScreen extends OptionsSubScreen {

    public static final OptionInstance.CaptionBasedToString<Double> X_2F_STRINGIFIER = (component, config) ->
            Options.genericValueLabel(component, Component.literal(String.format("%.2f", config)));
    public static final OptionInstance.CaptionBasedToString<Boolean> TOGGLE_STRINGIFIER;

    public static OptionInstance<Boolean> option(BooleanEntry entry) {
        return option(entry, entry.get(), BOOLEAN_VALUES, BOOLEAN_TO_STRING);
    }

    public static OptionInstance<Boolean> toggle(BooleanEntry entry) {
        return option(entry, entry.get(), BOOLEAN_VALUES, TOGGLE_STRINGIFIER);
    }

    public static OptionInstance<Double> slider(DoubleEntry entry, DoubleRange range) {
        return option(entry, entry.get(), range, X_2F_STRINGIFIER);
    }

    public static <T> OptionInstance<T> option(
            ConfigEntry<T> entry,
            T effective,
            OptionInstance.ValueSet<T> values,
            OptionInstance.CaptionBasedToString<T> stringifier
    ) {
        var host = entry.host;
        var prefix = translate(formatName(host));
        var tooltip = Tooltip.create(Component.translatable(prefix + ".tooltip"));
        var defined = host.getSpec().getTranslationKey();
        return new OptionInstance<>(defined == null ? prefix : defined, ignored -> tooltip, stringifier, values, effective, entry::set);
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
                option(client.pauseOnFluting),
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
    protected void addFooter() {
        var layout = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        layout.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> {
            ClientConfig.INSTANCE.getEntries().forEach(ConfigEntry::revert);
            this.onClose();
        }).build());
        layout.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).build());
    }

    @Override
    protected void setInitialFocus() {}

    static {
        var toggle = Component.translatable("options.key.toggle");
        var hold = Component.translatable("options.key.hold");
        TOGGLE_STRINGIFIER = ($, config) -> config ? toggle : hold;
    }
}
