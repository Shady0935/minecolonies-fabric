package com.minecolonies.fabric.common;

import com.minecolonies.api.util.Log;
import com.minecolonies.fabric.event.Event;
import com.minecolonies.fabric.event.EventPriority;
import com.minecolonies.fabric.event.SubscribeEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
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
        /**
         * GameTest and lifecycle callbacks can register colony-scoped listeners
         * while another server callback is dispatching an event. Copy-on-write
         * keeps registration/removal deterministic without exposing a mutable
         * ArrayList during dispatch.
         */
        private final List<Object> listeners = new CopyOnWriteArrayList<>();
        private final List<Consumer<Object>> consumers = new CopyOnWriteArrayList<>();

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
            final List<DispatchEntry> dispatchEntries = new ArrayList<>();
            long registrationOrder = 0;
            for (Consumer<Object> consumer : List.copyOf(consumers))
            {
                dispatchEntries.add(new DispatchEntry(consumer, null, null, EventPriority.NORMAL, registrationOrder++));
            }
            for (Object listener : List.copyOf(listeners))
            {
                final Class<?> type = listener instanceof Class<?> clazz ? clazz : listener.getClass();
                final Set<String> shadowedMethods = new HashSet<>();
                for (Class<?> currentType = type; currentType != null && currentType != Object.class;
                     currentType = currentType.getSuperclass())
                {
                    for (Method method : currentType.getDeclaredMethods())
                    {
                        final String methodKey = method.getName() + Arrays.toString(method.getParameterTypes());
                        if (!shadowedMethods.add(methodKey)
                          || !method.isAnnotationPresent(SubscribeEvent.class)
                          || method.getParameterCount() != 1
                          || !method.getParameterTypes()[0].isInstance(event)) continue;
                        dispatchEntries.add(new DispatchEntry(null, listener, method,
                          method.getAnnotation(SubscribeEvent.class).priority(), registrationOrder++));
                    }
                }
            }

            dispatchEntries.sort(Comparator.comparingInt((DispatchEntry entry) -> entry.priority().ordinal())
              .thenComparingLong(DispatchEntry::registrationOrder));
            for (DispatchEntry entry : dispatchEntries)
            {
                if (entry.consumer() != null)
                {
                    entry.consumer().accept(event);
                    continue;
                }

                try
                {
                    entry.method().setAccessible(true);
                    entry.method().invoke(Modifier.isStatic(entry.method().getModifiers()) ? null : entry.listener(), event);
                }
                catch (InvocationTargetException exception)
                {
                    final Throwable cause = exception.getCause() == null ? exception : exception.getCause();
                    Log.getLogger().error("Fabric event listener {} failed for {}", entry.method(), event.getClass().getName(), cause);
                }
                catch (ReflectiveOperationException ignored)
                {
                    Log.getLogger().error("Unable to invoke Fabric event listener {} for {}", entry.method(), event.getClass().getName(), ignored);
                }
            }
            return event instanceof Event forgeEvent && forgeEvent.isCanceled();
        }

        private record DispatchEntry(Consumer<Object> consumer, Object listener, Method method,
                                     EventPriority priority, long registrationOrder)
        {
        }
    }
}
