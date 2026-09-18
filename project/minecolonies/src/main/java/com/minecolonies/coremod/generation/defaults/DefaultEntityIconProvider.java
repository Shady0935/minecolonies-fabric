package com.minecolonies.coremod.generation.defaults;

import com.minecolonies.api.util.Log;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Datagen for entity_icon
 */
public class DefaultEntityIconProvider implements DataProvider
{
    private final FabricDataOutput output;

    public DefaultEntityIconProvider(@NotNull final FabricDataOutput output)
    {
        this.output = output;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "Default Citizen Icons";
    }

    private static boolean isEntitySkin(@NotNull final Path relativePath)
    {
        final String path = relativePath.toString().replace('\\', '/');
        return path.endsWith(".png") &&
                (path.startsWith("textures/entity/citizen/") || path.startsWith("textures/entity/raiders/"));
    }

    @NotNull
    @Override
    public CompletableFuture<?> run(@NotNull final CachedOutput cache)
    {
        final PackOutput.PathProvider outputProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "textures/entity_icon");
        final ModContainer modContainer = output.getModContainer();
        final Set<Path> skinFiles = new HashSet<>();

        try
        {
            for (final Path root : modContainer.getRootPaths())
            {
                final Path assetsRoot = root.resolve("assets").resolve(MOD_ID);
                final Path entityRoot = assetsRoot.resolve("textures").resolve("entity");
                if (!Files.isDirectory(entityRoot))
                {
                    continue;
                }

                try (final Stream<Path> files = Files.walk(entityRoot))
                {
                    files.filter(Files::isRegularFile)
                      .map(entityRoot.getParent().getParent()::relativize)
                      .filter(DefaultEntityIconProvider::isEntitySkin)
                      .map(entityRoot.getParent().getParent()::resolve)
                      .forEach(skinFiles::add);
                }
            }
        }
        catch (final IOException exception)
        {
            throw new IllegalStateException("Failed to enumerate MineColonies entity skins", exception);
        }

        final List<CompletableFuture<?>> icons = new ArrayList<>();
        for (final Path skinFile : skinFiles)
        {
            final Path assetsRoot = skinFile;
            final String normalized = assetsRoot.toString().replace('\\', '/');
            final int entityIndex = normalized.lastIndexOf("/textures/entity/");
            final String iconPath = normalized.substring(entityIndex + "/textures/entity/".length(), normalized.length() - ".png".length());
            icons.add(generateIcon(outputProvider, new ResourceLocation(MOD_ID, iconPath), skinFile, cache));
        }

        return CompletableFuture.allOf(icons.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> generateIcon(@NotNull final PackOutput.PathProvider outputProvider,
                                              @NotNull final ResourceLocation id,
                                              @NotNull final Path skinFile,
                                              @NotNull final CachedOutput cache)
    {
        return CompletableFuture.runAsync(() ->
        {
            try
            {
                try (final InputStream input = Files.newInputStream(skinFile))
                {
                    final BufferedImage skin = ImageIO.read(input);
                    if (skin == null)
                    {
                        throw new IOException("Unsupported image format");
                    }

                    saveIcon(outputProvider, id, createIconForSkin(skin), cache);
                }
            }
            catch (final IOException e)
            {
                Log.getLogger().error("Failed to save file to {}", id, e);
            }

        }, Util.backgroundExecutor());
    }

    private static BufferedImage createIconForSkin(@NotNull final BufferedImage skin)
    {
        if (skin.getWidth() < 16 || skin.getHeight() < 16)
        {
            throw new IllegalArgumentException("Entity skin is smaller than the vanilla head region: "
                                                 + skin.getWidth() + "x" + skin.getHeight());
        }

        final BufferedImage icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        final java.awt.Graphics2D graphics = icon.createGraphics();
        graphics.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                                   java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                                   java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        graphics.drawImage(skin, 0, 0, 16, 16, 8, 8, 16, 16, null);
        graphics.dispose();

        for (int i = 0; i < 16; ++i)
        {
            icon.setRGB(0, i, darken(icon.getRGB(0, i)));
            icon.setRGB(15, i, darken(icon.getRGB(15, i)));

            if (i > 0 && i < 15)
            {
                icon.setRGB(i, 0, darken(icon.getRGB(i, 0)));
                icon.setRGB(i, 15, darken(icon.getRGB(i, 15)));
            }
        }

        return icon;
    }

    private static int darken(final int argb)
    {
        final int alpha = (argb >>> 24) & 0xFF;
        final int red = ((argb >>> 16) & 0xFF) >> 1;
        final int green = ((argb >>> 8) & 0xFF) >> 1;
        final int blue = (argb & 0xFF) >> 1;
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private static void saveIcon(@NotNull final PackOutput.PathProvider outputProvider,
                                 @NotNull final ResourceLocation id,
                                 @NotNull final BufferedImage icon,
                                 @NotNull final CachedOutput cache) throws IOException
    {
        // convert to 24-bit, to reduce file size a bit
        final BufferedImage optimized = new BufferedImage(icon.getWidth(), icon.getHeight(), BufferedImage.TYPE_INT_RGB);
        optimized.getGraphics().drawImage(icon, 0, 0, null);

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final HashingOutputStream hashStream = new HashingOutputStream(Hashing.sha1(), outputStream);
        ImageIO.write(optimized, "PNG", hashStream);

        cache.writeIfNeeded(outputProvider.file(id, "png"), outputStream.toByteArray(), hashStream.hash());
    }
}
