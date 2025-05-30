package net.dragonmounts.plus.common.component.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.dragonmounts.plus.common.entity.dragon.TameableDragonEntity;
import net.dragonmounts.plus.common.init.DMConsumeEffects;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public record ContorlGrowthConsumeEffect(boolean isAllowed) implements ConsumeEffect {
    public static final MapCodec<ContorlGrowthConsumeEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.fieldOf("allowed").forGetter(ContorlGrowthConsumeEffect::isAllowed)
    ).apply(instance, ContorlGrowthConsumeEffect::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ContorlGrowthConsumeEffect> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            ContorlGrowthConsumeEffect::isAllowed,
            ContorlGrowthConsumeEffect::new
    );

    @Override
    public @NotNull Type<ContorlGrowthConsumeEffect> getType() {
        return DMConsumeEffects.CONTROL_GROWTH;
    }

    @Override
    public boolean apply(Level level, ItemStack stack, LivingEntity entity) {
        if (entity instanceof TameableDragonEntity dragon) {
            dragon.setAgeLocked(this.isAllowed());
            return true;
        }
        return false;
    }
}
