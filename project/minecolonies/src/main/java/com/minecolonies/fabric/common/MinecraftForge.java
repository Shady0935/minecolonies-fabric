package com.minecolonies.fabric.common;

import com.minecolonies.api.util.Log;
import com.minecolonies.fabric.event.Event;
import com.minecolonies.fabric.event.SubscribeEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Lightweight in-process event bus used by the Fabric port. */
public final class MinecraftForge
{
    public static final EventBus EVENT_BUS = new EventBus();

    private MinecraftForge()
    {
    }

    public static final class EventBus
    {
        private final List<Object> listeners = new ArrayList<>();
        private final List<Consumer<Object>> consumers = new ArrayList<>();

        public void register(final Object listener)
        {
            if (!listeners.contains(listener)) listeners.add(listener);
        }

        public void unregister(final Object listener)
        {
            listeners.remove(listener);
        }

        public <T> void addListener(final Consumer<T> listener)
        {
            consumers.add((Consumer<Object>) listener);
        }

        public boolean post(final Object event)
        {
            for (Consumer<Object> consumer : List.copyOf(consumers)) consumer.accept(event);
            for (Object listener : List.copyOf(listeners))
            {
                final Class<?> type = listener instanceof Class<?> clazz ? clazz : listener.getClass();
                for (Method method : type.getDeclaredMethods())
                {
                    if (!method.isAnnotationPresent(SubscribeEvent.class) || method.getParameterCount() != 1
                      || !method.getParameterTypes()[0].isInstance(event)) continue;
                    try
                    {
                        method.setAccessible(true);
                        method.invoke(Modifier.isStatic(method.getModifiers()) ? null : listener, event);
                    }
                    catch (InvocationTargetException exception)
                    {
                        final Throwable cause = exception.getCause() == null ? exception : exception.getCause();
                        Log.getLogger().error("Fabric event listener {} failed for {}", method, event.getClass().getName(), cause);
                    }
                    catch (ReflectiveOperationException ignored)
                    {
                        Log.getLogger().error("Unable to invoke Fabric event listener {} for {}", method, event.getClass().getName(), ignored);
                    }
                }
            }
            return event instanceof Event forgeEvent && forgeEvent.isCanceled();
        }
    }
}
