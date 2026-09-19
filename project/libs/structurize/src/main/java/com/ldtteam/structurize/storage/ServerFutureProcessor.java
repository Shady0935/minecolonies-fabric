package com.ldtteam.structurize.storage;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** Waits for blueprint futures to finish loading and processes them on server ticks. */
public final class ServerFutureProcessor
{
    private static final Queue<BlueprintProcessingData> blueprintConsumerQueue = new LinkedList<>();
    private static final Queue<BlueprintDataProcessingData> blueprintDataConsumerQueue = new LinkedList<>();
    private static final Queue<BlueprintListProcessingData> blueprintListConsumerQueue = new LinkedList<>();
    private static boolean registered;

    private ServerFutureProcessor()
    {
    }

    public static void register()
    {
        if (registered)
        {
            return;
        }
        registered = true;
        ServerTickEvents.END_WORLD_TICK.register(ServerFutureProcessor::processCompleted);
    }

    public static void queueBlueprint(@NotNull final BlueprintProcessingData processingData)
    {
        blueprintConsumerQueue.add(processingData);
    }

    public static void queueBlueprintList(@NotNull final BlueprintListProcessingData processingData)
    {
        blueprintListConsumerQueue.add(processingData);
    }

    public static void queueBlueprintData(@NotNull final BlueprintDataProcessingData processingData)
    {
        blueprintDataConsumerQueue.add(processingData);
    }

    private static void processCompleted(final net.minecraft.server.level.ServerLevel level)
    {
        BlueprintProcessingData blueprintData;
        while ((blueprintData = pollCompleted(blueprintConsumerQueue,
          data -> data.level == level && data.blueprintFuture.isDone())) != null)
        {
            accept(blueprintData.blueprintFuture, blueprintData.consumer);
        }

        BlueprintDataProcessingData blueprintDataBytes;
        while ((blueprintDataBytes = pollCompleted(blueprintDataConsumerQueue,
          data -> data.level == level && data.blueprintDataFuture.isDone())) != null)
        {
            accept(blueprintDataBytes.blueprintDataFuture, blueprintDataBytes.consumer);
        }

        BlueprintListProcessingData blueprintList;
        while ((blueprintList = pollCompleted(blueprintListConsumerQueue,
          data -> data.level == level && data.blueprintFuture.isDone())) != null)
        {
            accept(blueprintList.blueprintFuture, blueprintList.consumer);
        }
    }

    /**
     * Removes the first completed entry for the current level without allowing
     * a slow or unrelated dimension entry at the head of the global queue to
     * starve ready server work.
     */
    private static <T> T pollCompleted(final Queue<T> queue, final Predicate<T> predicate)
    {
        final Iterator<T> iterator = queue.iterator();
        while (iterator.hasNext())
        {
            final T entry = iterator.next();
            if (predicate.test(entry))
            {
                iterator.remove();
                return entry;
            }
        }
        return null;
    }

    private static <T> void accept(final Future<T> future, final Consumer<T> consumer)
    {
        try
        {
            consumer.accept(future.get());
        }
        catch (InterruptedException | ExecutionException e)
        {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    public record BlueprintProcessingData(Future<Blueprint> blueprintFuture, Level level, Consumer<Blueprint> consumer) { }

    public record BlueprintDataProcessingData(Future<byte[]> blueprintDataFuture, Level level, Consumer<byte[]> consumer) { }

    public record BlueprintListProcessingData(Future<List<Blueprint>> blueprintFuture, Level level, Consumer<List<Blueprint>> consumer) { }
}
