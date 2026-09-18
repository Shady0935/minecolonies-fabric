package com.minecolonies.coremod.network.messages.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

/**
 * Message to open the deco build window on the client.
 */
public class OpenDecoBuildWindowMessage extends OpenBuildWindowMessage
{
    public OpenDecoBuildWindowMessage()
    {
    }

    /**
     * Create a new message.
     *
     * @param pos      the position the deco will be anchored at.
     * @param packName the pack of the deco.
     * @param path     the path in the pack.
     */
    public OpenDecoBuildWindowMessage(
      final BlockPos pos,
      final String packName,
      final String path,
      final Rotation rotation,
      final Mirror mirror)
    {
        super(pos, packName, path, rotation, mirror);
    }

}
