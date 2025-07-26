package net.dragonmounts.plus.compat.platform;

import net.dragonmounts.plus.common.capability.FluteHolder;
import net.dragonmounts.plus.common.inventory.FluteHolderImpl;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;

public class DMAttachments {
    public static final AttachmentType<FluteHolder> FLUTE_HOLDER = AttachmentType
            .<FluteHolder>builder(FluteHolderImpl::new)
            .serialize(ItemStack.OPTIONAL_CODEC.xmap(FluteHolderImpl::of, FluteHolder::getFlute))
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
