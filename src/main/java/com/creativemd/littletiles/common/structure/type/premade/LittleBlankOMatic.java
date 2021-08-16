package com.creativemd.littletiles.common.structure.type.premade;

import com.creativemd.creativecore.common.utils.mc.InventoryUtils;
import com.creativemd.littletiles.client.gui.handler.LittleStructureGuiHandler;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.parent.IStructureTileList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import team.creative.littletiles.common.action.LittleActionException;

public class LittleBlankOMatic extends LittleStructurePremade {
    
    public InventoryBasic inventory;
    public int whiteColor;
    
    public LittleBlankOMatic(LittleStructureType type, IStructureTileList mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected void loadFromNBTExtra(NBTTagCompound nbt) {
        inventory = InventoryUtils.loadInventoryBasic(nbt, 1);
        whiteColor = nbt.getInteger("white");
    }
    
    @Override
    protected void writeToNBTExtra(NBTTagCompound nbt) {
        InventoryUtils.saveInventoryBasic(inventory);
        nbt.setInteger("white", whiteColor);
    }
    
    @Override
    public boolean onBlockActivated(World worldIn, LittleTile tile, BlockPos pos, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ, LittleActionActivated action) throws LittleActionException {
        LittleStructureGuiHandler.openGui("blankomatic", new NBTTagCompound(), playerIn, this);
        return true;
    }
    
}
