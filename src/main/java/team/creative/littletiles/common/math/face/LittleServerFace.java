package team.creative.littletiles.common.math.face;

import java.util.HashMap;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.geo.VectorFan;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.structure.attribute.LittleStructureAttribute;

public non-sealed class LittleServerFace implements ILittleFace {
    
    private final BETiles be;
    private final HashMap<Facing, Boolean> neighbours = new HashMap<>();
    private final HashMap<Facing, BETiles> neighboursTiles = new HashMap<>();
    private boolean validFace = false;
    private boolean partiallyFilled = false;
    private LittleTile tile;
    public LittleGrid grid;
    public LittleBox box = new LittleBox(0, 0, 0, 0, 0, 0);
    public Axis one;
    public Axis two;
    public Facing facing;
    public int minOne;
    public int minTwo;
    public int maxOne;
    public int maxTwo;
    public int origin;
    public int oldOrigin;
    
    public boolean[][] filled;
    
    public LittleServerFace(BETiles be) {
        this.be = be;
    }
    
    public LittleServerFace set(IParentCollection parent, LittleTile tile, LittleBox box, Facing facing) {
        this.box = box;
        this.facing = facing;
        this.one = facing.one();
        this.two = facing.two();
        this.grid = parent.getGrid();
        this.tile = tile;
        validFace = this.box.set(this, grid, facing);
        partiallyFilled = false;
        return this;
    }
    
    public void set(int minOne, int minTwo, int maxOne, int maxTwo, int origin) {
        this.minOne = minOne;
        this.minTwo = minTwo;
        this.maxOne = maxOne;
        this.maxTwo = maxTwo;
        this.origin = origin;
        this.oldOrigin = origin;
        this.filled = new boolean[maxOne - minOne][maxTwo - minTwo];
    }
    
    @Override
    public LittleGrid getGrid() {
        return grid;
    }
    
    @Override
    public LittleBox box() {
        return box;
    }
    
    @Override
    public void ensureGrid(LittleGrid context) {
        if (context == this.grid || this.grid.count > grid.count)
            return;
        
        int ratio = context.count / this.grid.count;
        this.minOne *= ratio;
        this.minTwo *= ratio;
        this.maxOne *= ratio;
        this.maxTwo *= ratio;
        this.origin *= ratio;
        this.oldOrigin *= ratio;
        box = box.copy(); // Make sure the original one will not be modified
        box.convertTo(this.grid, context);
        this.grid = context;
        filled = new boolean[maxOne - minOne][maxTwo - minTwo];
    }
    
    public boolean isPartiallyFilled() {
        if (partiallyFilled)
            return true;
        for (int one = 0; one < filled.length; one++)
            for (int two = 0; two < filled[one].length; two++)
                if (filled[one][two])
                    return true;
        return false;
    }
    
    public boolean isFilled() {
        for (int one = 0; one < filled.length; one++)
            for (int two = 0; two < filled[one].length; two++)
                if (!filled[one][two])
                    return false;
        return true;
    }
    
    public LittleBox getBox() {
        return box;
    }
    
    public boolean isFaceInsideBlock() {
        return origin > 0 && origin < grid.count;
    }
    
    public void move(Facing facing) {
        origin = facing.positive ? 0 : grid.count;
    }
    
    @Override
    public boolean supportsCutting() {
        return false;
    }
    
    @Override
    public void setPartiallyFilled() {
        partiallyFilled = true;
    }
    
    @Override
    public void cut(List<VectorFan> fans) {}
    
    @Override
    public Facing facing() {
        return facing;
    }
    
    @Override
    public Axis one() {
        return one;
    }
    
    @Override
    public Axis two() {
        return two;
    }
    
    @Override
    public int origin() {
        return origin;
    }
    
    @Override
    public int minOne() {
        return minOne;
    }
    
    @Override
    public int minTwo() {
        return minTwo;
    }
    
    @Override
    public int maxOne() {
        return maxOne;
    }
    
    @Override
    public int maxTwo() {
        return maxTwo;
    }
    
    @Override
    public void set(int one, int two, boolean value) {
        filled[one][two] = value;
    }
    
    public static LittleFaceState calculate(BETiles be, Facing facing, LittleServerFace face, LittleTile rendered, boolean outside) {
        face.ensureGrid(be.getGrid());
        
        for (Pair<IParentCollection, LittleTile> pair : be.allTiles()) {
            if (pair.key.isStructure() && LittleStructureAttribute.noCollision(pair.key.getAttribute()))
                continue;
            if (pair.value.doesProvideSolidFace() || pair.value.canBeRenderCombined(rendered))
                pair.value.fillFace(pair.key, face, be.getGrid());
        }
        
        if (outside)
            if (face.isFilled())
                return LittleFaceState.OUTISDE_COVERED;
            else if (face.isPartiallyFilled())
                return LittleFaceState.OUTSIDE_PARTIALLY_COVERED;
            else
                return LittleFaceState.OUTSIDE_UNCOVERED;
            
        if (face.isFilled())
            return LittleFaceState.INSIDE_COVERED;
        else if (face.isPartiallyFilled())
            return LittleFaceState.INSIDE_PARTIALLY_COVERED;
        return LittleFaceState.INSIDE_UNCOVERED;
    }
    
    public LittleFaceState calculate() {
        if (!validFace)
            return LittleFaceState.UNLOADED;
        
        if (isFaceInsideBlock())
            return calculate(be, facing, this, tile, false);
        
        if (!tile.cullOverEdge())
            return LittleFaceState.OUTSIDE_UNCOVERED;
        
        Boolean neighbourBlock = neighbours.get(facing);
        if (neighbourBlock == null) {
            neighbourBlock = checkforNeighbour(be.getLevel(), facing.toVanilla(), be.getBlockPos(), tile.getState(), tile.color);
            neighbours.put(facing, neighbourBlock);
        }
        if (neighbourBlock)
            return LittleFaceState.OUTISDE_COVERED;
        
        BETiles otherTile = null;
        if (!neighboursTiles.containsKey(facing)) {
            otherTile = checkforBE(be.getLevel(), facing.toVanilla(), be.getBlockPos());
            neighboursTiles.put(facing, otherTile);
        } else
            otherTile = neighboursTiles.get(facing);
        
        if (otherTile != null) {
            move(facing);
            return calculate(otherTile, facing.opposite(), this, tile, true);
        }
        return LittleFaceState.OUTSIDE_UNCOVERED;
    }
    
    private static BETiles checkforBE(Level level, Direction facing, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos.relative(facing));
        if (be instanceof BETiles)
            return (BETiles) be;
        return null;
    }
    
    private static boolean checkforNeighbour(Level level, Direction facing, BlockPos pos, BlockState state, int color) {
        return !Block.shouldRenderFace(state, level, pos, facing, pos.relative(facing)) || (ColorUtils.WHITE == color && state == level.getBlockState(pos.relative(facing)));
    }
}
