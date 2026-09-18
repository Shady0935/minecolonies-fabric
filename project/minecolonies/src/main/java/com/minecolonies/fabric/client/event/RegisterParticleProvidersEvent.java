package com.minecolonies.fabric.client.event;

import com.minecolonies.fabric.event.Event;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

import java.util.function.Function;

/** Marker event for client particle provider registration. */
public final class RegisterParticleProvidersEvent extends Event
{
    private final java.util.Map<ParticleType<?>, ParticleProvider<?>> providers = new java.util.LinkedHashMap<>();
    private final java.util.Map<ParticleType<?>, Function<SpriteSet, ? extends ParticleProvider<?>>> providerFactories = new java.util.LinkedHashMap<>();

    public <T extends ParticleOptions> void register(final ParticleType<T> type, final ParticleProvider<T> provider)
    {
        providers.put(type, provider);
    }

    public <T extends ParticleOptions> void register(final ParticleType<T> type, final Function<SpriteSet, ? extends ParticleProvider<T>> providerFactory)
    {
        providerFactories.put(type, providerFactory);
    }

    public java.util.Map<ParticleType<?>, ParticleProvider<?>> getProviders()
    {
        return java.util.Map.copyOf(providers);
    }

    public java.util.Map<ParticleType<?>, Function<SpriteSet, ? extends ParticleProvider<?>>> getProviderFactories()
    {
        return java.util.Map.copyOf(providerFactories);
    }
}
