package com.minecolonies.coremod.network.messages.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

/**
 * Message to open the plantation field build window on the client.
 */
public class OpenPlantationFieldBuildWindowMessage extends OpenBuildWindowMessage
{
    public OpenPlantationFieldBuildWindowMessage()
    {
        super();
    }

    /**
     * Create a new message.
     *
     * @param pos      the position the plantation field will be anchored at.
     * @param packName the pack of the plantation field.
     * @param path     the path in the pack.
     */
    public OpenPlantationFieldBuildWindowMessage(
      final BlockPos pos,
      final String packName,
      final String path,
      final Rotation rotation,
      final Mirror mirror)
    {
        super(pos, packName, path, rotation, mirror);
    }

}
