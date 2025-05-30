package net.dragonmounts.plus.common.entity.breath;


import net.minecraft.world.phys.Vec3;

/**
 * Created by TGG on 7/08/2015.
 * * Models an entity which is being affected by the breath weapon
 * Every tick that an entity is exposed to the breath weapon, its "hit density" increases.
 * Keeps track of the number of ticks that the entity has been exposed to the breath.
 * Typical usage:
 * 1) Create a new BreathAffectedEntity for the entity
 * 2) Each time it is hit, call addHitDensity() proportional to the exposure + strength of the weapon
 * 3) Every tick, call decayEntityEffectTick
 * <p>
 * Query the entity's damage by
 * 1) getHitDensity
 * 2) isUnaffected
 * 3) each tick: if applyDamageThisTick() is true, apply the weapon damage now.  (This is used to space out the
 * damage so that armour doesn't protect so much (eg 20 damage delivered once per second instead of 1 damage
 * delivered twenty times per second - (a player with armour is invulnerable to that)  )
 */
public class BreathAffectedEntity implements BreathEffectHandler {
    private static final float ENTITY_DECAY_PERCENTAGE_PER_TICK = 5.0F;
    private static final float ENTITY_RESET_EFFECT_THRESHOLD = 0.01F;
    private static final int TICKS_BEFORE_DECAY_STARTS = 7; // 40
    private static final int TICKS_BETWEEN_DAMAGE_APPLICATION = 50;  // apply damage every x ticks
    private float hitDensity;
    private int timeSinceLastHit;
    private Vec3 direction = Vec3.ZERO;

    /**
     * increases the hit density of the entity
     *
     * @param increase the amount to increase the hit density by
     */
    public void addHitDensity(Vec3 direction, float increase) {
        this.direction = this.direction.add(direction.scale(increase));
        hitDensity += increase;
        timeSinceLastHit = 0;
    }

    public float getHitDensity() {
        return hitDensity;
    }

    /**
     * Gets the average side of the applied hitDensity (--> for example: if the breath weapon is a stream of water,
     * this returns the average side the water is travelling in).
     *
     * @return the side with density (i.e. not normalised - magnitude equals the hitDensity)
     */
    public Vec3 getHitDirection() {
        return this.direction;
    }

    /**
     * updates the breath weapon's effect for a given entity
     * called every tick; used to decay the cumulative effect on the entity
     * for example - an entity being gently bathed in flame might gain 0.2 every time from the beam, and lose 0.2 every
     * tick in this method.
     */
    @Override
    public boolean decayEffectTick() {
        if (++timeSinceLastHit < TICKS_BEFORE_DECAY_STARTS) return this.isUnaffected();
        return true;
    }

    /**
     * Check if this block is unaffected by the breath weapon
     *
     * @return true if the block is currently unaffected
     */
    @Override
    public boolean isUnaffected() {
        return hitDensity < ENTITY_RESET_EFFECT_THRESHOLD;
    }
}
