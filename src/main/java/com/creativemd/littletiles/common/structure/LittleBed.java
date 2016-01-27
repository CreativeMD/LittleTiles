package com.creativemd.littletiles.common.structure;

import java.util.Iterator;

import com.creativemd.creativecore.common.gui.SubGui;
import com.creativemd.littletiles.common.utils.LittleTile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

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
	public boolean isBed(IBlockAccess world, int x, int y, int z, EntityLivingBase player)
	{
		return true;
	}
	
	@Override
	public boolean onBlockActivated(World world, LittleTile tile, int x, int y, int z, EntityPlayer player, int side, float moveX, float moveY, float moveZ)
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

        if (world.provider.canRespawnHere() && world.getBiomeGenForCoords(x, z) != BiomeGenBase.hell)
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

            EntityPlayer.EnumStatus enumstatus = player.sleepInBedAt(x, y, z);

            if (enumstatus == EntityPlayer.EnumStatus.OK)
            {
                //func_149979_a(world, x, y, z, true);
                return true;
            }
            else
            {
                if (enumstatus == EntityPlayer.EnumStatus.NOT_POSSIBLE_NOW)
                {
                    player.addChatComponentMessage(new ChatComponentTranslation("tile.bed.noSleep", new Object[0]));
                }
                else if (enumstatus == EntityPlayer.EnumStatus.NOT_SAFE)
                {
                	player.addChatComponentMessage(new ChatComponentTranslation("tile.bed.notSafe", new Object[0]));
                }

                return true;
            }
	}

}
