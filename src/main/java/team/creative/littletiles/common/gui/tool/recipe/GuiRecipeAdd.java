package team.creative.littletiles.common.gui.tool.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.controls.parent.GuiLeftRightBox;
import team.creative.creativecore.common.gui.controls.parent.GuiPanel;
import team.creative.creativecore.common.gui.controls.parent.GuiScrollY;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.controls.simple.GuiShowItem;
import team.creative.creativecore.common.gui.controls.tree.GuiTree;
import team.creative.creativecore.common.gui.controls.tree.GuiTreeItem;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.flow.GuiSizeRule.GuiSizeRatioRules;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.api.common.tool.ILittlePlacer;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.gui.AnimationPreview;
import team.creative.littletiles.common.gui.controls.animation.GuiAnimationPanel;
import team.creative.littletiles.common.gui.controls.animation.GuiAnimationViewerStorage;
import team.creative.littletiles.common.structure.LittleStructureType;

public class GuiRecipeAdd extends GuiLayer implements GuiAnimationViewerStorage {
    
    public static String generateGroupName(LittleGroup group) {
        String name = group.getStructureName();
        if (name != null)
            return name;
        
        LittleStructureType type = group.getStructureType();
        if (type != null)
            return type.id;
        return "none";
    }
    
    public GuiRecipe recipe;
    private ItemStack selected;
    
    private volatile int requestedPreview = 0;
    private volatile AnimationPreview current;
    private volatile int executedPreview = 0;
    private AtomicReference<GuiRecipeAddAnimationRequest> scheduled = new AtomicReference<>();
    
    public GuiRecipeAdd() {
        super("recipe.add", 400, 200);
        flow = GuiFlow.STACK_Y;
    }
    
    public void init(GuiRecipe recipe) {
        this.recipe = recipe;
        clear();
        init();
    }
    
