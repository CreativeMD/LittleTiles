package team.creative.littletiles.client.render.cache;

import java.util.Iterator;
import java.util.List;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.creativecore.common.mod.OptifineHelper;
import team.creative.littletiles.client.render.tile.LittleRenderBox;

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
        if (!OptifineHelper.installed())
            return;
        
        for (Iterator iterator = solid.iterator(); iterator.hasNext();) {
            LittleRenderBox littleRenderingCube = (LittleRenderBox) iterator.next();
            if (littleRenderingCube.needsResorting) {
                cutout_mipped.add(littleRenderingCube);
                iterator.remove();
            }
        }
    }
    
}
