package net.dragonmounts.plus.common.entity.breath;

import net.dragonmounts.plus.common.entity.dragon.TameableDragonEntity;
import net.dragonmounts.plus.compat.registry.DragonType;

/**
 * Created by TGG on 8/07/2015.
 * Responsible for
 * - retrieving the player's selected target (based on player's input from Dragon Orb item)
 * - synchronising the player-selected target between server AI and client copy - using datawatcher
 * - rendering the breath weapon on the client
 * - performing the effects of the weapon on the server (eg burning blocks, causing damage)
 * The selection of an actual target (typically - based on the player desired target), navigation of dragon to the appropriate range,
 * turning the dragon to face the target, is done by targeting AI.
 * DragonBreathHelper is also responsible for
 * - tracking the current breath state (IDLE, STARTING, SUSTAINED BREATHING, STOPPING)
 * - sound effects
 * - adding delays for jaw open / breathing start
 * - interrupting the beam when the dragon is facing the wrong way / the angle of the beam mismatches the head angle
 * Usage:
 * 1) Create instance, providing the parent dragon entity and a datawatcher index to use for breathing
 * 2) call onLivingUpdate(), onDeath(), onDeathUpdate(), readFromNBT() and writeFromNBT() from the corresponding
 * parent entity methods
 * 3a) The AI task responsible for targeting should call getPlayerSelectedTarget() to find out what the player wants
 * the dragon to target.
 * 3b) Once the target is in range and the dragon is facing the correct side, the AI should use setBreathingTarget()
 * to commence breathing at the target
 * 4) getCurrentBreathState() and getBreathStateFractionComplete() should be called by animation routines for
 * the dragon during breath weapon (eg jaw opening)
 */
public abstract class DragonBreathHelper<T extends TameableDragonEntity> {
    public static final int BREATH_START_DURATION = 5; // ticks
    public static final int BREATH_STOP_DURATION = 5; // ticks
    public final T dragon;
    protected BreathState currentBreathState = BreathState.IDLE;
    protected DragonBreath breath;
    protected int transitionStartTick;
    protected int tickCounter = 0;

    public DragonBreathHelper(T dragon) {
        this.dragon = dragon;
    }

    public abstract void tick();

    public BreathState getCurrentBreathState() {
        return currentBreathState;
    }

    public void onTypeChange(DragonType type) {
        this.breath = type.initBreath(this.dragon);
    }

    public boolean canBreathe() {
        return this.breath != null;
    }

    protected void updateBreathState(boolean isBreathing) {
        switch (currentBreathState) {
            case IDLE -> {
                if (isBreathing) {
                    transitionStartTick = tickCounter;
                    currentBreathState = BreathState.STARTING;
                }
            }
            case STARTING -> {
                if (tickCounter - transitionStartTick >= BREATH_START_DURATION) {
                    transitionStartTick = tickCounter;
                    if (isBreathing) {
                        currentBreathState = BreathState.SUSTAIN;
                        this.onBreathStart();
                    } else {
                        currentBreathState = BreathState.STOPPING;
                        this.onBreathStop();
                    }
                }
            }
            case SUSTAIN -> {
                if (!isBreathing) {
                    transitionStartTick = tickCounter;
                    currentBreathState = BreathState.STOPPING;
                    this.onBreathStop();
                }
            }
            case STOPPING -> {
                if (tickCounter - transitionStartTick >= BREATH_STOP_DURATION) {
                    currentBreathState = BreathState.IDLE;
                }
            }
        }
    }

    protected void onBreathStart() {}

    protected void onBreathStop() {}
}