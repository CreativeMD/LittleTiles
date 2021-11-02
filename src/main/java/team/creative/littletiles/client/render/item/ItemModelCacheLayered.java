package team.creative.littletiles.client.render.item;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import team.creative.creativecore.common.util.math.base.Facing;

public class ItemModelCacheLayered extends ItemModelCache {
    
    private List<BakedQuad> east;
    private List<BakedQuad> west;
    private List<BakedQuad> up;
    private List<BakedQuad> down;
    private List<BakedQuad> south;
    private List<BakedQuad> north;
    
    public ItemModelCacheLayered() {
        super();
    }
    
    @Override
    public void setQuads(RenderType type, Facing facing, List<BakedQuad> baked) {
        if (type == Sheets.translucentCullBlockSheet())
            switch (facing) {
            case DOWN:
                this.down = baked;
                break;
            case EAST:
                this.east = baked;
                break;
            case NORTH:
                this.north = baked;
                break;
            case SOUTH:
                this.south = baked;
                break;
            case UP:
                this.up = baked;
                break;
            case WEST:
                this.west = baked;
                break;
            }
        
        super.setQuads(type, facing, baked);
    }
    
    @Override
    public List<BakedQuad> getQuads(RenderType type, Facing facing) {
        if (type != Sheets.translucentCullBlockSheet())
            return super.getQuads(type, facing);
        switch (facing) {
        case DOWN:
            return down;
        case EAST:
            return east;
        case NORTH:
            return north;
        case SOUTH:
            return south;
        case UP:
            return up;
        case WEST:
            return west;
        default:
            return Collections.EMPTY_LIST;
        }
    }
    
}
