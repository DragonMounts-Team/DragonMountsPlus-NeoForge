package net.dragonmounts.plus.client;

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
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.resources.VanillaClientListeners;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;

import static net.dragonmounts.plus.DragonMounts.LOADED_MOD_ID;
import static net.dragonmounts.plus.common.DragonMountsShared.makeId;

@EventBusSubscriber(modid = LOADED_MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class DragonMountsClient {
    public static final ResourceLocation MODEL_RELOADER = makeId("model_reloader");

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        var play = NeoForge.EVENT_BUS;
        play.addListener(GameEvents::onClientTick);
        play.addListener(GameEvents::modifyPlayerFov);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        DMKeyMappings.register(event::register);
        DMKeyMappings.DESCEND.setKeyConflictContext(KeyConflictContext.IN_GAME);
        DMKeyMappings.BREATHE.setKeyConflictContext(KeyConflictContext.IN_GAME);
    }

    @SubscribeEvent
    public static void registerReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(MODEL_RELOADER, (ResourceManagerReloadListener) (manager) -> {
            var models = Minecraft.getInstance().getEntityModels();
            for (var variant : DragonVariant.REGISTRY) {
                variant.appearance.onReload(models);
            }
        });
        event.addDependency(VanillaClientListeners.MODELS, MODEL_RELOADER);
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpecial(DMParticles.DRAGON_BREATH, BreathParticleProvider.INSTANCE);
    }

    @SubscribeEvent
    public static void registerModels(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (var model : BuiltinFactory.values()) {
            event.registerLayerDefinition(model.location, model::makeModel);
        }
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(DMBlockEntities.DRAGON_CORE.get(), DragonCoreRenderer::new);
        event.registerBlockEntityRenderer(DMBlockEntities.DRAGON_HEAD.get(), DragonHeadRenderer.INSTANCE);
        event.registerEntityRenderer(DMEntities.HATCHABLE_DRAGON_EGG.get(), DragonEggRenderer::new);
        event.registerEntityRenderer(DMEntities.TAMEABLE_DRAGON.cast(), TameableDragonRenderer::new);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(DMScreenHandlers.DRAGON_CORE, DragonCoreScreen::new);
        event.register(DMScreenHandlers.DRAGON_INVENTORY, DragonInventoryScreen::new);
    }

    @SubscribeEvent
    public static void registerSpecialRendererCodecs(RegisterSpecialModelRendererEvent event) {
        event.register(makeId("dragon_core"), DragonCoreRenderer.Unbaked.CODEC);
        event.register(makeId("dragon_head"), DragonHeadRenderer.Unbaked.CODEC);
    }

    @SubscribeEvent
    public static void registerSpecialRenderers(RegisterSpecialBlockModelRendererEvent event) {
        event.register(DMBlocks.DRAGON_CORE.get(), new DragonCoreRenderer.Unbaked(0.0F, Direction.SOUTH));
        for (var variant : DragonVariants.BUILTIN_VALUES) {
            var head = variant.head;
            var renderer = new DragonHeadRenderer.Unbaked(variant, 0.0F);
            event.register(head.standing().get(), renderer);
            event.register(head.wall().get(), renderer);
        }
    }

    static class GameEvents {
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
}