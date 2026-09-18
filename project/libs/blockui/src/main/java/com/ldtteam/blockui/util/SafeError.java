package com.ldtteam.blockui.util;

import com.ldtteam.blockui.mod.Log;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

/**
 * Utility class for throwing errors which is safe during production.
 */
public class SafeError
{
    /**
     * Safe error throw call that only throws an exception during development, but logs an error in production instead so no crashes to desktop may occur.
     *
     * @param exception the exception instance.
     */
    public static void throwInDev(final RuntimeException exception)
    {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment())
        {
            Log.getLogger().error(exception.getMessage(), exception);
        }
        else
        {
            throw exception;
        }
    }

    /**
     * Safe error throw call that only throws an exception during development, but logs an error in production instead so no crashes to desktop may occur.
     *
     * @param exception the exception instance.
     * @param logger    calling class logger
     */
    public static void throwInDev(final RuntimeException exception, final Logger logger)
    {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment())
        {
            logger.error(exception.getMessage(), exception);
        }
        else
        {
            throw exception;
        }
    }
}
