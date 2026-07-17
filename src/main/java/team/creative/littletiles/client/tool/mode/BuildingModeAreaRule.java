package team.creative.littletiles.client.tool.mode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;
import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.gui.integration.ScreenEventListener;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.type.Color;
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
import team.creative.littletiles.common.level.context.ILittleLevelContext;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.box.collection.LittleBoxesSimple;
import team.creative.littletiles.common.placement.PlacementHelper;

public class BuildingModeAreaRule extends BuildingModeFeature implements BuildingModeRule {
    
    private static final Minecraft MC = Minecraft.getInstance();
    private final boolean exclude;
    
    private List<ShapePosition> positions = new ArrayList<>();
    private ShapePosition first;
    private ShapePosition last;
    private int markedPosition = -1;
    
    private boolean active;
    private LittleBoxes boxes;
    private BoxRenderResult result;
    
    private int lastMouseKey = -1;
    private double lastMouseClicked;
    
    @CreativeConfig
    public Color color;
    
    public BuildingModeAreaRule(boolean exclude) {
        this.exclude = exclude;
        this.color = new Color(exclude ? ColorUtils.RED : ColorUtils.GREEN);
        NeoForge.EVENT_BUS.register(this);
    }
    
    @Override
    public void populate(NamedTree<BooleanSupplier> tree) {
        String name = exclude ? "exclude" : "include";
        
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
        return exclude;
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
        positions.clear();
        first = null;
        boxes = null;
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
        renderer.player().displayClientMessage(Component.translatable("building." + (exclude ? "exclude" : "include") + ".tooltip"), true);
        super.tick(renderer);
    }
    
    @Override
    public boolean render(PreviewRenderer renderer, PoseStack pose, Vec3 cam, boolean lines) {
        if (!lines)
            return active;
        
        var blockHit = renderer.blockHit();
        var player = renderer.player();
        var level = renderer.level();
        
        var context = renderer.blockHitContext();
        if (context.isSubLevel()) {
            Vec3 newLocation = context.toRealWorld(blockHit.getLocation());
            BlockPos blockPos = BlockPos.containing(newLocation);
            if (blockHit.getType() == Type.MISS)
                blockHit = BlockHitResult.miss(newLocation, blockHit.getDirection(), blockPos);
            else
                blockHit = new BlockHitResult(newLocation, blockHit.getDirection(), blockPos, blockHit.isInside());
        }
        
        if (active) {
            if (first != null)
                positions.add(first);
            if (last != null)
                positions.add(last);
            renderer.renderPositions(pose, ILittleLevelContext.STANDARD, positions.getFirst().getPos(), cam, positions, x -> markedPosition == x);
            if (first != null)
                positions.removeLast();
            if (last != null)
                positions.removeLast();
            
            if (first != null && last != null) {
                var boxes = new LittleBoxesSimple(first.getPos(), first.getGrid());
                boxes.minGrid(last);
                LittleBox box = LittleBox.ofNothing();
                box.growToInclude(first.getRelative(boxes.pos, boxes.grid));
                box.growToInclude(last.getRelative(boxes.pos, boxes.grid));
                boxes.add(box);
                var result = renderer.buildBoxes(pose, boxes, lines, true);
                renderer.renderSeethroughLines(cam, lines, result.pos(), result.data(), color.toInt(), 1);
                result.close();
            }
        }
        
        RenderSystem.enableBlend();
        
        if (blockHit != null)
            last = new ShapePosition(player, level, PlacementHelper.getPosition(level, blockHit, positionGrid()), blockHit, false, inside());
        
        if (result == null && boxes != null && !boxes.isEmpty())
            result = renderer.buildBoxes(pose, boxes, lines, true);
        
        if (boxes != null && result != null)
            renderer.renderSeethroughLines(cam, lines, result.pos(), result.data(), color.toInt(), 1);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        return active; // Disable standard rendering during selection mode.
    }
    
    private void removeCache() {
        if (result != null) {
            result.close();
            result = null;
        }
    }
    
    private void buildBoxes() {
        removeCache();
        
        if (positions.isEmpty()) {
            boxes = null;
            return;
        }
        
        boxes = new LittleBoxesSimple(positions.getFirst().getPos(), positionGrid());
        for (int i = 0; i < positions.size(); i += 2) {
            boxes.minGrid(positions.get(i));
            boxes.minGrid(positions.get(i + 1));
            LittleBox box = LittleBox.ofNothing();
            box.growToInclude(positions.get(i).getRelative(boxes.pos, boxes.grid));
            box.growToInclude(positions.get(i + 1).getRelative(boxes.pos, boxes.grid));
            boxes.add(box);
        }
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
                    int index = LittleTilesClient.PREVIEW_RENDERER.renderer.select(ILittleLevelContext.STANDARD, positions);
                    if (index >= 0) {
                        index /= 2;
                        index *= 2;
                        positions.remove(index);
                        positions.remove(index);
                        markedPosition = -1;
                    }
                }
            else
                markedPosition = LittleTilesClient.PREVIEW_RENDERER.renderer.select(ILittleLevelContext.STANDARD, positions);
        } else if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (first == null)
                first = last.copy();
            else {
                positions.add(first);
                positions.add(last.copy());
                first = null;
            }
        }
        
        event.setCanceled(true);
        buildBoxes();
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int action, int modifiers) {
        if (!active || action == InputConstants.RELEASE)
            return false;
        
        if (keyCode == InputConstants.KEY_ESCAPE) {
            endFocus();
            return true;
        }
        
        var facing = LittleTilesClient.facingFromKeybind(MC.player, keyCode, scanCode);
        if (facing != null && !positions.isEmpty() && markedPosition != -1) {
            positions.get(markedPosition).move(positions.get(markedPosition).getGrid(), facing);
            buildBoxes();
            return true;
        }
        
        return super.keyPressed(keyCode, scanCode, action, modifiers);
    }
    
    @Override
    public LittleAction prepareAction(LittleAction action) {
        if (boxes == null || boxes.isEmpty())
            return action;
        if (exclude)
            action.exclude(boxes);
        else
            action.include(boxes);
        return super.prepareAction(action);
    }
    
    @Override
    public Component tooltip() {
        if (!active)
            return null;
        return Component.translatable("building.area.mode", MC.options.keyUse.getTranslatedKeyMessage(), MC.options.keyAttack.getTranslatedKeyMessage(), MC.options.keyAttack
                .getTranslatedKeyMessage(), MC.options.keyAttack.getTranslatedKeyMessage());
    }
    
    @Override
    public void unloadLevel() {
        positions.clear();
        boxes = null;
        first = null;
        last = null;
        if (active)
            endFocus();
        removeCache();
    }
    
}
