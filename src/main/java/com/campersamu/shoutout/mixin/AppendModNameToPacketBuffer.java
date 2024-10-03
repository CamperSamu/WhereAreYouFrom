package com.campersamu.shoutout.mixin;

import com.campersamu.shoutout.duck.OriginalItemDuck;
import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMapImpl;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

import static com.campersamu.shoutout.Config.*;
import static com.campersamu.shoutout.Init.getModName;
import static net.minecraft.text.Text.literal;


@Mixin(targets = "net/minecraft/item/ItemStack$1", priority = 5000)
public class AppendModNameToPacketBuffer {
    //fixme: workaround LoreComponent([lore], null) misuse
    //       check LoreComponent#STYLE
    @Unique private static final Style DEFAULT_LORE_STYLE = Style.EMPTY.withColor(Formatting.DARK_PURPLE).withItalic(true);

    @Redirect(method = "encode(Lnet/minecraft/network/RegistryByteBuf;Lnet/minecraft/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/PacketCodec;encode(Ljava/lang/Object;Ljava/lang/Object;)V", ordinal = 1))
    private void shoutout$appendStuff(@NotNull PacketCodec<RegistryByteBuf, ComponentChanges> instance, Object _buf, Object _changes, @Local(argsOnly = true) ItemStack itemStack) {
        // Safely cast the two values
        if (!(_buf instanceof RegistryByteBuf buf && _changes instanceof ComponentChanges changes)) return;

        // Get mod name
        final var modName = getModName(((OriginalItemDuck) this).whereAreYouFrom$getOgItemStack().getItem());
        // Avoid manipulation if a flag is triggered
        if (checkFlags(modName)) {
            instance.encode(buf, changes);
            return;
        }

        // Add the mod name and a lore component to the item stack
        final ComponentMapImpl cmap = appendModName(ComponentMapImpl.create(itemStack.getItem().getComponents(), changes), modName);

        // Encode the manipulated component map
        instance.encode(buf, cmap.getChanges());
    }

    @Unique
    @Contract("_, _ -> param1")
    private @NotNull ComponentMapImpl appendModName(@NotNull final ComponentMapImpl cmap, @NotNull final String modName) {
        // Get the lore component
        var lore = cmap.getOrDefault(DataComponentTypes.LORE, new LoreComponent(List.of()));

        // Check if the ignore list is enabled
        if (IGNORE_LIST_ENABLED) {
            for (Text text : lore.lines()) {
                // If so, check the lore lines if something included in the ignore list is found and avoid further actions
                if (IGNORE_LIST.contains(text.getString())) {
                    return cmap;
                }
            }
        }

        // Append mod name
        final var modText = literal(modName).formatted(Formatting.BLUE, Formatting.ITALIC);

        //fixme: workaround LoreComponent([lore], null) misuse
        final List<Text> styledLore = (lore.styledLines() != null)
                ? lore.styledLines()
                : Lists.transform(lore.lines(), style -> Texts.setStyleIfAbsent(style.copy(), DEFAULT_LORE_STYLE));

        // Check if the mod name is already present to avoid duplication (edge-case proofing)
        if (!styledLore.contains(modText))
            lore = lore.with(modText); // Add the lore (record since 1.20.5, it returns an updated version of itself)
        else {
            final var loreList = new ArrayList<>(styledLore);
            loreList.remove(modText);
            lore = new LoreComponent(loreList);
        }

        // Update the ComponentMap
        cmap.set(DataComponentTypes.LORE, lore);

        return cmap;
    }

}
