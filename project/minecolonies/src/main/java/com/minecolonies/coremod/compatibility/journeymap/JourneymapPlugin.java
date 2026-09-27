package com.minecolonies.coremod.compatibility.journeymap;

import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.client.JmApiVersion;
import journeymap.api.v2.common.JourneyMapPlugin;
import journeymap.api.v2.common.event.ClientEventRegistry;
import journeymap.api.v2.client.event.MappingEvent;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Plugin entrypoint for JourneyMap
 */
@JourneyMapPlugin(apiVersion = JmApiVersion.VERSION)
public class JourneymapPlugin implements IClientPlugin
{
    private Journeymap jmap;
    private EventListener listener;

    @Override
    public void initialize(@NotNull final IClientAPI api)
    {
        this.jmap = new Journeymap(api);
        this.listener = new EventListener(this.jmap);

        ClientEventRegistry.MAPPING_EVENT.subscribe(MOD_ID, event ->
        {
            if (event.getStage() == MappingEvent.Stage.MAPPING_STARTED)
            {
                this.jmap.beginMapping(event.dimension);
                ColonyBorderMapping.load(this.jmap, event.dimension);
            }
            else if (event.getStage() == MappingEvent.Stage.MAPPING_STOPPED)
            {
                ColonyBorderMapping.unload(this.jmap, event.dimension);
                ColonyDeathpoints.unload(this.jmap, event.dimension);
                this.jmap.endMapping(event.dimension);
            }
        });

        ClientEventRegistry.OPTIONS_REGISTRY_EVENT.subscribe(MOD_ID,
          event -> this.jmap.setOptions(new JourneymapOptions()));

        ClientEventRegistry.INFO_SLOT_REGISTRY_EVENT.subscribe(MOD_ID,
          event -> event.register(MOD_ID, "com.minecolonies.coremod.journeymap.currentcolony",
            2500, ColonyBorderMapping::getCurrentColony));
    }

    @Override
    public String getModId()
    {
        return MOD_ID;
    }

}
