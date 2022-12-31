package team.creative.littletiles.common.structure.registry.gui;

import javax.annotation.Nullable;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.animation.preview.AnimationPreview;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.gui.controls.IAnimationControl;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;

@OnlyIn(Dist.CLIENT)
public abstract class LittleStructureGuiControl extends GuiParent implements IAnimationControl {
    
    public final LittleStructureType type;
    public final AnimationGuiHandler handler;
    
    public LittleStructureGuiControl(LittleStructureType type, AnimationGuiHandler handler) {
        this.type = type;
        this.handler = handler;
    }
    
    @Override
    public GuiParent getParent() {
        return (GuiParent) super.getParent();
    }
    
    public void create(LittleGroup group, @Nullable LittleStructure structure) {
        createExtra(group, structure);
        //add(new GuiSignalEventsButton("signal", group, structure, type));
    }
    
    protected abstract void createExtra(LittleGroup group, @Nullable LittleStructure structure);
    
    public LittleStructure save(LittleGroup group) {
        LittleStructure structure = type.createStructure(null);
        saveExtra(structure, group);
        //get("signal", GuiSignalEventsButton.class).setEventsInStructure(structure);
        return structure;
    }
    
    protected abstract void saveExtra(LittleStructure structure, LittleGroup previews);
    
    @Override
    public void onLoaded(AnimationPreview animationPreview) {}
    
}
