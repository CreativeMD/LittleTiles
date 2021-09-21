package com.creativemd.littletiles.common.tile.math.box;

import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.util.EnumFacing.Axis;

public class LittleBoxReturnedVolume {
    
    private int volume;
    
    public LittleBoxReturnedVolume() {
        
    }
    
    public void addPixel() {
        volume++;
    }
    
    public void addDifBox(LittleBox box, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        int sizeX = (maxX - minX) - box.getSize(Axis.X);
        int sizeY = (maxY - minY) - box.getSize(Axis.Y);
        int sizeZ = (maxZ - minZ) - box.getSize(Axis.Z);
        volume += sizeX * sizeY * sizeZ;
    }
    
    public void addBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        volume += (maxX - minX) * (maxY - minY) * (maxZ - minZ);
    }
    
    public boolean has() {
        return volume > 0;
    }
    
    public int getVolume() {
        return volume;
    }
    
    public double getPercentVolume(LittleGridContext context) {
        return volume / (double) context.maxTilesPerBlock;
    }
    
    public void clear() {
        volume = 0;
    }
    
    public LittlePreview createFakePreview(LittlePreview preview) {
        LittlePreview copy = preview.copy();
        copy.setBox(new LittleBox(0, 0, 0, volume, 1, 1));
        return copy;
    }
    
    public LittleTile createFakeTile(LittleTile tile) {
        LittleTile copy = tile.copy();
        copy.setBox(new LittleBox(0, 0, 0, volume, 1, 1));
        return copy;
    }
    
}
