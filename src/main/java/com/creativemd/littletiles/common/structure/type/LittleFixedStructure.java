package com.creativemd.littletiles.common.structure.type;

import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.animation.AnimationGuiHandler;
import com.creativemd.littletiles.common.tile.parent.IStructureTileList;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.littletiles.common.structure.registry.LittleStructureGuiParser;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.registry.LittleStructureType;

public class LittleFixedStructure extends LittleStructure {
    
    public LittleFixedStructure(LittleStructureType type, IStructureTileList mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected void loadFromNBTExtra(NBTTagCompound nbt) {
        
    }
    
    @Override
    protected void writeToNBTExtra(NBTTagCompound nbt) {
        
    }
    
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
