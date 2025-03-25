package team.creative.littletiles.client.tool.shaper;

import java.util.Iterator;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;

public class ShapeSelection implements Iterable<ShapePosition> {
    
    public static ShapeSelection of(Level level, LittleGrid grid, List<ShapePosition> positions, boolean inside) {
        return new ShapeSelection(level, grid, positions, inside);
    }
    
    public final boolean inside;
    public final LittleGrid grid;
    public final BlockPos pos;
    
    protected final Level level;
    protected final List<ShapePosition> positions;
    protected final LittleBox overallBox;
    
    public ShapeSelection(Level level, LittleGrid grid, List<ShapePosition> positions, boolean inside) {
        this.level = level;
        this.inside = inside;
        this.positions = positions;
        this.pos = positions.getFirst().getPos();
        this.overallBox = LittleBox.ofNothing();
        for (ShapePosition p : positions)
            this.overallBox.growToIncludePixel(p.getRelative(pos));
        int smallest = grid.count;
        for (int i = 0; i < positions.size(); i++)
            smallest = Math.max(smallest, positions.get(i).getSmallest());
        this.grid = LittleGrid.get(smallest);
        for (int i = 0; i < positions.size(); i++)
            positions.get(i).convertTo(this.grid);
    }
    
    public ShapePosition getFirst() {
        return positions.getFirst();
    }
    
    public ShapePosition getLast() {
        return positions.getLast();
    }
    
    public LittleBox getOverallBox() {
        return overallBox.copy();
    }
    
    @Override
    public Iterator<ShapePosition> iterator() {
        return positions.iterator();
    }
    
    public int size() {
        return positions.size();
    }
    
    public ShapePosition get(int index) {
        return positions.get(index);
    }
    
}
