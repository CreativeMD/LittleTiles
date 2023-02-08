package team.creative.littletiles.common.gui.tool.recipe;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.math.vec.SmoothValue;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.common.animation.preview.AnimationPreview;

public class GuiRecipeAnimationStorage implements Iterable<Entry<GuiTreeItemStructure, AnimationPreview>> {
    
    private LinkedHashMap<GuiTreeItemStructure, AnimationPreview> availablePreviews = new LinkedHashMap<>();
    private AABB overall = null;
    private SmoothValue offX = new SmoothValue(200);
    private SmoothValue offY = new SmoothValue(200);
    private SmoothValue offZ = new SmoothValue(200);
    
    public GuiRecipeAnimationStorage() {}
    
    public boolean isReady() {
        return overall != null && !availablePreviews.isEmpty();
    }
    
    private void updateBox() {
        if (availablePreviews.isEmpty()) {
            overall = null;
            return;
        }
        
        boolean init = overall == null;
        
        overall = null;
        for (AnimationPreview preview : availablePreviews.values())
            if (overall == null)
                overall = preview.box;
            else
                overall = overall.minmax(preview.box);
            
        Vec3 center = overall.getCenter();
        if (init) {
            offX.setStart(center.x);
            offY.setStart(center.y);
            offZ.setStart(center.z);
        } else {
            offX.set(center.x);
            offY.set(center.y);
            offZ.set(center.z);
        }
    }
    
    public Vec3d center() {
        return new Vec3d(offX.current(), offY.current(), offZ.current());
    }
    
    public double longestSide() {
        return Math.max(overall.maxX - overall.minX, Math.max(overall.maxY - overall.minY, overall.maxZ - overall.minZ));
    }
    
    public void removed(GuiTreeItemStructure structure) {
        availablePreviews.remove(structure);
        updateBox();
    }
    
    public void completed(GuiTreeItemStructure structure, AnimationPreview preview) {
        availablePreviews.put(structure, preview);
        updateBox();
    }
    
    public void tick() {
        offX.tick();
        offY.tick();
        offZ.tick();
    }
    
    @Override
    public Iterator<Entry<GuiTreeItemStructure, AnimationPreview>> iterator() {
        return availablePreviews.entrySet().iterator();
    }
    
}
