package team.creative.littletiles.common.level.little;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.type.list.SingletonList;

public class BlockUpdateLevelSystem {
    
    public final LittleLevel level;
    
    private final List<LevelBoundsListener> levelBoundListeners = new ArrayList<>();
    
    private int boundMinX = Integer.MAX_VALUE;
    private int boundMinY = Integer.MAX_VALUE;
    private int boundMinZ = Integer.MAX_VALUE;
    private int boundMaxX = Integer.MIN_VALUE;
    private int boundMaxY = Integer.MIN_VALUE;
    private int boundMaxZ = Integer.MIN_VALUE;
    
    private HashSet<BlockPos> edgePositions = new HashSet<>();
    private HashSet<BlockPos> allBlocks = new HashSet<>();
    
    public BlockUpdateLevelSystem(LittleLevel level) {
        this.level = level;
    }
    
    public void registerLevelBoundListener(LevelBoundsListener listener) {
        this.levelBoundListeners.add(listener);
    }
    
    public Iterable<LevelBoundsListener> levelBoundListeners() {
        return levelBoundListeners;
    }
    
    private int getBound(Facing facing) {
        switch (facing) {
            case EAST:
                return boundMaxX;
            case WEST:
                return boundMinX;
            case UP:
                return boundMaxY;
            case DOWN:
                return boundMinY;
            case SOUTH:
                return boundMaxZ;
            case NORTH:
                return boundMinZ;
            default:
                throw new UnsupportedOperationException();
        }
    }
    
    private void setBound(Facing facing, int value) {
        switch (facing) {
            case EAST:
                this.boundMaxX = value;
                break;
            case WEST:
                this.boundMinX = value;
                break;
            case UP:
                this.boundMaxY = value;
                break;
            case DOWN:
                this.boundMinY = value;
                break;
            case SOUTH:
                this.boundMaxZ = value;
                break;
            case NORTH:
                this.boundMinZ = value;
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }
    
    protected boolean isWithinBoundsNoEdge(BlockPos pos) {
        return boundMinX < pos.getX() && boundMaxX > pos.getX() && boundMinY < pos.getY() && boundMaxY > pos.getY() && boundMinZ < pos.getZ() && boundMaxZ > pos.getZ();
    }
    
    protected boolean isWithinBounds(BlockPos pos) {
        return boundMinX <= pos.getX() && boundMaxX >= pos.getX() && boundMinY <= pos.getY() && boundMaxY >= pos.getY() && boundMinZ <= pos.getZ() && boundMaxZ >= pos.getZ();
    }
    
    private boolean isEdgeExcept(BlockPos pos, Facing facing) {
        for (int i = 0; i < Facing.values().length; i++) {
            Facing other = Facing.values()[i];
            if (other != facing && pos.get(other.axis.toVanilla()) == getBound(other))
                return true;
        }
        return false;
    }
    
    public void init(Iterable<BlockPos> positions) {
        boundMinX = Integer.MAX_VALUE;
        boundMinY = Integer.MAX_VALUE;
        boundMinZ = Integer.MAX_VALUE;
        boundMaxX = Integer.MIN_VALUE;
        boundMaxY = Integer.MIN_VALUE;
        boundMaxZ = Integer.MIN_VALUE;
        
        for (BlockPos pos : positions) {
            boundMinX = Math.min(boundMinX, pos.getX());
            boundMinY = Math.min(boundMinY, pos.getY());
            boundMinZ = Math.min(boundMinZ, pos.getZ());
            boundMaxX = Math.max(boundMaxX, pos.getX());
            boundMaxY = Math.max(boundMaxY, pos.getY());
            boundMaxZ = Math.max(boundMaxZ, pos.getZ());
            allBlocks.add(pos);
        }
        
        for (BlockPos pos : allBlocks)
            if (pos.getX() == boundMinX || pos.getX() == boundMaxX || pos.getY() == boundMinY || pos.getY() == boundMaxY || pos.getZ() == boundMinZ || pos.getZ() == boundMaxZ)
                edgePositions.add(pos);
        levelBoundListeners.forEach(x -> x.rescan(level, this, edgePositions));
    }
    
    public void blockChanged(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) { // Shrinking
            if (allBlocks.remove(pos) && edgePositions.remove(pos)) {
                for (int i = 0; i < Facing.values().length; i++) {
                    Facing facing = Facing.values()[i];
                    Axis axis = facing.axis.toVanilla();
                    int bound = getBound(facing);
                    if (bound == pos.get(axis)) {
                        List<BlockPos> remaining = new ArrayList<>();
                        for (BlockPos edge : edgePositions)
                            if (edge.get(axis) == bound)
                                remaining.add(edge);
                            
                        if (remaining.isEmpty()) {
                            int newBound = facing.positive ? Integer.MIN_VALUE : Integer.MAX_VALUE;
                            for (BlockPos scan : allBlocks)
                                newBound = facing.positive ? Math.max(newBound, scan.get(axis)) : Math.min(newBound, scan.get(axis));
                            for (BlockPos scan : allBlocks)
                                if (scan.get(axis) == newBound) {
                                    remaining.add(scan);
                                    edgePositions.add(scan);
                                }
                            setBound(facing, newBound);
                        }
                        
                        levelBoundListeners.forEach(x -> x.rescan(level, this, facing, remaining, facing.positive ? bound + 1 : bound));
                    }
                }
            }
        } else if (allBlocks.add(pos) && !isWithinBoundsNoEdge(pos)) { // Expanding
            for (int i = 0; i < Facing.values().length; i++) {
                Facing facing = Facing.values()[i];
                Axis axis = facing.axis.toVanilla();
                int bound = getBound(facing);
                if (bound == pos.get(axis)) {
                    List<BlockPos> remaining = new ArrayList<>();
                    edgePositions.add(pos);
                    for (BlockPos edge : edgePositions)
                        if (edge.get(axis) == bound)
                            remaining.add(edge);
                        
                    levelBoundListeners.forEach(x -> x.rescan(level, this, facing, remaining, facing.positive ? bound + 1 : bound));
                } else if (bound > pos.get(axis)) {
                    for (Iterator<BlockPos> itr = edgePositions.iterator(); itr.hasNext();) {
                        BlockPos edge = itr.next();
                        if (edge.get(axis) == bound && !isEdgeExcept(edge, facing))
                            itr.remove();
                    }
                    
                    edgePositions.add(pos);
                    levelBoundListeners.forEach(x -> x.rescan(level, this, facing, new SingletonList<>(pos), facing.positive ? bound + 1 : bound));
                }
            }
        }
        
    }
}
