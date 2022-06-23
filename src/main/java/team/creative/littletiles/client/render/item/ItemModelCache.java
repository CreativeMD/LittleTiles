package team.creative.littletiles.client.render.item;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.littletiles.LittleTiles;

public class ItemModelCache {
    
    private boolean building = true;
    private List<BakedQuad> east;
    private List<BakedQuad> west;
    private List<BakedQuad> up;
    private List<BakedQuad> down;
    private List<BakedQuad> south;
    private List<BakedQuad> north;
    private long lastUsed;
    
    public ItemModelCache() {
        lastUsed = System.currentTimeMillis();
    }
    
    public boolean expired() {
        return System.currentTimeMillis() - lastUsed >= LittleTiles.CONFIG.rendering.itemCacheDuration;
    }
    
    public void setQuads(RenderType type, Facing facing, List<BakedQuad> baked) {
        if (type != Sheets.cutoutBlockSheet())
            return;
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
    }
    
    public boolean isBuilding() {
        return building;
    }
    
    public void complete() {
        building = false;
    }
    
    public List<BakedQuad> getQuads(RenderType type, Facing facing) {
        lastUsed = System.currentTimeMillis();
        if (type != Sheets.cutoutBlockSheet())
            return Collections.EMPTY_LIST;
        if (facing == null)
            return Collections.EMPTY_LIST;
        return switch (facing) {
            case DOWN -> down;
            case EAST -> east;
            case NORTH -> north;
            case SOUTH -> south;
            case UP -> up;
            case WEST -> west;
        };
    }
    
}
