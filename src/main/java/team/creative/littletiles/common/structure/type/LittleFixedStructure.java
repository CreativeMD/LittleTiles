package team.creative.littletiles.common.structure.type;

import net.minecraft.nbt.CompoundTag;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.registry.LittleStructureGuiParser;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;

public class LittleFixedStructure extends LittleStructure {
    
    public LittleFixedStructure(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt) {}
    
    @Override
    protected void saveExtra(CompoundTag nbt) {}
    
    public static class LittleFixedStructureParser extends LittleStructureGuiParser {
        
        public LittleFixedStructureParser(GuiParent parent, AnimationGuiHandler handler) {
            super(parent, handler);
        }
        
        @Override
        public void createControls(LittleGroup previews, LittleStructure structure) {}
        
        @Override
        public LittleStructure parseStructure(LittleGroup previews) {
            return createStructure(LittleFixedStructure.class, null);
        }
        
        @Override
        protected LittleStructureType getStructureType() {
            return LittleStructureRegistry.getStructureType(LittleFixedStructure.class);
        }
    }
}
