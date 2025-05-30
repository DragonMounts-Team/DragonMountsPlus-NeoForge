package net.dragonmounts.plus.common.client.renderer.dragon;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dragonmounts.plus.common.client.ClientDragonEntity;
import net.dragonmounts.plus.common.client.model.dragon.DragonModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.client.renderer.entity.EnderDragonRenderer.renderCrystalBeams;

public class TameableDragonRenderer extends MobRenderer<ClientDragonEntity, DragonRenderState, DragonModel> {
    public TameableDragonRenderer(EntityRendererProvider.Context context) {
        super(context, null, 0);
        this.addLayer(new TameableDragonLayer(this));
    }

    @Override
    public void extractRenderState(ClientDragonEntity dragon, DragonRenderState state, float partialTick) {
        super.extractRenderState(dragon, state, partialTick);
        dragon.animator.extractRenderState(state, partialTick);
        if (state.pose == Pose.SLEEPING) {
            state.pose = Pose.SITTING;
        }
    }

    @Override
    public void render(DragonRenderState state, PoseStack matrices, MultiBufferSource buffers, int light) {
        this.model = state.variant.appearance.getModel();
        if (state.renderCrystalBeams && state.crystal != null) {
            matrices.pushPose();
            var crystal = state.crystal;
            renderCrystalBeams(
                    (float) (crystal.x - state.x),
                    (float) (crystal.y - state.y),
                    (float) (crystal.z - state.z),
                    state.ageInTicks,
                    matrices,
                    buffers,
                    light
            );
            matrices.popPose();
        }
        super.render(state, matrices, buffers, light);
    }

    @Override
    protected void scale(DragonRenderState state, PoseStack matrices) {
        super.scale(state, matrices);
        float scale = state.ageScale;
        matrices.scale(scale, scale, scale);
        matrices.translate(0.0F, state.offsetY, -1.5F);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(DragonRenderState state) {
        return state.variant.appearance.getBody(state);
    }

    @Override
    public @NotNull DragonRenderState createRenderState() {
        return new DragonRenderState();
    }

    @Override
    protected @Nullable RenderType getRenderType(DragonRenderState state, boolean visible, boolean translucent, boolean glowing) {
        // During death, do not use the standard rendering and let the death layer handle it. Hacky, but better than mixins.
        return state.deathTime > 0 ? null : super.getRenderType(state, visible, translucent, glowing);
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F; // dragons dissolve during death, not flip.
    }
}
