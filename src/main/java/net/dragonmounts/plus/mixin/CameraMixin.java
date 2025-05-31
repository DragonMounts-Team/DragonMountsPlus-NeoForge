package net.dragonmounts.plus.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import net.dragonmounts.plus.common.entity.dragon.TameableDragonEntity;
import net.dragonmounts.plus.config.ClientConfig;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    protected abstract float getMaxZoom(float distance);

    @Shadow
    protected abstract void move(float x, float y, float z);

    @ModifyExpressionValue(method = "setup", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/ClientHooks;getDetachedCameraDistance(Lnet/minecraft/client/Camera;ZFF)F"))
    public float detachedCameraOffset(float original, @Local(argsOnly = true) Entity host, @Cancellable CallbackInfo info) {
        if (host.getVehicle() instanceof TameableDragonEntity) {
            this.move(-this.getMaxZoom(ClientConfig.INSTANCE.camera_distance.get().floatValue()), 0.0F, -ClientConfig.INSTANCE.camera_offset.get().floatValue());
            info.cancel();
        }
        return original;
    }

    private CameraMixin() {}
}
