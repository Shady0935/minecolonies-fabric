package com.ldtteam.structurize.storage;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/** Waits for blueprint futures to finish loading and processes them on client ticks. */
public final class ClientFutureProcessor
{
    private static final Queue<BlueprintProcessingData> blueprintConsumerQueue = new LinkedList<>();
    private static final Queue<BlueprintDataProcessingData> blueprintDataConsumerQueue = new LinkedList<>();
    private static boolean registered;

    private ClientFutureProcessor()
    {
    }

    public static void register()
    {
        if (registered)
        {
            return;
        }
        registered = true;
        ClientTickEvents.END_CLIENT_TICK.register(client -> processCompleted());
    }

    public static void queueBlueprint(@NotNull final BlueprintProcessingData processingData)
    {
        blueprintConsumerQueue.add(processingData);
    }

    public static void queueBlueprintData(@NotNull final BlueprintDataProcessingData processingData)
    {
        blueprintDataConsumerQueue.add(processingData);
    }

    private static void processCompleted()
    {
        if (!blueprintConsumerQueue.isEmpty() && blueprintConsumerQueue.peek().blueprintFuture.isDone())
        {
            final BlueprintProcessingData data = blueprintConsumerQueue.poll();
            try
            {
                data.consumer.accept(data.blueprintFuture.get());
            }
            catch (InterruptedException | ExecutionException e)
            {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }

        if (!blueprintDataConsumerQueue.isEmpty() && blueprintDataConsumerQueue.peek().blueprintDataFuture.isDone())
        {
            final BlueprintDataProcessingData data = blueprintDataConsumerQueue.poll();
            try
            {
                data.consumer.accept(data.blueprintDataFuture.get());
            }
            catch (InterruptedException | ExecutionException e)
            {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }
    }

    public record BlueprintProcessingData(Future<Blueprint> blueprintFuture, Consumer<Blueprint> consumer) { }

    public record BlueprintDataProcessingData(Future<byte[]> blueprintDataFuture, Consumer<byte[]> consumer) { }
}
