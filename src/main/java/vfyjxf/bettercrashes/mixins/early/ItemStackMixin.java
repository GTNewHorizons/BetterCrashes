package vfyjxf.bettercrashes.mixins.early;

import java.util.Collections;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import vfyjxf.bettercrashes.BetterCrashes;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    private static final Set<Item> BROKEN_ITEMS = Collections.synchronizedSet(new HashSet<>());

    @Redirect(
            method = "getDisplayName",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/Item;getItemStackDisplayName(Lnet/minecraft/item/ItemStack;)Ljava/lang/String;"))
    private String betterCrashes$safeGetDisplayName(Item item, ItemStack stack) {
        try {
            return item.getItemStackDisplayName(stack);
        } catch (IllegalFormatException e) {
            String unlocalizedName;
            try {
                unlocalizedName = item.getUnlocalizedName(stack);
            } catch (IllegalFormatException ex2) {
                unlocalizedName = item.getClass().getSimpleName();
            }
            if (BROKEN_ITEMS.add(item)) {
                BetterCrashes.logger.error("Format error in display name for item '{}'", unlocalizedName, e);
            }
            return unlocalizedName + " [FORMAT ERROR]";
        }
    }
}
