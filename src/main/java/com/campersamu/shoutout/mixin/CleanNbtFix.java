package com.campersamu.shoutout.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.campersamu.shoutout.Init.getModName;
import static java.util.Objects.requireNonNull;
import static net.minecraft.item.ItemStack.DISPLAY_KEY;
import static net.minecraft.item.ItemStack.LORE_KEY;
import static net.minecraft.nbt.NbtElement.LIST_TYPE;
import static net.minecraft.nbt.NbtElement.STRING_TYPE;
import static net.minecraft.text.Text.Serialization.toJsonString;
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
        cleanNbt(inventory.offHand.get(0));
        for (ItemStack itemStack : inventory.armor) {
            cleanNbt(itemStack);
        }
        player.getInventory().markDirty();
    }

    @Unique
    private void cleanNbt(@NotNull ItemStack stack) {
        if (!stack.equals(ItemStack.EMPTY) && stack.getNbt() != null && stack.getNbt().getCompound(DISPLAY_KEY) != null) {
            try {   //attempt to fix the real item lore alteration caused by other mods
                final var list = requireNonNull(stack.getOrCreateNbt().getCompound(DISPLAY_KEY)).getList(LORE_KEY, STRING_TYPE);
                final var modText = NbtString.of(toJsonString(literal(getModName(stack.getItem())).formatted(Formatting.BLUE, Formatting.ITALIC)));
                if (list != null) {
                    list.remove(modText);
                    stack.getOrCreateNbt().getCompound(DISPLAY_KEY).put(LORE_KEY, list);
                }
                if (stack.getNbt().getCompound(DISPLAY_KEY).getList(LORE_KEY, STRING_TYPE).isEmpty())
                    stack.getNbt().getCompound(DISPLAY_KEY).remove(LORE_KEY);
                if (stack.getNbt().getCompound(DISPLAY_KEY).isEmpty())
                    stack.getNbt().remove(DISPLAY_KEY);
            } catch (NullPointerException ignored) {}
        }
    }
}
