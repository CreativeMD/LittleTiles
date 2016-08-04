package com.creativemd.littletiles.common.structure;

import javax.annotation.Nullable;

import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.littletiles.common.utils.LittleTile;

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
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createControls(SubGui gui, LittleStructure structure) {
		// TODO Auto-generated method stub
		
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
	public boolean onBlockActivated(World world, LittleTile tile, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (world.isRemote)
            return true;

        /*if (!isBlockHeadOfBed(i1))
        {
            int j1 = getDirection(i1);
            p_149727_2_ += field_149981_a[j1][0];
            p_149727_4_ += field_149981_a[j1][1];

            if (world.getBlock(x, y, z) != this)
            {
                return true;
            }

            i1 = world.getBlockMetadata(x, y, z);
        }*/

        if (world.provider.canRespawnHere() && world.getBiomeGenForCoords(pos) != Biomes.HELL)
        {
            /*if (func_149976_c(i1))
            {
                EntityPlayer entityplayer1 = null;
                Iterator iterator = world.playerEntities.iterator();

                while (iterator.hasNext())
                {
                    EntityPlayer entityplayer2 = (EntityPlayer)iterator.next();

                    if (entityplayer2.isPlayerSleeping())
                    {
                        ChunkCoordinates chunkcoordinates = entityplayer2.playerLocation;

                        if (chunkcoordinates.posX == x && chunkcoordinates.posY == y && chunkcoordinates.posZ == z)
                        {
                            entityplayer1 = entityplayer2;
                        }
                    }
                }

                if (entityplayer1 != null)
                {
                    player.addChatComponentMessage(new ChatComponentTranslation("tile.bed.occupied", new Object[0]));
                    return true;
                }
				*/
                //func_149979_a(world, x, y, z, false);
            }

            SleepResult enumstatus = player.trySleep(pos);

            if (enumstatus == SleepResult.OK)
            {
                //func_149979_a(world, x, y, z, true);
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

}
