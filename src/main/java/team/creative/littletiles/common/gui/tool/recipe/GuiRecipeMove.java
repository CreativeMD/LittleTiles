package team.creative.littletiles.common.gui.tool.recipe;

import java.util.Collections;
import java.util.Iterator;

import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.parent.GuiLeftRightBox;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButtonMapped;
import team.creative.creativecore.common.gui.controls.tree.GuiTree;
import team.creative.creativecore.common.gui.controls.tree.GuiTreeItem;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.flow.GuiSizeRule.GuiSizeRatioRules;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.creativecore.common.util.type.itr.FunctionIterator;
import team.creative.creativecore.common.util.type.itr.TreeIterator;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.controls.GuiLTDistance;
import team.creative.littletiles.common.gui.controls.GuiAnimationPanel;
import team.creative.littletiles.common.gui.tool.recipe.test.RecipeTest;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecGrid;

public class GuiRecipeMove extends GuiLayer {
    
    public GuiRecipe recipe;
    public GuiTree tree;
    
    public GuiRecipeMove() {
        super("gui.recipe.move", 400, 200);
        flow = GuiFlow.STACK_Y;
        registerEventChanged(x -> {
            if (x.control.is("modes"))
                ((GuiStateButtonMapped<GuiRecipeMoveMode>) x.control).getSelected().select(tree);
        });
    }
    
    public void init(GuiRecipe recipe) {
        this.recipe = recipe;
        clear();
        init();
    }
    
    @Override
    public void closed() {
        if (tree == null)
            return;
        for (GuiTreeItem item : (Iterable<GuiTreeItem>) () -> tree.allItems())
            if (item instanceof GuiRecipeMoveItem move)
                move.structure.resetOffset();
    }
    
    @Override
    public void create() {
        if (recipe == null)
            return;
        
        GuiParent upper = new GuiParent(GuiFlow.STACK_X);
        add(upper);
        
        GuiParent sidebar = new GuiParent(GuiFlow.STACK_Y);
        upper.add(sidebar.setDim(new GuiSizeRatioRules().widthRatio(0.3F).maxWidth(100)).setExpandableY());
        
        tree = new GuiTree("tree").setRootVisibility(false);
        sidebar.add(tree.setExpandable());
        
        for (GuiTreeItem item : recipe.tree.root().items()) {
            GuiRecipeMoveItem child = new GuiRecipeMoveItem(tree, (GuiTreeItemStructure) item);
            tree.root().addItem(child);
            child.addChildren();
        }
        
        GuiStateButtonMapped<GuiRecipeMoveMode> modes = new GuiStateButtonMapped<>("modes", new TextMapBuilder<GuiRecipeMoveMode>()
                .addComponent(GuiRecipeMoveMode.values(), x -> x.title()));
        sidebar.add(modes.setExpandableX());
        
        GuiParent config = new GuiParent(GuiFlow.STACK_Y).setAlign(Align.CENTER);
        upper.add(config.setDim(new GuiSizeRatioRules().widthRatio(0.3F)));
        
        config.add(new GuiLTDistance("distance", LittleGrid.defaultGrid(), 1));
        
        GuiParent row1 = new GuiParent();
        config.add(row1);
        row1.add(new GuiRecipeMoveButton(Facing.EAST));
        row1.add(new GuiRecipeMoveButton(Facing.UP));
        row1.add(new GuiRecipeMoveButton(Facing.SOUTH));
        
        GuiParent row2 = new GuiParent();
        config.add(row2);
        row2.add(new GuiRecipeMoveButton(Facing.WEST));
        row2.add(new GuiRecipeMoveButton(Facing.DOWN));
        row2.add(new GuiRecipeMoveButton(Facing.NORTH));
        
        upper.add(new GuiAnimationPanel(recipe.tree, recipe.storage, true));
        
        GuiLeftRightBox bottom = new GuiLeftRightBox();
        add(bottom);
        bottom.addLeft(new GuiButton("cancel", x -> closeThisLayer()).setTranslate("gui.cancel"));
        bottom.addRight(new GuiButton("test", x -> {
            recipe.storage.resetOverlap();
            RecipeTest.testModule(recipe, RecipeTest.OVERLAP_TEST);
        }).setTranslate("gui.recipe.test.overlap"));
        bottom.addRight(new GuiButton("save", x -> {
            for (GuiTreeItem item : (Iterable<GuiTreeItem>) () -> tree.allItems())
                if (item instanceof GuiRecipeMoveItem move)
                    move.structure.applyOffset();
            closeThisLayer();
        }).setTranslate("gui.save"));
        
        tree.updateTree();
    }
    
    public void move(Facing facing) {
        GuiLTDistance distance = get("distance");
        GuiStateButtonMapped<GuiRecipeMoveMode> modes = get("modes");
        LittleVecGrid vec = new LittleVecGrid(new LittleVec(facing), distance.getDistanceGrid());
        for (GuiRecipeMoveItem item : (Iterable<GuiRecipeMoveItem>) () -> modes.getSelected().iterator(tree))
            item.addOffset(vec);
    }
    
    public class GuiRecipeMoveButton extends GuiButton {
        
        public GuiRecipeMoveButton(Facing facing) {
            super(facing.name, x -> move(facing));
            setTitle(facing.translate());
        }
        
    }
    
    public enum GuiRecipeMoveMode {
        
        DEFAULT(false) {
            
            @Override
            public Iterator<GuiRecipeMoveItem> iterator(GuiTree tree) {
                if (tree.selected() == null)
                    return Collections.emptyIterator();
                return new TreeIterator<>((GuiRecipeMoveItem) tree.selected(), x -> new FunctionIterator<GuiRecipeMoveItem>(x.items(), y -> (GuiRecipeMoveItem) y));
            }
        },
        CHECKBOX(true) {
            @Override
            public Iterator<GuiRecipeMoveItem> iterator(GuiTree tree) {
                return new FunctionIterator<>(tree.root().itemsChecked(), x -> (GuiRecipeMoveItem) x);
            }
        };
        
        public final boolean checkboxes;
        
        private GuiRecipeMoveMode(boolean checkboxes) {
            this.checkboxes = checkboxes;
        }
        
        public Component title() {
            return Component.translatable("gui.recipe.move.mode." + name().toLowerCase());
        }
        
        public void select(GuiTree tree) {
            tree.setCheckboxes(checkboxes, false);
            tree.updateTree();
        }
        
        public abstract Iterator<GuiRecipeMoveItem> iterator(GuiTree tree);
    }
    
    public class GuiRecipeMoveItem extends GuiTreeItem {
        
        public final GuiTreeItemStructure structure;
        
        public GuiRecipeMoveItem(GuiTree tree, GuiTreeItemStructure structure) {
            super("item", tree);
            this.structure = structure;
            setTitle(Component.literal(structure.getTitle()));
        }
        
        public void addOffset(LittleVecGrid vec) {
            LittleVecGrid offset = structure.getOffset();
            if (offset == null)
                structure.setOffset(vec.copy());
            else
                offset.add(vec);
        }
        
        public void addChildren() {
            for (GuiTreeItem item : structure.items()) {
                GuiRecipeMoveItem child = new GuiRecipeMoveItem(tree, (GuiTreeItemStructure) item);
                addItem(child);
                child.addChildren();
            }
        }
        
    }
    
}
