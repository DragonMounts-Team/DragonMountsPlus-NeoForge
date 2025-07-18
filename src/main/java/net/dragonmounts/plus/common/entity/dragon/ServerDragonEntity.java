package net.dragonmounts.plus.common.entity.dragon;

import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.doubles.DoubleIterators;
import net.dragonmounts.plus.common.api.BredDragonsTrigger;
import net.dragonmounts.plus.common.block.DragonCoreBlock;
import net.dragonmounts.plus.common.component.DragonFood;
import net.dragonmounts.plus.common.entity.ai.control.DragonHeadLocator;
import net.dragonmounts.plus.common.entity.ai.navigation.DragonPathNavigation;
import net.dragonmounts.plus.common.entity.breath.impl.ServerBreathHelper;
import net.dragonmounts.plus.common.init.*;
import net.dragonmounts.plus.common.inventory.DragonInventory;
import net.dragonmounts.plus.common.item.DragonEssenceItem;
import net.dragonmounts.plus.common.network.s2c.FeedDragonPayload;
import net.dragonmounts.plus.common.network.s2c.SyncDragonAgePayload;
import net.dragonmounts.plus.common.tag.DMItemTags;
import net.dragonmounts.plus.common.util.ArrayUtil;
import net.dragonmounts.plus.common.util.Segment;
import net.dragonmounts.plus.compat.platform.ServerNetworkHandler;
import net.dragonmounts.plus.compat.registry.DragonType;
import net.dragonmounts.plus.compat.registry.DragonVariant;
import net.dragonmounts.plus.config.ServerConfig;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.function.BiConsumer;

