package net.dragonmounts.plus.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.dragonmounts.plus.common.api.DescribedArmorEffect;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.neoforged.neoforge.client.gui.ClientTooltipComponentManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ClientTooltipComponentManager.class)
public class ClientTooltipComponentManagerMixin {
    @WrapMethod(method = "createClientTooltipComponent")
    private static ClientTooltipComponent tryCreateArmorEffectComponent(TooltipComponent component, Operation<ClientTooltipComponent> original) {
        var result = original.call(component);
        return result == null && component instanceof DescribedArmorEffect effect ? effect.getClientTooltip() : result;
    }
}
