package net.dragonmounts.plus.client;


import net.dragonmounts.plus.DragonMounts;
import net.dragonmounts.plus.common.client.ClientDragonEntity;
import net.dragonmounts.plus.common.client.gui.DragonCoreScreen;
import net.dragonmounts.plus.common.client.gui.DragonInventoryScreen;
import net.dragonmounts.plus.common.client.model.dragon.BuiltinFactory;
import net.dragonmounts.plus.common.client.renderer.block.DragonCoreRenderer;
import net.dragonmounts.plus.common.client.renderer.block.DragonHeadRenderer;
import net.dragonmounts.plus.common.client.renderer.dragon.TameableDragonRenderer;
import net.dragonmounts.plus.common.client.renderer.egg.DragonEggRenderer;
import net.dragonmounts.plus.common.init.*;
import net.dragonmounts.plus.common.item.DragonScaleBowItem;
import net.dragonmounts.plus.common.network.c2s.ControlDragonPayload;
import net.dragonmounts.plus.common.util.ArrayUtil;
import net.dragonmounts.plus.compat.platform.ClientNetworkHandler;
import net.dragonmounts.plus.compat.platform.DMScreenHandlers;
import net.dragonmounts.plus.compat.registry.DragonVariant;
import net.dragonmounts.plus.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.resources.VanillaClientListeners;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;

import static net.dragonmounts.plus.common.DragonMountsShared.makeId;

@Mod(value = DragonMounts.MOD_ID, dist = Dist.CLIENT)
public class DragonMountsClient {
    public static final ResourceLocation MODEL_RELOADER = makeId("model_reloader");

    public DragonMountsClient(IEventBus modbus, ModContainer container) {
        ClientConfig.INSTANCE.register(container);
        modbus.addListener(DragonMountsClient::onClientSetup);
        modbus.addListener(DragonMountsClient::registerKeyMappings);
        modbus.addListener(DragonMountsClient::registerReloadListeners);
        modbus.addListener(DragonMountsClient::registerParticles);
        modbus.addListener(DragonMountsClient::registerModels);
        modbus.addListener(DragonMountsClient::registerRenderers);
        modbus.addListener(DragonMountsClient::registerScreens);
        modbus.addListener(DragonMountsClient::registerSpecialRendererCodecs);
        modbus.addListener(DragonMountsClient::registerSpecialRenderers);
        container.registerExtensionPoint(IConfigScreenFactory.class, DMConfigScreen::new);
    }

    public static void onClientSetup(FMLClientSetupEvent event) {
        var play = NeoForge.EVENT_BUS;
        play.addListener(DragonMountsClient::onClientTick);
        play.addListener(DragonMountsClient::modifyPlayerFov);
    }

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        DMKeyMappings.register(event::register);
        DMKeyMappings.DESCEND.setKeyConflictContext(KeyConflictContext.IN_GAME);
        DMKeyMappings.BREATHE.setKeyConflictContext(KeyConflictContext.IN_GAME);
    }

    public static void registerReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(MODEL_RELOADER, (ResourceManagerReloadListener) (manager) -> {
            var models = Minecraft.getInstance().getEntityModels();
            for (var variant : DragonVariant.REGISTRY) {
                variant.appearance.onReload(models);
            }
        });
        event.addDependency(VanillaClientListeners.MODELS, MODEL_RELOADER);
    }

    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpecial(DMParticles.DRAGON_BREATH, BreathParticleProvider.INSTANCE);
    }

    public static void registerModels(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (var model : BuiltinFactory.values()) {
            event.registerLayerDefinition(model.location, model::makeModel);
        }
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(DMBlockEntities.DRAGON_CORE.get(), DragonCoreRenderer::new);
        event.registerBlockEntityRenderer(DMBlockEntities.DRAGON_HEAD.get(), DragonHeadRenderer.INSTANCE);
        event.registerEntityRenderer(DMEntities.HATCHABLE_DRAGON_EGG.get(), DragonEggRenderer::new);
        event.registerEntityRenderer(DMEntities.TAMEABLE_DRAGON.cast(), TameableDragonRenderer::new);
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(DMScreenHandlers.DRAGON_CORE, DragonCoreScreen::new);
        event.register(DMScreenHandlers.DRAGON_INVENTORY, DragonInventoryScreen::new);
    }

    public static void registerSpecialRendererCodecs(RegisterSpecialModelRendererEvent event) {
        event.register(makeId("dragon_core"), DragonCoreRenderer.Unbaked.CODEC);
        event.register(makeId("dragon_head"), DragonHeadRenderer.Unbaked.CODEC);
    }

    public static void registerSpecialRenderers(RegisterSpecialBlockModelRendererEvent event) {
        event.register(DMBlocks.DRAGON_CORE.get(), new DragonCoreRenderer.Unbaked(0.0F, Direction.SOUTH));
        for (var variant : DragonVariants.BUILTIN_VALUES) {
            var head = variant.head;
            var renderer = new DragonHeadRenderer.Unbaked(variant, 0.0F);
            event.register(head.standing.get(), renderer);
            event.register(head.wall.get(), renderer);
        }
    }

    public static void onClientTick(ClientTickEvent.Pre event) {
        var client = Minecraft.getInstance();
        var player = client.player;
        if (player == null) return;
        if (player.getVehicle() instanceof ClientDragonEntity dragon) {
            if (player != dragon.getControllingPassenger()) return;
            int flags = ArrayUtil.compressFlags(
                    DMKeyMappings.DESCEND.isDown(),
                    client.options.keySprint.isDown(),
                    DMKeyMappings.BREATHE.isDown()
            );
            if (flags == dragon.controlFlags) return;
            dragon.controlFlags = flags;
            ClientNetworkHandler.send(new ControlDragonPayload(dragon.getId(), flags));
        }
    }

    static void modifyPlayerFov(ComputeFovModifierEvent event) {
        var player = event.getPlayer();
        if (player.isUsingItem() && player.getUseItem().getItem() instanceof DragonScaleBowItem) {
            event.setNewFovModifier(event.getFovModifier() * (
                    1.0F - Mth.square(Math.min((float) player.getTicksUsingItem() / 20.0F, 1.0F)) * 0.15F
            ));
        }
    }
}
