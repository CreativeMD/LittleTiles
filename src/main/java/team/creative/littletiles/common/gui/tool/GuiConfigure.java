package team.creative.littletiles.common.gui.tool;

import com.mojang.serialization.DataResult;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.littletiles.client.LittleTilesClient;

public abstract class GuiConfigure extends GuiLayer {
    
    public ContainerSlotView tool;
    public final GuiSyncLocal<CompoundTag> SAVE_CONFIG = getSyncHolder().register("save_config", nbt -> {
        DataResult<DataComponentPatch> result = DataComponentPatch.CODEC.parse(NbtOps.INSTANCE, nbt);
        tool.get().applyComponents(result.getOrThrow());
        tool.changed();
    });
    
    public GuiConfigure(String name, int width, int height, ContainerSlotView tool) {
        super(name, width, height);
        this.tool = tool;
    }
    
    public GuiConfigure(String name, ContainerSlotView tool) {
        super(name);
        this.tool = tool;
    }
    
    /** @param data
     *            all changes to save to
     * @return if true the data will be send to the server and changes will be applied */
    public abstract boolean saveConfiguration(PatchedDataComponentMap data);
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers))
            return true;
        if (LittleTilesClient.KEY_CONFIGURE.matches(keyCode, scanCode)) {
            closeTopLayer();
            return true;
        }
        return false;
    }
    
    @Override
    public void closed() {
        if (isClient()) {
            PatchedDataComponentMap map = new PatchedDataComponentMap(DataComponentMap.EMPTY);
            if (saveConfiguration(map)) {
                DataResult<Tag> dataresult = DataComponentPatch.CODEC.encode(map.asPatch(), NbtOps.INSTANCE, new CompoundTag());
                SAVE_CONFIG.send((CompoundTag) dataresult.getOrThrow());
            }
        }
        super.closed();
    }
    
}
