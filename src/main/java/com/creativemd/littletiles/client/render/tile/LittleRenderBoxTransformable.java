package com.creativemd.littletiles.client.render.tile;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.client.rendering.face.IFaceRenderType;
import com.creativemd.creativecore.common.utils.math.box.AlignedBox;
import com.creativemd.creativecore.common.utils.math.vec.VectorFan;
import com.creativemd.littletiles.common.tile.math.box.LittleTransformableBox;
import com.creativemd.littletiles.common.tile.math.box.LittleTransformableBox.VectorFanCache;
import com.creativemd.littletiles.common.tile.math.box.LittleTransformableBox.VectorFanFaceCache;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;

public class LittleRenderBoxTransformable extends LittleRenderBox {
    
    private float scale;
    private float inverseScale;
    public VectorFanCache cache;
    
    public LittleRenderBoxTransformable(AlignedBox cube, LittleGridContext context, LittleTransformableBox box, Block block, int meta) {
        super(cube, box, block, meta);
        this.cache = box.requestCache();
        this.scale = (float) context.pixelSize;
        this.inverseScale = context.size;
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
        //this.scale /= scale;
    }
    
    public VectorFanFaceCache getFaceCache(EnumFacing facing) {
        if (cache != null)
            return cache.get(facing);
        return null;
    }
    
    @Override
    public boolean renderSide(EnumFacing facing) {
        VectorFanFaceCache cache = getFaceCache(facing);
        if (cache == null)
            return false;
        if (super.renderSide(facing))
            return true;
        return cache.hasTiltedStrip();
    }
    
    @Override
    protected Object getRenderQuads(EnumFacing facing) {
        if (getType(facing).hasCachedFans())
            return getType(facing).getCachedFans();
        VectorFanFaceCache cache = getFaceCache(facing);
        
        if (cache.hasTiltedStrip()) {
            if (super.renderSide(facing) && cache.hasAxisStrip()) {
                List<VectorFan> strips = new ArrayList<>(cache.axisStrips);
                if (cache.tiltedStrip1 != null)
                    strips.add(cache.tiltedStrip1);
                if (cache.tiltedStrip2 != null)
                    strips.add(cache.tiltedStrip2);
                return strips;
            }
            
            if (cache.tiltedStrip1 != null ^ cache.tiltedStrip2 != null) {
                if (cache.tiltedStrip1 != null)
                    return cache.tiltedStrip1;
                return cache.tiltedStrip2;
            }
            
            List<VectorFan> strips = new ArrayList<>();
            if (cache.tiltedStrip1 != null)
                strips.add(cache.tiltedStrip1);
            if (cache.tiltedStrip2 != null)
                strips.add(cache.tiltedStrip2);
            return strips;
        }
        if (super.renderSide(facing))
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
    protected boolean scaleAndOffsetQuads(EnumFacing facing) {
        return true;
    }
    
    @Override
    protected boolean onlyScaleOnceNoOffset(EnumFacing facing) {
        return true;
    }
    
    @Override
    protected float getOverallScale(EnumFacing facing) {
        IFaceRenderType type = getType(facing);
        if (type.hasCachedFans())
            return type.getScale();
        return scale;
    }
}
