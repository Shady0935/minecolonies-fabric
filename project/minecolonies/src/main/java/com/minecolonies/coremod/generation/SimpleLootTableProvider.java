package com.minecolonies.coremod.generation;

import com.google.gson.Gson;
import net.minecraft.data.PackOutput;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Wrapper around the vanilla loot table provider which makes it easier to use.
 * Just override getName and registerTables
 */
public abstract class SimpleLootTableProvider implements DataProvider
{
    private static final Gson GSON = net.minecraft.world.level.storage.loot.Deserializers.createLootTableSerializer().create();
    private final PackOutput packOutput;

    protected SimpleLootTableProvider(PackOutput output)
    {
        this.packOutput = output;
    }

    protected abstract void registerTables(@NotNull final LootTableRegistrar registrar);

    @NotNull
    @Override
    public final CompletableFuture<?> run(@NotNull final CachedOutput cache)
    {
        final Map<ResourceLocation, LootTable.Builder> tables = new HashMap<>();

        registerTables((id, type, table) -> tables.put(id, table));

        final PackOutput.PathProvider pathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "loot_tables");
        final List<CompletableFuture<?>> futures = new ArrayList<>();
        tables.forEach((id, builder) -> futures.add(DataProvider.saveStable(
                cache,
                GSON.toJsonTree(builder.build()),
                pathProvider.json(id))));
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    protected void validate(@NotNull final Map<ResourceLocation, LootTable> tables,
                            @NotNull final ValidationContext validationContext)
    {
        tables.forEach((id, table) -> table.validate(validationContext));
    }

    @FunctionalInterface
    public interface LootTableRegistrar
    {
        void register(@NotNull ResourceLocation id, @NotNull LootContextParamSet type, @NotNull LootTable.Builder table);
    }

    /**
     * Helper method to make a loot entry builder for an ItemStack
     * @param stack The loot ItemStack
     * @return A loot entry builder for this stack
     */
    public static LootPoolSingletonContainer.Builder<?> itemStack(@NotNull final ItemStack stack)
    {
        if (!stack.isEmpty())
        {
            final LootPoolSingletonContainer.Builder<?> builder = LootItem.lootTableItem(stack.getItem());
            if (stack.hasTag())
            {
                assert stack.getTag() != null;
                builder.apply(SetNbtFunction.setTag(stack.getTag()));
            }
            if (stack.getCount() > 1)
            {
                builder.apply(SetItemCountFunction.setCount(ConstantValue.exactly(stack.getCount())));
            }
            return builder;
        }
        return EmptyLootItem.emptyItem();
    }
}
