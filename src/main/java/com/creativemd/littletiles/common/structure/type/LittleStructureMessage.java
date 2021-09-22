package com.creativemd.littletiles.common.structure.type;

import com.creativemd.creativecore.common.gui.CoreControl;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTextfield;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.animation.AnimationGuiHandler;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.parent.IStructureTileList;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.structure.registry.LittleStructureGuiParser;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.registry.LittleStructureType;
import team.creative.littletiles.common.structure.signal.output.InternalSignalOutput;

public class LittleStructureMessage extends LittleStructure {
    
    public String text;
    public boolean allowRightClick = true;
    
    public LittleStructureMessage(LittleStructureType type, IStructureTileList mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    public boolean onBlockActivated(World worldIn, LittleTile tile, BlockPos pos, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ, LittleActionActivated action) throws LittleActionException {
        if (allowRightClick) {
            playerIn.sendStatusMessage(new TextComponentString(text), true);
            return true;
        }
        return false;
    }
    
    @Override
    protected void loadFromNBTExtra(NBTTagCompound nbt) {
        text = nbt.getString("text");
        allowRightClick = nbt.getBoolean("right");
    }
    
    @Override
    protected void writeToNBTExtra(NBTTagCompound nbt) {
        nbt.setString("text", text);
        nbt.setBoolean("right", allowRightClick);
    }
    
    @Override
    public void performInternalOutputChange(InternalSignalOutput output) {
        if (output.component.is("message")) {
            World world = getWorld();
            if (world.isRemote)
                return;
            PlayerChunkMapEntry entry = ((WorldServer) world).getPlayerChunkMap().getEntry(getPos().getX() >> 4, getPos().getZ() >> 4);
            if (entry != null)
                for (EntityPlayerMP player : entry.getWatchingPlayers())
                    player.sendStatusMessage(new TextComponentString(text), true);
        }
    }
    
    public static class LittleMessageStructureParser extends LittleStructureGuiParser {
        
        public LittleMessageStructureParser(GuiParent parent, AnimationGuiHandler handler) {
            super(parent, handler);
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public void createControls(LittlePreviews previews, LittleStructure structure) {
            parent
                .addControl(new GuiTextfield("text", structure instanceof LittleStructureMessage ? ((LittleStructureMessage) structure).text : "Hello World!", 0, 0, 140, 14));
            parent.controls.add(new GuiCheckBox("rightclick", CoreControl
                .translate("gui.door.rightclick"), 0, 20, structure instanceof LittleStructureMessage ? ((LittleStructureMessage) structure).allowRightClick : true));
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public LittleStructureMessage parseStructure(LittlePreviews previews) {
            LittleStructureMessage structure = createStructure(LittleStructureMessage.class, null);
            GuiTextfield text = (GuiTextfield) parent.get("text");
            structure.text = text.text;
            GuiCheckBox box = (GuiCheckBox) parent.get("rightclick");
            structure.allowRightClick = box.value;
            return structure;
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        protected LittleStructureType getStructureType() {
            return LittleStructureRegistry.getStructureType(LittleStructureMessage.class);
        }
    }
    
}
