package net.dragonmounts.plus.compat.registry;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static net.dragonmounts.plus.common.DragonMountsShared.makeKey;

public class BlockHolder<T extends Block> extends DeferredHolder<T, Block> implements ItemLike {
    private static final ObjectArrayList<BlockHolder<?>> BLOCKS = new ObjectArrayList<>();

    public static <T extends Block> BlockHolder<T> registerBlock(String name, Function<Properties, T> factory) {
        var holder = new BlockHolder<>(makeKey(Registries.BLOCK, name), factory);
        BLOCKS.add(holder);
        return holder;
    }

    static void registerEntries(Registry<Block> registry) {
        for (var entity : BLOCKS) {
            entity.register(registry);
        }
    }

    private final Function<Properties, T> factory;

    public BlockHolder(ResourceKey<Block> key, Function<Properties, T> factory) {
        super(key);
        this.factory = factory;
    }

    @Override
    protected T create() {
        return this.factory.apply(Properties.of().setId(this.key));
    }

    @Override
    public final @NotNull Item asItem() {
        return this.value().asItem();
    }

    public final BlockState defaultBlockState() {
        return this.value().defaultBlockState();
    }
}