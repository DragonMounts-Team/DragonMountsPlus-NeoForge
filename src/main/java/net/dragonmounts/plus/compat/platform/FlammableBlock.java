package net.dragonmounts.plus.compat.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class FlammableBlock extends Block {
    public static int getFlammability(Level level, BlockPos pos, BlockState state, Direction side) {
        return state.getFlammability(level, pos, side);
    }

    public final int flammability;
    public final int spreadSpeed;

    public FlammableBlock(int flammability, int spreadSpeed, Properties props) {
        super(props);
        this.flammability = flammability;
        this.spreadSpeed = spreadSpeed;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return this.flammability;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return this.spreadSpeed;
    }
}
