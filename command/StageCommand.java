package net.dragonmounts.plus.common.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.dragonmounts.plus.common.api.ScoreboardAccessor;
import net.dragonmounts.plus.common.entity.dragon.DragonLifeStage;
import net.dragonmounts.plus.common.entity.dragon.HatchableDragonEggEntity;
import net.dragonmounts.plus.common.entity.dragon.ServerDragonEntity;
import net.dragonmounts.plus.common.entity.dragon.TameableDragonEntity;
import net.dragonmounts.plus.compat.registry.DragonType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import java.util.function.Predicate;

import static net.dragonmounts.plus.common.command.DMCommand.createClassCastException;
import static net.dragonmounts.plus.common.entity.dragon.DragonLifeStage.EGG_TRANSLATION_KEY;
import static net.dragonmounts.plus.common.entity.dragon.HatchableDragonEggEntity.AGE_DATA_PARAMETER_KEY;

public class StageCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register(Predicate<CommandSourceStack> permission) {
        var target = Commands.argument("target", EntityArgument.entity())
                .executes(context -> get(
                        context.getSource(),
                        EntityArgument.getEntity(context, "target")
                )).then(Commands.literal("egg")
                        .requires(permission)
                        .executes(context -> egg(
                                context.getSource(),
                                EntityArgument.getEntity(context, "target")
                        ))
                );
        for (var stage : DragonLifeStage.values()) {
            target.then(Commands.literal(stage.name)
                    .requires(permission)
                    .executes(context -> set(
                            context.getSource(),
                            EntityArgument.getEntity(context, "target"),
                            stage
                    ))
            );
        }
        return Commands.literal("stage").then(target);
    }

    public static int egg(CommandSourceStack source, Entity target) {
        if (target instanceof TameableDragonEntity dragon) {
            dragon.inventory.dropContents(false, 1.25);
            var level = source.getLevel();
            var egg = new HatchableDragonEggEntity(level);
            var tag = dragon.saveWithoutId(new CompoundTag());
            tag.remove(AGE_DATA_PARAMETER_KEY);
            tag.remove(DragonType.DATA_PARAMETER_KEY);
            egg.load(tag);
            egg.setDragonType(dragon.getDragonType(), false);
            ((ScoreboardAccessor) level.getScoreboard()).dragonmounts$preventRemoval(dragon);
            dragon.discard();
            level.addFreshEntity(egg);
        } else if (target instanceof HatchableDragonEggEntity) {
            ((HatchableDragonEggEntity) target).setAge(0, false);
        } else {
            source.sendFailure(createClassCastException(target, TameableDragonEntity.class));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("commands.dragonmounts.plus.stage.set", target.getDisplayName(), Component.translatable(EGG_TRANSLATION_KEY)), true);
        return 1;
    }

    public static int get(CommandSourceStack source, Entity target) {
        if (target instanceof TameableDragonEntity dragon) {
            source.sendSuccess(() -> Component.translatable("commands.dragonmounts.plus.stage.get", target.getDisplayName(), dragon.getLifeStage().getText()), true);
        } else if (target instanceof HatchableDragonEggEntity) {
            source.sendSuccess(() -> Component.translatable("commands.dragonmounts.plus.stage.get", target.getDisplayName(), Component.translatable(EGG_TRANSLATION_KEY)), true);
        } else {
            source.sendFailure(createClassCastException(target, TameableDragonEntity.class));
            return 0;
        }
        return 1;
    }

    public static int set(CommandSourceStack source, Entity target, DragonLifeStage stage) {
        if (target instanceof TameableDragonEntity dragon) {
            dragon.setLifeStage(stage, true, true);
        } else if (target instanceof HatchableDragonEggEntity egg) {
            var level = source.getLevel();
            ((ScoreboardAccessor) level.getScoreboard()).dragonmounts$preventRemoval(egg);
            ServerDragonEntity dragon = HatchableDragonEggEntity.hatch(level, egg, stage);
            egg.discard();
            level.addFreshEntity(dragon);
        } else {
            source.sendFailure(createClassCastException(target, TameableDragonEntity.class));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("commands.dragonmounts.plus.stage.set", target.getDisplayName(), stage.getText()), true);
        return 1;
    }
}
