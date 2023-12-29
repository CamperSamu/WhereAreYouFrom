package com.campersamu.shoutout.mixin;

import com.campersamu.shoutout.duck.OriginalItemDuck;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import static com.campersamu.shoutout.Config.*;
import static com.campersamu.shoutout.Init.getModName;
import static net.minecraft.item.ItemStack.DISPLAY_KEY;
import static net.minecraft.item.ItemStack.LORE_KEY;
import static net.minecraft.nbt.NbtElement.STRING_TYPE;
import static net.minecraft.text.Text.Serialization.toJsonString;
import static net.minecraft.text.Text.literal;

@Mixin(value = PacketByteBuf.class, priority = 5000)
public class AppendModNameToPacketBuffer {

    @ModifyVariable(method = "writeItemStack(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/network/PacketByteBuf;", at = @At("HEAD"), argsOnly = true)
    private ItemStack alterLore(ItemStack inStack) {
        return inStack;
    }

    @Redirect(
            method = "writeItemStack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/PacketByteBuf;writeNbt(Lnet/minecraft/nbt/NbtElement;)Lnet/minecraft/network/PacketByteBuf;"
            )
    )
    private PacketByteBuf appendModNameToNBT(PacketByteBuf instance, NbtElement nbt){
        if (nbt == null) nbt = new NbtCompound();
        if (nbt instanceof NbtCompound compound) {
            //region Get the mod name & check flags
            final var modName = getModName(((OriginalItemDuck) this).whereAreYouFrom$getOgItemStack().getItem());
            if (checkFlags(modName)) return instance.writeNbt(nbt); // skip if flag triggered
            //endregion

            return instance.writeNbt(appendModName(compound, modName));
        }
        return instance.writeNbt(nbt);
    }

    @Unique
    @Contract("_, _ -> param1")
    private @NotNull NbtCompound appendModName(@NotNull NbtCompound nbtCompound, @NotNull final String modName) {
        // Get the NBT structure
        NbtCompound display = nbtCompound.getCompound(DISPLAY_KEY);
        NbtList list = display.getList(LORE_KEY, STRING_TYPE);

        // Append mod name
        NbtString modText = NbtString.of(toJsonString(literal(modName).formatted(Formatting.BLUE, Formatting.ITALIC)));

        // Check if the mod name is already present to avoid duplication (edge-case proofing)
        if (!list.contains(modText))
            list.add(modText);

        // Update Item Lore / NBT
        display.put(LORE_KEY, list);
        nbtCompound.put(DISPLAY_KEY, display);

        return nbtCompound;
    }

}
