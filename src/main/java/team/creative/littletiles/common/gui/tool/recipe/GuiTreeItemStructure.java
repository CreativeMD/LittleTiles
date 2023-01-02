package team.creative.littletiles.common.gui.tool.recipe;

import java.util.concurrent.CompletableFuture;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.controls.tree.GuiTree;
import team.creative.creativecore.common.gui.controls.tree.GuiTreeItem;
import team.creative.littletiles.common.animation.preview.AnimationPreview;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
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
        LittleStructureGui gui = recipe.types.getSelected();
        recipe.control = gui.create(this);
        recipe.config.clear();
        recipe.config.add(recipe.control);
        recipe.control.create(group, structure);
        recipe.config.init();
        recipe.reflow();
    }
    
    public void save() {
        structure = recipe.control.save(group);
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
