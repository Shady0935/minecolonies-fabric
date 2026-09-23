package com.ldtteam.multipiston.client;

import com.ldtteam.multipiston.network.MultiPistonChangeMessage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/** Client-only sender for the Multi-Piston configuration payload. */
public final class MultiPistonClientNetworking
{
    private MultiPistonClientNetworking()
    {
    }

    public static void sendConfiguration(final BlockPos pos, final Direction input, final Direction output,
                                         final int range, final int speed)
    {
        final var buffer = PacketByteBufs.create();
        new MultiPistonChangeMessage(pos, input, output, range, speed).write(buffer);
        ClientPlayNetworking.send(MultiPistonChangeMessage.ID, buffer);
    }
}
