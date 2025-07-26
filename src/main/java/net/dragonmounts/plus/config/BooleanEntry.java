package net.dragonmounts.plus.config;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.dragonmounts.plus.common.network.s2c.BooleanConfigPayload;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;

public class BooleanEntry extends ConfigEntry<Boolean> {
    protected boolean saved;
    protected boolean effective;

    public BooleanEntry(ModConfigSpec.ConfigValue<Boolean> host) {
        super(host);
        this.effective = this.saved = host.getDefault();
    }

    public boolean get() {
        return this.effective;
    }

    @Override
    public void override(Boolean value) {
        this.effective = value;
    }

    @Override
    public String getAsString() {
        return Boolean.toString(this.get());
    }

    @Override
    public Tag dump() {
        return ByteTag.valueOf(forcedGet(this.host));
    }

    @Override
    public Boolean load(@Nullable Tag data) {
        return data instanceof NumericTag ? ((NumericTag) data).getAsByte() != 0 : this.host.getDefault();
    }

    @Override
    public void revert() {
        this.set(this.saved);
    }

    @Override
    public void setSaved() {
        this.saved = forcedGet(this.host);
    }

    @Override
    public CustomPacketPayload wrap(int id) {
        return new BooleanConfigPayload(id, this.get());
    }

    @Override
    public ArgumentType<Boolean> getArgument() {
        return BoolArgumentType.bool();
    }

    @Override
    public Boolean parse(CommandContext<?> context, String name) {
        return BoolArgumentType.getBool(context, name);
    }
}
