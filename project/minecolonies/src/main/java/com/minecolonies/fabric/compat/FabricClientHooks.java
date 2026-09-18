package com.minecolonies.fabric.compat;

import net.minecraft.core.BlockPos;

/** Small reflection boundary for client-only conveniences used by common code. */
public final class FabricClientHooks
{
    private FabricClientHooks()
    {
    }

    public static void openReactivationWindow(final BlockPos pos)
    {
        try
        {
            final Class<?> type = Class.forName("com.minecolonies.coremod.client.gui.WindowReactivateBuilding");
            final Object window = type.getConstructor(BlockPos.class).newInstance(pos);
            type.getMethod("open").invoke(window);
        }
        catch (ReflectiveOperationException ignored)
        {
            // The method is only meaningful on a client; dedicated servers do nothing.
        }
    }

    public static boolean isClientWorldEmpty()
    {
        try
        {
            final Class<?> minecraft = Class.forName("net.minecraft.client.Minecraft");
            final Object instance = minecraft.getMethod("getInstance").invoke(null);
            return minecraft.getField("level").get(instance) == null;
        }
        catch (ReflectiveOperationException ignored)
        {
            return false;
        }
    }
}
