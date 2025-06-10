package net.dragonmounts.plus;

import net.dragonmounts.plus.common.DragonMountsShared;
import net.dragonmounts.plus.common.capability.ArmorEffectManager;
import net.dragonmounts.plus.common.capability.ArmorEffectManagerImpl;
import net.dragonmounts.plus.common.command.DMCommand;
import net.dragonmounts.plus.common.init.*;
import net.dragonmounts.plus.common.network.c2s.*;
import net.dragonmounts.plus.common.network.s2c.*;
import net.dragonmounts.plus.compat.platform.ClientNetworkHandler;
import net.dragonmounts.plus.compat.platform.DMGameRules;
import net.dragonmounts.plus.compat.platform.ServerNetworkHandler;
import net.dragonmounts.plus.compat.registry.EntityHolder;
import net.dragonmounts.plus.compat.registry.RegistryHandler;
import net.dragonmounts.plus.config.ServerConfig;
import net.dragonmounts.plus.data.*;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Collections;

import static net.dragonmounts.plus.common.block.HatchableDragonEggBlock.spawn;
import static net.dragonmounts.plus.common.util.EntityUtil.addOrMergeEffect;
import static net.dragonmounts.plus.compat.platform.DMGameRules.IS_EGG_OVERRIDDEN;

@Mod(DragonMounts.MOD_ID)
public class DragonMounts {
    public static final String MOD_ID = "dragonmounts_plus";

    public DragonMounts(IEventBus modbus, ModContainer container) {
        ServerConfig.INSTANCE.register(container);
        modbus.addListener(DragonMounts::commonSetup);
        modbus.addListener(RegistryHandler::registerRegistries);
        modbus.addListener(RegistryHandler::registerEntries);
        modbus.addListener(DragonMounts::registerAttributes);
        modbus.addListener(DragonMounts::initNetwork);
        modbus.addListener(DragonMounts::modifyCreativeTab);
        modbus.addListener(DragonMounts::gatherClientData);
        modbus.addListener(DragonMounts::gatherServerData);
        DMEntities.init();
        DMDataComponents.init();
        DMItems.init();
        DMBlocks.init();
        DMBlockEntities.init();
        DMSounds.init();
        DMActivities.init();
        DMMemories.init();
        DMSensors.init();
        DMStructures.init();
        DMParticles.init();
        DMMobEffects.init();
    }

    public static void initNetwork(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(ControlDragonPayload.TYPE, ControlDragonPayload.CODEC, ServerNetworkHandler::handleDragonRiding);
        registrar.playToServer(TeleportDragonPayload.TYPE, TeleportDragonPayload.CODEC, ServerNetworkHandler::handleTeleportDragon);
        registrar.playToServer(ToggleSittingByUUIDPayload.TYPE, ToggleSittingByUUIDPayload.CODEC, ServerNetworkHandler::handleToggleSitting);
        registrar.playToServer(ToggleSittingByIDPayload.TYPE, ToggleSittingByIDPayload.CODEC, ServerNetworkHandler::handleToggleSitting);
        registrar.playToServer(ToggleTrustPayload.TYPE, ToggleTrustPayload.CODEC, ServerNetworkHandler::handleToggleTrust);
        registrar.playToServer(ToggleFollowingPayload.TYPE, ToggleFollowingPayload.CODEC, ServerNetworkHandler::handleToggleFollowing);
        registrar.playToServer(RenameWhistlePayload.TYPE, RenameWhistlePayload.CODEC, ServerNetworkHandler::handleRenameWhistle);
        registrar.playToClient(SyncCooldownPayload.TYPE, SyncCooldownPayload.CODEC, ClientNetworkHandler::handleCooldownSync);
        registrar.playToClient(ArmorRipostePayload.TYPE, ArmorRipostePayload.CODEC, ClientNetworkHandler::handleArmorRiposte);
        registrar.playToClient(InitCooldownPayload.TYPE, InitCooldownPayload.CODEC, ClientNetworkHandler::handleCooldownInit);
        registrar.playToClient(ShakeEggPayload.TYPE, ShakeEggPayload.CODEC, ClientNetworkHandler::handleEggShake);
        registrar.playToClient(SyncDragonAgePayload.TYPE, SyncDragonAgePayload.CODEC, ClientNetworkHandler::handleDragonSync);
        registrar.playToClient(FeedDragonPayload.TYPE, FeedDragonPayload.CODEC, ClientNetworkHandler::handleFeedDragon);
        registrar.playToClient(SyncEggAgePayload.TYPE, SyncEggAgePayload.CODEC, ClientNetworkHandler::handleEggSync);
        registrar.playToClient(EggPushablePayload.TYPE, EggPushablePayload.CODEC, ClientNetworkHandler::handleEggPushable);
    }

    static void commonSetup(FMLCommonSetupEvent event) {
        var play = NeoForge.EVENT_BUS;
        play.addListener(DragonMounts::registerCommands);
        play.addListener(DragonMounts::onPlayReady);
        play.addListener(DragonMounts::onPlayerClone);
        play.addListener(DragonMounts::onDropExperience);
        play.addListener(DragonMounts::onAttackEntity);
        play.addListener(DragonMounts::onEntityHurt);
        play.addListener(DragonMounts::onPlayerInteract);
        event.enqueueWork(DMGameRules::init);
    }

