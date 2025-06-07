package net.dragonmounts.plus.common.entity.dragon;

import com.mojang.logging.LogUtils;
import net.dragonmounts.plus.common.api.AutoJumpRideable;
import net.dragonmounts.plus.common.api.ConditionalShearable;
import net.dragonmounts.plus.common.api.DragonTypified;
import net.dragonmounts.plus.common.client.ClientDragonEntity;
import net.dragonmounts.plus.common.component.DragonFood;
import net.dragonmounts.plus.common.entity.ai.control.DragonBodyControl;
import net.dragonmounts.plus.common.entity.ai.control.DragonMoveControl;
import net.dragonmounts.plus.common.entity.breath.DragonBreathHelper;
import net.dragonmounts.plus.common.init.DMItems;
import net.dragonmounts.plus.common.init.DMSounds;
import net.dragonmounts.plus.common.init.DragonVariants;
import net.dragonmounts.plus.common.inventory.DragonInventory;
import net.dragonmounts.plus.common.inventory.DragonInventoryHandler;
import net.dragonmounts.plus.common.item.DragonArmorItem;
import net.dragonmounts.plus.common.item.DragonScalesItem;
import net.dragonmounts.plus.common.item.DragonSpawnEggItem;
import net.dragonmounts.plus.common.tag.DMItemTags;
import net.dragonmounts.plus.common.util.EntityUtil;
import net.dragonmounts.plus.common.util.math.MathUtil;
import net.dragonmounts.plus.compat.platform.MenuProvider;
import net.dragonmounts.plus.compat.registry.DragonType;
import net.dragonmounts.plus.compat.registry.DragonVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import static net.dragonmounts.plus.common.DragonMountsShared.makeId;
import static net.dragonmounts.plus.compat.platform.DMGameRules.*;

/**
 * @see Mule
 * @see Horse
 */
