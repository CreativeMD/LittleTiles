package team.creative.littletiles.common.tile.parent;

import java.util.Iterator;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.util.compression.LittleNBTCompressionTools;

import net.minecraft.nbt.NBTTagCompound;
import team.creative.littletiles.common.block.TETiles;
import team.creative.littletiles.common.filter.TileFilter;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.tile.LittleCollectionSafe;
import team.creative.littletiles.common.tile.LittleTile;

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
    
    public void read(NBTTagCompound nbt) {
        this.clear();
        this.addAll(LittleNBTCompressionTools.readTiles(nbt.getTagList("tiles", 10)));
        readExtra(nbt);
    }
    
    protected abstract void readExtra(NBTTagCompound nbt);
    
    public NBTTagCompound write() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("tiles", LittleNBTCompressionTools.writeTiles(this));
        writeExtra(nbt);
        return nbt;
    }
    
    protected abstract void writeExtra(NBTTagCompound nbt);
    
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
    public abstract TETiles getTe();
    
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
