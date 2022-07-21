package team.creative.littletiles.common.gui.configure;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.packet.item.ConfigurePacket;

public abstract class GuiConfigure extends GuiLayer {
    
    public ItemStack stack;
    
    public GuiConfigure(String name, int width, int height, ItemStack stack) {
        super(name, width, height);
        this.stack = stack;
    }
    
    public abstract CompoundTag saveConfiguration(CompoundTag nbt);
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers))
            return true;
        if (LittleTilesClient.configure.matches(keyCode, scanCode)) {
            closeTopLayer();
            return true;
        }
        return false;
    }
    
    @Override
    public void closed() {
        if (isClient())
            LittleTiles.NETWORK.sendToServer(new ConfigurePacket(stack, saveConfiguration(new CompoundTag())));
        
        super.closed();
    }
    
}
