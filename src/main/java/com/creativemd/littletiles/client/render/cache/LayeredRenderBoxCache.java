package com.creativemd.littletiles.client.render.cache;

import java.util.Iterator;
import java.util.List;

import com.creativemd.creativecore.client.mods.optifine.OptifineHelper;
import com.creativemd.littletiles.client.render.tile.LittleRenderBox;

import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayeredRenderBoxCache {
    
    private List<LittleRenderBox> solid = null;
    private List<LittleRenderBox> cutout_mipped = null;
    private List<LittleRenderBox> cutout = null;
    private List<LittleRenderBox> translucent = null;
    
    public List<LittleRenderBox> get(BlockRenderLayer layer) {
        switch (layer) {
        case SOLID:
            return solid;
        case CUTOUT_MIPPED:
            return cutout_mipped;
        case CUTOUT:
            return cutout;
        case TRANSLUCENT:
            return translucent;
        }
        return null;
    }
    
    public void set(List<LittleRenderBox> cubes, BlockRenderLayer layer) {
        switch (layer) {
        case SOLID:
            solid = cubes;
            break;
        case CUTOUT_MIPPED:
            cutout_mipped = cubes;
            break;
        case CUTOUT:
            cutout = cubes;
            break;
        case TRANSLUCENT:
            translucent = cubes;
            break;
        }
    }
    
    public boolean needUpdate() {
        return solid == null || cutout_mipped == null || cutout == null || translucent == null;
    }
    
    public void clear() {
        solid = null;
        cutout_mipped = null;
        cutout = null;
        translucent = null;
    }
    
    public void sort() {
        if (!OptifineHelper.isActive())
            return;
        
        for (Iterator iterator = solid.iterator(); iterator.hasNext();) {
            LittleRenderBox littleRenderingCube = (LittleRenderBox) iterator.next();
            if (littleRenderingCube.isEmissive) {
                cutout_mipped.add(littleRenderingCube);
                iterator.remove();
            }
        }
    }
    
}
