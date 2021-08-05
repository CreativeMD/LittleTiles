package team.creative.littletiles.common.grid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.math.IntMath;

import net.minecraft.nbt.CompoundTag;
import team.creative.littletiles.common.math.LittleUtils;
import team.creative.littletiles.common.math.vec.LittleVec;

public class LittleGrid {
    
    public static final int OVERALL_DEFAULT = 16;
    
    private static List<String> names;
    private static int[] grid_sizes;
    private static LittleGrid[] grids;
    private static int overallDefaultIndex;
    private static int defaultIndex;
    private static int base;
    private static int scale;
    private static int exponent;
    
    public static void loadGrid(int base, int scale, int exponent, int defaultGrid) {
        LittleGrid.overallDefaultIndex = -1;
        LittleGrid.defaultIndex = -1;
        
        LittleGrid.base = base;
        LittleGrid.scale = scale;
        LittleGrid.exponent = exponent;
        
        LittleGrid.grids = new LittleGrid[scale];
        LittleGrid.grid_sizes = new int[scale];
        int size = base;
        for (int i = 0; i < LittleGrid.grids.length; i++) {
            LittleGrid.grid_sizes[i] = size;
            LittleGrid.grids[i] = new LittleGrid(size, i);
            if (LittleGrid.grids[i].isDefault)
                LittleGrid.overallDefaultIndex = i;
            if (size == defaultGrid)
                LittleGrid.defaultIndex = i;
            size *= exponent;
        }
        
        names = new ArrayList<>();
        for (int i = 0; i < grids.length; i++)
            names.add(grids[i].count + "");
        names = Collections.unmodifiableList(names);
    }
    
    public static List<String> names() {
        return names;
    }
    
    public static LittleGrid overallDefault() {
        if (overallDefaultIndex != -1)
            return grids[overallDefaultIndex];
        return null;
    }
    
    public static LittleGrid get(int grid) {
        for (int i = 0; i < grids.length; i++)
            if (grids[i].count == grid)
                return grids[i];
        throw new RuntimeException("Invalid gridsize = '" + grid + "'!");
    }
    
    public static LittleGrid defaultGrid() {
        if (defaultIndex != -1)
            return grids[defaultIndex];
        return null;
    }
    
    public static LittleGrid min() {
        return grids[0];
    }
    
    public static LittleGrid getMax() {
        return grids[grids.length - 1];
    }
    
    public static LittleGrid max(LittleGrid context, LittleGrid context2) {
        if (context.count >= context2.count)
            return context;
        return context2;
    }
    
    public static LittleGrid get(CompoundTag nbt) {
        if (nbt != null && nbt.contains("grid"))
            return LittleGrid.get(nbt.getInt("grid"));
        return LittleGrid.overallDefault();
    }
    
    public final int count;
    public final int count2d;
    public final int count3d;
    public final double pixelLength;
    public final double halfPixelLength;
    public final double pixelVolume;
    protected final int index;
    public final boolean isDefault;
    
    /** doubled **/
    public final LittleVec rotationCenter;
    
    public final int[] minSizes;
    
    protected LittleGrid(int gridSize, int index) {
        this.index = index;
        this.count = gridSize;
        this.pixelLength = 1D / gridSize;
        this.halfPixelLength = pixelLength * 0.5;
        this.count2d = gridSize * gridSize;
        this.count3d = gridSize * gridSize * gridSize;
        this.pixelVolume = Math.max(Double.MIN_VALUE, 1D / this.count3d);
        this.isDefault = LittleGrid.OVERALL_DEFAULT == gridSize;
        
        this.minSizes = new int[this.count];
        this.minSizes[0] = LittleGrid.base;
        for (int i = 1; i < this.minSizes.length; i++) {
            this.minSizes[i] = this.count / IntMath.gcd(i, this.count);
            if (this.minSizes[i] < LittleGrid.base || this.minSizes[i] % LittleGrid.base != 0)
                this.minSizes[i] = this.count;
        }
        
        this.rotationCenter = new LittleVec(this.count, this.count, this.count);
    }
    
    public void set(CompoundTag nbt) {
        if (!isDefault)
            nbt.putInt("grid", count);
        else
            nbt.remove("grid");
    }
    
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
