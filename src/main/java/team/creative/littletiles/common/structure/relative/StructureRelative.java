package team.creative.littletiles.common.structure.relative;

import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.creativemd.littletiles.common.tile.place.PlacePreviewRelative;

import net.minecraft.core.BlockPos;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.grid.IGridBased;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.structure.directional.StructureDirectionalField;

public class StructureRelative implements IGridBased {
    
    protected LittleGrid grid;
    protected LittleBox box;
    
    public StructureRelative(LittleBox box, LittleGrid grid) {
        this.box = box;
        this.grid = grid;
    }
    
    public StructureRelative(int[] array) {
        this.box = new LittleBox(array[0], array[1], array[2], array[3], array[4], array[5]);
        this.grid = LittleGrid.get(array[6]);
    }
    
    public LittleVec getDoubledCenterVec() {
        return new LittleVec(box.maxX + box.minX, box.maxY + box.minY, box.maxZ + box.minZ);
    }
    
    public Vec3d getCenter() {
        return new Vec3d(grid.toVanillaGrid(box.maxX + box.minX) / 2D, grid.toVanillaGrid(box.maxY + box.minY) / 2D, grid.toVanillaGrid(box.maxZ + box.minZ) / 2D);
    }
    
    public boolean isEven() {
        return box.getSize(Axis.X) > 1;
    }
    
    public LittleBox getBox() {
        return box;
    }
    
    @Override
    public LittleGrid getGrid() {
        return grid;
    }
    
    @Override
    public void convertTo(LittleGrid grid) {
        box.convertTo(this.grid, grid);
        this.grid = grid;
    }
    
    @Override
    public int getSmallest() {
        return box.getSmallest(grid);
    }
    
    public int[] write() {
        return new int[] { box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, grid.count };
    }
    
    public void setBox(BlockPos pos, LittleBox box, LittleGrid grid) {
        this.box = box;
        this.grid = grid;
        add(pos);
    }
    
    public LittleVecGrid getMinVec() {
        return new LittleVecGrid(box.getMinVec(), grid);
    }
    
    public PlacePreview getPlacePreview(LittleGroup previews, StructureDirectionalField type) {
        return new PlacePreviewRelative(box, this, type);
    }
    
    public void move(LittleVecGrid offset) {
        sameGrid(offset, () -> box.add(offset.getVec()));
    }
    
    public void mirror(LittleGrid grid, Axis axis, LittleVec doubledCenter) {
        if (grid.count > this.grid.count)
            convertTo(grid);
        else if (grid.count < this.grid.count) {
            doubledCenter = doubledCenter.copy();
            doubledCenter.convertTo(grid, this.grid);
        }
        
        box.mirror(axis, doubledCenter);
        
        convertToSmallest();
    }
    
    public void rotate(LittleGrid grid, Rotation rotation, LittleVec doubledCenter) {
        if (grid.count > this.grid.count)
            convertTo(grid);
        else if (grid.count < this.grid.count) {
            doubledCenter = doubledCenter.copy();
            doubledCenter.convertTo(grid, this.grid);
        }
        
        box.rotate(rotation, doubledCenter);
        
        convertToSmallest();
    }
    
    public BlockPos getOffset() {
        return box.getMinVec().getBlockPos(grid);
    }
    
    public void add(BlockPos pos) {
        box.add(new LittleVec(grid, pos));
    }
    
    public void add(LittleVecGrid vec) {
        int scale = 1;
        if (vec.getGrid().count > this.grid.count)
            convertTo(vec.getGrid());
        else if (vec.getGrid().count < this.grid.count)
            scale = this.grid.count / vec.getGrid().count;
        
        box.add(vec.getVec().x * scale, vec.getVec().y * scale, vec.getVec().z * scale);
        
        convertToSmallest();
    }
    
    public void sub(BlockPos pos) {
        box.sub(new LittleVec(grid, pos));
    }
    
    public void sub(LittleVecGrid vec) {
        int scale = 1;
        if (vec.getGrid().count > this.grid.count)
            convertTo(vec.getGrid());
        else if (vec.getGrid().count < this.grid.count)
            scale = this.grid.count / vec.getGrid().count;
        
        box.sub(vec.getVec().x * scale, vec.getVec().y * scale, vec.getVec().z * scale);
        
        convertToSmallest();
    }
    
    public void advancedScale(int from, int to) {
        int current = grid.count;
        if (from > to)
            current *= from / to;
        else
            current /= to / from;
        
        grid = LittleGrid.get(current);
    }
    
}
