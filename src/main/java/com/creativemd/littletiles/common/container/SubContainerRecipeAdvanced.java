package com.creativemd.littletiles.common.container;

import com.creativemd.creativecore.common.gui.opener.GuiHandler;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.item.ItemLittleRecipeAdvanced;
import com.creativemd.littletiles.common.packet.LittleActionMessagePacket;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.selection.mode.SelectionMode;
import com.creativemd.littletiles.common.util.tooltip.ActionMessage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;

public class SubContainerRecipeAdvanced extends SubContainerRecipe {
    
    public SubContainerRecipeAdvanced(EntityPlayer player, ItemStack stack) {
        super(player, stack);
    }
    
    @Override
    public void onPacketReceive(NBTTagCompound nbt) {
        if (nbt.getBoolean("save_selection")) {
            SelectionMode mode = ItemLittleRecipeAdvanced.getSelectionMode(stack);
            try {
                LittlePreviews previews = mode.getPreviews(player.world, player, stack, nbt.getBoolean("includeVanilla"), nbt.getBoolean("includeCB"), nbt.getBoolean("includeLT"), nbt
                    .getBoolean("remember_structure"));
                
                if (nbt.hasKey("grid")) {
                    LittleGridContext grid = LittleGridContext.get(nbt.getInteger("grid"));
                    previews.convertTo(grid);
                    LittleGridContext aimedGrid = LittleGridContext.get(nbt.getInteger("aimedGrid"));
                    if (aimedGrid.size > grid.size)
                        LittlePreviews.setLittlePreviewsContextSecretly(previews, aimedGrid);
                    else
                        LittlePreviews.advancedScale(previews, aimedGrid.size, grid.size);
                    previews.combinePreviewBlocks();
                }
                
                previews.removeOffset();
                
                ((ItemLittleRecipeAdvanced) stack.getItem()).saveLittlePreview(stack, previews);
                mode.clearSelection(stack);
                
                sendNBTToGui(stack.getTagCompound());
                GuiHandler.openGui("recipeadvanced", new NBTTagCompound(), player);
            } catch (LittleActionException e) {
                ActionMessage message = e.getActionMessage();
                if (message != null)
                    PacketHandler.sendPacketToPlayer(new LittleActionMessagePacket(message), (EntityPlayerMP) player);
                else
                    player.sendMessage(new TextComponentString(e.getLocalizedMessage()));
            }
            
        }
        
        super.onPacketReceive(nbt);
        
        if (nbt.getBoolean("clear_content"))
            GuiHandler.openGui("recipeadvanced", new NBTTagCompound(), player);
    }
    
}
