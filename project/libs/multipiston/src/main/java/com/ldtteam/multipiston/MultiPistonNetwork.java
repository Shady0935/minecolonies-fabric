package com.ldtteam.multipiston;

import com.ldtteam.multipiston.network.MultiPistonChangeMessage;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/** Server receiver for the small configuration payload sent by the client. */
public final class MultiPistonNetwork
{
    private MultiPistonNetwork()
    {
    }

    public static void registerServerReceiver()
    {
        ServerPlayNetworking.registerGlobalReceiver(MultiPistonChangeMessage.ID,
          (server, player, handler, buffer, responseSender) -> {
              final MultiPistonChangeMessage message;
              try
              {
                  message = MultiPistonChangeMessage.read(buffer);
              }
              catch (final RuntimeException exception)
              {
                  return;
              }
              server.execute(() -> message.apply(player));
          });
    }
}
