package com.campersamu.shoutout.mixin;

import com.campersamu.shoutout.duck.OriginalItemDuck;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.Optional;

import static net.minecraft.item.ItemStack.DISPLAY_KEY;
import static net.minecraft.item.ItemStack.LORE_KEY;
import static net.minecraft.text.Text.Serializer.toJson;

@Mixin(value = PacketByteBuf.class, priority = 5000)
public class AppendModNameToPacketBuffer {
    @ModifyVariable(method = "writeItemStack(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/network/PacketByteBuf;", at = @At("HEAD"), argsOnly = true)
    private ItemStack alterLore(ItemStack stack) {
        return appendModName(stack);
    }

    private ItemStack appendModName(ItemStack itemStack) {
        var tooltips = new ArrayList<Text>(0);
        var ogStack = ((OriginalItemDuck) this).getOgItemStack();
        var item = ogStack.getItem();
        try {
            item.appendTooltip(ogStack, itemStack.getHolder() instanceof PlayerEntity player ? player.world : null, tooltips, TooltipContext.Default.NORMAL);
        } catch (Exception ignored) {
        }

        NbtList list = new NbtList();
        for (Text line : tooltips) {
            if (line instanceof MutableText mt)
                list.add(NbtString.of(toJson(mt.setStyle(mt.getStyle().withItalic(false)))));
        }

        String modName = getModName(item);
        list.add(NbtString.of(toJson(new LiteralText(modName).formatted(Formatting.BLUE, Formatting.ITALIC))));

        NbtCompound display = itemStack.getOrCreateSubNbt(DISPLAY_KEY);
        display.put(LORE_KEY, list);
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
