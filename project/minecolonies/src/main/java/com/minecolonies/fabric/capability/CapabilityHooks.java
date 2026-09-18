package com.minecolonies.fabric.capability;

import com.minecolonies.coremod.event.capabilityproviders.MinecoloniesChunkCapabilityProvider;
import com.minecolonies.coremod.event.capabilityproviders.MinecoloniesWorldCapabilityProvider;
import com.minecolonies.coremod.event.capabilityproviders.MinecoloniesWorldColonyManagerCapabilityProvider;
import com.minecolonies.fabric.inventory.InvWrapper;
import com.minecolonies.fabric.inventory.IItemHandler;
import com.minecolonies.fabric.util.LazyOptional;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Runtime bridge for the small Forge-capability compatibility API.
 *
 * <p>Fabric 1.20.1 does not add methods to vanilla {@code Level}, chunks or
 * block entities.  Callers therefore route lookups through this class.  The
 * providers are retained per live object, which gives world/chunk state the
 * same lifetime semantics as the old attached capabilities without requiring
 * invasive mixins just to compile and bootstrap the port.</p>
 */
public final class CapabilityHooks
{
    private static final Map<Object, List<ICapabilityProvider>> ATTACHED =
        Collections.synchronizedMap(new WeakHashMap<>());

    private CapabilityHooks()
    {
    }

    public static void attach(final Object target, final ICapabilityProvider provider)
    {
        if (target == null || provider == null)
        {
            return;
        }
        ATTACHED.computeIfAbsent(target, ignored -> new ArrayList<>()).add(provider);
    }

    public static <T> LazyOptional<T> getCapability(final Object target, final Capability<T> capability, final Direction side)
    {
        if (target == null || capability == null)
        {
            return LazyOptional.empty();
        }

        if (target instanceof ICapabilityProvider provider)
        {
            final LazyOptional<T> result = provider.getCapability(capability, side);
            if (result.isPresent())
            {
                return result;
            }
        }

        ensureDefaultProviders(target);
        final List<ICapabilityProvider> providers = ATTACHED.get(target);
        if (providers != null)
        {
            for (final ICapabilityProvider provider : providers)
            {
                final LazyOptional<T> result = provider.getCapability(capability, side);
                if (result.isPresent())
                {
                    return result;
                }
            }
        }
        return LazyOptional.empty();
    }

    /** Adapts a vanilla/Fabric object to the provider-shaped inventory API. */
    public static ICapabilityProvider asProvider(final Object target)
    {
        if (target instanceof ICapabilityProvider provider)
        {
            return provider;
        }
        return new ICapabilityProvider()
        {
            @Override
            public <T> LazyOptional<T> getCapability(final Capability<T> capability, final Direction side)
            {
                return CapabilityHooks.getCapability(target, capability, side);
            }
        };
    }

    private static void ensureDefaultProviders(final Object target)
    {
        if (ATTACHED.containsKey(target))
        {
            return;
        }

        final List<ICapabilityProvider> providers = new ArrayList<>();
        if (target instanceof LevelChunk)
        {
            providers.add(new MinecoloniesChunkCapabilityProvider());
        }
        else if (target instanceof Level)
        {
            providers.add(new MinecoloniesWorldCapabilityProvider());
            providers.add(new MinecoloniesWorldColonyManagerCapabilityProvider());
        }
        else if (target instanceof Container container)
        {
            final IItemHandler handler = new InvWrapper(container);
            providers.add(itemHandlerProvider(handler));
        }
        else if (target instanceof Player player)
        {
            final IItemHandler handler = new InvWrapper(player.getInventory());
            providers.add(itemHandlerProvider(handler));
        }

        if (!providers.isEmpty())
        {
            ATTACHED.put(target, providers);
        }
    }

    private static ICapabilityProvider itemHandlerProvider(final IItemHandler handler)
    {
        return new ICapabilityProvider()
        {
            @Override
            public <T> LazyOptional<T> getCapability(final Capability<T> capability, final Direction side)
            {
                return capability == ForgeCapabilities.ITEM_HANDLER
                    ? LazyOptional.of(() -> handler).cast()
                    : LazyOptional.empty();
            }
        };
    }
}
