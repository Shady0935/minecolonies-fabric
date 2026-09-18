package net.minecraftforge.fml;

import com.minecolonies.fabric.dist.Dist;
import net.fabricmc.loader.api.FabricLoader;

import java.util.function.Supplier;

/** Distribution helper backed by Fabric's environment. */
public final class DistExecutor
{
    private DistExecutor()
    {
    }

    public static <T> T unsafeRunForDist(final Supplier<Supplier<T>> client, final Supplier<Supplier<T>> server)
    {
        return (FabricLoader.getInstance().getEnvironmentType().name().equals("CLIENT") ? client : server).get().get();
    }

    public static void unsafeRunWhenOn(final Dist dist, final Supplier<Runnable> action)
    {
        if ((dist == Dist.CLIENT) == FabricLoader.getInstance().getEnvironmentType().name().equals("CLIENT"))
        {
            final Runnable runnable = action.get();
            if (runnable != null) runnable.run();
        }
    }
}
