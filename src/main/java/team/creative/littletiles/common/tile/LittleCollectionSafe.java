package team.creative.littletiles.common.tile;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LittleCollectionSafe extends LittleCollection {
    
    @Override
    protected List<LittleTile> createInternalList() {
        return new CopyOnWriteArrayList<>();
    }
    
}
