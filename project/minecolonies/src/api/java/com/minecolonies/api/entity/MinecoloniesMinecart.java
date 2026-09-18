package com.minecolonies.api.entity;

import com.minecolonies.fabric.network.NetworkHooks;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Special MineColonies minecart that does not collide with entities.
 *
 * <p>The Forge implementation duplicated vanilla rail movement so it could
 * call Forge-only hooks. Fabric 1.20.1 already performs the same movement in
 * {@link Minecart}; delegating to it keeps rail acceleration, slopes and
 * client interpolation aligned with vanilla while retaining MineColonies'
 * non-colliding semantics.</p>
 */
public class MinecoloniesMinecart extends Minecart
{
    public MinecoloniesMinecart(final EntityType<?> type, final Level world)
    {
        super(type, world);
    }

    @Override
    protected void moveAlongTrack(final BlockPos pos, final BlockState state)
    {
        super.moveAlongTrack(pos, state);
    }

    @Override
    public void destroy(final DamageSource source)
    {
        this.kill();
    }

    @Override
    public InteractionResult interact(final Player player, final InteractionHand hand)
    {
        return InteractionResult.FAIL;
    }

    @Override
    public boolean isPickable()
    {
        return false;
    }

    @NotNull
    public AbstractMinecart.Type getMinecartType()
    {
        return AbstractMinecart.Type.RIDEABLE;
    }

    @Override
    public void push(@NotNull final Entity entity)
    {
        // Do nothing.
    }

    @Override
    public void playerTouch(final Player player)
    {
        // Do nothing.
    }

    @Override
    public boolean isPushable()
    {
        return false;
    }

    @Override
    public boolean canCollideWith(final Entity entity)
    {
        return false;
    }

    @Override
    public void tick()
    {
        super.tick();
        if (this.tickCount % 20 == 19 && getPassengers().isEmpty())
        {
            this.remove(RemovalReason.DISCARDED);
        }
    }

    @NotNull
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
