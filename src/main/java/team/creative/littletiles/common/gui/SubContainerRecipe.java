package team.creative.littletiles.common.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.item.ItemStack;

public class SubContainerRecipe extends SubContainerConfigure {
    
    public SubContainerRecipe(EntityPlayer player, ItemStack stack) {
        super(player, stack);
    }
    
    @Override
    public void onPacketReceive(NBTTagCompound nbt) {
        if (nbt.getBoolean("clear_content")) {
            /*LittleTilePreview.removePreviewTiles(stack);
            stack.getTagCompound().removeTag("structure");*/
            stack.setTagCompound(null);
            sendNBTToGui(new NBTTagCompound());
        } else if (nbt.getBoolean("set_structure")) {
            stack.setTagCompound(nbt.getCompoundTag("stack"));
        }
    }
    
}
