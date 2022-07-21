package team.creative.littletiles.common.structure.registry;

import javax.annotation.Nullable;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.animation.preview.AnimationPreview;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.StructureParentCollection;
import team.creative.littletiles.common.gui.controls.IAnimationControl;
import team.creative.littletiles.common.gui.dialogs.SubGuiSignalEvents.GuiSignalEventsButton;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;

@OnlyIn(Dist.CLIENT)
public abstract class LittleStructureGuiParser implements IAnimationControl {
    
    public final GuiParent parent;
    public final AnimationGuiHandler handler;
    
    public LittleStructureGuiParser(GuiParent parent, AnimationGuiHandler handler) {
        this.parent = parent;
        this.handler = handler;
    }
    
    public void create(LittleGroup previews, @Nullable LittleStructure structure) {
        createControls(previews, structure);
        parent.add(new GuiSignalEventsButton("signal", previews, structure, getStructureType()));
        
    }
    
    public LittleStructure parse(LittleGroup previews) {
        LittleStructure structure = parseStructure(previews);
        GuiSignalEventsButton button = (GuiSignalEventsButton) parent.get("signal");
        button.setEventsInStructure(structure);
        return structure;
    }
    
    protected abstract void createControls(LittleGroup previews, @Nullable LittleStructure structure);
    
    protected abstract LittleStructure parseStructure(LittleGroup previews);
    
    protected abstract LittleStructureType getStructureType();
    
    public <T extends LittleStructure> T createStructure(Class<T> structureClass, StructureParentCollection parent) {
        LittleStructureType type = LittleStructureRegistry.getStructureType(structureClass);
        if (type == null)
            throw new RuntimeException("Could find structure for " + structureClass);
        return (T) type.createStructure(parent);
    }
    
    @Override
    public void onLoaded(AnimationPreview animationPreview) {}
    
    public static abstract class LittleStructureGuiParserNotFoundHandler {
        
        public abstract LittleStructureGuiParser create(LittleStructure structure, GuiParent parent, AnimationGuiHandler handler);
        
    }
    
}
