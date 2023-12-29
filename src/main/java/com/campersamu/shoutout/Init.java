package com.campersamu.shoutout;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.campersamu.shoutout.Config.getMappedId;

public class Init implements DedicatedServerModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("Where Are You From?");

    @Override
    public void onInitializeServer() {
        LOGGER.info("Ooh you already know who it is...");
    }

    public static @NotNull String getModName(Item item) {
        String modName = "Unknown";

        Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer(getMappedId(Registries.ITEM.getId(item).getNamespace()));
        if (mod.isPresent()) {
            modName = mod.get().getMetadata().getName();
        }

        return modName;
    }
}
