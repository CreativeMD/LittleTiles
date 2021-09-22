package com.creativemd.littletiles.common.structure.type;

import java.util.List;

import javax.vecmath.Vector3f;

import com.creativemd.creativecore.common.utils.math.BooleanUtils;
import com.creativemd.creativecore.common.utils.math.box.AlignedBox;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.client.render.tile.LittleRenderBox;
import com.creativemd.littletiles.client.render.tile.LittleRenderBoxItem;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.directional.StructureDirectional;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.parent.IStructureTileList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.structure.registry.LittleStructureType;
import team.creative.littletiles.common.structure.relative.StructureRelative;

public class LittleItemHolder extends LittleStructure {
    
    @StructureDirectional(color = ColorUtils.CYAN)
    public StructureRelative frame;
    
    @StructureDirectional
    public EnumFacing facing;
    
    @StructureDirectional
    public Vector3f topRight;
    
    public ItemStack stack;
    
    public LittleItemHolder(LittleStructureType type, IStructureTileList mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    protected void loadFromNBTExtra(NBTTagCompound nbt) {
        stack = new ItemStack(nbt.getCompoundTag("stack"));
    }
    
    @Override
    protected void writeToNBTExtra(NBTTagCompound nbt) {
        nbt.setTag("stack", stack.writeToNBT(new NBTTagCompound()));
    }
    
    @Override
    public boolean onBlockActivated(World worldIn, LittleTile tile, BlockPos pos, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ, LittleActionActivated action) throws LittleActionException {
        if (worldIn.isRemote)
            return true;
        ItemStack mainStack = playerIn.getHeldItemMainhand();
        if (mainStack.isEmpty() && !stack.isEmpty()) {
            playerIn.replaceItemInInventory(playerIn.inventory.currentItem, stack.copy());
            stack = ItemStack.EMPTY;
            updateInput();
            updateStructure();
        } else if (stack.isEmpty()) {
            playerIn.replaceItemInInventory(playerIn.inventory.currentItem, ItemStack.EMPTY);
            stack = mainStack.copy();
            updateInput();
            updateStructure();
        }
        return true;
    }
    
    public void updateInput() {
        getInput(0).updateState(BooleanUtils.asArray(!stack.isEmpty()));
    }
    
    @Override
    public void getRenderingCubes(BlockPos pos, BlockRenderLayer layer, List<LittleRenderBox> cubes) {
        if (layer == BlockRenderLayer.CUTOUT) {
            AlignedBox box = frame.getBox().getCube(frame.getContext());
            if (!stack.isEmpty())
                cubes.add(new LittleRenderBoxItem(this, box, frame.getBox()));
        }
    }
    
}
