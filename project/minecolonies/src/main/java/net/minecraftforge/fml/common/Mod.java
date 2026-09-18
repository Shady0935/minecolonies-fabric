package net.minecraftforge.fml.common;

import com.minecolonies.fabric.dist.Dist;
import com.minecolonies.fabric.event.IEventBus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Supplier;

/**
 * Source-compatibility annotation for upstream MineColonies classes.  Fabric
 * discovers the real entrypoints from fabric.mod.json; this annotation is
 * intentionally metadata only.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Mod
{
    String value() default "";

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface EventBusSubscriber
    {
        String modid() default "";
        Dist[] value() default {};
        Bus bus() default Bus.FORGE;

        enum Bus
        {
            FORGE,
            MOD;

            public Supplier<IEventBus> bus()
            {
                return () -> this == FORGE ? IEventBus.FORGE : IEventBus.MOD;
            }
        }
    }

}
