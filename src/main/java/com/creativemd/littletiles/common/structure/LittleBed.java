package com.creativemd.littletiles.common.structure;

import javax.annotation.Nullable;

import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;

import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.SleepResult;
import net.minecraft.init.Biomes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeManager.BiomeType;

public class LittleBed extends LittleStructure{

	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		
	}

	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		
	}

	@Override
	public void createControls(SubGui gui, LittleStructure structure) {
		
	}

	@Override
	public LittleStructure parseStructure(SubGui gui) {
		return new LittleBed();
	}
	
	@Override
	public boolean isBed(IBlockAccess world, BlockPos pos, EntityLivingBase player)
	{
		return true;
	}
	
	@Override
	public EnumFacing getBedDirection(IBlockState state, IBlockAccess world, BlockPos pos)
    {
		LittleTileSize size = getSize();
		if(size.sizeZ > size.sizeX)
			return EnumFacing.EAST;
		return EnumFacing.SOUTH;
    }
	
	@Override
	public boolean onBlockActivated(World world, LittleTile tile, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (world.isRemote)
            return true;

        if (world.provider.canRespawnHere() && world.getBiomeGenForCoords(pos) != Biomes.HELL)
        {
        	LittleTileVec vec = getHighestCenterPoint();
        	LittleTiles.blockTile.sleepingTile = mainTile;
            SleepResult enumstatus = player.trySleep(pos);
            LittleTiles.blockTile.sleepingTile = null;
            
            if (enumstatus == SleepResult.OK)
            {
                return true;
            }
            else
            {
                if (enumstatus == SleepResult.NOT_POSSIBLE_NOW)
                {
                    player.addChatComponentMessage(new TextComponentTranslation("tile.bed.noSleep"));
                }
                else if (enumstatus == SleepResult.NOT_SAFE)
                {
                	player.addChatComponentMessage(new TextComponentTranslation("tile.bed.notSafe"));
                }

                return true;
            }
        }
        return true;
	}

}
