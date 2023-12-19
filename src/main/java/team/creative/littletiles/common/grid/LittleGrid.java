package team.creative.littletiles.common.grid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.math.IntMath;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.common.math.LittleUtils;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;

public class LittleGrid {
    
    public static final int BASE = 2;
    public static final int OVERALL_DEFAULT = 16;
    public static final int OVERALL_DEFAULT_COUNT2D = 256;
    public static final int OVERALL_DEFAULT_COUNT3D = 4096;
    public static final double OVERALL_DEFAULT_PIXEL_LENGTH = 1 / (double) OVERALL_DEFAULT;
    
    public static final String GRID_KEY = "grid";
    public static final LittleGrid MIN = new LittleGrid(1, 0);
    private static LittleGrid MAX = new LittleGrid(1, 0);
    private static int MAX_INDEX = 0;
    private static LittleGrid GRID_DEFAULT;
    private static final List<LittleGrid> GRIDS = new ArrayList<>(Arrays.asList(MIN));
    private static final List<String> NAMES = new ArrayList<>(Arrays.asList("1"));
    private static final Int2ObjectMap<LittleGrid> GRID_MAP = new Int2ObjectArrayMap<>(new int[] { 1 }, new LittleGrid[] { MIN });
    private static List<String> UNMODFIAABLE_NAMES = Collections.unmodifiableList(NAMES);
    private static int HIGHEST = 1;
    private static TextMapBuilder<LittleGrid> STANDARD_TEXTMAP;
    
    private static void ensure(int highest) {
        int exponent = 2;
        int size = GRIDS.get(GRIDS.size() - 1).count * exponent;
        while (size <= highest) {
            LittleGrid grid = new LittleGrid(size, GRIDS.size());
            GRIDS.add(grid);
            GRID_MAP.put(size, grid);
            NAMES.add(size + "");
            if (size == OVERALL_DEFAULT)
                GRID_DEFAULT = grid;
            size *= exponent;
        }
    }
    
    public static void configure(int highest) {
        if (highest < 1)
            highest = 1;
        ensure(highest);
        
        HIGHEST = highest;
        MAX_INDEX = 0;
        while (MAX_INDEX + 1 < GRIDS.size() && GRIDS.get(MAX_INDEX + 1).count <= HIGHEST)
            MAX_INDEX++;
        MAX = GRIDS.get(MAX_INDEX);
        UNMODFIAABLE_NAMES = Collections.unmodifiableList(NAMES);
        
        STANDARD_TEXTMAP = new TextMapBuilder<LittleGrid>().addComponent(GRIDS, x -> Component.literal("" + x.count));
    }
    
    public static Iterable<LittleGrid> grids() {
        return GRIDS;
    }
    
    public static int gridCount() {
        return MAX_INDEX + 1;
    }
    
    public static LittleGrid gridByIndex(int index) {
        return GRIDS.get(index);
    }
    
    public static List<String> names() {
        return UNMODFIAABLE_NAMES;
    }
    
    public static TextMapBuilder<LittleGrid> mapBuilder() {
        return STANDARD_TEXTMAP;
    }
    
    public static LittleGrid overallDefault() {
        if (GRID_DEFAULT == null)
            return MAX;
        return GRID_DEFAULT;
    }
    
    public static LittleGrid get(int grid) {
        return GRID_MAP.get(grid);
    }
    
    public static LittleGrid getMax() {
        return MAX;
    }
    
    public static LittleGrid max(LittleGrid context, LittleGrid context2) {
        if (context.count >= context2.count)
            return context;
        return context2;
    }
    
    public static LittleGrid getOrThrow(CompoundTag nbt) {
        if (nbt != null && nbt.contains(GRID_KEY)) {
            LittleGrid grid = LittleGrid.get(nbt.getInt(GRID_KEY));
            if (grid != null)
                return grid;
            throw new RuntimeException("Grid " + nbt.getInt(GRID_KEY) + " is not available.");
        }
        return LittleGrid.overallDefault();
    }
    
    public static LittleGrid get(CompoundTag nbt) {
        if (nbt != null && nbt.contains(GRID_KEY)) {
            LittleGrid grid = LittleGrid.get(nbt.getInt(GRID_KEY));
            if (grid != null)
                return grid;
        }
        return LittleGrid.overallDefault();
    }
    
    public final int count;
    public final long count2d;
    public final long count3d;
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
        this.count2d = (long) gridSize * gridSize;
        this.count3d = gridSize * count2d;
        this.pixelVolume = Math.max(Double.MIN_VALUE, 1D / count3d);
        this.isDefault = LittleGrid.OVERALL_DEFAULT == gridSize;
        
        this.minSizes = new int[this.count];
        this.minSizes[0] = BASE;
        for (int i = 1; i < this.minSizes.length; i++) {
            this.minSizes[i] = this.count / IntMath.gcd(i, this.count);
            if (this.minSizes[i] < BASE || this.minSizes[i] % BASE != 0)
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
    
    public int getIndex() {
        return index;
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
    
    public int findNextValue(int value, int grid, boolean positive) {
        int index = value;
        if (value < 0 || value > count)
            index = value % count;
        if (index < 0)
            index += count;
        
        while (index < count && minSizes[index] > grid) {
            if (positive) {
                index++;
                value++;
            } else {
                index--;
                value--;
            }
        }
        return value;
    }
    
    @Override
    public String toString() {
        return "" + count;
    }
    
    public LittleBox box() {
        return new LittleBox(0, 0, 0, count, count, count);
    }
    
    public LittleGrid next() {
        if (index >= GRIDS.size())
            return null;
        return GRIDS.get(index + 1);
    }
    
}
