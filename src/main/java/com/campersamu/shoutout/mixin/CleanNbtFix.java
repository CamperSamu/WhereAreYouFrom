package com.campersamu.shoutout.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

import static com.campersamu.shoutout.Init.getModName;
import static net.minecraft.text.Text.literal;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class CleanNbtFix {
    @Shadow
    @Final
    protected ServerPlayerEntity player;

    @Inject(method = "setGameMode", at = @At("RETURN"))
    protected void setGameMode(GameMode gameMode, GameMode previousGameMode, CallbackInfo ci) {
        final var inventory = player.getInventory();
        for (ItemStack itemStack : inventory.main) {
            cleanNbt(itemStack);
        }
        cleanNbt(inventory.offHand.getFirst());
        for (ItemStack itemStack : inventory.armor) {
            cleanNbt(itemStack);
        }
        player.getInventory().markDirty();
    }

    @Unique
    private void cleanNbt(@NotNull ItemStack stack) {
        // Get lore & formatted mod name
        final var list = new ArrayList<>(stack.getComponents().getOrDefault(DataComponentTypes.LORE, new LoreComponent(List.of())).styledLines());
        final var modText = literal(getModName(stack.getItem())).formatted(Formatting.BLUE, Formatting.ITALIC);

        // If the lore isn't empty, try to remove the lore
        if (!list.isEmpty()) {
            list.remove(modText);

            // Update the lore component
            stack.set(DataComponentTypes.LORE, new LoreComponent(list));
        }
    }
}
