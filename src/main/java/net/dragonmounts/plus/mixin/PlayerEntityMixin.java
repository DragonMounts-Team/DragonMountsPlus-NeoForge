package net.dragonmounts.plus.mixin;

import net.dragonmounts.plus.common.capability.ArmorEffectManager.Provider;
import net.dragonmounts.plus.common.capability.ArmorEffectManagerImpl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.dragonmounts.plus.common.capability.ArmorEffectManagerImpl.DATA_PARAMETER_KEY;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity implements Provider {
    @Unique
    protected ArmorEffectManagerImpl dragonmounts$plus$manager = new ArmorEffectManagerImpl(Player.class.cast(this));

    @Inject(method = "tick", at = @At("HEAD"))
    public void tickManager(CallbackInfo info) {
        this.dragonmounts$plus$manager.tick();
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void saveCooldown(CompoundTag tag, CallbackInfo info) {
        var data = this.dragonmounts$plus$manager.saveNBT();
        if (data.isEmpty()) return;
        tag.put(DATA_PARAMETER_KEY, data);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void readCooldown(CompoundTag tag, CallbackInfo info) {
        this.dragonmounts$plus$manager.readNBT(tag.getCompound(DATA_PARAMETER_KEY));
    }

    @Override
    public ArmorEffectManagerImpl dragonmounts$plus$getManager() {
        return this.dragonmounts$plus$manager;
    }

    private PlayerEntityMixin(EntityType<? extends LivingEntity> a, Level b) {super(a, b);}
}
