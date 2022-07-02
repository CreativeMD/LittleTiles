package team.creative.littletiles.client.render.block;

import org.jetbrains.annotations.Nullable;

import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import team.creative.littletiles.common.block.entity.BETiles;

public class BEModelData implements IModelData {
    
    public BETiles tiles;
    
    public BEModelData(BETiles tiles) {
        this.tiles = tiles;
    }
    
    @Override
    public boolean hasProperty(ModelProperty<?> prop) {
        return false;
    }
    
    @Override
    public <T> @Nullable T getData(ModelProperty<T> prop) {
        return null;
    }
    
    @Override
    public <T> @Nullable T setData(ModelProperty<T> prop, T data) {
        return null;
    }
    
}
