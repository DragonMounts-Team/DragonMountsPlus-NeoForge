package net.dragonmounts.plus.compat.platform;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.function.BiConsumer;

public final class DoubleRule extends GameRules.Value<DoubleRule> {
    public static GameRules.Type<DoubleRule> of(double fallback, double min, double max) {
        return of(fallback, min, max, (l, r) -> {});
    }

    public static GameRules.Type<DoubleRule> of(double fallback, double min, double max, BiConsumer<MinecraftServer, DoubleRule> onChange) {
        return new GameRules.Type<>(
                () -> DoubleArgumentType.doubleArg(min, max),
                type -> new DoubleRule(type, fallback, min, max),
                onChange,
                DoubleVisitor.FACTORY,
                FeatureFlagSet.of()
        );
    }

    private static final Logger LOGGER = LogUtils.getLogger();
    private final double min;
    private final double max;
    private double value;

    public DoubleRule(GameRules.Type<DoubleRule> type, double value, double min, double max) {
        super(type);
        this.value = value;
        this.min = min;
        this.max = max;
    }

    @Override
    protected void updateFromArgument(CommandContext<CommandSourceStack> context, String name) {
        this.value = context.getArgument(name, Double.class);
    }

    @Override
    protected void deserialize(String value) {
        if (!value.isEmpty()) {
            try {
                double result = Double.parseDouble(value);
                if (this.isInvalid(result)) {
                    LOGGER.warn("Failed to parse double {}. Was out of bounds {} - {}", value, this.min, this.max);
                } else {
                    this.value = result;
                }
            } catch (NumberFormatException e) {
                LOGGER.warn("Failed to parse double {}", value);
            }
        }
    }

    @Override
    public @NotNull String serialize() {
        return Double.toString(this.value);
    }

    @Override
    public int getCommandResult() {
        return Double.compare(this.value, 0.0D);
    }

    @Override
    protected @NotNull DoubleRule getSelf() {
        return this;
    }

    @Override
    protected @NotNull DoubleRule copy() {
        return new DoubleRule(this.type, this.value, this.min, this.max);
    }

    @Override
    public void setFrom(DoubleRule rule, @Nullable MinecraftServer server) {
        if (this.isInvalid(rule.value)) throw new IllegalArgumentException(
                String.format("Could not set value to %s. Was out of bounds %s - %s", rule.value, this.min, this.max)
        );
        this.value = rule.value;
        this.onChanged(server);
    }

    public boolean tryDeserialize(String value) {
        try {
            double result = Double.parseDouble(value);
            if (!this.isInvalid(result)) return false;
            this.value = result;
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    public double get() {
        return this.value;
    }

    private boolean isInvalid(double value) {
        return value < this.min || value > this.max;
    }
}
