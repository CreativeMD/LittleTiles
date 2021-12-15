package com.creativemd.littletiles.common.structure.type;

import com.creativemd.creativecore.common.gui.CoreControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.animation.AnimationGuiHandler;
import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.structure.registry.LittleStructureGuiParser;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.signal.output.InternalSignalOutput;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.parent.IStructureTileList;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleLight extends LittleStructure {
    
    public int level;
    public boolean disableRightClick = false;
    
    public LittleLight(LittleStructureType type, IStructureTileList mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected void loadFromNBTExtra(NBTTagCompound nbt) {
        level = nbt.getInteger("level");
        disableRightClick = nbt.getBoolean("disableRightClick");
    }
    
    @Override
    protected void writeToNBTExtra(NBTTagCompound nbt) {
        nbt.setInteger("level", level);
        nbt.setBoolean("disableRightClick", disableRightClick);
    }
    
    @Override
    public int getLightValue(BlockPos pos) {
        return getOutput(0).getState()[0] ? level : 0;
    }
    
    @Override
    public boolean onBlockActivated(World world, LittleTile tile, BlockPos pos, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ, LittleActionActivated action) throws LittleActionException {
        if (!world.isRemote && !disableRightClick)
            getOutput(0).toggle();
        return true;
    }
    
    @Override
    public void performInternalOutputChange(InternalSignalOutput output) {
        if (output.component.is("enabled")) {
            World world = getWorld();
            try {
                tryAttributeChangeForBlocks();
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        }
    }
    
    @Override
    public int getAttribute() {
        if (getOutput(0).getState()[0])
            return super.getAttribute() | LittleStructureAttribute.EMISSIVE;
        return super.getAttribute();
    }
    
    public static class LittleLightStructureParser extends LittleStructureGuiParser {
        
        public LittleLightStructureParser(GuiParent parent, AnimationGuiHandler handler) {
            super(parent, handler);
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public void createControls(LittlePreviews previews, LittleStructure structure) {
            parent.addControl(new GuiSteppedSlider("level", 0, 0, 100, 12, structure instanceof LittleLight ? ((LittleLight) structure).level : 15, 0, 15));
            parent.addControl(new GuiCheckBox("rightclick", CoreControl
                .translate("gui.door.rightclick"), 0, 20, structure instanceof LittleLight ? !((LittleLight) structure).disableRightClick : true));
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public LittleLight parseStructure(LittlePreviews previews) {
            LittleLight structure = createStructure(LittleLight.class, null);
            GuiSteppedSlider slider = (GuiSteppedSlider) parent.get("level");
            GuiCheckBox rightclick = (GuiCheckBox) parent.get("rightclick");
            structure.level = (int) slider.value;
            structure.disableRightClick = !rightclick.value;
            return structure;
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        protected LittleStructureType getStructureType() {
            return LittleStructureRegistry.getStructureType(LittleLight.class);
        }
    }
    
}
