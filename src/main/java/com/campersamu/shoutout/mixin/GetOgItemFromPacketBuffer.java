package com.campersamu.shoutout.mixin;

import com.campersamu.shoutout.duck.OriginalItemDuck;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = PacketByteBuf.class, priority = 500)
public class GetOgItemFromPacketBuffer implements OriginalItemDuck {
    ItemStack originalItem = ItemStack.EMPTY;

    @ModifyVariable(method = "writeItemStack(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/network/PacketByteBuf;", at = @At("HEAD"), argsOnly = true)
    private ItemStack saveOgItem(ItemStack itemStack) {
        originalItem = itemStack;
        return itemStack;
    }

    @Override
    public ItemStack getOgItemStack() {
        return originalItem;
    }
}
