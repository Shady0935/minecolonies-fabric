package net.minecraftforge.fml;

import net.fabricmc.loader.api.FabricLoader;

import java.util.Optional;

/** Mod discovery facade backed by Fabric Loader. */
public final class ModList
{
    private static final ModList INSTANCE = new ModList();

    private ModList()
    {
    }

    public static ModList get()
    {
        return INSTANCE;
    }

    public boolean isLoaded(final String id)
    {
        return FabricLoader.getInstance().isModLoaded(id);
    }

    public Optional<ModContainer> getModContainerById(final String id)
    {
        return FabricLoader.getInstance().getModContainer(id).map(container -> new ModContainer(id, container.getMetadata().getName(), container.getMetadata().getVersion().getFriendlyString()));
    }

    public Optional<ModFileInfo> getModFileById(final String id)
    {
        return getModContainerById(id).map(container -> new ModFileInfo());
    }

    public static final class ModContainer
    {
        private final ModInfo modInfo;

        private ModContainer(final String id, final String displayName, final String version)
        {
            this.modInfo = new ModInfo(id, displayName, version);
        }

        public ModInfo getModInfo()
        {
            return modInfo;
        }
    }

    public static final class ModInfo
    {
        private final String id;
        private final String displayName;
        private final String version;

        private ModInfo(final String id, final String displayName, final String version)
        {
            this.id = id;
            this.displayName = displayName;
            this.version = version;
        }

        public String getDisplayName()
        {
            return displayName;
        }

        public String getVersion()
        {
            return version;
        }

        public String getModId()
        {
            return id;
        }
    }

    public static final class ModFileInfo
    {
    }
}
