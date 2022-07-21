package team.creative.littletiles.common.math.face;

import java.util.List;

import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.geo.VectorFan;
import team.creative.littletiles.common.grid.LittleGrid;

public sealed interface ILittleFace permits LittleFace,LittleServerFace {
    
    public LittleGrid getGrid();
    
    public void ensureGrid(LittleGrid context);
    
    public Facing facing();
    
    public Axis one();
    
    public Axis two();
    
    public int origin();
    
    public int minOne();
    
    public int minTwo();
    
    public int maxOne();
    
    public int maxTwo();
    
    public void set(int one, int two, boolean value);
    
    public boolean supportsCutting();
    
    public void cut(List<VectorFan> fans);
    
    public void setPartiallyFilled();
    
}
