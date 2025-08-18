package net.dragonmounts.plus.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.dragonmounts.plus.common.api.DescribedArmorEffect;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.neoforged.neoforge.client.gui.ClientTooltipComponentManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Function;

@Mixin(value = ClientTooltipComponentManager.class)
public class ClientTooltipComponentManagerMixin {
    @ModifyExpressionValue(method = "createClientTooltipComponent", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap;get(Ljava/lang/Object;)Ljava/lang/Object;"))
    private static Object tryCreateArmorEffectComponent(Object original, @Local(argsOnly = true) TooltipComponent component) {
        return original == null && component instanceof DescribedArmorEffect
                ? (Function<DescribedArmorEffect, ClientTooltipComponent>) DescribedArmorEffect::getClientTooltip
                : original;
    }
}
