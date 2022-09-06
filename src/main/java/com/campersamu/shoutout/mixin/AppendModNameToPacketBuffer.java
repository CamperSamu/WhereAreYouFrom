package com.campersamu.shoutout.mixin;

import com.campersamu.shoutout.duck.OriginalItemDuck;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Optional;

import static net.minecraft.item.ItemStack.DISPLAY_KEY;
import static net.minecraft.item.ItemStack.LORE_KEY;
import static net.minecraft.nbt.NbtElement.*;
import static net.minecraft.text.Text.Serializer.toJson;
import static net.minecraft.text.Text.literal;

@Mixin(value = PacketByteBuf.class, priority = 5000)
public class AppendModNameToPacketBuffer {
        @ModifyVariable(method = "writeItemStack(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/network/PacketByteBuf;", at = @At("HEAD"), argsOnly = true)
        private ItemStack alterLore(ItemStack inStack) {
            if (!inStack.isEmpty()) {
                return appendModName(inStack.copy());   //We have to copy the ItemStack, otherwise it will cause a Concurrent Modification Exception with more than one player
            }
            return inStack;
        }

    @Contract("_ -> param1")
    private @NotNull ItemStack appendModName(@NotNull ItemStack itemStack) {
        var ogStack = ((OriginalItemDuck) this).getOgItemStack();
        var item = ogStack.getItem();

        NbtCompound display = itemStack.getOrCreateNbt().getCompound(DISPLAY_KEY);
        NbtList list = display.getList(LORE_KEY, STRING_TYPE);


        NbtString modText = NbtString.of(toJson(literal(getModName(item)).formatted(Formatting.BLUE, Formatting.ITALIC)));

        if (!list.contains(modText))
            list.add(modText);

        display.put(LORE_KEY, list);
        itemStack.getOrCreateNbt().put(DISPLAY_KEY, display);
        return itemStack;
    }

    private String getModName(Item item) {
        String modName = "Unknown";

        Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer(Registry.ITEM.getId(item).getNamespace());
        if (mod.isPresent()) {
            modName = mod.get().getMetadata().getName();
        }

        return modName;
    }
}
