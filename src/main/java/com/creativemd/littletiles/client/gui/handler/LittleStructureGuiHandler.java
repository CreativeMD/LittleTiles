package com.creativemd.littletiles.client.gui.handler;

import com.creativemd.creativecore.common.gui.container.SubContainer;
import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.opener.CustomGuiHandler;
import com.creativemd.creativecore.common.gui.opener.GuiHandler;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tile.math.location.StructureLocation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.littletiles.common.action.LittleActionException;

public abstract class LittleStructureGuiHandler extends CustomGuiHandler {
    
    public static void openGui(String id, NBTTagCompound nbt, EntityPlayer player, LittleStructure structure) {
        nbt.setTag("location", new StructureLocation(structure).write());
        GuiHandler.openGui(id, nbt, player);
    }
    
    public abstract SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt, LittleStructure structure);
    
    @Override
    public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt) {
        try {
            return getContainer(player, nbt, new StructureLocation(nbt.getCompoundTag("location")).find(player.world));
        } catch (LittleActionException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @SideOnly(Side.CLIENT)
    public abstract SubGui getGui(EntityPlayer player, NBTTagCompound nbt, LittleStructure structure);
    
    @Override
    @SideOnly(Side.CLIENT)
    public SubGui getGui(EntityPlayer player, NBTTagCompound nbt) {
        try {
            return getGui(player, nbt, new StructureLocation(nbt.getCompoundTag("location")).find(player.world));
        } catch (LittleActionException e) {
            e.printStackTrace();
        }
        return null;
    }
    
}
