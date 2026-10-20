package com.minecolonies.api.util;

import com.minecolonies.coremod.MineColonies;
import net.minecraft.world.entity.Entity;
import java.util.Locale;

/** Shared selection for navigation, recovery and worker diagnostic messages. */
public final class CitizenAIDiagnostics
{
    private CitizenAIDiagnostics() { }

    public static boolean isSelected(final Entity entity)
    {
        if (entity == null) return false;
        final var filters = MineColonies.getConfig().getServer().citizenAiDebugCitizens.get();
        if (filters.isEmpty()) return true;
        final String name = entity.getName().getString().toLowerCase(Locale.ROOT);
        final String uuid = entity.getUUID().toString();
        for (final String filter : filters)
        {
            final String term = filter.trim().toLowerCase(Locale.ROOT);
            if (!term.isEmpty() && (name.contains(term) || uuid.equals(term))) return true;
        }
        return false;
    }

    public static boolean shouldLog(final Entity entity)
    {
        return MineColonies.getConfig().getServer().citizenAiDebugLogging.get() && isSelected(entity);
    }
}
