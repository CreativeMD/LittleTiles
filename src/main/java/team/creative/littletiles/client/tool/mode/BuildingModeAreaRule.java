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
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;
import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.gui.integration.ScreenEventListener;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.type.tree.NamedTree;
import team.creative.littletiles.api.common.tool.ILittleTool;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.client.render.overlay.OverlayRenderer.OverlayGuiLayer;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.client.render.overlay.PreviewRenderer.BoxRenderResult;
import team.creative.littletiles.client.tool.LittleTool;
import team.creative.littletiles.client.tool.mode.BuildingModeRules.BuildingModeRule;
import team.creative.littletiles.client.tool.shaper.ShapePosition;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.grid.LittleGrid;
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
    private int color;
    
    public BuildingModeAreaRule(boolean exclude) {
        this.exclude = exclude;
        this.color = exclude ? ColorUtils.RED : ColorUtils.GREEN;
        NeoForge.EVENT_BUS.register(this);
    }
    
    @Override
    public void populate(NamedTree<BooleanSupplier> tree) {
        String name = exclude ? "exclude" : "include";
        
        if (active)
            tree.add(name + ".end", () -> {
                end();
                return true;
            });
        else
            tree.add(name + ".configure", () -> {
                active = true;
                return true;
            });
        
        tree.add(name + ".clear", () -> {
            boxes = null;
            return true;
        });
    }
    
    public boolean inside() {
        return exclude;
    }
    
    public void end() {
        MC.player.displayClientMessage(Component.literal(""), true);
        active = false;
    }
    
    @Override
    public void reset() {
        boxes = null;
    }
    
    @Override
    public void create(OverlayGuiLayer gui, LittleTool tool, List<BuildingModeFeature> allFeatures) {}
    
    @Override
    public void remove(OverlayGuiLayer gui) {
        active = false;
    }
    
    @Override
    public void tick(PreviewRenderer renderer) {
        if (!active)
            return;
        renderer.player().displayClientMessage(Component.translatable("building." + (exclude ? "exclude" : "include") + ".tooltip"), true);
        super.tick(renderer);
    }
    
    protected void renderBoxes(PreviewRenderer renderer, Vec3 cam, boolean lines, BoxRenderResult result) {
        renderer.renderBoxes(cam, result.pos(), lines, result.data(), () -> {
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor(0, 0, 0, 0.4F);
            RenderSystem.lineWidth(4);
        });
        renderer.renderBoxes(cam, result.pos(), lines, result.data(), () -> {
            RenderSystem.disableDepthTest();
            RenderSystem.setShaderColor(ColorUtils.redF(color), ColorUtils.greenF(color), ColorUtils.blueF(color), 0.4F);
            RenderSystem.lineWidth(2);
        });
        
        RenderSystem.enableDepthTest();
    }
    
    @Override
    public boolean render(PreviewRenderer renderer, PoseStack pose, Vec3 cam, boolean lines) {
        if (!lines)
            return active;
        
        var blockHit = renderer.blockHit();
        var player = renderer.player();
        var level = renderer.level();
        var stack = renderer.manager.tool().stack;
        
        if (active) {
            if (first != null)
                positions.add(first);
            if (last != null)
                positions.add(last);
            renderer.renderPositions(pose, cam, positions, x -> markedPosition == x);
            if (first != null)
                positions.removeLast();
            if (last != null)
                positions.removeLast();
            
            if (first != null && last != null) {
                first.sameGrid(last, () -> {
                    var boxes = new LittleBoxesSimple(first.getPos(), first.getGrid());
                    LittleBox box = LittleBox.ofNothing();
                    box.growToIncludePixel(first.getRelative(boxes.pos));
                    box.growToIncludePixel(last.getRelative(boxes.pos));
                    boxes.add(box);
                    var result = renderer.buildBoxes(pose, boxes, lines);
                    renderBoxes(renderer, cam, lines, result);
                    result.close();
                });
            }
        }
        
        if (blockHit != null)
            last = new ShapePosition(player, PlacementHelper.getPosition(level, blockHit, ((ILittleTool) stack.getItem()).getPositionGrid(player,
                stack)), blockHit, false, inside());
        
        if (result == null && boxes != null && !boxes.isEmpty())
            result = renderer.buildBoxes(pose, boxes, lines);
        
        if (boxes != null && result != null)
            renderBoxes(renderer, cam, lines, result);
        
        return active; // Disable standard rendering during selection mode.
    }
    
    protected void ensurePositionsGrid() {
        int smallest = LittleGrid.MIN.count;
        for (int i = 0; i < positions.size(); i++)
            smallest = Math.max(smallest, positions.get(i).getGrid().count);
        
        LittleGrid grid = LittleGrid.get(smallest);
        for (int i = 0; i < positions.size(); i++)
            positions.get(i).convertTo(grid);
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
        
        ensurePositionsGrid();
        
        boxes = new LittleBoxesSimple(positions.getFirst().getPos(), positions.getFirst().getGrid());
        for (int i = 0; i < positions.size(); i += 2) {
            LittleBox box = LittleBox.ofNothing();
            box.growToIncludePixel(positions.get(i).getRelative(boxes.pos));
            box.growToIncludePixel(positions.get(i + 1).getRelative(boxes.pos));
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
                if (doubleClick) {
                    positions.clear();
                    first = null;
                } else {
                    int index = LittleTilesClient.PREVIEW_RENDERER.renderer.select(positions);
                    if (index >= 0) {
                        index /= 2;
                        index *= 2;
                        positions.remove(index);
                        positions.remove(index);
                    }
                }
            else
                markedPosition = LittleTilesClient.PREVIEW_RENDERER.renderer.select(positions);
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
        if (!active || action != InputConstants.PRESS)
            return false;
        
        if (keyCode == InputConstants.KEY_ESCAPE) {
            end();
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
        active = false;
        removeCache();
    }
    
}
