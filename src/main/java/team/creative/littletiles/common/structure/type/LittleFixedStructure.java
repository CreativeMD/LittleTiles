package team.creative.littletiles.common.structure.type;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import com.creativemd.littletiles.common.tile.preview.LittlePreviews;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
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
    protected void loadFromNBTExtra(CompoundTag nbt) {}
    
    @Override
    protected void writeToNBTExtra(CompoundTag nbt) {}
    
    public static class LittleFixedStructureParser extends LittleStructureGuiParser {
        
        public LittleFixedStructureParser(GuiParent parent, AnimationGuiHandler handler) {
            super(parent, handler);
        }
        
        @Override
        public void createControls(LittlePreviews previews, LittleStructure structure) {
            
        }
        
        @Override
        public LittleStructure parseStructure(LittlePreviews previews) {
            return createStructure(LittleFixedStructure.class, null);
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        protected LittleStructureType getStructureType() {
            return LittleStructureRegistry.getStructureType(LittleFixedStructure.class);
        }
    }
}
