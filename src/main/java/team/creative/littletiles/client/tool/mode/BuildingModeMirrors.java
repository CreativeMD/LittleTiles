package team.creative.littletiles.client.tool.mode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;
import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.gui.integration.ScreenEventListener;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.ABB;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.mc.PlayerUtils;
import team.creative.creativecore.common.util.type.tree.NamedTree;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.client.render.overlay.OverlayRenderer.OverlayGuiLayer;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.client.render.overlay.PreviewRenderer.BoxRenderResult;
import team.creative.littletiles.client.tool.LittleTool;
import team.creative.littletiles.client.tool.mode.BuildingModeRules.BuildingModeRule;
import team.creative.littletiles.client.tool.shaper.ShapePosition;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.LittleActions;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.box.collection.LittleBoxesSimple;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.placement.PlacementHelper;

public class BuildingModeMirrors extends BuildingModeFeature implements BuildingModeRule {
    
    private static final Minecraft MC = Minecraft.getInstance();
    
    private List<MirrorOrigin> mirrorOrigins = new ArrayList<>();
    private ShapePosition first;
    private ShapePosition last;
    private int marked = -1;
    
    private boolean active;
    private BoxRenderResult result;
    
    private int lastMouseKey = -1;
    private double lastMouseClicked;
    
    @CreativeConfig
    private int color = ColorUtils.ORANGE;
    
    @CreativeConfig
    private int xColor = ColorUtils.LIGHT_BLUE;
    @CreativeConfig
    private int yColor = ColorUtils.CYAN;
    @CreativeConfig
    private int zColor = ColorUtils.MAGENTA;
    
    public BuildingModeMirrors() {
        NeoForge.EVENT_BUS.register(this);
    }
    
    @Override
    public void populate(NamedTree<BooleanSupplier> tree) {
        String name = "mirror";
        
        if (active)
            tree.add(name + ".end", () -> {
                endFocus();
                return true;
            });
        else
            tree.add(name + ".configure", () -> {
                startFocus();
                return true;
            });
        
        tree.add(name + ".clear", () -> {
            reset();
            endFocus();
            return true;
        });
    }
    
    public boolean inside() {
        return false;
    }
    
    @Override
    public void startFocus() {
        super.startFocus();
        active = true;
    }
    
    @Override
    public void endFocus() {
        super.endFocus();
        if (active)
            MC.player.displayClientMessage(Component.literal(""), true);
        active = false;
    }
    
    @Override
    public void reset() {
        removeCache();
        mirrorOrigins.clear();
        first = null;
    }
    
    @Override
    public void create(OverlayGuiLayer gui, LittleTool tool, List<BuildingModeFeature> allFeatures) {}
    
    @Override
    public void remove(OverlayGuiLayer gui) {
        if (active)
            endFocus();
    }
    
    @Override
    public void tick(PreviewRenderer renderer) {
        if (!active)
            return;
        renderer.player().displayClientMessage(Component.translatable("building.mirror.tooltip"), true);
        super.tick(renderer);
    }
    
    @Override
    public boolean render(PreviewRenderer renderer, PoseStack pose, Vec3 cam, boolean lines) {
        var blockHit = renderer.blockHit();
        var player = renderer.player();
        var level = renderer.level();
        
        if (!lines) {
            pose.pushPose();
            pose.translate(-cam.x, -cam.y, -cam.z);
            renderer.setupPreviewRenderer(false);
            for (MirrorOrigin mirror : mirrorOrigins)
                mirror.renderMirrors(renderer, pose);
            pose.popPose();
            
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.enableCull();
            return active;
        }
        
        if (!active)
            return false;
        
        var grid = positionGrid();
        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);
        if (first != null)
            renderer.renderLineBox(pose, first.getBB(grid), false);
        if (last != null)
            renderer.renderLineBox(pose, last.getBB(grid), false);
        pose.popPose();
        
        if (first != null && last != null) {
            first.sameGrid(last, () -> {
                var boxes = new LittleBoxesSimple(first.getPos(), first.getGrid());
                LittleBox box = LittleBox.ofNothing();
                box.growToIncludePixel(first.getRelative(boxes.pos));
                box.growToIncludePixel(last.getRelative(boxes.pos));
                boxes.add(box);
                var result = renderer.buildBoxes(pose, boxes, lines);
                renderer.renderSeethroughLines(cam, lines, result.pos(), result.data(), color);
                result.close();
            });
        }
        
        if (blockHit != null)
            last = new ShapePosition(player, PlacementHelper.getPosition(level, blockHit, positionGrid()), blockHit, false, inside());
        
        if (result == null && mirrorOrigins != null && !mirrorOrigins.isEmpty()) {
            LittleBoxes boxes = new LittleBoxesSimple(mirrorOrigins.getFirst().pos(), LittleGrid.MIN);
            for (MirrorOrigin origin : mirrorOrigins)
                boxes.addBox(origin.box);
            result = renderer.buildBoxes(pose, boxes, lines);
        }
        
