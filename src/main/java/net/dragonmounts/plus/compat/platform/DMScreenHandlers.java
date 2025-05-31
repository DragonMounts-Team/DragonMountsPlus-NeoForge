package net.dragonmounts.plus.compat.platform;

import net.dragonmounts.plus.common.entity.dragon.TameableDragonEntity;
import net.dragonmounts.plus.common.inventory.DragonCoreHandler;
import net.dragonmounts.plus.common.inventory.DragonInventoryHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.VarInt;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.RegisterEvent;

import static net.dragonmounts.plus.common.DragonMountsShared.makeId;
import static net.dragonmounts.plus.common.client.ClientUtil.getLevel;
import static net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create;

public class DMScreenHandlers {
    public static final MenuType<DragonCoreHandler> DRAGON_CORE = create((id, inventory, buffer) ->
            new DragonCoreHandler(id, inventory, BlockPos.STREAM_CODEC.decode(buffer))
    );
    public static final MenuType<DragonInventoryHandler> DRAGON_INVENTORY = create((id, inventory, buffer) -> {
        if (getLevel().getEntity(VarInt.read(buffer)) instanceof TameableDragonEntity dragon) {
            return new DragonInventoryHandler(id, inventory, dragon);
        }
        throw new NullPointerException();
    });

    public static void register(RegisterEvent.RegisterHelper<MenuType<?>> event) {
        event.register(makeId("dragon_core"), DRAGON_CORE);
        event.register(makeId("dragon_inventory"), DRAGON_INVENTORY);
    }
}
