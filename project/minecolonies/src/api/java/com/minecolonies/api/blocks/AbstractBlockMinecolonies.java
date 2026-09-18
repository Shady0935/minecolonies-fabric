package com.minecolonies.api.blocks;

import com.minecolonies.api.blocks.interfaces.IBlockMinecolonies;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import com.minecolonies.fabric.registry.FabricRegistry;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public abstract class AbstractBlockMinecolonies<B extends AbstractBlockMinecolonies<B>> extends Block implements IBlockMinecolonies<B>
{
    public AbstractBlockMinecolonies(final Properties properties)
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