        if (mirrorOrigins != null && result != null) {
            if (marked != -1 && marked < mirrorOrigins.size()) {
                pose.pushPose();
                pose.translate(-cam.x, -cam.y, -cam.z);
                mirrorOrigins.get(marked).render(pose, true);
                pose.popPose();
            }
            
            renderer.renderSeethroughLines(cam, lines, result.pos(), result.data(), color);
            
        }
        
        return active; // Disable standard rendering during selection mode.
    }
    
    private void removeCache() {
        if (result != null) {
            result.close();
            result = null;
        }
    }
    
    public int select(PreviewRenderer renderer) {
        int index = -1;
        double distance = Double.MAX_VALUE;
        var player = renderer.player();
        float partialTickTime = renderer.partialTickTime();
        Vec3 pos = player.getEyePosition(partialTickTime);
        double reach = PlayerUtils.getReach(player);
        Vec3 view = player.getViewVector(partialTickTime);
        Vec3 look = pos.add(view.x * reach, view.y * reach, view.z * reach);
        for (int i = 0; i < mirrorOrigins.size(); i++) {
            Optional<Vec3> result = mirrorOrigins.get(i).box.toAABB().clip(pos, look);
            if (result.isPresent()) {
                double tempDistance = pos.distanceToSqr(result.get());
                if (tempDistance < distance) {
                    index = i;
                    distance = tempDistance;
                }
            }
        }
        return index;
    }
    
    @SubscribeEvent
    public void mouseClicked(InputEvent.MouseButton.Pre event) {
        if (event.getAction() != InputConstants.PRESS || MC.player == null || MC.screen != null || !active)
            return;
        
        boolean doubleClick = lastMouseKey == event.getButton() && Blaze3D.getTime() - lastMouseClicked < ScreenEventListener.DOUBLE_CLICK_TIME;
        lastMouseKey = event.getButton();
        lastMouseClicked = Blaze3D.getTime();
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (LittleActionHandlerClient.isUsingSecondMode())
                if (doubleClick)
                    reset();
                else {
                    int index = select(LittleTilesClient.PREVIEW_RENDERER.renderer);
                    if (index >= 0)
                        mirrorOrigins.remove(index);
                    marked = -1;
                    removeCache();
                }
            else
                marked = select(LittleTilesClient.PREVIEW_RENDERER.renderer);
        } else if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            int index = select(LittleTilesClient.PREVIEW_RENDERER.renderer);
            if (index >= 0) {
                var renderer = LittleTilesClient.PREVIEW_RENDERER.renderer;
                var player = renderer.player();
                float partialTickTime = renderer.partialTickTime();
                Vec3 pos = player.getEyePosition(partialTickTime);
                double reach = PlayerUtils.getReach(player);
                Vec3 view = player.getViewVector(partialTickTime);
                Vec3 look = pos.add(view.x * reach, view.y * reach, view.z * reach);
                var direction = mirrorOrigins.get(index).facing(pos, look);
                if (direction != null)
                    mirrorOrigins.get(index).toggle(direction.getAxis());
            } else {
                if (first == null)
                    first = last.copy();
                else {
                    mirrorOrigins.add(new MirrorOrigin(first, last));
                    first = null;
                    removeCache();
                }
            }
        }
        
        event.setCanceled(true);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int action, int modifiers) {
        if (!active || action != InputConstants.PRESS)
            return false;
        
        if (keyCode == InputConstants.KEY_ESCAPE) {
            endFocus();
            return true;
        }
        
        var facing = LittleTilesClient.facingFromKeybind(MC.player, keyCode, scanCode);
        if (facing != null && !mirrorOrigins.isEmpty() && marked != -1) {
            mirrorOrigins.get(marked).move(positionGrid(), facing);
            removeCache();
            return true;
        }
        
        return super.keyPressed(keyCode, scanCode, action, modifiers);
    }
    
    @Override
    public LittleAction prepareAction(LittleAction action) {
        List<LittleAction> actions = new ArrayList<>();
        actions.add(action);
        
        int size = actions.size();
        for (MirrorOrigin mirror : mirrorOrigins)
            if (mirror.x)
                mirror.mirror(actions, size, team.creative.creativecore.common.util.math.base.Axis.X);
            
        size = actions.size();
        for (MirrorOrigin mirror : mirrorOrigins)
            if (mirror.y)
                mirror.mirror(actions, size, team.creative.creativecore.common.util.math.base.Axis.Y);
            
        size = actions.size();
        for (MirrorOrigin mirror : mirrorOrigins)
            if (mirror.z)
                mirror.mirror(actions, size, team.creative.creativecore.common.util.math.base.Axis.Z);
            
        return new LittleActions(actions.toArray(new LittleAction[0]));
    }
    
    @Override
    public Component tooltip() {
        if (!active)
            return null;
        return Component.translatable("building.mirror.mode", MC.options.keyUse.getTranslatedKeyMessage(), MC.options.keyUse.getTranslatedKeyMessage(), MC.options.keyAttack
                .getTranslatedKeyMessage(), MC.options.keyAttack.getTranslatedKeyMessage(), MC.options.keyAttack.getTranslatedKeyMessage());
    }
    
    @Override
    public void unloadLevel() {
        mirrorOrigins.clear();
        first = null;
        last = null;
        if (active)
            endFocus();
        removeCache();
    }
    
    public class MirrorOrigin {
        
        public final LittleBoxAbsolute box;
        public boolean x;
        public boolean y;
        public boolean z;
        
        public MirrorOrigin(ShapePosition first, ShapePosition last) {
            BlockPos pos = first.getPos();
            this.box = first.sameGrid(last, () -> {
                LittleBox box = LittleBox.ofNothing();
                box.growToIncludePixel(first.getRelative(pos));
                box.growToIncludePixel(last.getRelative(pos));
                return new LittleBoxAbsolute(pos, box, first.getGrid());
            });
        }
        
        public MirrorOrigin(LittleBoxAbsolute box) {
            this.box = box;
        }
        
        public void move(LittleGrid positionGrid, Facing facing) {
            LittleVecGrid vec = new LittleVecGrid(new LittleVec(facing), positionGrid);
            box.sameGrid(vec, () -> box.box.add(vec.getVec()));
        }
        
        public BlockPos pos() {
            return box.pos;
        }
        
        public void mirror(List<LittleAction> actions, int count, team.creative.creativecore.common.util.math.base.Axis axis) {
            for (int i = 0; i < count; i++)
                actions.add(actions.get(i).mirror(axis, box));
        }
        
        private void renderMirror(PoseStack pose, Axis axis, Vec3 center, int color) {
            float normalX = axis == Axis.X ? 1 : 0;
            float normalY = axis == Axis.Y ? 1 : 0;
            float normalZ = axis == Axis.Z ? 1 : 0;
            
            color = ColorUtils.setAlpha(color, 100);
            
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_NORMAL);
            var a = team.creative.creativecore.common.util.math.base.Axis.get(axis);
            float size = 1000;
            float doubleSize = size * 2;
            Vec3d vec = new Vec3d(center);
            
            vec.set(a.one(), vec.get(a.one()) - size);
            vec.set(a.two(), vec.get(a.two()) - size);
            
            RenderSystem.disableCull();
            bufferbuilder.addVertex(pose.last().pose(), (float) vec.x, (float) vec.y, (float) vec.z).setColor(color).setNormal(normalX, normalY, normalZ);
            vec.set(a.one(), vec.get(a.one()) + doubleSize);
            bufferbuilder.addVertex(pose.last().pose(), (float) vec.x, (float) vec.y, (float) vec.z).setColor(color).setNormal(normalX, normalY, normalZ);
            vec.set(a.two(), vec.get(a.two()) + doubleSize);
            bufferbuilder.addVertex(pose.last().pose(), (float) vec.x, (float) vec.y, (float) vec.z).setColor(color).setNormal(normalX, normalY, normalZ);
            vec.set(a.one(), vec.get(a.one()) - doubleSize);
            bufferbuilder.addVertex(pose.last().pose(), (float) vec.x, (float) vec.y, (float) vec.z).setColor(color).setNormal(normalX, normalY, normalZ);
            
            BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        }
        
        public void renderMirrors(PreviewRenderer renderer, PoseStack pose) {
            Vec3 center = this.box.toAABB().getCenter();
            if (x)
                renderMirror(pose, Axis.X, center, xColor);
            if (y)
                renderMirror(pose, Axis.Y, center, yColor);
            if (z)
                renderMirror(pose, Axis.Z, center, zColor);
        }
        
        public void render(PoseStack pose, boolean selected) {
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
            
            RenderSystem.depthMask(true);
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            
            ABB box = this.box.toABB();
            box.inflate(0.002);
            
            RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
            
            RenderSystem.lineWidth(3.0F);
            box.renderLines(pose, bufferbuilder, 0, 0, 0, 1F);
            
            BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
            
            RenderSystem.disableDepthTest();
            if (selected) {
                bufferbuilder = tesselator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
                RenderSystem.lineWidth(6.0F);
                box.renderLines(pose, bufferbuilder, 1F, 0.3F, 0.0F, 1F);
                BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
            }
            
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
        }
        
        public void toggle(Axis axis) {
            switch (axis) {
                case X -> x = !x;
                case Y -> y = !y;
                case Z -> z = !z;
            }
        }
        
        @Nullable
        public Direction facing(Vec3 pos, Vec3 look) {
            var bb = box.toABB();
            var hit = bb.rayTrace(pos, look, BlockPos.ZERO);
            if (hit != null)
                return hit.getDirection();
            return null;
        }
        
    }
    
}
