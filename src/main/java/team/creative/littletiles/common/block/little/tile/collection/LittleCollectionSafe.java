package team.creative.littletiles.common.block.little.tile.collection;

import java.util.Collection;

import team.creative.creativecore.common.util.type.list.CopyArrayCollection;
import team.creative.littletiles.common.block.little.tile.LittleTile;

public class LittleCollectionSafe extends LittleCollection {
    
    @Override
    protected Collection<LittleTile> createInternalCollection() {
        return new CopyArrayCollection<>();
    }
    
}
