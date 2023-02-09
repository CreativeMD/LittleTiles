package team.creative.littletiles.common.gui.tool.recipe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.math.vec.SmoothValue;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.common.animation.preview.AnimationPreview;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.box.collection.LittleBoxesNoOverlap;

public class GuiRecipeAnimationStorage implements Iterable<Entry<GuiTreeItemStructure, AnimationPreview>> {
    
    private LinkedHashMap<GuiTreeItemStructure, AnimationPreview> availablePreviews = new LinkedHashMap<>();
    private LittleBoxesNoOverlap overlappingBoxes = null;
    private ConcurrentLinkedQueue<AnimationPair> change = new ConcurrentLinkedQueue<>();
    
    private AABB overall = null;
    private SmoothValue offX = new SmoothValue(200);
    private SmoothValue offY = new SmoothValue(200);
    private SmoothValue offZ = new SmoothValue(200);
    
    public GuiRecipeAnimationStorage() {}
    
    public boolean isReady() {
        return overall != null && !availablePreviews.isEmpty();
    }
    
    public void resetOverlap() {
        overlappingBoxes = null;
    }
    
    public boolean hasOverlap() {
        return overlappingBoxes != null && !overlappingBoxes.isEmpty();
    }
    
    public LittleBoxesNoOverlap getOverlap() {
        return overlappingBoxes;
    }
    
    public void addOverlap(LittleBoxes boxes) {
        if (overlappingBoxes == null)
            overlappingBoxes = new LittleBoxesNoOverlap(BlockPos.ZERO, LittleGrid.min());
        if (boxes instanceof LittleBoxesNoOverlap no)
            for (Entry<BlockPos, ArrayList<LittleBox>> entry : no.generateBlockWise().entrySet())
                overlappingBoxes.addBoxes(boxes.getGrid(), entry.getKey(), entry.getValue());
        else
            for (LittleBox box : boxes.all())
                overlappingBoxes.addBox(boxes.getGrid(), boxes.pos, box.copy());
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
    
    protected void remove(GuiTreeItemStructure structure) {
        availablePreviews.remove(structure).unload();
        updateBox();
    }
    
    protected void put(GuiTreeItemStructure structure, AnimationPreview preview) {
        AnimationPreview previous = availablePreviews.put(structure, preview);
        if (previous != null)
            previous.unload();
        updateBox();
    }
    
    public void removed(GuiTreeItemStructure structure) {
        if (RenderSystem.isOnRenderThread())
            remove(structure);
        else
            change.add(new AnimationPair(structure, null));
    }
    
    public void completed(GuiTreeItemStructure structure, AnimationPreview preview) {
        if (RenderSystem.isOnRenderThread()) {
            availablePreviews.put(structure, preview);
            updateBox();
        } else
            change.add(new AnimationPair(structure, preview));
    }
    
    public void tick() {
        offX.tick();
        offY.tick();
        offZ.tick();
        
        if (!change.isEmpty()) {
            AnimationPair pair;
            while ((pair = change.poll()) != null) {
                if (pair.preview == null)
                    remove(pair.item);
                else
                    availablePreviews.put(pair.item, pair.preview);
            }
            updateBox();
        }
    }
    
    @Override
    public Iterator<Entry<GuiTreeItemStructure, AnimationPreview>> iterator() {
        return availablePreviews.entrySet().iterator();
    }
    
    private static record AnimationPair(GuiTreeItemStructure item, AnimationPreview preview) {}
}
