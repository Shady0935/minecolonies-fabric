package com.minecolonies.fabric.network;

import java.lang.reflect.InvocationTargetException;

/**
 * Common-side dispatcher for packets whose execution is client-only.
 *
 * <p>The message classes themselves stay loadable in a dedicated-server
 * environment. Client API types are resolved only after the Fabric client
 * entrypoint has installed {@code ClientNetworkHooks}.</p>
 */
public final class ClientMessageBridge
{
    private static final String CLIENT_HOOKS = "com.minecolonies.fabric.client.network.ClientNetworkHooks";

    private ClientMessageBridge()
    {
    }

    /** Invoke a named client hook, ignoring the bridge only on a dedicated server. */
    public static void invoke(final String methodName, final Class<?>[] parameterTypes, final Object... arguments)
    {
        try
        {
            final Class<?> bridge = Class.forName(CLIENT_HOOKS);
            bridge.getMethod(methodName, parameterTypes).invoke(null, arguments);
        }
        catch (final ClassNotFoundException ignored)
        {
            // Client-bound packet; the client bridge is absent from dedicated-server execution.
        }
        catch (final NoSuchMethodException | IllegalAccessException exception)
        {
            throw new IllegalStateException("MineColonies client message bridge is unavailable: " + methodName, exception);
        }
        catch (final InvocationTargetException exception)
        {
            final Throwable cause = exception.getCause() == null ? exception : exception.getCause();
            if (cause instanceof RuntimeException runtimeException)
            {
                throw runtimeException;
            }
            if (cause instanceof Error error)
            {
                throw error;
            }
            throw new IllegalStateException("MineColonies client message bridge failed: " + methodName, cause);
        }
    }
}