public abstract class TameableDragonEntity extends TamableAnimal implements
        MenuProvider<TameableDragonEntity>,
        HasCustomInventoryScreen,
        ConditionalShearable,
        AutoJumpRideable,
        FlyingAnimal,
        Saddleable,
        DragonTypified.Mutable {
    public static TameableDragonEntity construct(EntityType<? extends TameableDragonEntity> type, Level level) {
        return level instanceof ServerLevel server ? new ServerDragonEntity(type, server) : new ClientDragonEntity(type, level);
    }

    public static boolean isBodyArmorItem(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof DragonArmorItem;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, DEFAULT_DRAGON_BASE_HEALTH)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FLYING_SPEED, BASE_AIR_SPEED)
                .add(Attributes.MOVEMENT_SPEED, BASE_GROUND_SPEED)
                .add(Attributes.ATTACK_DAMAGE, DEFAULT_DRAGON_BASE_DAMAGE)
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.SCALE, 1.6)
                .add(Attributes.FOLLOW_RANGE, BASE_FOLLOW_RANGE)
                .add(Attributes.ARMOR, DEFAULT_DRAGON_BASE_ARMOR)
                .add(Attributes.ARMOR_TOUGHNESS, BASE_TOUGHNESS)
                .add(Attributes.JUMP_STRENGTH, 1.0)
                .add(Attributes.TEMPT_RANGE, 16.0);
    }

    public static final ResourceLocation STAGE_MODIFIER_ID = makeId("dragon_stage_bonus");
    // base attributes
    public static final double BASE_GROUND_SPEED = 0.4;
    public static final double BASE_AIR_SPEED = 0.25;
    public static final double BASE_TOUGHNESS = 30.0D;
    public static final double BASE_FOLLOW_RANGE = 64;
    public static final double BASE_FOLLOW_RANGE_FLYING = BASE_FOLLOW_RANGE * 2;
    public static final int HOME_RADIUS = 64;
    public static final double LIFTOFF_THRESHOLD = 10;
    protected static final Logger LOGGER = LogUtils.getLogger();
    // data value IDs
    private static final EntityDataAccessor<Boolean> DATA_FLYING = SynchedEntityData.defineId(TameableDragonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_BOOSTING = SynchedEntityData.defineId(TameableDragonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_AGE_LOCKED = SynchedEntityData.defineId(TameableDragonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_HOVER_DISABLED = SynchedEntityData.defineId(TameableDragonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_BREATHING = SynchedEntityData.defineId(TameableDragonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_SHEARED = SynchedEntityData.defineId(TameableDragonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_TRUST_OTHER = SynchedEntityData.defineId(TameableDragonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ItemStack> DATA_CHEST_ITEM = SynchedEntityData.defineId(TameableDragonEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<ItemStack> DATA_SADDLE_ITEM = SynchedEntityData.defineId(TameableDragonEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<DragonVariant> DATA_DRAGON_VARIANT = SynchedEntityData.defineId(TameableDragonEntity.class, DragonVariant.SERIALIZER);
    public static final String AGE_LOCKED_DATA_PARAMETER_KEY = "AgeLocked";
    public static final String FLYING_DATA_PARAMETER_KEY = "Flying";
    public static final String SADDLE_DATA_PARAMETER_KEY = "Saddle";
    public static final String SHEARED_DATA_PARAMETER_KEY = "ShearCooldown";
    private DragonType lastType;
    protected EndCrystal nearestCrystal;
    protected DragonLifeStage stage;
    protected boolean hasChest;
    protected boolean isSaddled;
    protected int flightTicks;
    protected int crystalTicks;
    protected int shearCooldown;
    public final DragonInventory inventory = new DragonInventory(
            this,
            DATA_CHEST_ITEM,
            this::setChested,
            DATA_SADDLE_ITEM,
            this::setSaddled
    );
    public final DragonBreathHelper<?> breathHelper = this.createBreathHelper();

    public TameableDragonEntity(EntityType<? extends TameableDragonEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.moveControl = new DragonMoveControl(this);
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new DragonBodyControl(this);
    }

    protected abstract DragonBreathHelper<?> createBreathHelper();

    public abstract Vec3 getHeadRelativeOffset(float x, float y, float z);

    public final float getAdjustedSize() {
        return this.getAgeScale() * this.getScale();
    }

    @Override
    public boolean canUseSlot(EquipmentSlot slot) {
        return true;
    }

    public final DragonVariant getVariant() {
        return this.entityData.get(DATA_DRAGON_VARIANT);
    }

    public final void setVariant(DragonVariant variant) {
        this.entityData.set(DATA_DRAGON_VARIANT, variant);
    }

    public boolean isNearGround(double factor) {
        var box = this.getBoundingBox();
        double offset = (box.maxY - box.minY) * factor;
        for (var shape : this.level().getBlockCollisions(this, new AABB(
                box.minX,
                box.minY - offset,
                box.minZ,
                box.maxX,
                box.maxY - offset,
                box.maxZ
        ))) {
            if (!shape.isEmpty()) return true;
        }
        return false;
    }

    public final int getMaxDeathTime() {
        return 120;
    }

    protected final void setFlying(boolean flying) {
        this.entityData.set(DATA_FLYING, flying);
        this.setNoGravity(flying);
    }

    @Override
    public final boolean isFlying() {
        return this.entityData.get(DATA_FLYING);
    }

    public final void setBoosting(boolean boosting) {
        this.entityData.set(DATA_BOOSTING, boosting);
    }

    public final float adjustSpeed(float speed) {
        return this.entityData.get(DATA_BOOSTING) ? speed * 1.5F + 0.25F : speed;
    }

    public final void setHoverDisabled(boolean disabled) {
        this.entityData.set(DATA_HOVER_DISABLED, disabled);
    }

    public final boolean isHoverDisabled() {
        return this.entityData.get(DATA_HOVER_DISABLED);
    }

    public final boolean isBreathing() {
        return this.entityData.get(DATA_BREATHING);
    }

    public final void setBreathing(boolean breathing) {
        this.entityData.set(DATA_BREATHING, breathing && this.getLifeStage().isOldEnough(DragonLifeStage.INFANT) && this.breathHelper.canBreathe());
    }

    protected abstract void checkCrystals();

    protected @Nullable EndCrystal findCrystal() {
        EndCrystal result = null;
        double min = Double.MAX_VALUE;
        for (var crystal : this.level().getEntitiesOfClass(EndCrystal.class, this.getBoundingBox().inflate(32.0))) {
            double distance = crystal.distanceToSqr(this);
            if (distance < min) {
                min = distance;
                result = crystal;
            }
        }
        return result;
    }

    //----------Entity----------

    protected final void applyType(DragonType type) {
        if (this.lastType == type) return;
        float health = this.getHealth() / this.getMaxHealth();
        if (this.lastType != null) {
            this.getAttributes().removeAttributeModifiers(this.lastType.attributes);
        }
        this.getAttributes().addTransientAttributeModifiers(type.attributes);
        this.setHealth(health * this.getMaxHealth());
        this.breathHelper.onTypeChange(type);
        this.lastType = type;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLYING, false);
        builder.define(DATA_BOOSTING, false);
        builder.define(DATA_SHEARED, false);
        builder.define(DATA_AGE_LOCKED, false);
        builder.define(DATA_HOVER_DISABLED, false);
        builder.define(DATA_BREATHING, false);
        builder.define(DATA_TRUST_OTHER, false);
        builder.define(DATA_SADDLE_ITEM, ItemStack.EMPTY);
        builder.define(DATA_CHEST_ITEM, ItemStack.EMPTY);
        builder.define(DATA_DRAGON_VARIANT, DragonVariants.ENDER_FEMALE);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        if (accessor.equals(DATA_SADDLE_ITEM)) {
            this.inventory.saddle.setLocal(this.entityData.get(DATA_SADDLE_ITEM), false);
        } else if (accessor.equals(DATA_CHEST_ITEM)) {
            this.inventory.chest.setLocal(this.entityData.get(DATA_CHEST_ITEM), false);
        } else if (accessor.equals(DATA_DRAGON_VARIANT)) {
            this.applyType(this.entityData.get(DATA_DRAGON_VARIANT).type);
        } else {
            super.onSyncedDataUpdated(accessor);
        }
    }

    @Deprecated
    @Override
    public @Nullable TameableDragonEntity getBreedOffspring(ServerLevel level, AgeableMob parent) {
        return null;
    }

    public boolean hasChest() {
        return this.hasChest;
    }

    @Override
    public final boolean isSaddled() {
        return this.isSaddled;
    }

    @Override
    public void refreshDimensions() {
        Vec3 pos = this.position();
        super.refreshDimensions();
        this.setPos(pos.x, pos.y, pos.z);
    }

    @Override
    public float getAgeScale() {
        return DragonLifeStage.getSize(this.stage, this.age);
    }

    public EntityDimensions getDefaultDimensions(Pose pose) {
        return this.getType().getDimensions().scale(DragonLifeStage.getSizeAverage(this.stage));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return !stack.isEmpty() && DragonFood.isDragonFood(stack);
    }

    /*@Override
    public boolean hurt(DamageSource source, float amount) {
        return super.hurt(source, amount);
    }*/

    @Override
    protected void dropEquipment(ServerLevel level) {
        this.inventory.dropContents(false, 0);
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        var entity = source.getEntity();
        return (entity != null && (entity == this || this.hasPassenger(entity))) || super.isInvulnerableTo(level, source) || this.getDragonType().isInvulnerableTo(source);
    }

    @Override
    protected int calculateFallDamage(float distance, float damageMultiplier) {return 0;}

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {return false;}

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {}

    public abstract void setLifeStage(DragonLifeStage stage, boolean reset, boolean sync);

    public final DragonLifeStage getLifeStage() {
        return this.stage;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(this.getDragonType().getInstance(DragonSpawnEggItem.class, DMItems.ENDER_DRAGON_SPAWN_EGG.get()));
    }

    public boolean isRiddenByPlayer() {
        return this.getFirstPassenger() instanceof Player;
    }

    @Override
    public @Nullable Player getControllingPassenger() {
        return !this.isNoAi() && this.getFirstPassenger() instanceof Player player ? player : null;
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions dimensions, float scale) {
        return this.getDragonType().locatePassenger(
                this.getPassengers().indexOf(entity),
                this.isInSittingPose()
        ).scale(scale).yRot(MathUtil.TO_RAD_FACTOR * -this.yBodyRot);
    }

    @Override
    protected Component getTypeName() {
        return this.getDragonType().getFormattedName("entity.dragonmounts.plus.dragon.name");
    }

    //----------MobEntity----------

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return !effect.is(MobEffects.WEAKNESS) && super.canBeAffected(effect);
    }

    /*@Override
    public int getMaxHeadYRot() {
        return 90;
    }*/

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        super.setItemSlot(slot, stack);
    }

    @Override
    public SlotAccess getSlot(int slot) {
        return switch (slot) {
            case 400 -> this.inventory.saddle;
            case 499 -> this.inventory.chest;
            default -> {
                var access = this.inventory.access(slot);
                yield access == SlotAccess.NULL ? super.getSlot(slot) : access;
            }
        };
    }

    //----------AgeableEntity----------

    protected void refreshAge() {
        switch (this.stage.ordinal()) {
            case 0:// NEWBORN
            case 1:// INFANT
                this.age = -this.stage.duration;
                return;
            case 2:// JUVENILE
            case 3:// PREJUVENILE
                this.age = this.stage.duration;
                return;
            default:
                this.age = 0;
        }
    }

    public void setAgeLocked(boolean locked) {
        this.entityData.set(DATA_AGE_LOCKED, locked);
    }

    public final boolean isAgeLocked() {
        return this.entityData.get(DATA_AGE_LOCKED);
    }

    @Override
    protected void ageBoundaryReached() {
        this.setLifeStage(DragonLifeStage.byId(this.stage.ordinal() + 1), true, false);
    }

    @Override
    public void ageUp(int amount, boolean forced) {
        int old = this.age;
        //Notice:                           ↓↓                      ↓↓              ↓↓           ↓↓
        if (!this.isAgeLocked() && (old < 0 && (this.age += amount) >= 0 || old > 0 && (this.age -= amount) <= 0)) {
            this.ageBoundaryReached();
            if (forced) {
                this.forcedAge += old < 0 ? -old : old;
            }
        }
    }

    @Override
    public final int getAge() {
        return this.age;
    }

    @Override
    public final void setBaby(boolean value) {
        this.setLifeStage(value ? DragonLifeStage.HATCHLING : DragonLifeStage.ADULT, true, true);
    }

    @Override
    protected int getBaseExperienceReward(ServerLevel level) {
        return 0;
    }

    public boolean isPlayerTrusted(Player player) {
        return this.isTrustingAnyPlayer();
    }

    public boolean isTrustingAnyPlayer() {
        return this.entityData.get(DATA_TRUST_OTHER);
    }

    public void setTrustingAnyPlayer(boolean state) {
        this.entityData.set(DATA_TRUST_OTHER, state);
    }

    //----------IDragonTypified.Mutable----------

    @Override
    public final void setDragonType(DragonType type, boolean reset) {
        var previous = this.getVariant();
        if (previous.type != type || reset) {
            this.setVariant(type.variants.draw(this.random, previous, true));
        }
        if (reset) {
            this.setHealth(this.getMaxHealth());
        }
    }

    @Override
    public final DragonType getDragonType() {
        return this.getVariant().type;
    }

    //----------ConditionalShearable----------

    public final boolean isSheared() {
        return this.entityData.get(DATA_SHEARED);
    }

    public final void setSheared(int cooldown) {
        this.shearCooldown = cooldown;
        this.entityData.set(DATA_SHEARED, cooldown > 0);
    }

    @Override
    public boolean readyForShearing(ServerLevel level, ItemStack stack) {
        return this.isAlive() && this.stage.ordinal() >= 2 && !this.isSheared() && stack.is(DMItemTags.HARD_SHEARS);
    }

    @Override
    public boolean shear(ServerLevel level, @Nullable Player player, ItemStack stack, BlockPos pos, SoundSource source) {
        var scale = this.getDragonType().getInstance(DragonScalesItem.class, null);
        if (scale == null) return false;
        level.playSound(player, this, DMSounds.DRAGON_PURR, source, 1.0F, 1.0F);
        var random = this.random;
        var item = this.spawnAtLocation(level, new ItemStack(scale), 1.0F);
        if (item != null) {
            item.setDeltaMovement(item.getDeltaMovement().add(
                    (random.nextFloat() - random.nextFloat()) * 0.1F,
                    random.nextFloat() * 0.05F,
                    (random.nextFloat() - random.nextFloat()) * 0.1F
            ));
        }
        this.setSheared(2500 + random.nextInt(1000));
        return true;
    }

    @Override
    public boolean isSaddleable() {
        return !this.isBaby() && this.isTame();
    }

    @Override
    public void equipSaddle(ItemStack stack, @Nullable SoundSource source) {
        this.inventory.saddle.set(stack);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        if (data instanceof DragonSpawnData $data) {
            this.setLifeStage($data.stage, true, false);
        }
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        return switch (target) {
            case ArmorStand ignored -> false;
            case TamableAnimal other -> !other.isTame() || other.getOwner() != owner;
            case AbstractHorse horse -> !horse.isTamed();
            case Player other when owner instanceof Player $owner && !$owner.canHarmPlayer(other) -> false;
            case null, default -> true;
        };
    }

    @Override
    public final boolean shouldTryTeleportToOwner() {
        var owner = this.getOwner();
        return owner != null && this.distanceToSqr(owner) >= 400.0;
    }

    //----------Player Control----------

    @Override
    protected float getFlyingSpeed() {
        return this.adjustSpeed((float) this.getAttributeValue(Attributes.FLYING_SPEED));
    }

    @Override
    public void travel(Vec3 motion) {
        if (this.isFlying()) {
            this.moveRelative(this.getFlyingSpeed(), motion);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.91F));
        } else {
            super.travel(motion);
        }
    }

    @Override
    protected void tickRidden(Player player, Vec3 input) {
        super.tickRidden(player, input);
        float rotY = this.getYRot();
        var rot = EntityUtil.getRiddenRotation(player);
        rotY += +Mth.wrapDegrees(rot.y - rotY) * 0.08F;
        this.setRot(rotY, rot.x * 1.5F);
        this.yRotO = this.yBodyRot = this.yHeadRot = rotY;
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 motion) {
        if (this.onGround()) {
            float forward = player.zza;
            return new Vec3(
                    player.xxa * 0.5F,
                    0.0,
                    forward < 0.0F ? forward * 0.25F : forward
            );
        }
        float upward = 0.0F;
        float forward = 0.0F;
        if (player.zza != 0.0F) {
            float facing = player.getXRot() * MathUtil.TO_RAD_FACTOR;
            float i = Mth.cos(facing);
            float j = -Mth.sin(facing);
            if (player.zza < 0.0F) {
                i *= -0.5F;
                j *= -0.5F;
            }

            upward = j;
            forward = i;
        }
        return new Vec3(
                player.xxa * 0.75F,
                player.jumping ? upward + 0.5F : this.isDescending() ? upward - 0.5F : upward,
                forward
        );
    }

    private void setChested(boolean chested) {
        if (!this.firstTick && chested) {
            this.playSound(DMSounds.DRAGON_CHEST, 0.5F, 1.0F);
        }
        this.hasChest = chested;
    }

    private void setSaddled(boolean saddled) {
        if (!this.firstTick && saddled) {
            this.playSound(SoundEvents.HORSE_SADDLE, 0.5F, 1.0F);
        }
        this.isSaddled = saddled;
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        return this.adjustSpeed((float) this.getAttributeValue(Attributes.MOVEMENT_SPEED));
    }

    @Override
    public boolean canJump() {
        return this.onGround() && this.isSaddled();
    }

    @Override
    public void handleStartJump(int power) {}

    @Override
    public void handleStopJump() {}

    @Override
    public TameableDragonEntity getScreenOpeningData(ServerPlayer player) {
        return this;
    }

    @Override
    public DragonInventoryHandler createMenu(int id, Inventory inventory, Player player) {
        return new DragonInventoryHandler(id, inventory, this);
    }

    @Override
    public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(this.getId());
    }
}