package team.creative.littletiles.common.tile.parent;

import java.util.Iterator;

import com.creativemd.littletiles.common.util.compression.LittleNBTCompressionTools;

import net.minecraft.nbt.CompoundTag;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.filter.TileFilter;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.collection.LittleCollectionSafe;

public abstract class ParentCollection extends LittleCollectionSafe implements IParentCollection {
    
    private int collisionChecks = 0;
    
    protected boolean checkCollision() {
        return collisionChecks > 0;
    }
    
    @Override
    protected void added(LittleTile tile) {
        if (tile.shouldCheckForCollision())
            collisionChecks++;
    }
    
    @Override
    protected void refresh() {
        collisionChecks = 0;
        for (LittleTile tile : this)
            added(tile);
    }
    
    @Override
    protected void removed(LittleTile tile) {
        if (tile.shouldCheckForCollision())
            collisionChecks--;
    }
    
    public void read(CompoundTag nbt) {
        this.clear();
        this.addAll(LittleNBTCompressionTools.readTiles(nbt.getList("tiles", 10)));
        readExtra(nbt);
    }
    
    protected abstract void readExtra(CompoundTag nbt);
    
    public CompoundTag write() {
        CompoundTag nbt = new CompoundTag();
        nbt.setTag("tiles", LittleNBTCompressionTools.writeTiles(this));
        writeExtra(nbt);
        return nbt;
    }
    
    protected abstract void writeExtra(CompoundTag nbt);
    
    public Iterable<LittleTile> filter(TileFilter selector) {
        return new Iterable<LittleTile>() {
            
            @Override
            public Iterator<LittleTile> iterator() {
                return new Iterator<LittleTile>() {
                    
                    Iterator<LittleTile> itr = content.iterator();
                    LittleTile next = null;
                    
                    @Override
                    public boolean hasNext() {
                        while (next == null && itr.hasNext()) {
                            LittleTile test = itr.next();
                            if (selector.is(ParentCollection.this, test))
                                next = test;
                        }
                        return next != null;
                    }
                    
                    @Override
                    public LittleTile next() {
                        LittleTile result = next;
                        next = null;
                        return result;
                    }
                    
                };
            }
        };
    }
    
    @Override
    public abstract BETiles getBE();
    
    @Override
    public abstract boolean isStructure();
    
    @Override
    public boolean isStructureChildSafe(LittleStructure structure) {
        try {
            return isStructureChild(structure);
        } catch (CorruptedConnectionException | NotYetConnectedException e) {
            return false;
        }
    }
    
    @Override
    public abstract boolean isStructureChild(LittleStructure structure) throws CorruptedConnectionException, NotYetConnectedException;
    
    @Override
    public abstract boolean isMain();
    
    @Override
    public abstract LittleStructure getStructure() throws CorruptedConnectionException, NotYetConnectedException;
    
    @Override
    public abstract int getAttribute();
    
    @Override
    public abstract boolean isClient();
    
    public void unload() {}
}