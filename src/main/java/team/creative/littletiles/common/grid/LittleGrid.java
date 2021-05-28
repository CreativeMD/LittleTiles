package team.creative.littletiles.common.grid;

import com.creativemd.littletiles.common.tile.math.LittleUtils;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

public class LittleGrid {
    
    public static final int OVERALL_DEFAULT = 16;
    
    public static LittleGrid get(int grid) {}
    
    public static LittleGrid defaultGrid() {
        return 
    }
    
    public static LittleGrid getMin() {
        
    }
    
    public static LittleGrid getMax() {
        return context[context.length - 1];
    }
    
    public static LittleGrid max(LittleGrid context, LittleGrid context2) {
        if (context.count >= context2.count)
            return context;
        return context2;
    }
    
    public final int count;
    public final int count2d;
    public final int count3d;
    public final double pixelLength;
    public final double halfPixelLength;
    public final double pixelVolume;
    protected final int index;
    
    public int getMinGrid(int value) {
        return minSizes[Math.abs(value % count)];
    }
    
    public double toVanillaGrid(double grid) {
        return grid * pixelLength;
    }
    
    public float toVanillaGrid(float grid) {
        return (float) (grid * pixelLength);
    }
    
    public double toVanillaGrid(long grid) {
        return grid * pixelLength;
    }
    
    public double toVanillaGrid(int grid) {
        return grid * pixelLength;
    }
    
    public int toBlockOffset(long grid) {
        if (grid > 0)
            return (int) (grid * pixelLength);
        return (int) Math.floor(grid * pixelLength);
    }
    
    public int toBlockOffset(int grid) {
        if (grid > 0)
            return (int) (grid * pixelLength);
        return (int) Math.floor(grid * pixelLength);
    }
    
    public boolean isAtEdge(double pos) {
        double result = pos % pixelLength;
        return LittleUtils.equals(result, 0) || LittleUtils.equals(result, pixelLength);
    }
    
    public int toGrid(int pos) {
        return pos * count;
    }
    
    public long toGridAccurate(double pos) {
        pos = LittleUtils.round(pos * count);
        if (pos < 0)
            return (long) Math.floor(pos);
        return (long) pos;
    }
    
    public int toGrid(double pos) {
        pos = LittleUtils.round(pos * count);
        if (pos < 0)
            return (int) Math.floor(pos);
        return (int) pos;
    }
    
    @Override
    public String toString() {
        return "" + count;
    }
    
}
