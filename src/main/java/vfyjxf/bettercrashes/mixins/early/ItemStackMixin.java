package vfyjxf.bettercrashes.mixins.early;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    private static final Logger LOGGER = LogManager.getLogger("BetterCrashes");

    @Redirect(
            method = "getDisplayName",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/Item;getItemStackDisplayName(Lnet/minecraft/item/ItemStack;)Ljava/lang/String;"))
    private String betterCrashes$safeGetDisplayName(Item item, ItemStack stack) {
        try {
            return item.getItemStackDisplayName(stack);
        } catch (Exception e) {
            String unlocalizedName = item.getUnlocalizedName(stack);
            LOGGER.error("Format error in display name for item '{}'", unlocalizedName, e);
            return unlocalizedName + " [FORMAT ERROR]";
        }
    }
}
