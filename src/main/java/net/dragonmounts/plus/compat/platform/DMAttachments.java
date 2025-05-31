package net.dragonmounts.plus.compat.platform;

import net.dragonmounts.plus.common.capability.WhistleHolder;
import net.dragonmounts.plus.common.inventory.WhistleHolderImpl;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;

public class DMAttachments {
    public static final AttachmentType<WhistleHolder> WHISTLE_HOLDER = AttachmentType
            .<WhistleHolder>builder(WhistleHolderImpl::new)
            .serialize(ItemStack.OPTIONAL_CODEC.xmap(WhistleHolderImpl::of, WhistleHolder::getWhistle))
            .copyOnDeath()
            .build();

    public static <T> boolean has(AttachmentHolder host, AttachmentType<T> type) {
        return host.hasData(type);
    }

    public static <T> T get(AttachmentHolder host, AttachmentType<T> type) {
        return host.getExistingData(type).orElse(null);
    }

    public static <T> T getOrCreate(AttachmentHolder host, AttachmentType<T> type) {
        return host.getData(type);
    }
}
