package com.minecolonies.api.blocks;

import com.minecolonies.api.blocks.interfaces.IBlockMinecolonies;
import com.minecolonies.api.util.constant.Suppression;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import com.minecolonies.fabric.registry.FabricRegistry;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public abstract class AbstractBlockMinecoloniesFalling<B extends AbstractBlockMinecoloniesFalling<B>> extends FallingBlock implements IBlockMinecolonies<B>
{
    public AbstractBlockMinecoloniesFalling(final Properties properties)
    {
        super(properties);
    }

    @Override
    public void registerBlockItem(final FabricRegistry<Item> registry, final Item.Properties properties)
    {
        registry.register(getRegistryName(), new BlockItem(this, properties));
    }

    @Override
    public B registerBlock(final FabricRegistry<Block> registry)
    {
        registry.register(getRegistryName(), this);
        return (B) this;
    }
}
