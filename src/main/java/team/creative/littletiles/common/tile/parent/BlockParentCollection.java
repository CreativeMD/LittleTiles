package team.creative.littletiles.common.tile.parent;

import java.util.BitSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import team.creative.creativecore.common.util.type.Pair;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureAttribute;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.CorruptedLinkException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.tile.LittleTile;

public class BlockParentCollection extends ParentCollection {
    
    public BETiles be;
    
    private final ConcurrentHashMap<Integer, StructureParentCollection> structures = new ConcurrentHashMap<>();
    private int attributes = LittleStructureAttribute.NONE;
    
    private final boolean client;
    
    public BlockParentCollection(BETiles be, boolean client) {
        super();
        this.be = be;
        this.client = client;
    }
    
    @Override
    protected void readExtra(CompoundTag nbt) {
        structures.clear();
        ListTag list = nbt.getList("children", 10);
        for (int i = 0; i < list.size(); i++) {
            StructureParentCollection child = new StructureParentCollection(this, list.getCompound(i));
            structures.put(child.getIndex(), child);
        }
        reloadAttributes();
    }
    
    @Override
    protected void writeExtra(CompoundTag nbt) {
        ListTag list = new ListTag();
        for (StructureParentCollection child : structures.values())
            list.add(child.write());
        nbt.put("children", list);
    }
    
    public void clearEverything() {
        super.clear();
        structures.clear();
    }
    
    public int countStructures() {
        return structures.size();
    }
    
    public boolean removeStructure(int index) {
        StructureParentCollection list = structures.remove(index);
        if (list != null)
            list.removed();
        boolean removed = list != null;
        reloadAttributes();
        return removed;
    }
    
    public void addStructure(int index, StructureParentCollection list) {
        if (structures.containsKey(index))
            throw new IllegalArgumentException("index '" + index + "' already exists");
        structures.put(index, list);
    }
    
    public StructureParentCollection addStructure(int index, int attribute) {
        if (structures.containsKey(index))
            throw new IllegalArgumentException("index '" + index + "' already exists");
        StructureParentCollection list = new StructureParentCollection(this, index, attribute);
        structures.put(index, list);
        reloadAttributes();
        return list;
    }
    
    public Iterable<LittleStructure> loadedStructures() {
        return new Iterable<LittleStructure>() {
            
            @Override
            public Iterator<LittleStructure> iterator() {
                return new Iterator<LittleStructure>() {
                    
                    Iterator<StructureParentCollection> itr = structures.values().iterator();
                    StructureParentCollection next = null;
                    
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
                            StructureParentCollection temp = next;
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
    
    public Iterable<IStructureParentCollection> structures() {
        return new Iterable<IStructureParentCollection>() {
            
            @Override
            public Iterator<IStructureParentCollection> iterator() {
                return new Iterator<IStructureParentCollection>() {
                    
                    Iterator<StructureParentCollection> iterator = structures.values().iterator();
                    
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }
                    
                    @Override
                    public IStructureParentCollection next() {
                        return iterator.next();
                    }
                };
            }
        };
    }
    
    public Iterable<StructureParentCollection> structuresReal() {
        return structures.values();
    }
    
    public Iterable<LittleStructure> loadedStructures(int attribute) {
        return new Iterable<LittleStructure>() {
            
            @Override
            public Iterator<LittleStructure> iterator() {
                if (LittleStructureAttribute.listener(attribute) || LittleStructureAttribute.active(attribute)) {
                    return new Iterator<LittleStructure>() {
                        
                        public Iterator<StructureParentCollection> iterator = structures.values().iterator();
                        public LittleStructure next;
                        
                        {
                            findNext();
                        }
                        
                        public void findNext() {
                            while (iterator.hasNext()) {
                                StructureParentCollection structure = iterator.next();
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
        for (StructureParentCollection child : structures.values())
            if (child.checkCollision())
                return true;
        return false;
    }
    
    public Iterable<IParentCollection> groups() {
        return new Iterable<IParentCollection>() {
            
            @Override
            public Iterator<IParentCollection> iterator() {
                return new Iterator<IParentCollection>() {
                    
                    IParentCollection current = BlockParentCollection.this;
                    Iterator<IStructureParentCollection> children = structures().iterator();
                    
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
                    public IParentCollection next() {
                        IParentCollection result = current;
                        current = null;
                        return result;
                    }
                };
            }
        };
    }
    
    public Iterable<Pair<IParentCollection, LittleTile>> allTileTypes() {
        Iterator<IParentCollection> iterator = groups().iterator();
        return new Iterable<Pair<IParentCollection, LittleTile>>() {
            
            @Override
            public Iterator<Pair<IParentCollection, LittleTile>> iterator() {
                return new Iterator<Pair<IParentCollection, LittleTile>>() {
                    
                    Iterator<LittleTile> inBlock = null;
                    Pair<IParentCollection, LittleTile> pair = null;
                    
                    @Override
                    public boolean hasNext() {
                        while (inBlock == null || !inBlock.hasNext()) {
                            if (!iterator.hasNext())
                                return false;
                            IParentCollection list = iterator.next();
                            pair = new Pair<>(list, null);
                            inBlock = list.iterator();
                        }
                        return true;
                    }
                    
                    @Override
                    public Pair<IParentCollection, LittleTile> next() {
                        pair.setValue(inBlock.next());
                        return pair;
                    }
                };
            }
        };
    }
    
    @Override
    public int totalSize() {
        int size = size();
        for (StructureParentCollection list : structures.values())
            size += list.totalSize();
        return size;
    }
    
    private void reloadAttributes() {
        attributes = LittleStructureAttribute.NONE;
        for (StructureParentCollection structure : structures.values())
            attributes |= structure.getAttribute();
    }
    
    @Override
    public BETiles getBE() {
        return be;
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
    
    public StructureParentCollection getStructure(int index) {
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
    
    public void add(BlockParentCollection tiles) {
        addAll(tiles);
        tiles.structures.putAll(structures);
        for (StructureParentCollection list : structures.values()) {
            list.setParent(this);
        }
    }
    
    public void fillUsedIds(BitSet usedIds) {
        for (Integer id : structures.keySet())
            if (id >= 0)
                usedIds.set(id);
    }
    
    public void removeEmptyLists() {
        for (Iterator<StructureParentCollection> iterator = structures.values().iterator(); iterator.hasNext();) {
            StructureParentCollection child = iterator.next();
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
        for (StructureParentCollection child : structures.values())
            child.unload();
    }
    
}