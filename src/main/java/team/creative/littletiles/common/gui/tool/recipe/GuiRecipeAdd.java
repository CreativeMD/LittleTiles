package team.creative.littletiles.common.gui.tool.recipe;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
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
import team.creative.littletiles.common.api.tool.ILittlePlacer;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.structure.LittleStructureType;

public class GuiRecipeAdd extends GuiLayer {
    
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
    
    public GuiRecipeAdd() {
        super("recipe.add", 300, 200);
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
            get("save").setEnabled(true);
            return;
        }
        
        get("save").setEnabled(false);
    }
    
    @Override
    public void create() {
        GuiParent upper = new GuiParent();
        add(upper.setExpandable());
        
        GuiScrollY items = new GuiScrollY();
        upper.add(items);
        
        List<ItemStack> stacks = collectTiles();
        for (ItemStack stack : stacks)
            items.add(new GuiRecipeAddEntry(stack, getPlayer()));
        
        GuiTree tree = new GuiTree("tree").setRootVisibility(false).setCheckboxes(true);
        upper.add(tree.setDim(new GuiSizeRatioRules().widthRatio(0.3F).maxWidth(100)).setExpandableY());
        
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
        return new LittleGroup(null, LittleGrid.min(), children);
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
    
    public static class GuiRecipeAddTreeItem extends GuiTreeItem {
        
        public final LittleGroup group;
        
        public GuiRecipeAddTreeItem(GuiTree tree, LittleGroup group) {
            super("item", tree);
            this.group = group;
            this.setTitle(Component.literal(generateGroupName(group)));
        }
        
    }
    
}
