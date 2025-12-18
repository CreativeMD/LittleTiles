package team.creative.littletiles.mixin.common.menu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.common.ingredient.ColorIngredient;
import team.creative.littletiles.common.item.ItemColorIngredient;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {
    
    @Inject(method = "moveItemStackTo(Lnet/minecraft/world/item/ItemStack;IIZ)Z", require = 1, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;isStackable()Z"), cancellable = true)
    protected void moveItemStackTo(ItemStack stack, int index, int indexTo, boolean reverse, CallbackInfoReturnable<Boolean> info) {
        if (stack.getItem() instanceof ItemColorIngredient t) {
            boolean changed = false;
            int i = reverse ? indexTo - 1 : index;
            
            int amount = stack.getOrDefault(LittleTilesRegistry.COLOR_AMOUNT, 0);
            while (!stack.isEmpty() && (reverse ? i >= index : i < indexTo)) {
                Slot slot = ((AbstractContainerMenu) (Object) this).slots.get(i);
                ItemStack other = slot.getItem();
                
                if (other.getItem() instanceof ItemColorIngredient o && o.type == t.type) {
                    int otherAmount = other.getOrDefault(LittleTilesRegistry.COLOR_AMOUNT, 0);
                    int toAdd = Math.min(ColorIngredient.BOTTLE_SIZE - otherAmount, amount);
                    if (toAdd > 0) {
                        slot.getItem().set(LittleTilesRegistry.COLOR_AMOUNT, otherAmount + toAdd);
                        amount -= toAdd;
                        slot.setChanged();
                        changed = true;
                    }
                }
                
                if (amount <= 0)
                    break;
                
                if (reverse)
                    i--;
                else
                    i++;
            }
            
            if (changed) {
                if (amount <= 0) {
                    stack.remove(LittleTilesRegistry.COLOR_AMOUNT);
                    stack.setCount(0);
                } else
                    stack.set(LittleTilesRegistry.COLOR_AMOUNT, amount);
                info.setReturnValue(changed);
            }
        }
    }
    
}
