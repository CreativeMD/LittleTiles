package team.creative.littletiles.common.gui.tool.recipe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.common.util.math.vec.SmoothValue;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.AnimationPreview;
import team.creative.littletiles.common.gui.controls.GuiAnimationViewer;
import team.creative.littletiles.common.gui.controls.GuiAnimationViewer.GuiAnimationViewerStorage;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.box.collection.LittleBoxesNoOverlap;
import team.creative.littletiles.common.math.vec.LittleVecGrid;

public class GuiRecipeAnimationStorage implements Iterable<Entry<GuiTreeItemStructure, AnimationPreview>>, GuiAnimationViewerStorage {
    
    private boolean highlightSelected = false;
    
    private LinkedHashMap<GuiTreeItemStructure, AnimationPreview> availablePreviews = new LinkedHashMap<>();
    private LittleBoxesNoOverlap overlappingBoxes = null;
    private ConcurrentLinkedQueue<AnimationPair> change = new ConcurrentLinkedQueue<>();
    
    private AABB overall = null;
    private SmoothValue offX = new SmoothValue(200);
    private SmoothValue offY = new SmoothValue(200);
    private SmoothValue offZ = new SmoothValue(200);
    
    private boolean unloaded = false;
    
    public GuiRecipeAnimationStorage() {}
    
    @Override
    public boolean highlightSelected() {
        return highlightSelected;
    }
    
    @Override
    public void highlightSelected(boolean value) {
        this.highlightSelected = value;
    }
    
    @Override
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
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(PoseStack pose, PoseStack projection, GuiAnimationViewer viewer, Minecraft mc) {
        GuiTreeItemStructure selected = null;
        for (Entry<GuiTreeItemStructure, AnimationPreview> entry : this) {
            if (entry.getKey().tree.hasCheckboxes() && !entry.getKey().isChecked())
                continue;
            
            pose.pushPose();
            RenderSystem.applyModelViewMatrix();
            LittleVecGrid offset = entry.getKey().getOffset();
            if (offset != null)
                pose.translate(offset.getPosX(), offset.getPosY(), offset.getPosZ());
            
            viewer.renderPreview(pose, projection, entry.getValue(), mc);
            
            if (highlightSelected && entry.getKey().selected())
                selected = entry.getKey();
            
            pose.popPose();
        }
        
        RenderSystem.depthMask(true);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        
        PoseStack empty = new PoseStack();
        empty.setIdentity();
        
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        RenderSystem.applyModelViewMatrix();
        
        if (selected != null) {
            LittleVecGrid offset = selected.getOffset();
            double x = 0;
            double y = 0;
            double z = 0;
            if (offset != null) {
                x = offset.getPosX();
                y = offset.getPosY();
                z = offset.getPosZ();
            }
            
            LittleGrid grid = selected.group.getGrid();
            VoxelShape shape = Shapes.empty();
            for (LittleBox box : selected.group.allBoxes())
                shape = Shapes.or(box.getShape(grid), shape);
            
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();
            
            bufferbuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
            RenderSystem.lineWidth(1.0F);
            PreviewRenderer.renderShape(empty, bufferbuilder, shape, x, y, z, 1, 1, 1, 1);
            tesselator.end();
        }
        
        if (hasOverlap()) {
            
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();
            int colorAlpha = 102;
            LittleBoxesNoOverlap overlap = overlappingBoxes;
            LittleGrid grid = overlap.getGrid();
            for (Entry<BlockPos, ArrayList<LittleBox>> entry : overlap.generateBlockWise().entrySet()) {
                pose.pushPose();
                pose.translate(entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ());
                RenderSystem.applyModelViewMatrix();
                
                for (LittleBox box : entry.getValue()) {
                    RenderBox renderBox = box.getRenderingBox(grid);
                    RenderSystem.disableDepthTest();
                    RenderSystem.lineWidth(8.0F);
                    renderBox.renderLines(empty, bufferbuilder, colorAlpha);
                    RenderSystem.enableDepthTest();
                    RenderSystem.lineWidth(4.0F);
                    renderBox.color = ColorUtils.RED;
                    renderBox.renderLines(empty, bufferbuilder, colorAlpha);
                }
                pose.popPose();
            }
            RenderSystem.disableDepthTest();
        }
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
    
    @Override
    public Vec3d center() {
        return new Vec3d(offX.current(), offY.current(), offZ.current());
    }
    
    @Override
    public double longestSide() {
        return Math.max(overall.maxX - overall.minX, Math.max(overall.maxY - overall.minY, overall.maxZ - overall.minZ));
    }
    
    @Override
    public AABB overall() {
        return overall;
    }
    
    protected void remove(GuiTreeItemStructure structure) {
        availablePreviews.remove(structure).unload();
        updateBox();
    }
    
    protected void put(GuiTreeItemStructure structure, AnimationPreview preview) {
        AnimationPreview previous = availablePreviews.put(structure, preview);
        if (previous != null)
            previous.unload();
        if (unloaded)
            preview.unload();
        updateBox();
    }
    
    public void removed(GuiTreeItemStructure structure) {
        if (RenderSystem.isOnRenderThread())
            remove(structure);
        else
            change.add(new AnimationPair(structure, null));
    }
    
    public void completed(GuiTreeItemStructure structure, AnimationPreview preview) {
        if (RenderSystem.isOnRenderThread())
            put(structure, preview);
        else
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
                    put(pair.item, pair.preview);
            }
            updateBox();
        }
    }
    
    @Override
    public Iterator<Entry<GuiTreeItemStructure, AnimationPreview>> iterator() {
        return availablePreviews.entrySet().iterator();
    }
    
    public void unload() {
        unloaded = true;
        
        for (AnimationPair pair : change)
            if (pair.preview != null)
                pair.preview.unload();
            
        for (AnimationPreview preview : availablePreviews.values())
            preview.unload();
    }
    
    private static record AnimationPair(GuiTreeItemStructure item, AnimationPreview preview) {}
}
