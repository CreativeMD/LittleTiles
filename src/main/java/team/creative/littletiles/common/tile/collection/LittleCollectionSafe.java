package team.creative.littletiles.common.tile.collection;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import team.creative.littletiles.common.tile.LittleTile;

public class LittleCollectionSafe extends LittleCollection {
    
    @Override
    protected List<LittleTile> createInternalList() {
        return new CopyOnWriteArrayList<>();
    }
    
}
