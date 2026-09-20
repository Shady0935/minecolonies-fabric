package com.minecolonies.fabric.inventory;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.resolver.retrying.IRetryingRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.requestsystem.management.IStandardRequestManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Bridges vanilla container mutations to the MineColonies request lifecycle. */
public final class ContainerUpdateHooks
{
    private record PendingContainerUpdate(ServerLevel level, BlockPos position)
    {
    }

    private static final Set<PendingContainerUpdate> PENDING_UPDATES = ConcurrentHashMap.newKeySet();

    private ContainerUpdateHooks()
    {
    }

    /** Register the one-tick retry used for requests created in the same tick as a container mutation. */
    public static void registerServerTick()
    {
        ServerTickEvents.END_SERVER_TICK.register(ContainerUpdateHooks::processDeferredUpdates);
    }

    /**
     * Re-evaluate deliverable requests when a registered vanilla container receives items.
     *
     * @param blockEntity the changed container block entity.
     */
    public static void notifyRegisteredContainerChanged(final BlockEntity blockEntity)
    {
        if (processContainerChanged(blockEntity) && blockEntity.getLevel() instanceof ServerLevel level)
        {
            // A worker can create and assign its request immediately before the
            // vanilla container mutation. The persisted assignment is then
            // visible while the building's request map is still being updated;
            // retry once after the current server tick has settled that state.
            PENDING_UPDATES.add(new PendingContainerUpdate(level, blockEntity.getBlockPos()));
        }
    }

    private static void processDeferredUpdates(final MinecraftServer server)
    {
        PENDING_UPDATES.removeIf(update -> update.level().getServer() != server);
        for (final PendingContainerUpdate update : PENDING_UPDATES)
        {
            if (update.level().getServer() != server)
            {
                continue;
            }

            PENDING_UPDATES.remove(update);
            final BlockEntity blockEntity = update.level().getBlockEntity(update.position());
            if (blockEntity != null)
            {
                processContainerChanged(blockEntity);
            }
        }
    }

    private static boolean processContainerChanged(final BlockEntity blockEntity)
    {
        if (!(blockEntity instanceof Container container))
        {
            return false;
        }

        final Level level = blockEntity.getLevel();
        if (level == null || level.isClientSide)
        {
            return false;
        }

        final List<ItemStack> contents = new ArrayList<>();
        for (int slot = 0; slot < container.getContainerSize(); slot++)
        {
            final ItemStack stack = container.getItem(slot);
            if (!ItemStackUtils.isEmpty(stack))
            {
                contents.add(stack.copy());
            }
        }

        if (contents.isEmpty())
        {
            return false;
        }

        final var containerPos = blockEntity.getBlockPos();
        boolean matchedContainer = false;
        for (final IColony colony : IColonyManager.getInstance().getColonies(level))
        {
            for (final IBuilding building : colony.getBuildingManager().getBuildings().values())
            {
                if (!building.getContainers().contains(containerPos))
                {
                    continue;
                }

                matchedContainer = true;

                // The retrying resolver normally performs this reassignment
                // from onColonyUpdate. Its persisted assignment table can be
                // ahead of the resolver's in-memory request cache while a
                // worker-created request is still in its first tick, however.
                // In that window the callback is received but the request is
                // left in the retrying resolver. Revisit only matching open
                // requests owned by this building as a Fabric-side fallback;
                // this keeps vanilla container insertion from depending on a
                // later retry delay and does not disturb unrelated resolvers.
                final Set<IToken<?>> requestTokens = new LinkedHashSet<>();
                for (final Collection<IToken<?>> tokens : building.getOpenRequestsByRequestableType().values())
                {
                    requestTokens.addAll(tokens);
                }
                for (final var citizen : colony.getCitizenManager().getCitizens())
                {
                    for (final IRequest<?> request : building.getOpenRequests(citizen.getId()))
                    {
                        requestTokens.add(request.getId());
                    }
                }
                try
                {
                    for (final IRequest<?> request : ((IStandardRequestManager) colony.getRequestManager()).getRequestHandler()
                      .getRequestsMadeByRequester(building.getRequester()))
                    {
                        requestTokens.add(request.getId());
                    }
                }
                catch (final ClassCastException ignored)
                {
                    // The portable request manager is standard in production;
                    // the building map above remains sufficient for wrappers.
                }

                // onColonyUpdate can remove requests from the building's
                // open-request index, so the fallback must use this snapshot.
                colony.getRequestManager().onColonyUpdate(request ->
                    request.getRequest() instanceof IDeliverable deliverable
                        && contents.stream().anyMatch(deliverable::matches));

                for (final IToken<?> requestToken : requestTokens)
                {
                    final IRequest<?> request = colony.getRequestManager().getRequestForToken(requestToken);
                    if (request == null
                          || request.getState().ordinal() >= RequestState.COMPLETED.ordinal()
                          || request.getRequest() instanceof IDeliverable deliverable && contents.stream().noneMatch(deliverable::matches)
                          || request.hasChildren())
                    {
                        continue;
                    }

                    final IRequestResolver<?> resolver;
                    try
                    {
                        resolver = colony.getRequestManager().getResolverForRequest(requestToken);
                    }
                    catch (final IllegalArgumentException ignored)
                    {
                        continue;
                    }

                    if (!(resolver instanceof IRetryingRequestResolver)
                          || !(request.getRequest() instanceof IDeliverable))
                    {
                        continue;
                    }

                    try
                    {
                        colony.getRequestManager().reassignRequest(requestToken, Collections.emptyList());
                    }
                    catch (final IllegalArgumentException exception)
                    {
                        // A request can be completed by another resolver
                        // between the callback and this defensive pass.
                    }
                }
            }
        }

        return matchedContainer;
    }
}