    static void registerAttributes(EntityAttributeCreationEvent event) {
        EntityHolder.registerAttributes(event);
    }

    static void modifyCreativeTab(BuildCreativeModeTabContentsEvent event) {
        var tab = event.getTabKey();
        if (tab.equals(CreativeModeTabs.SPAWN_EGGS)) {
            DMItemGroups.DRAGON_SPAWN_EGGS.accept(event.getParameters(), event);
        } else if (tab.equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)) {
            DMItemGroups.DRAGON_SPAWN_EGGS.accept(event.getParameters(), event);
        }
    }

    static void registerCommands(RegisterCommandsEvent event) {
        DMCommand.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    }

    static void onPlayReady(PlayerEvent.PlayerLoggedInEvent event) {
        var player = event.getEntity();
        ((ArmorEffectManager.Provider) player).dragonmounts$plus$getManager().sendInitPacket();
        DMGameRules.sendInitPacket((ServerPlayer) player);
    }

    static void onPlayerClone(PlayerEvent.Clone event) {
        ArmorEffectManagerImpl.onPlayerClone(event.getEntity(), event.getOriginal());
    }

    static void onDropExperience(LivingExperienceDropEvent event) {
        var player = event.getAttackingPlayer();
        if (player != null && ((ArmorEffectManager.Provider) player).dragonmounts$plus$getManager().isActive(DMArmorEffects.ENCHANTED)) {
            event.setDroppedExperience((int) (event.getOriginalExperience() * 1.5F + 0.5F));//Math.ceil(original * 1.5)
        }
    }

    static void onAttackEntity(AttackEntityEvent event) {
        var player = event.getEntity();
        DMArmorEffects.meleeChanneling(player, player.level(), InteractionHand.MAIN_HAND, event.getTarget(), null);
    }

    static void onEntityHurt(LivingIncomingDamageEvent event) {
        var self = event.getEntity();
        if (!(self instanceof ArmorEffectManager.Provider)) return;
        var level = (ServerLevel) self.level();
        var ice = DMArmorEffects.ICE;
        var nether = DMArmorEffects.NETHER;
        var manager = ((ArmorEffectManager.Provider) self).dragonmounts$plus$getManager();
        var iceFlag = manager.isActive(ice) && manager.getCooldown(ice) <= 0;
        var netherFlag = manager.isActive(nether) && manager.getCooldown(nether) <= 0;
        int flag = (iceFlag ? 0b01 : 0b00) | (netherFlag ? 0b10 : 0b00);
        if (flag == 0) return;
        var entities = level.getEntities(self, self.getBoundingBox().inflate(5.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR);
        if (entities.isEmpty()) return;
        var freeze = level.damageSources().freeze();
        for (var entity : entities) {
            if (entity instanceof LivingEntity target) {
                target.knockback(0.4F, 1, 1);
                if (iceFlag) {
                    addOrMergeEffect(target, MobEffects.MOVEMENT_SLOWDOWN, 200, 1, false, true, true);
                    entity.invulnerableTime = 0;
                    entity.hurtServer(level, freeze, 1F);
                }
            } else if (iceFlag) {
                entity.invulnerableTime = 0;
                entity.hurtServer(level, freeze, 1F);
            }
            if (netherFlag) {
                int current = entity.getRemainingFireTicks();
                entity.setRemainingFireTicks(current > 0 ? current + 200 : 200);
            }
        }
        if (iceFlag) {
            manager.setCooldown(ice, ice.cooldown);
        }
        if (netherFlag) {
            manager.setCooldown(nether, nether.cooldown);
        }
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(self, new ArmorRipostePayload(self.getId(), flag));
    }

    static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        var level = event.getLevel();
        if (level instanceof ServerLevel server && !level.dimension().equals(Level.END) && server.getGameRules().getBoolean(IS_EGG_OVERRIDDEN)) {
            var pos = event.getPos();
            if (level.getBlockState(pos).getBlock() == Blocks.DRAGON_EGG) {
                event.setUseBlock(TriState.FALSE);
                spawn(level, pos, DragonTypes.ENDER, true);
            }
        }
    }

    public static void gatherCommonData(GatherDataEvent event) {
        event.createDatapackRegistryObjects(new RegistrySetBuilder()
                        .add(Registries.STRUCTURE, DMStructures::bootstrap)
                        .add(Registries.STRUCTURE_SET, DMStructureSets::bootstrap),
                Collections.singleton(DragonMountsShared.NAMESPACE)
        );
        event.createProvider(DMRecipeProvider.Factory::new);
        event.createProvider(DMLootProvider::new);
        event.createProvider(DMBiomeTagProvider::new);
        event.createProvider(DMEntityTagProvider::new);
        event.createProvider(DMStructureTagProvider::new);
        event.createBlockAndItemTags(DMBlockTagProvider::new, DMItemTagProvider::new);
    }

    public static void gatherClientData(GatherDataEvent.Client event) {
        event.createProvider(DMModelProvider::new);
        event.createProvider(DMEquipmentAssetProvider::new);
        gatherCommonData(event);
    }

    public static void gatherServerData(GatherDataEvent.Server event) {
        gatherCommonData(event);
    }
}
