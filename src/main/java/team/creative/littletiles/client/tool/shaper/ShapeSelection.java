package team.creative.littletiles.client.tool.shaper;

import java.util.Iterator;
import java.util.List;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;

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
    
    @OnlyIn(Dist.CLIENT)
    public LittleBox getOverallBox() {
        if (positions.size() > 1 && Screen.hasAltDown()) {
            LittleVec size = overallBox.getSize();
            int smallestSize = Math.min(size.x, Math.min(size.y, size.z));
            var first = positions.getFirst();
            var second = positions.get(1);
            var box = overallBox.copy();
            for (int i = 0; i < Axis.values().length; i++) {
                Axis axis = Axis.values()[i];
                if (first.getVanillaGrid(axis) < second.getVanillaGrid(axis))
                    box.setMax(axis, box.getMin(axis) + smallestSize);
                else
                    box.setMin(axis, box.getMax(axis) - smallestSize);
            }
            return box;
        }
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
