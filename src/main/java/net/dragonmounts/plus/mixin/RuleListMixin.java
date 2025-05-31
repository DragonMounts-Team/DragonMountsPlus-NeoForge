package net.dragonmounts.plus.mixin;

import net.dragonmounts.plus.client.DoubleRuleEntry;
import net.dragonmounts.plus.compat.platform.DoubleRule;
import net.dragonmounts.plus.compat.platform.DoubleVisitor;
import net.minecraft.client.gui.screens.worldselection.EditGameRulesScreen;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.client.gui.screens.worldselection.EditGameRulesScreen$RuleList$1")
public abstract class RuleListMixin implements DoubleVisitor {
    @Shadow
    protected abstract <T extends GameRules.Value<T>> void addEntry(GameRules.Key<T> key, EditGameRulesScreen.EntryFactory<T> factory);

    @Shadow
    @Final
    EditGameRulesScreen val$this$0;

    @Override
    public void dragonmounts$plus$visitDouble(GameRules.Key<DoubleRule> key, GameRules.Type<DoubleRule> type) {
        this.addEntry(key, (label, tooltip, description, value) ->
                new DoubleRuleEntry(this.val$this$0, label, tooltip, description, value)
        );
    }
}
