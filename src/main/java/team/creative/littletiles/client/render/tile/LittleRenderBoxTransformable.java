package team.creative.littletiles.client.render.tile;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.client.render.face.RenderBoxFace;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.geo.VectorFan;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleTransformableBox;
import team.creative.littletiles.common.math.box.LittleTransformableBox.VectorFanCache;
import team.creative.littletiles.common.math.box.LittleTransformableBox.VectorFanFaceCache;

public class LittleRenderBoxTransformable extends LittleRenderBox {
    
    private float scale;
    private float inverseScale;
    public VectorFanCache cache;
    
    public LittleRenderBoxTransformable(LittleGrid grid, LittleTransformableBox box) {
        super(grid, box);
        this.cache = box.requestCache();
        this.scale = (float) grid.pixelLength;
        this.inverseScale = grid.count;
    }
    
    public LittleRenderBoxTransformable(LittleGrid grid, LittleTransformableBox box, BlockState state) {
        super(grid, box, state);
        this.cache = box.requestCache();
        this.scale = (float) grid.pixelLength;
        this.inverseScale = grid.count;
    }
    
    public LittleRenderBoxTransformable(LittleGrid grid, LittleTransformableBox box, LittleElement element) {
        super(grid, box, element);
        this.cache = box.requestCache();
        this.scale = (float) grid.pixelLength;
        this.inverseScale = grid.count;
    }
    
    @Override
    public void add(float x, float y, float z) {
        super.add(x, y, z);
        cache.add(x * inverseScale, y * inverseScale, z * inverseScale);
    }
    
    @Override
    public void sub(float x, float y, float z) {
        super.sub(x, y, z);
        cache.sub(x * inverseScale, y * inverseScale, z * inverseScale);
    }
    
    @Override
    public void scale(float scale) {
        super.scale(scale);
        cache.scale(scale);
    }
    
    public VectorFanFaceCache getFaceCache(Facing facing) {
        if (cache != null)
            return cache.get(facing);
        return null;
    }
    
    @Override
    public boolean shouldRenderFace(Facing facing) {
        VectorFanFaceCache cache = getFaceCache(facing);
        if (cache == null)
            return false;
        if (cache.hasAxisStrip() && super.shouldRenderFace(facing))
            return true;
        return cache.hasTiltedStripsRendering();
    }
    
    @Override
    protected Object getRenderQuads(Facing facing) {
        if (getFace(facing).hasCachedFans())
            return getFace(facing).getCachedFans();
        VectorFanFaceCache cache = getFaceCache(facing);
        
        if (cache.hasTiltedStripsRendering()) {
            if (super.shouldRenderFace(facing) && cache.hasAxisStrip()) {
                List<VectorFan> strips = new ArrayList<>(cache.axisStrips);
                cache.collectAllTiltedStripsRendering(strips);
                return strips;
            }
            
            if (cache.hasSingleTiltedStripRendering())
                return cache.getSingleTiltedStripRendering();
            
            List<VectorFan> strips = new ArrayList<>();
            cache.collectAllTiltedStripsRendering(strips);
            return strips;
        }
        if (super.shouldRenderFace(facing))
            return cache.axisStrips;
        return null;
    }
    
    @Override
    public float getPreviewOffX() {
        return 0;
    }
    
    @Override
    public float getPreviewOffY() {
        return 0;
    }
    
    @Override
    public float getPreviewOffZ() {
        return 0;
    }
    
    @Override
    public float getPreviewScaleX() {
        return scale;
    }
    
    @Override
    public float getPreviewScaleY() {
        return scale;
    }
    
    @Override
    public float getPreviewScaleZ() {
        return scale;
    }
    
    @Override
    protected boolean scaleAndOffsetQuads(Facing facing) {
        return true;
    }
    
    @Override
    protected boolean onlyScaleOnceNoOffset(Facing facing) {
        return true;
    }
    
    @Override
    protected float getOverallScale(Facing facing) {
        RenderBoxFace type = getFace(facing);
        if (type.hasCachedFans())
            return type.getScale();
        return scale;
    }
}
