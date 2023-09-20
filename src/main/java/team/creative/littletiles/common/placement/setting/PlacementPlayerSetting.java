package team.creative.littletiles.common.placement.setting;

import net.minecraft.nbt.CompoundTag;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.packet.action.PlacementPlayerSettingPacket;
import team.creative.littletiles.common.placement.mode.PlacementMode;

public class PlacementPlayerSetting {
    
    public static final String SETTING_KEY = "littletiles:place";
    
    protected PlacementMode mode;
    protected LittleGrid grid;
    
    public PlacementPlayerSetting() {
        mode = PlacementMode.getDefault();
        grid = LittleGrid.defaultGrid();
    }
    
    public PlacementPlayerSetting(CompoundTag nbt) {
        grid = LittleGrid.get(nbt);
        mode = PlacementMode.getMode(nbt.getString("mode"));
    }
    
    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        grid.set(nbt);
        nbt.putString("mode", mode.getId());
        return nbt;
    }
    
    public LittleGrid grid() {
        return grid;
    }
    
    public PlacementMode placementMode() {
        return mode;
    }
    
    public void changed() {
        LittleTiles.NETWORK.sendToServer(new PlacementPlayerSettingPacket(this));
    }
    
    public void grid(LittleGrid grid) {
        this.grid = grid;
        changed();
    }
    
    public void placementMode(PlacementMode mode) {
        this.mode = mode;
        changed();
    }
    
    public void set(LittleGrid grid, PlacementMode mode) {
        this.grid = grid;
        this.mode = mode;
        changed();
    }
    
    public void refreshGrid() {
        if (grid != null)
            grid = LittleGrid.getOrDefault(grid.count);
        else
            grid = LittleGrid.defaultGrid();
    }
    
}
