package com.creativemd.littletiles.client.gui.configure;

import com.creativemd.creativecore.common.gui.container.SubGui;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import team.creative.littletiles.client.LittleTilesClient;

public abstract class SubGuiConfigure extends SubGui {
    
    public ItemStack stack;
    
    public SubGuiConfigure(int width, int height, ItemStack stack) {
        super(width, height);
        this.stack = stack;
    }
    
    public abstract void saveConfiguration();
    
    @Override
    public boolean onKeyPressed(char character, int key) {
        if (super.onKeyPressed(character, key))
            return true;
        if (LittleTilesClient.configure.getKeyCode() == key) {
            closeGui();
            return true;
        }
        return false;
    }
    
    @Override
    public void onClosed() {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        saveConfiguration();
        
        sendPacketToServer(stack.getTagCompound());
        
        super.onClosed();
    }
    
}
