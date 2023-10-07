package team.creative.littletiles.common.placement.setting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.packet.action.PlacementPlayerSettingPacket;
import team.creative.littletiles.common.placement.mode.PlacementMode;

public class PlacementPlayerSetting {
    
    public static final String SETTING_KEY = "littletiles:place";
    
    protected PlacementMode mode;
    protected LittleGrid grid;
    private int configuredGrid;
    
    public PlacementPlayerSetting() {
        mode = PlacementMode.getDefault();
        configuredGrid = -1;
    }
    
    public PlacementPlayerSetting(CompoundTag nbt) {
        mode = PlacementMode.getMode(nbt.getString("mode"));
        configuredGrid = nbt.contains(LittleGrid.GRID_KEY) ? nbt.getInt(LittleGrid.GRID_KEY) : -1;
    }
    
    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt(LittleGrid.GRID_KEY, configuredGrid);
        nbt.putString("mode", mode.getId());
        return nbt;
    }
    
    public void receive(Player player, PlacementPlayerSetting settings) {
        this.configuredGrid = settings.configuredGrid;
        this.grid = settings.grid;
        this.mode = settings.mode;
        refreshGrid(player);
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
        this.configuredGrid = grid.count;
        changed();
    }
    
    public void placementMode(PlacementMode mode) {
        this.mode = mode;
        changed();
    }
    
    public void set(LittleGrid grid, PlacementMode mode) {
        this.grid = grid;
        this.configuredGrid = grid.count;
        this.mode = mode;
        changed();
    }
    
    public void refreshGrid(Player player) {
        grid = LittleTiles.CONFIG.build.get(player).getOrDefault(configuredGrid);
    }
    
}
