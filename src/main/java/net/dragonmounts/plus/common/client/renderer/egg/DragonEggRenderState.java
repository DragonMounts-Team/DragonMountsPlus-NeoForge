package net.dragonmounts.plus.common.client.renderer.egg;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.EmptyBlockAndTintGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DragonEggRenderState extends EntityRenderState implements BlockAndTintGetter {
    public float amplitude;
    public float axis;
    public int age;
    public RandomSource random;
    public BlockPos pos = BlockPos.ZERO;
    public BlockState block;
    public @Nullable Holder<Biome> biome;
    public BlockAndTintGetter level = EmptyBlockAndTintGetter.INSTANCE;

    @Override
    public float getShade(Direction direction, boolean shade) {
        return this.level.getShade(direction, shade);
    }

    @Override
    public @NotNull LevelLightEngine getLightEngine() {
        return this.level.getLightEngine();
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver resolver) {
        return this.biome == null ? -1 : resolver.getColor(this.biome.value(), pos.getX(), pos.getZ());
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
    public @NotNull BlockState getBlockState(BlockPos pos) {
        return pos.equals(this.pos) ? this.block : Blocks.AIR.defaultBlockState();
    }

    @Override
    public @NotNull FluidState getFluidState(BlockPos pos) {
        return this.getBlockState(pos).getFluidState();
    }

    @Override
    public int getHeight() {
        return 1;
    }

    @Override
    public int getMinY() {
        return this.pos.getY();
    }
}