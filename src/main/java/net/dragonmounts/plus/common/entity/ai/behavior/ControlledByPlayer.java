package net.dragonmounts.plus.common.entity.ai.behavior;

import net.dragonmounts.plus.common.entity.dragon.ServerDragonEntity;
import net.dragonmounts.plus.common.init.DMMemories;
import net.minecraft.server.level.ServerLevel;

public class ControlledByPlayer extends GoalBehavior<ServerDragonEntity> {
    @Override
    protected boolean canUse(ServerLevel level, ServerDragonEntity dragon) {
        return true;
    }

    @Override
    protected boolean canContinueToUse(ServerLevel level, ServerDragonEntity dragon) {
        return dragon.isRiddenByPlayer();
    }

    @Override
    public void doStop(ServerLevel level, ServerDragonEntity dragon, long time) {
        super.doStop(level, dragon, time);
        dragon.getBrain().eraseMemory(DMMemories.IS_CONTROLLED);
        dragon.setBreathing(false);
    }
}