    private List<ItemStack> collectTiles() {
        List<ItemStack> recipes = new ArrayList<>();
        Inventory inv = getPlayer().getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof ILittlePlacer placer && placer.hasTiles(stack)) {
                LittleGroup group = placer.getTiles(stack);
                if (group == null || group.isEmptyIncludeChildren())
                    continue;
                recipes.add(stack);
            }
        }
        return recipes;
    }
    
    private void addGroup(GuiTreeItem parent, LittleGroup group) {
        GuiRecipeAddTreeItem item = new GuiRecipeAddTreeItem(parent.tree, group);
        parent.addItem(item);
        
        for (LittleGroup child : group.children.children())
            addGroup(item, child);
    }
    
    public void setAnimation(int index, AnimationPreview preview) {
        synchronized (scheduled) {
            if (requestedPreview == -1 || requestedPreview > index) {
                if (preview != null)
                    preview.unload();
            } else
                scheduled.set(new GuiRecipeAddAnimationRequest(preview, index));
        }
    }
    
    public void select(ItemStack stack) {
        if (this.selected == stack)
            return;
        
        this.selected = stack;
        GuiTree tree = get("tree");
        tree.root().clearItems();
        
        ILittlePlacer placer = (ILittlePlacer) stack.getItem();
        if (placer.hasTiles(stack)) {
            LittleGroup group = placer.getTiles(stack);
            
            if (group.isEmpty())
                for (LittleGroup child : group.children.children())
                    addGroup(tree.root(), child);
            else
                addGroup(tree.root(), group);
            
            tree.updateTree();
            synchronized (scheduled) {
                requestedPreview++;
            }
            final int request = requestedPreview;
            CompletableFuture.supplyAsync(() -> {
                try {
                    return new AnimationPreview(group);
                } catch (LittleActionException e) {
                    throw new RuntimeException(e);
                }
            }).whenComplete((preview, throwable) -> {
                setAnimation(request, preview);
                if (throwable != null)
                    throwable.printStackTrace();
            });
            get("save").setEnabled(true);
            return;
        }
        synchronized (scheduled) {
            requestedPreview++;
        }
        setAnimation(requestedPreview, null);
        get("save").setEnabled(false);
    }
    
    @Override
    public void create() {
        GuiParent upper = new GuiParent();
        add(upper.setExpandable());
        
        GuiScrollY items = new GuiScrollY();
        upper.add(items.setDim(new GuiSizeRatioRules().widthRatio(0.3F).maxWidth(150)));
        
        List<ItemStack> stacks = collectTiles();
        for (ItemStack stack : stacks)
            items.add(new GuiRecipeAddEntry(stack, getPlayer()));
        
        GuiTree tree = new GuiTree("tree").setRootVisibility(false).setCheckboxes(true, true);
        upper.add(tree.setDim(new GuiSizeRatioRules().widthRatio(0.3F).maxWidth(100)).setExpandableY());
        
        upper.add(new GuiAnimationPanel(tree, this, false, null).setExpandable());
        
        GuiLeftRightBox bottom = new GuiLeftRightBox();
        add(bottom);
        
        bottom.addLeft(new GuiButton("cancel", x -> closeThisLayer()).setTranslate("gui.cancel"));
        bottom.addLeft(new GuiButton("reload", x -> {
            this.selected = null;
            clear();
            init();
        }).setTranslate("gui.reload"));
        bottom.addRight(new GuiButton("save", x -> {
            if (selected == null)
                return;
            
            GuiTreeItem parent = recipe.tree.selected();
            if (parent == null)
                parent = recipe.tree.root();
            
            LittleGroup group = reconstructBlueprint();
            if (group == null || group.isEmptyIncludeChildren())
                return;
            recipe.buildStructureTree(recipe.tree, parent, group, parent.itemsCount());
            
            closeThisLayer();
            
            recipe.tree.updateTree();
        }).setTranslate("gui.import").setEnabled(false));
    }
    
    @Override
    public void render(PoseStack pose, GuiChildControl control, Rect controlRect, Rect realRect, double scale, int mouseX, int mouseY) {
        synchronized (scheduled) {
            GuiRecipeAddAnimationRequest request = scheduled.getAndSet(null);
            if (request != null && executedPreview < request.index) {
                if (current != null)
                    current.unload();
                current = request.preview;
                executedPreview = request.index;
            }
        }
        super.render(pose, control, controlRect, realRect, scale, mouseX, mouseY);
    }
    
    protected LittleGroup reconstructBlueprint(GuiRecipeAddTreeItem item) {
        List<LittleGroup> children = new ArrayList<>();
        for (GuiTreeItem child : item.itemsChecked())
            children.add(reconstructBlueprint((GuiRecipeAddTreeItem) child));
        return new LittleGroup(item.group.getStructureTag(), item.group.copyExceptChildren(), children);
    }
    
    protected LittleGroup reconstructBlueprint() {
        GuiTree tree = get("tree");
        if (tree.root().itemsCount() == 1)
            return reconstructBlueprint((GuiRecipeAddTreeItem) tree.root().items().iterator().next());
        List<LittleGroup> children = new ArrayList<>();
        for (GuiTreeItem child : tree.root().itemsChecked())
            children.add(reconstructBlueprint((GuiRecipeAddTreeItem) child));
        if (children.isEmpty())
            return null;
        return new LittleGroup((CompoundTag) null, children);
    }
    
    @Override
    public void closed() {
        super.closed();
        requestedPreview = -1;
        if (current != null)
            current.unload();
    }
    
    public class GuiRecipeAddEntry extends GuiPanel {
        
        public final ItemStack stack;
        
        public GuiRecipeAddEntry(ItemStack stack, Player player) {
            this.stack = stack;
            flow = GuiFlow.STACK_X;
            
            add(new GuiShowItem("show", stack).setDim(40, 40));
            GuiParent right = new GuiParent(GuiFlow.STACK_Y);
            add(right.setVAlign(VAlign.CENTER).setExpandable());
            
            for (Component line : stack.getTooltipLines(player, TooltipFlag.NORMAL))
                right.add(new GuiLabel("label").setTitle(line));
        }
        
        @Override
        public boolean mouseClicked(Rect rect, double x, double y, int button) {
            select(stack);
            playSound(SoundEvents.UI_BUTTON_CLICK);
            return true;
        }
        
    }
    
    @Override
    public boolean isReady() {
        return current != null;
    }
    
    @Override
    public double longestSide() {
        return Math.max(current.box.maxX - current.box.minX, Math.max(current.box.maxY - current.box.minY, current.box.maxZ - current.box.minZ));
    }
    
    @Override
    public AABB overall() {
        return current.box;
    }
    
    @Override
    public Vec3d center() {
        return new Vec3d(current.box.getCenter());
    }
    
    @Override
    public boolean highlightSelected() {
        return false;
    }
    
    @Override
    public void highlightSelected(boolean value) {}
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public Iterable<AnimationPreview> previewsToRender() {
        return Collections.EMPTY_LIST;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderAll(PoseStack pose, Matrix4f projection, Minecraft mc) {
        renderPreview(pose, projection, current, mc);
    }
    
    public static class GuiRecipeAddTreeItem extends GuiTreeItem {
        
        public final LittleGroup group;
        
        public GuiRecipeAddTreeItem(GuiTree tree, LittleGroup group) {
            super("item", tree);
            this.group = group;
            this.setTitle(Component.literal(generateGroupName(group)));
        }
        
    }
    
    private static record GuiRecipeAddAnimationRequest(AnimationPreview preview, int index) {}
    
}
