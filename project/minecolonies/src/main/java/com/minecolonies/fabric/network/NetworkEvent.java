package com.minecolonies.fabric.network;

import com.minecolonies.fabric.LogicalSide;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.Executor;

/** Context passed to MineColonies packet handlers. */
public final class NetworkEvent
{
    private NetworkEvent()
    {
    }

    public static final class Context
    {
        private final ServerPlayer sender;
        private final Direction direction;
        private final Executor workExecutor;
        private boolean packetHandled;

        public Context()
        {
            this(null, LogicalSide.SERVER, Runnable::run);
        }

        public Context(final ServerPlayer sender, final LogicalSide origin)
        {
            this(sender, origin, Runnable::run);
        }

        public Context(final ServerPlayer sender, final LogicalSide origin, final Executor workExecutor)
        {
            this.sender = sender;
            this.direction = new Direction(origin);
            this.workExecutor = workExecutor;
        }

        public ServerPlayer getSender()
        {
            return sender;
        }

        public Direction getDirection()
        {
            return direction;
        }

        public void enqueueWork(final Runnable work)
        {
            workExecutor.execute(work);
        }

        public void setPacketHandled(final boolean handled)
        {
            packetHandled = handled;
        }

        public boolean isPacketHandled()
        {
            return packetHandled;
        }
    }

    public static final class Direction
    {
        private final LogicalSide originationSide;

        private Direction(final LogicalSide originationSide)
        {
            this.originationSide = originationSide;
        }

        public LogicalSide getOriginationSide()
        {
            return originationSide;
        }
    }
}
