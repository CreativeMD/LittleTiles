package team.creative.littletiles.common.tile.collection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.tile.LittleTile;

public class LittleCollection implements Iterable<LittleTile> {
    
    private final Iterable<LittleBox> boxesIterable = new Iterable<LittleBox>() {
        
        @Override
        public Iterator<LittleBox> iterator() {
            return iteratorBoxes();
        }
    };
    
    protected List<LittleTile> content = createInternalList();
    
    public LittleCollection() {
        
    }
    
    public void add(LittleTile tile) {
        if (!canAdd())
            return;
        for (LittleTile other : this) {
            if (other.equals(tile)) {
                other.add(tile);
                return;
            }
        }
        content.add(tile);
    }
    
    public void addAll(Iterable<LittleTile> tiles) {
        for (LittleTile tile : tiles)
            add(tile);
    }
    
    protected boolean canAdd() {
        return true;
    }
    
    protected void added(LittleTile tile) {}
    
    protected void refresh() {}
    
    protected void removed(LittleTile tile) {}
    
    public void clear() {
        content.clear();
        refresh();
    }
    
    protected List<LittleTile> createInternalList() {
        return new ArrayList<>();
    }
    
    public Iterable<LittleBox> boxes() {
        return boxesIterable;
    }
    
    protected Iterator<LittleBox> iteratorBoxes() {
        return new Iterator<LittleBox>() {
            
            Iterator<LittleTile> itr = content.iterator();
            Iterator<LittleBox> itrBox = null;
            LittleBox next;
            boolean seek = true;
            
            @Override
            public boolean hasNext() {
                if (seek) {
                    if (itrBox.hasNext()) {
                        next = itrBox.next();
                        seek = false;
                        return true;
                    } else
                        next = null;
                    while (itr.hasNext()) {
                        LittleTile tile = itr.next();
                        itrBox = tile.iterator();
                        if (itrBox.hasNext()) {
                            next = itrBox.next();
                            seek = false;
                            return true;
                        }
                        next = null;
                    }
                    seek = false;
                }
                return next != null;
            }
            
            @Override
            public LittleBox next() {
                seek = true;
                return next;
            }
            
            @Override
            public void remove() {
                itr.remove();
            }
        };
    }
    
    @Override
    public Iterator<LittleTile> iterator() {
        return new Iterator<LittleTile>() {
            
            Iterator<LittleTile> itr = content.iterator();
            
            @Override
            public LittleTile next() {
                return itr.next();
            }
            
            @Override
            public boolean hasNext() {
                return itr.hasNext();
            }
            
            @Override
            public void remove() {
                itr.remove();
                refresh();
            }
        };
    }
    
    public void combineBlockwise() {
        for (LittleTile tile : content)
            tile.combineBlockwise();
    }
    
    public boolean isEmpty() {
        return content.isEmpty();
    }
    
    public int size() {
        return content.size();
    }
    
}