package team.creative.littletiles.common.gui.tool.recipe;

import java.util.concurrent.CompletableFuture;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.gui.controls.tree.GuiTree;
import team.creative.creativecore.common.gui.controls.tree.GuiTreeItem;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.littletiles.common.animation.preview.AnimationPreview;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.gui.signal.GuiSignalEventsButton;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.registry.gui.LittleStructureGui;
import team.creative.littletiles.common.structure.registry.gui.LittleStructureGuiRegistry;

public class GuiTreeItemStructure extends GuiTreeItem {
    
    private GuiRecipe recipe;
    public final LittleGroup group;
    public LittleStructure structure;
    private LittleVecGrid offset;
    private int index;
    public LittleStructureGui gui;
    
    public GuiTreeItemStructure(String name, GuiRecipe recipe, GuiTree tree, LittleGroup group, int index) {
        super(name, tree);
        this.recipe = recipe;
        this.group = group;
        if (group.hasStructure()) {
            this.structure = group.getStructureType().createStructure(null);
            this.structure.load(group.getStructureTag());
        }
        this.index = index;
        refreshAnimation();
        updateTitle();
    }
    
    @Override
    protected void select() {
        super.select();
        updateTitle();
        recipe.types.select(LittleStructureGuiRegistry.get(structure != null ? structure.type : null, group));
    }
    
    public void load() {
        gui = recipe.types.getSelected();
        recipe.control = gui.create(this);
        recipe.control.setExpandableY();
        recipe.config.clear();
        recipe.config.add(recipe.control);
        recipe.control.create(structure);
        recipe.config.init();
        GuiParent parent = new GuiParent("bottomStructure", GuiFlow.STACK_X);
        recipe.config.add(parent);
        GuiTextfield text = new GuiTextfield("name");
        if (structure != null && structure.name != null)
            text.setText(structure.name);
        else
            text.setText("");
        parent.add(text.setEnabled(gui.supportsName()).setDim(100, 7));
        parent.add(new GuiSignalEventsButton("signal", this).setEnabled(gui.type() != null));
        recipe.reflow();
    }
    
    public void save() {
        LittleStructureType type = recipe.types.getSelected().type();
        structure = recipe.control.save(type != null ? type.createStructure(null) : null);
        if (structure != null) {
            GuiParent parent = recipe.config.get("bottomStructure");
            GuiTextfield textfield = parent.get("name");
            structure.name = textfield.getText().isBlank() ? null : textfield.getText();
        }
        recipe.config.get("bottomStructure", GuiParent.class).get("signal", GuiSignalEventsButton.class).setEventsInStructure(structure);
        updateTitle();
    }
    
    @Override
    protected void deselect() {
        super.deselect();
        updateTitle();
        save();
    }
    
    public void updateTitle() {
        int index = getParentItem() != null ? getParentItem().indexOf(this) : this.index;
        String name = structure != null ? structure.name : null;
        boolean hasStructureName = true;
        if (name == null) {
            hasStructureName = false;
            LittleStructureType type = structure != null ? structure.type : null;
            if (type != null)
                name = type.id + " " + index;
            else
                name = "none " + index;
        }
        
        if (selected())
            name = "<" + name + ">";
        
        if (hasStructureName)
            name = ChatFormatting.ITALIC + "" + name;
        
        setTitle(Component.literal(name));
    }
    
    @OnlyIn(Dist.CLIENT)
    private void refreshAnimation() {
        CompletableFuture.supplyAsync(() -> new AnimationPreview(group)).whenComplete((preview, throwable) -> {
            recipe.availablePreviews.put(this, preview);
            if (throwable != null)
                throwable.printStackTrace();
        });
    }
    
    @Override
    public void removed() {
        super.removed();
        recipe.availablePreviews.remove(this);
    }
    
}
