package net.dragonmounts.plus.mixin;

import net.dragonmounts.plus.common.item.EntityContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Shadow
    public abstract void setUnlimitedLifetime();

    @Inject(method = "setItem", at = @At("TAIL"))
    public void persistent(ItemStack stack, CallbackInfo info) {
        if (stack.getItem() instanceof EntityContainer<?> item && !item.isEmpty(stack)) {
            this.setUnlimitedLifetime();
        }
    }

    private ItemEntityMixin() {}
}