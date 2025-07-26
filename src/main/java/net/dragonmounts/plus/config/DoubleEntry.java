package net.dragonmounts.plus.config;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.dragonmounts.plus.common.network.s2c.DoubleConfigPayload;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;

import java.util.function.DoubleConsumer;

public class DoubleEntry extends ConfigEntry<Double> {
    public final double min;
    public final double max;
    protected final DoubleConsumer onChanged;
    protected double saved;
    protected double effective;

    public DoubleEntry(
            ModConfigSpec.ConfigValue<Double> host,
            double min,
            double max,
            DoubleConsumer onChanged
    ) {
        super(host);
        this.min = min;
        this.max = max;
        this.onChanged = onChanged;
        this.effective = this.saved = host.getDefault();
    }

    public double get() {
        return this.effective;
    }

    public float getAsFloat() {
        return (float) this.effective;
    }

    @Override
    public void override(Double wrapped) {
        double value = Mth.clamp(wrapped, this.min, this.max);
        if (value == this.effective) return;
        this.effective = value;
        if (this.onChanged == null) return;
        this.onChanged.accept(value);
    }

    @Override
    public String getAsString() {
        return Double.toString(this.get());
    }

    @Override
    public Tag dump() {
        return DoubleTag.valueOf(forcedGet(this.host));
    }

    @Override
    public Double load(@Nullable Tag data) {
        return data instanceof NumericTag ? ((NumericTag) data).getAsDouble() : this.host.getDefault();
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
        return new DoubleConfigPayload(id, this.get());
    }

    @Override
    public ArgumentType<Double> getArgument() {
        return DoubleArgumentType.doubleArg(this.min, this.max);
    }

    @Override
    public Double parse(CommandContext<?> context, String name) {
        return DoubleArgumentType.getDouble(context, name);
    }
}
