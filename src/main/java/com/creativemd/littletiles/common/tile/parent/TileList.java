package com.creativemd.littletiles.common.tile.parent;

import java.util.BitSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.CorruptedLinkException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class TileList extends ParentTileList {
    
    public TileEntityLittleTiles te;
    
    private final ConcurrentHashMap<Integer, StructureTileList> structures = new ConcurrentHashMap<>();
    private int attributes = LittleStructureAttribute.NONE;
    
    private final boolean client;
    
    public TileList(TileEntityLittleTiles te, boolean client) {
        super();
        this.te = te;
        this.client = client;
    }

    private void clearStructures() {
        for (StructureTileList structure : structures.values())
            structure.unload();
        structures.clear();
    }
    @Override
    protected void readExtra(NBTTagCompound nbt) {
        clearStructures();
        NBTTagList list = nbt.getTagList("children", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            StructureTileList child = new StructureTileList(this, list.getCompoundTagAt(i));
            structures.put(child.getIndex(), child);
        }
        reloadAttributes();
    }
    
    @Override
    protected void writeExtra(NBTTagCompound nbt) {
        NBTTagList list = new NBTTagList();
        for (StructureTileList child : structures.values())
            list.appendTag(child.write());
        nbt.setTag("children", list);
    }
    
    public void clearEverything() {
        super.clear();
        clearStructures();
    }
    
    public int countStructures() {
        return structures.size();
    }
    
    public boolean removeStructure(int index) {
        StructureTileList list = structures.remove(index);
        if (list != null)
            list.removed();
        boolean removed = list != null;
        reloadAttributes();
        return removed;
    }
    
    public void addStructure(int index, StructureTileList list) {
        if (structures.containsKey(index))
            throw new IllegalArgumentException("index '" + index + "' already exists");
        structures.put(index, list);
    }
    
    public StructureTileList addStructure(int index, int attribute) {
        if (structures.containsKey(index))
            throw new IllegalArgumentException("index '" + index + "' already exists");
        StructureTileList list = new StructureTileList(this, index, attribute);
        structures.put(index, list);
        reloadAttributes();
        return list;
    }
    
    public Iterable<LittleStructure> loadedStructures() {
        return new Iterable<LittleStructure>() {
            
            @Override
            public Iterator<LittleStructure> iterator() {
                return new Iterator<LittleStructure>() {
                    
                    Iterator<StructureTileList> itr = structures.values().iterator();
                    StructureTileList next = null;
                    
                    @Override
                    public boolean hasNext() {
                        while (next == null) {
                            if (itr.hasNext())
                                next = itr.next();
                            else
                                return false;
                            
                            try {
                                next.checkConnection();
                            } catch (CorruptedConnectionException | NotYetConnectedException e) {
                                next = null;
                            }
                        }
                        return next != null;
                    }
                    
                    @Override
                    public LittleStructure next() {
                        try {
                            StructureTileList temp = next;
                            next = null;
                            return temp.getStructure();
                        } catch (CorruptedConnectionException | NotYetConnectedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
            }
        };
    }
    
    public Iterable<IStructureTileList> structures() {
        return new Iterable<IStructureTileList>() {
            
            @Override
            public Iterator<IStructureTileList> iterator() {
                return new Iterator<IStructureTileList>() {
                    
                    Iterator<StructureTileList> iterator = structures.values().iterator();
                    
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }
                    
                    @Override
                    public IStructureTileList next() {
                        return iterator.next();
                    }
                };
            }
        };
    }
    
    public Iterable<StructureTileList> structuresReal() {
        return structures.values();
    }
    
    public Iterable<LittleStructure> loadedStructures(int attribute) {
        return new Iterable<LittleStructure>() {
            
            @Override
            public Iterator<LittleStructure> iterator() {
                if (LittleStructureAttribute.listener(attribute) || LittleStructureAttribute.active(attribute)) {
                    return new Iterator<LittleStructure>() {
                        
                        public Iterator<StructureTileList> iterator = structures.values().iterator();
                        public LittleStructure next;
                        
                        {
                            findNext();
                        }
                        
                        public void findNext() {
                            while (iterator.hasNext()) {
                                StructureTileList structure = iterator.next();
                                if ((structure.getAttribute() & attribute) != 0) {
                                    try {
                                        next = structure.getStructure();
                                    } catch (CorruptedConnectionException | NotYetConnectedException e) {
                                    
                                    }
                                    return;
                                }
                            }
                            
                            next = null;
                        }
                        
                        @Override
                        public boolean hasNext() {
                            return next != null;
                        }
                        
                        @Override
                        public LittleStructure next() {
                            LittleStructure toReturn = next;
                            findNext();
                            return toReturn;
                        }
                    };
                }
                return new Iterator<LittleStructure>() {
                    
                    @Override
                    public boolean hasNext() {
                        return false;
                    }
                    
                    @Override
                    public LittleStructure next() {
                        return null;
                    }
                };
            }
        };
    }
    
    public boolean hasTicking() {
        return LittleStructureAttribute.ticking(attributes);
    }
    
    public boolean hasRendered() {
        return LittleStructureAttribute.tickRendering(attributes);
    }
    
    public boolean hasCollisionListener() {
        if (checkCollision() && LittleStructureAttribute.collisionListener(attributes))
            return true;
        for (StructureTileList child : structures.values())
            if (child.checkCollision())
                return true;
        return false;
    }
    
    public Iterable<IParentTileList> groups() {
        return new Iterable<IParentTileList>() {
            
            @Override
            public Iterator<IParentTileList> iterator() {
                return new Iterator<IParentTileList>() {
                    
                    IParentTileList current = TileList.this;
                    Iterator<IStructureTileList> children = structures().iterator();
                    
                    @Override
                    public boolean hasNext() {
                        if (current != null)
                            return true;
                        if (!children.hasNext())
                            return false;
                        current = children.next();
                        return true;
                    }
                    
                    @Override
                    public IParentTileList next() {
                        IParentTileList result = current;
                        current = null;
                        return result;
                    }
                };
            }
        };
    }
    
    public Iterable<Pair<IParentTileList, LittleTile>> allTiles() {
        Iterator<IParentTileList> iterator = groups().iterator();
        return new Iterable<Pair<IParentTileList, LittleTile>>() {
            
            @Override
            public Iterator<Pair<IParentTileList, LittleTile>> iterator() {
                return new Iterator<Pair<IParentTileList, LittleTile>>() {
                    
                    Iterator<LittleTile> inBlock = null;
                    Pair<IParentTileList, LittleTile> pair = null;
                    
                    @Override
                    public boolean hasNext() {
                        while (inBlock == null || !inBlock.hasNext()) {
                            if (!iterator.hasNext())
                                return false;
                            IParentTileList list = iterator.next();
                            pair = new Pair<>(list, null);
                            inBlock = list.iterator();
                        }
                        return true;
                    }
                    
                    @Override
                    public Pair<IParentTileList, LittleTile> next() {
                        pair.setValue(inBlock.next());
                        return pair;
                    }
                };
            }
        };
    }
    
    @Override
    public LittleTile first() {
        return isEmpty() ? null : super.get(0);
    }
    
    @Override
    public int totalSize() {
        int size = size();
        for (StructureTileList list : structures.values())
            size += list.totalSize();
        return size;
    }
    
    @Override
    public LittleTile last() {
        return isEmpty() ? null : super.get(size() - 1);
    }
    
    private void reloadAttributes() {
        attributes = LittleStructureAttribute.NONE;
        for (StructureTileList structure : structures.values())
            attributes |= structure.getAttribute();
    }
    
    @Override
    public TileEntityLittleTiles getTe() {
        return te;
    }
    
    @Override
    public boolean isStructure() {
        return false;
    }
    
    @Override
    public boolean isMain() {
        return false;
    }
    
    @Override
    public boolean isStructureChild(LittleStructure structure) throws CorruptedConnectionException, NotYetConnectedException {
        return false;
    }
    
    @Override
    public LittleStructure getStructure() throws CorruptedLinkException {
        throw new CorruptedLinkException();
    }
    
    public StructureTileList getStructure(int index) {
        return structures.get(index);
    }
    
    @Override
    public int getAttribute() {
        return 0;
    }
    
    @Override
    public void setAttribute(int attribute) {}
    
    @Override
    public boolean isClient() {
        return client;
    }
    
    public void add(TileList tiles) {
        addAll(tiles);
        tiles.structures.putAll(structures);
        for (StructureTileList list : structures.values()) {
            list.setParent(this);
        }
    }
    
    public void fillUsedIds(BitSet usedIds) {
        for (Integer id : structures.keySet())
            if (id >= 0)
                usedIds.set(id);
    }
    
    public void removeEmptyLists() {
        for (Iterator<StructureTileList> iterator = structures.values().iterator(); iterator.hasNext();) {
            StructureTileList child = iterator.next();
            if (child.isEmpty()) {
                child.isRemoved();
                iterator.remove();
            }
        }
    }
    
    public boolean isCompletelyEmpty() {
        return super.isEmpty() && structures.isEmpty();
    }
    
    @Override
    public void unload() {
        super.unload();
        for (StructureTileList child : structures.values())
            child.unload();
    }
    
}