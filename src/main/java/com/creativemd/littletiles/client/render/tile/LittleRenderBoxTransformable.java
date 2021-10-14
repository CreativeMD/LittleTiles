package com.creativemd.littletiles.client.render.tile;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.world.level.block.Block;
import team.creative.creativecore.client.render.face.IFaceRenderType;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.geo.VectorFan;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleTransformableBox;
import team.creative.littletiles.common.math.box.LittleTransformableBox.VectorFanCache;
import team.creative.littletiles.common.math.box.LittleTransformableBox.VectorFanFaceCache;

public class LittleRenderBoxTransformable extends LittleRenderBox {
    
    private float scale;
    private float inverseScale;
    public VectorFanCache cache;
    
    public LittleRenderBoxTransformable(AlignedBox cube, LittleGrid context, LittleTransformableBox box, Block block, int meta) {
        super(cube, box, block, meta);
        this.cache = box.requestCache();
        this.scale = (float) context.pixelLength;
        this.inverseScale = context.count;
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
    
    public VectorFanFaceCache getFaceCache(Facing facing) {
        if (cache != null)
            return cache.get(facing);
        return null;
    }
    
    @Override
    public boolean renderSide(Facing facing) {
        VectorFanFaceCache cache = getFaceCache(facing);
        if (cache == null)
            return false;
        if (super.renderSide(facing))
            return true;
        return cache.hasTiltedStrip();
    }
    
    @Override
    protected Object getRenderQuads(Facing facing) {
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
    protected void setupPreviewRendering(double x, double y, double z) {
        GlStateManager.translate(x, y, z);
        GlStateManager.scale(scale, scale, scale);
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
        IFaceRenderType type = getType(facing);
        if (type.hasCachedFans())
            return type.getScale();
        return scale;
    }
}
