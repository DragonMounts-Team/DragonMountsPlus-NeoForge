package net.dragonmounts.plus.client;

import net.dragonmounts.plus.compat.platform.DoubleRule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.worldselection.EditGameRulesScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DoubleRuleEntry extends EditGameRulesScreen.GameRuleEntry {
    private final EditBox input;

    public DoubleRuleEntry(EditGameRulesScreen screen, Component title, List<FormattedCharSequence> description, final String name, DoubleRule rule) {
        screen.super(description, title);
        this.input = new EditBox(Minecraft.getInstance().font, 10, 5, 42, 20, title.copy()
                .append(CommonComponents.NEW_LINE)
                .append(name)
                .append(CommonComponents.NEW_LINE)
        );

        this.input.setValue(Double.toString(rule.get()));
        this.input.setResponder(value -> {
            if (rule.tryDeserialize(value)) {
                this.input.setTextColor(0xE0E0E0);
                screen.clearInvalid(this);
            } else {
                this.input.setTextColor(0xFF0000);
                screen.markInvalid(this);
            }
        });

        this.children.add(this.input);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int index, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        this.renderLabel(graphics, top, left);
        this.input.setPosition(left + entryWidth - 44, top);
        this.input.render(graphics, mouseX, mouseY, tickDelta);
    }
}
