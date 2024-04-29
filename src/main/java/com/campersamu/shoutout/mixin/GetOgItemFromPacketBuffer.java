package com.campersamu.shoutout.mixin;

import com.campersamu.shoutout.duck.OriginalItemDuck;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "net.minecraft.item.ItemStack$1", priority = 300)
public class GetOgItemFromPacketBuffer implements OriginalItemDuck {
    @Unique
    ItemStack originalItem = ItemStack.EMPTY;

    @ModifyVariable(method = "encode(Lnet/minecraft/network/RegistryByteBuf;Lnet/minecraft/item/ItemStack;)V", ordinal = 0, at = @At("HEAD"), argsOnly = true)
    private ItemStack saveOgItem(ItemStack itemStack) {
        originalItem = itemStack;
        return itemStack;
    }

    @Override
    public ItemStack whereAreYouFrom$getOgItemStack() {
        return originalItem;
    }
}
