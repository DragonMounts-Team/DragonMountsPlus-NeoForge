package net.dragonmounts.plus.compat.platform;

import net.dragonmounts.plus.common.entity.dragon.Relation;
import net.dragonmounts.plus.common.entity.dragon.ServerDragonEntity;
import net.dragonmounts.plus.common.init.DMMemories;
import net.dragonmounts.plus.common.init.DMSounds;
import net.dragonmounts.plus.common.inventory.DragonInventoryHandler;
import net.dragonmounts.plus.common.item.WhistleItem;
import net.dragonmounts.plus.common.network.c2s.*;
import net.dragonmounts.plus.common.util.ArrayUtil;
import net.dragonmounts.plus.common.util.EntityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

public class ServerNetworkHandler {
    public static void sendTo(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void sendTracking(Entity entity, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
    }

    public static void sendToAll(@Nullable MinecraftServer ignored, CustomPacketPayload payload) {
        PacketDistributor.sendToAllPlayers(payload);
    }

    public static void handleDragonRiding(ControlDragonPayload payload, IPayloadContext context) {
        if (context.player().level().getEntity(payload.id()) instanceof ServerDragonEntity dragon) {
            boolean[] flags = ArrayUtil.readFlags(payload.flags());
            dragon.setShiftKeyDown(flags[0]);
            dragon.setBoosting(flags[1]);
            dragon.setBreathing(flags[2]);
        }
    }

    public static void handleTeleportDragon(TeleportDragonPayload payload, IPayloadContext context) {
        var player = (ServerPlayer) context.player();
        var dragon = WhistleItem.getOrDeny(player, payload.dragon());
        if (dragon == null) return;
        dragon.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        var pos = payload.pos();
        if (!EntityUtil.teleportToAround(dragon, pos.getX(), pos.getY(), pos.getZ())) {
            player.sendSystemMessage(Component.translatable("message.dragonmounts.plus.whistle.invalid_pos"), true);
        }
        player.level().playSound(null, player, DMSounds.WHISTLE_BLOW_LONG, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static void handleToggleSitting(ToggleSittingByUUIDPayload payload, IPayloadContext context) {
        var player = (ServerPlayer) context.player();
        var dragon = WhistleItem.getOrDeny(player, payload.dragon());
        if (dragon == null) return;
        dragon.setOrderedToSit(!dragon.isOrderedToSit());
        player.level().playSound(null, player, DMSounds.WHISTLE_BLOW_SHORT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static void handleToggleSitting(ToggleSittingByIDPayload payload, IPayloadContext context) {
        var player = (ServerPlayer) context.player();
        if (player.serverLevel().getEntity(payload.dragon()) instanceof ServerDragonEntity dragon && Relation.checkRelation(dragon, player).isTrusted) {
            dragon.setOrderedToSit(!dragon.isOrderedToSit());
        }
    }

    public static void handleToggleTrust(ToggleTrustPayload payload, IPayloadContext context) {
        var player = (ServerPlayer) context.player();
        if (player.serverLevel().getEntity(payload.dragon()) instanceof ServerDragonEntity dragon && dragon.isOwnedBy(player)) {
            dragon.setTrustingAnyPlayer(!dragon.isTrustingAnyPlayer());
        }
    }

    public static void handleToggleFollowing(ToggleFollowingPayload payload, IPayloadContext context) {
        var player = (ServerPlayer) context.player();
        var dragon = WhistleItem.getOrDeny(player, payload.dragon());
        if (dragon == null) return;
        var brain = dragon.getBrain();
        if (brain.hasMemoryValue(DMMemories.DISABLED_FOLLOWING_OWNER)) {
            brain.eraseMemory(DMMemories.DISABLED_FOLLOWING_OWNER);
        } else {
            brain.setMemory(DMMemories.DISABLED_FOLLOWING_OWNER, Unit.INSTANCE);
            var walk = brain.getMemory(MemoryModuleType.WALK_TARGET).orElse(null);
            if (walk != null && walk.getTarget() instanceof EntityTracker tracker && tracker.getEntity() == player) {
                brain.eraseMemory(MemoryModuleType.WALK_TARGET);
            }
        }
        player.level().playSound(null, player, DMSounds.WHISTLE_BLOW_SHORT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static void handleRenameWhistle(RenameWhistlePayload payload, IPayloadContext context) {
        if (context.player().containerMenu instanceof DragonInventoryHandler handler) {
            handler.whistle.applyName(payload.name());
        }
    }
}