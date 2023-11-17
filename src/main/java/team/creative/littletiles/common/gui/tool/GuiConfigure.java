package team.creative.littletiles.common.gui.tool;

import net.minecraft.nbt.CompoundTag;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.littletiles.api.common.tool.ILittleTool;
import team.creative.littletiles.client.LittleTilesClient;

public abstract class GuiConfigure extends GuiLayer {
    
    public ContainerSlotView tool;
    public final GuiSyncLocal<CompoundTag> SAVE_CONFIG = getSyncHolder().register("save_config", nbt -> {
        if (supportsConfiguration() && tool.get().getItem() instanceof ILittleTool item) {
            item.configured(tool.get(), nbt);
            tool.changed();
        }
    });
    
    public GuiConfigure(String name, int width, int height, ContainerSlotView tool) {
        super(name, width, height);
        this.tool = tool;
    }
    
    public GuiConfigure(String name, ContainerSlotView tool) {
        super(name);
        this.tool = tool;
    }
    
    protected boolean supportsConfiguration() {
        return true;
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
        if (isClient()) {
            CompoundTag nbt = saveConfiguration(new CompoundTag());
            if (nbt != null)
                SAVE_CONFIG.send(nbt);
        }
        super.closed();
    }
    
}
