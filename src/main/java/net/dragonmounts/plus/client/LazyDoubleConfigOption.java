package net.dragonmounts.plus.client;

import com.mojang.serialization.Codec;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class LazyDoubleConfigOption extends AbstractLazyConfigOption<Double> {
    public static final OptionInstance.CaptionBasedToString<Double> X_2F_STRINGIFIER = (component, config) -> Options.genericValueLabel(component, Component.literal(String.format("%.2f", config)));
    public final Range range;

    public LazyDoubleConfigOption(
            String caption,
            ModConfigSpec.ConfigValue<Double> config,
            Range range,
            Component tooltip,
            OptionInstance.CaptionBasedToString<Double> stringifier
    ) {
        super(caption, config, tooltip, stringifier);
        this.range = range;
    }

    @Override
    public OptionInstance<Double> makeInstance() {
        return new OptionInstance<>(this.caption, this, this.stringifier, this.range, this.config.get(), this.config::set);
    }

    public static class Range implements OptionInstance.SliderableValueSet<Double> {
        public final double max;
        public final double min;
        public final double step;
        protected final double range;

        public Range(float min, float max, float step) {
            if (min >= max) throw new IllegalArgumentException();
            this.max = max;
            this.min = min;
            this.step = step;
            this.range = max - min;
        }

        @Override
        public double toSliderValue(@NotNull Double value) {
            return value < this.max ? value > this.min ? (value - this.min) / this.range : 0.0 : 1.0;
        }

        @Override
        public @NotNull Double fromSliderValue(double delta) {
            return Math.round(Mth.lerp(delta, this.min, this.max) / this.step) * this.step;
        }

        @Override
        public @NotNull Optional<Double> validateValue(Double value) {
            return value.compareTo(this.min) < 0 || value.compareTo(this.max) > 0 ? Optional.empty() : Optional.of(value);
        }

        @Override
        public @NotNull Codec<Double> codec() {
            return Codec.doubleRange(this.min, this.max);
        }
    }
}
