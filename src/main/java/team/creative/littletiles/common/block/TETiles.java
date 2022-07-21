package team.creative.littletiles.common.block;

import net.minecraft.tileentity.TileEntity;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.grid.IGridBased;
import team.creative.littletiles.common.grid.LittleGrid;

public class TETiles extends TileEntity implements IGridBased {
    
    public TETiles() {
        super(LittleTiles.TILES_TE_TYPE);
    }
    
    @Override
    public LittleGrid getGrid() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public void convertTo(LittleGrid to) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public int getSmallest() {
        // TODO Auto-generated method stub
        return 0;
    }
    
}
