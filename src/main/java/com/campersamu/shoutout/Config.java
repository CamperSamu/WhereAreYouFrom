package com.campersamu.shoutout;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class Config {
    private Config(){}

    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("wayf.cfg");
    private static final Properties MAPPED_MODS = new Properties();

    static {
        load();
    }

    private static void save() {
        try (final OutputStream out = Files.newOutputStream(FILE)) {
            MAPPED_MODS.store(out, """
                    Remap Unknown Item IDs to Mod IDs.
                    Format:
                    item_id_namespace = mod_id
                    Example:
                    gofish = go-fish""");
        } catch (IOException ignored) {}

    }
    private static void load() {
        try (final InputStream in = Files.newInputStream(FILE)) {
            MAPPED_MODS.load(in);
        } catch (IOException ignored) {
            save();
        }
    }

    public static String getMappedId(String itemNamespaceId) {
        return MAPPED_MODS.getProperty(itemNamespaceId, itemNamespaceId);
    }
}
