package team.creative.littletiles.common.tile;

import java.util.Iterator;
import java.util.List;

import team.creative.creativecore.common.util.type.Pair;
import team.creative.littletiles.common.filter.TileFilter;
import team.creative.littletiles.common.math.box.LittleBox;

public class LittleCollection implements Iterable<LittleTile> {
    
    private final Iterable<Pair<LittleTile, LittleBox>> boxesIterable = new Iterable<Pair<LittleTile, LittleBox>>() {
        
        @Override
        public Iterator<Pair<LittleTile, LittleBox>> iterator() {
            return iteratorBoxes();
        }
    };
    
    protected List<LittleTile> content;
    
    public Iterable<Pair<LittleTile, LittleBox>> boxes() {
        return boxesIterable;
    }
    
    public Iterator<Pair<LittleTile, LittleBox>> iteratorBoxes() {
        
    }
    
    @Override
    public Iterator<LittleTile> iterator() {
        return new Iterator<LittleTile>() {
            
            @Override
            public boolean hasNext() {
                // TODO Auto-generated method stub
                return false;
            }
            
            @Override
            public LittleTile next() {
                // TODO Auto-generated method stub
                return null;
            }
        };
    }
    
    public Iterable<LittleTile> filter(TileFilter selector) {
        
    }
    
    public boolean isEmpty() {
        return content.isEmpty();
    }
    
    public int tilesCount() {
        return content.size();
    }
    
}
