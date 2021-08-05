package team.creative.littletiles.common.tile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import team.creative.creativecore.common.util.type.Pair;
import team.creative.littletiles.common.math.box.LittleBox;

public class LittleCollection implements Iterable<LittleTile> {
    
    private final Iterable<Pair<LittleTile, LittleBox>> boxesIterable = new Iterable<Pair<LittleTile, LittleBox>>() {
        
        @Override
        public Iterator<Pair<LittleTile, LittleBox>> iterator() {
            return iteratorBoxes();
        }
    };
    
    protected List<LittleTile> content = createInternalList();
    
    public void add(LittleTile tile) {
        
        adasd
    }
    
    public void addAll(Iterable<LittleTile> tile) {
        testoisot
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
    
    public Iterable<Pair<LittleTile, LittleBox>> boxes() {
        return boxesIterable;
    }
    
    protected Iterator<Pair<LittleTile, LittleBox>> iteratorBoxes() {
        return new Iterator<Pair<LittleTile, LittleBox>>() {
            
            Iterator<LittleTile> itr = content.iterator();
            Iterator<LittleBox> itrBox = null;
            Pair<LittleTile, LittleBox> next;
            boolean seek = true;
            
            @Override
            public boolean hasNext() {
                if (seek) {
                    if (itrBox.hasNext()) {
                        next.setValue(itrBox.next());
                        seek = false;
                        return true;
                    } else
                        next = null;
                    while (itr.hasNext()) {
                        LittleTile tile = itr.next();
                        itrBox = tile.boxes.iterator();
                        if (itrBox.hasNext()) {
                            next = new Pair<LittleTile, LittleBox>(tile, itrBox.next());
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
            public Pair<LittleTile, LittleBox> next() {
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
    
    public boolean isEmpty() {
        return content.isEmpty();
    }
    
    public int size() {
        return content.size();
    }
    
}
