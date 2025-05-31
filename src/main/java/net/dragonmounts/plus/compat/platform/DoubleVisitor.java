package net.dragonmounts.plus.compat.platform;

import net.minecraft.world.level.GameRules;

public interface DoubleVisitor {
    void dragonmounts$plus$visitDouble(GameRules.Key<DoubleRule> key, GameRules.Type<DoubleRule> type);

    GameRules.VisitorCaller<DoubleRule> FACTORY = (visitor, key, type) -> {
        if (visitor instanceof DoubleVisitor) {
            ((DoubleVisitor) visitor).dragonmounts$plus$visitDouble(key, type);
        }
    };
}
