package com.campersamu.shoutout;

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class Config {
    private Config(){}

    public static final Path
            CFG_FILE = FabricLoader.getInstance().getConfigDir().resolve("wayf.cfg"),
            FLAGS_FILE = FabricLoader.getInstance().getConfigDir().resolve("wayf.flags");

    public static final String
            ANNOTATE_VANILLA_KEY = "annotateVanilla",
            ANNOTATE_UNKNOWN_KEY = "annotateUnknown",
            UNKNOWN_TOOLTIP_NAME_KEY = "unknownTooltipName",
            MINECRAFT_KEY = "Minecraft",
            UNKNOWN_KEY /* = FLAGS.UNKNOWN_KEY */;

    public static final boolean ANNOTATE_VANILLA, ANNOTATE_UNKNOWN;

    private static final Properties
            MAPPED_MODS = new Properties(),
            FLAGS = new Properties();

    static {
        load();
        ANNOTATE_VANILLA = annotateVanilla();
        ANNOTATE_UNKNOWN = annotateUnknown();
        UNKNOWN_KEY = FLAGS.getProperty(UNKNOWN_TOOLTIP_NAME_KEY, "Unknown");
    }

    private static void save() {
        try (final OutputStream out = Files.newOutputStream(CFG_FILE)) {
            MAPPED_MODS.store(out, """
                    - Where Are You From - ID Remapper Configuration -#
                    -         Remove '#' to uncomment a line         -#
                    
                    - Remap Unknown Item IDs to Mod IDs. -#
                    - Format:
                    item_id_namespace = mod_id
                    - Example:
                    gofish = go-fish
                    """);
        } catch (IOException ignored) {}

        try (final OutputStream out = Files.newOutputStream(FLAGS_FILE)) {
            FLAGS.store(out, """
                    - Where Are You From - Configuration Flags -#
                    -      Remove '#' to uncomment a line      -#

                    - Determines whether or not vanilla items have the "Minecraft" lore annotation -#
                    annotateVanilla = true

                    - Determines whether or not unknown mod items have the "Unknown" lore annotation -#
                    annotateUnknown = true

                    - Configure the  "Unknown" lore annotation display name -#
                    unknownTooltipName = Unknown
                    """);
        } catch (IOException ignored) {}
    }
    private static void load() {
        try (final InputStream in = Files.newInputStream(CFG_FILE)) {
            MAPPED_MODS.load(in);
        } catch (IOException ignored) {
            save();
        }
        try (final InputStream in = Files.newInputStream(FLAGS_FILE)) {
            FLAGS.load(in);
        } catch (IOException ignored) {
            save();
        }
    }

    public static @NotNull String getMappedId(@NotNull final String itemNamespaceId) {
        return MAPPED_MODS.getProperty(itemNamespaceId, itemNamespaceId);
    }

    /**
     * Calculate the {@link Config#ANNOTATE_VANILLA} config option.
     * The precalculated {@link Config#ANNOTATE_VANILLA} static variable should be used instead.
     */
    @ApiStatus.Internal
    public static boolean annotateVanilla() {
        var flag = FLAGS.getOrDefault(ANNOTATE_VANILLA_KEY, true);
        System.out.println(flag);
        return Boolean.parseBoolean(flag.toString());
    }

    /**
     * Calculate the {@link Config#ANNOTATE_UNKNOWN} config option.
     * The precalculated {@link Config#ANNOTATE_UNKNOWN} static variable should be used instead.
     */
    @ApiStatus.Internal
    public static boolean annotateUnknown() {
        return Boolean.parseBoolean(FLAGS.getOrDefault(ANNOTATE_UNKNOWN_KEY, true).toString());
    }

    /**
     * Checks wether a config flag is triggered or not.
     *
     * @param modName to be checked
     * @return true if any flag is triggered, false if it is not
     */
    @ApiStatus.Internal
    public static boolean checkFlags(@NotNull final String modName) {
        return (!ANNOTATE_VANILLA && modName.equals(MINECRAFT_KEY)) || (!ANNOTATE_UNKNOWN && modName.equals(UNKNOWN_KEY));
    }
}