import static net.dragonmounts.plus.common.entity.dragon.DragonModelContracts.NECK_SEGMENTS;
import static net.dragonmounts.plus.common.util.EntityUtil.addOrResetEffect;
import static net.dragonmounts.plus.common.util.EntityUtil.addOrUpdateTransientModifier;
import static net.minecraft.resources.ResourceLocation.tryParse;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class ServerDragonEntity extends TameableDragonEntity {
    private final Segment[] neckSegments = ArrayUtil.fillArray(new Segment[NECK_SEGMENTS], Segment::new);
    public final DragonHeadLocator<ServerDragonEntity> headLocator = new DragonHeadLocator<>(this);

    public ServerDragonEntity(EntityType<? extends TameableDragonEntity> type, ServerLevel level) {
        super(type, level);
        this.resetAttributes();
        this.setLifeStage(DragonLifeStage.ADULT, true, false);
    }

    public ServerDragonEntity(ServerLevel level, BiConsumer<ServerLevel, ServerDragonEntity> init) {
        super(DMEntities.TAMEABLE_DRAGON.get(), level);
        this.resetAttributes();
        init.accept(level, this);
        if (this.stage != null) return;
        this.setLifeStage(DragonLifeStage.ADULT, true, false);
    }

    @Override
    protected ServerBreathHelper createBreathHelper() {
        return new ServerBreathHelper(this);
    }

    @Override
    public final Vec3 getHeadRelativeOffset(float x, float y, float z) {
        return this.headLocator.getHeadRelativeOffset(x, y, z);
    }

    @SuppressWarnings("DataFlowIssue")
    public void resetAttributes() {
        var config = ServerConfig.INSTANCE;
        AttributeMap map = this.getAttributes();
        map.getInstance(Attributes.MAX_HEALTH).setBaseValue(config.baseHealth.get());
        map.getInstance(Attributes.ATTACK_DAMAGE).setBaseValue(config.baseDamage.get());
        map.getInstance(Attributes.ARMOR).setBaseValue(config.baseArmor.get());
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new DragonPathNavigation(this, level);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString(DragonVariant.DATA_PARAMETER_KEY, this.getVariant().identifier.toString());
        if (this.stage != null) {
            tag.putString(DragonLifeStage.DATA_PARAMETER_KEY, this.stage.getSerializedName());
        }
        tag.putBoolean(AGE_LOCKED_DATA_PARAMETER_KEY, this.isAgeLocked());
        tag.putInt(SHEARED_DATA_PARAMETER_KEY, this.isSheared() ? this.shearCooldown : 0);
        var items = this.inventory.saveItems(this.registryAccess());
        if (!items.isEmpty()) {
            tag.put(DragonInventory.DATA_PARAMETER_KEY, items);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        int age = this.age;
        DragonLifeStage stage = this.stage;
        if (tag.contains(DragonLifeStage.DATA_PARAMETER_KEY)) {
            this.setLifeStage(DragonLifeStage.byName(tag.getString(DragonLifeStage.DATA_PARAMETER_KEY)), false, false);
        }
        super.readAdditionalSaveData(tag);
        if (!this.firstTick && (this.age != age || stage != this.stage)) {
            ServerNetworkHandler.sendTracking(this, new SyncDragonAgePayload(this.getId(), this.age, this.stage));
        }
        if (tag.contains(DragonVariant.DATA_PARAMETER_KEY)) {
            this.setVariant(DragonVariant.REGISTRY.getValue(tryParse(tag.getString(DragonVariant.DATA_PARAMETER_KEY))));
        } else if (tag.contains(DragonType.DATA_PARAMETER_KEY)) {
            this.setVariant(DragonType.REGISTRY.getValue(tryParse(tag.getString(DragonType.DATA_PARAMETER_KEY))).variants.draw(this.random, DragonVariants.ENDER_FEMALE, true));
        } else {
            this.applyType(this.getDragonType());
        }
        if (tag.contains(SADDLE_DATA_PARAMETER_KEY)) {
            this.inventory.saddle.setLocal(ItemStack.parseOptional(this.registryAccess(), tag.getCompound(SADDLE_DATA_PARAMETER_KEY)), true);
        }
        if (tag.contains(SHEARED_DATA_PARAMETER_KEY)) {
            this.setSheared(tag.getInt(SHEARED_DATA_PARAMETER_KEY));
        }
        if (tag.contains(AGE_LOCKED_DATA_PARAMETER_KEY)) {
            this.setAgeLocked(tag.getBoolean(AGE_LOCKED_DATA_PARAMETER_KEY));
        }
        if (tag.contains(DragonInventory.DATA_PARAMETER_KEY)) {
            this.inventory.loadItems(tag.getList(DragonInventory.DATA_PARAMETER_KEY, 10), this.registryAccess());
        }
    }

    public void spawnEssence(ItemStack stack) {
        var pos = this.blockPosition();
        var level = this.level();
        var state = DMBlocks.DRAGON_CORE.defaultBlockState().setValue(HORIZONTAL_FACING, this.getDirection());
        if (!DragonCoreBlock.tryPlaceAt(level, pos, state, stack)) {
            int y = pos.getY(), max = Math.min(y + 5, level.getMaxY());
            var mutable = pos.mutable();
            while (++y < max) {
                if (DragonCoreBlock.tryPlaceAt(level, mutable.setY(y), state, stack)) {
                    return;
                }
            }
        } else return;
        level.addFreshEntity(new ItemEntity(level, this.getX(), this.getY(), this.getZ(), stack));
    }

    @Override
    protected void checkCrystals() {
        if (this.nearestCrystal != null && this.nearestCrystal.isAlive()) {
            if (++this.crystalTicks > 0 && this.getHealth() < this.getMaxHealth()) {
                this.crystalTicks = -10;
                this.setHealth(this.getHealth() + 1.0F);
                addOrResetEffect(this, MobEffects.DAMAGE_BOOST, 300, 0, false, true, true, 101);//15s
            }
            if (this.random.nextInt(20) == 0) {
                this.nearestCrystal = this.findCrystal();
            }
        } else {
            this.nearestCrystal = this.random.nextInt(10) == 0 ? this.findCrystal() : null;
        }
    }

    @Override
    protected void applyType(DragonType type) {
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
    protected Brain.Provider<ServerDragonEntity> brainProvider() {
        return DragonAi.brainProvider();
    }

    @Override
    protected Brain<ServerDragonEntity> makeBrain(Dynamic<?> dynamic) {
        return DragonAi.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Brain<ServerDragonEntity> getBrain() {
        return (Brain<ServerDragonEntity>) super.getBrain();
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        DragonAi.tickBrain(level, this);
    }

    @Override
    public void aiStep() {
        if (this.isDeadOrDying()) {
            this.nearestCrystal = null;
        } else {
            this.checkCrystals();
        }
        if (this.shearCooldown > 0) {
            this.setSheared(this.shearCooldown - 1);
        }
        this.headLocator.tick();
        this.headLocator.calculateHeadAndNeck(
                this.neckSegments,
                this.getXRot(),
                this.yHeadRot - this.yBodyRot
        );
        this.breathHelper.tick();
        if (this.isAgeLocked()) {
            int age = this.age;
            this.age = 0;
            super.aiStep();
            this.age = age;
        } else {
            super.aiStep();
        }
        if (this.isNearGround(0.25)) {
            this.flightTicks = 0;
        } else {
            ++this.flightTicks;
        }
        this.setFlying(++this.flightTicks > LIFTOFF_THRESHOLD && !this.isBaby() && (
                this.fluidHeight.isEmpty() || DoubleIterators.all(
                        this.fluidHeight.values().doubleIterator(),
                        value -> value == 0.0
                ) || this.isRiddenByPlayer()
        ));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        boolean isOwner = this.isOwnedBy(player);
        var stack = player.getItemInHand(hand);
        if (!this.isBreathing()) {
            var food = DragonFood.getInstance(stack);
            if (food != null) {
                if (food.requiresOwner() && !isOwner) return InteractionResult.FAIL;
                var level = this.level();
                var locked = this.isAgeLocked();
                for (var effect : food.effects()) {
                    effect.apply(level, stack, this);
                }
                if (!locked) {
                    // detoxification should not ripen the dragon
                    this.ageUp(food.age(), false);
                }
                this.setHealth(this.getHealth() + food.health());
                if (isOwner) {
                    if (this.getLifeStage() == DragonLifeStage.ADULT && this.canFallInLove()) {
                        this.setInLove(player);
                    }
                } else if (!this.isTame()) {
                    if (this.random.nextFloat() < food.tamingProbability()) {
                        level.broadcastEntityEvent(this, ON_TAMING_SUCCEED);
                        this.tame(player);
                        this.setOrderedToSit(true);
                    } else {
                        level.broadcastEntityEvent(this, ON_TAMING_FAIL);
                    }
                }
                int count = stack.getCount();
                var remainder = stack.get(DataComponents.USE_REMAINDER);
                stack.consume(1, player);
                if (remainder != null) {
                    player.setItemInHand(hand, remainder.convertIntoRemainder(
                            stack,
                            count,
                            player.hasInfiniteMaterials(),
                            player::handleExtraItemsCreatedOnUse
                    ));
                }
                ServerNetworkHandler.sendTracking(this, new FeedDragonPayload(this.getId(), this.age, this.stage, stack));
                return InteractionResult.SUCCESS_SERVER;
            }
        }
        if (!isOwner) return InteractionResult.PASS;
        if (this.inventory.onInteract(stack)) return InteractionResult.SUCCESS_SERVER;
        if (stack.is(DMItemTags.BATONS)) {
            this.setOrderedToSit(!this.isOrderedToSit());
            return InteractionResult.SUCCESS_SERVER;
        }
        var result = stack.interactLivingEntity(player, this, hand);
        if (result.consumesAction()) return result;
        if (player.isSecondaryUseActive()) {
            this.openCustomInventoryScreen(player);
        } else if (this.isBaby()) {
            this.setTarget(null);
            this.getNavigation().stop();
            this.setInSittingPose(false);
            var tag = new CompoundTag();
            if (this.save(tag) && player.setEntityOnShoulder(tag)) {
                this.discard();
            }
        } else if (this.isSaddled) {
            this.setOrderedToSit(false);
            player.setYRot(this.getYRot());
            player.setXRot(this.getXRot());
            player.startRiding(this);
        } else {
            this.openCustomInventoryScreen(player);
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void thunderHit(ServerLevel level, LightningBolt bolt) {
        super.thunderHit(level, bolt);
        this.getDragonType().onThunderHit(this, bolt);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        level.broadcastEntityEvent(this, ON_ATTACK);
        return super.doHurtTarget(level, target);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (super.hurtServer(level, source, amount)) {
            if (!this.isBreathing() && this.random.nextFloat() < 0.25F) {
                level.broadcastEntityEvent(this, ON_ROAR);
            }
            if (!source.is(DamageTypes.IN_WALL)) {
                // don't just sit there!
                this.setOrderedToSit(false);
            }
            return true;
        }
        return false;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.isTame()) {
            this.spawnEssence(this.getDragonType().getInstance(DragonEssenceItem.class, DMItems.ENDER_DRAGON_ESSENCE.get())
                    .saveEntity(this, DataComponentPatch.EMPTY)
            );
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
        return new ClientboundAddEntityPacket(this, entity, (this.age << 3) | this.stage.ordinal());
    }

    @Override
    public void setLifeStage(DragonLifeStage stage, boolean reset, boolean sync) {
        var modifier = stage.makeModifier(1.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        var attributes = this.getAttributes();
        float health = this.getHealth() / this.getMaxHealth();
        addOrUpdateTransientModifier(attributes, Attributes.MAX_HEALTH, modifier);
        addOrUpdateTransientModifier(attributes, Attributes.ATTACK_DAMAGE, modifier);
        addOrUpdateTransientModifier(attributes, Attributes.ARMOR, stage.makeModifier(ServerConfig.INSTANCE.baseArmor.get(), AttributeModifier.Operation.ADD_VALUE));
        this.setHealth(health * this.getMaxHealth());
        if (this.stage == stage) return;
        this.stage = stage;
        if (reset) {
            this.refreshAge();
        }
        this.reapplyPosition();
        this.refreshDimensions();
        if (sync) {
            ServerNetworkHandler.sendTracking(this, new SyncDragonAgePayload(this.getId(), this.age, stage));
        }
    }

    @Override
    public void setAge(int age) {
        if (this.age == age) return;
        if (this.age < 0 && age >= 0 || this.age > 0 && age <= 0) {
            this.ageBoundaryReached();
        } else {
            this.age = age;
        }
        ServerNetworkHandler.sendTracking(this, new SyncDragonAgePayload(this.getId(), age, this.stage));
    }

    @Override
    protected void tickDeath() {
        if (++this.deathTime >= this.getMaxDeathTime()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    protected void addPassenger(Entity passenger) {
        boolean flag = this.isRiddenByPlayer();
        super.addPassenger(passenger);
        if (!flag && this.isRiddenByPlayer()) {
            this.getBrain().setMemory(DMMemories.IS_CONTROLLED, Unit.INSTANCE);
        }
    }

    /// @see #finalizeSpawnChildFromBreeding(ServerLevel, Animal, AgeableMob)
    @Override
    public void spawnChildFromBreeding(ServerLevel level, Animal other) {
        if (!(other instanceof ServerDragonEntity mate)) return;
        var egg = new HatchableDragonEggEntity(level);
        egg.setDragonType(this.getDragonType(), true);
        var pos = this.position();
        egg.moveTo(pos.x, pos.y, pos.z, 0.0F, 0.0F);
        var cause = this.getLoveCause();
        if (cause == null) {
            cause = mate.getLoveCause();
        }
        if (cause != null) {
            cause.awardStat(Stats.ANIMALS_BRED);
            ((BredDragonsTrigger) CriteriaTriggers.BRED_ANIMALS).dragonmounts$plus$trigger(cause, this, mate, egg);
        }
        this.setAge(6000);
        mate.setAge(6000);
        this.resetLove();
        mate.resetLove();
        level.broadcastEntityEvent(this, (byte) 18);
        if (level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            level.addFreshEntity(new ExperienceOrb(level, pos.x, pos.y, pos.z, this.getRandom().nextInt(12) + 4));
        }
        level.addFreshEntityWithPassengers(egg);
    }

    @Override
    public void checkDespawn() {
        this.noActionTime = 0;
    }

    @Override
    public boolean dismountsUnderwater() {
        return false;
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;// double insurance
    }

    @Override
    public void openCustomInventoryScreen(Player player) {
        player.openMenu(this);
    }

    /// Never called from server side
    @Override
    public void onPlayerJump(int power) {}

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }
}
