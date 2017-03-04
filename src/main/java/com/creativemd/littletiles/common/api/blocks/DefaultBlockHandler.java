package com.creativemd.littletiles.common.api.blocks;

import javax.annotation.Nullable;

import com.creativemd.littletiles.common.entity.EntitySizedTNTPrimed;
import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;

import net.minecraft.block.BlockTNT;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class DefaultBlockHandler {
	
	public static void initVanillaBlockHandlers()
	{
		SpecialBlockHandler.registerSpecialHandler(BlockTNT.class, new SpecialBlockHandler() {
			
			@Override
			public boolean onBlockActivated(LittleTileBlock tile, World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
			{
				if (heldItem != null && (heldItem.getItem() == Items.FLINT_AND_STEEL || heldItem.getItem() == Items.FIRE_CHARGE))
		        {
		            if (!worldIn.isRemote && !tile.boundingBoxes.isEmpty())
		            {
		            	explodeTile(tile, playerIn, false);
		            }
		            tile.destroy();
		            //worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);

		            if (heldItem.getItem() == Items.FLINT_AND_STEEL)
		            {
		                heldItem.damageItem(1, playerIn);
		            }
		            else if (!playerIn.capabilities.isCreativeMode)
		            {
		                heldItem.stackSize--;
		            }

		            return true;
		        }
				return false;
			}
			
			@Override
			public void onTileExplodes(LittleTileBlock tile, Explosion explosion)
			{
				explodeTile(tile, explosion.getExplosivePlacedBy(), true);
			}
			
			public void explodeTile(LittleTileBlock tile, EntityLivingBase entity, boolean randomFuse)
			{
				BlockPos pos = tile.te.getPos();
				LittleTileSize size = tile.boundingBoxes.get(0).getSize();
		        EntitySizedTNTPrimed entitytntprimed = new EntitySizedTNTPrimed(tile.te.getWorld(), (double)((float)pos.getX() + tile.cornerVec.getPosX()/2 + size.getPosX()/2), (double)(pos.getY() + tile.cornerVec.getPosY()/2 + size.getPosY()/2), (double)((float)pos.getZ() + tile.cornerVec.getPosZ()/2 + size.getPosZ()/2), entity, size);
		        if(randomFuse)
		        	entitytntprimed.setFuse((short)(tile.te.getWorld().rand.nextInt(entitytntprimed.getFuse() / 4) + entitytntprimed.getFuse() / 8));
		        tile.te.getWorld().spawnEntityInWorld(entitytntprimed);
		        tile.te.getWorld().playSound((EntityPlayer)null, entitytntprimed.posX, entitytntprimed.posY, entitytntprimed.posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
			}
			
		});
		
		SpecialBlockHandler.registerSpecialHandler(Blocks.CRAFTING_TABLE, new SpecialBlockHandler() {
			
			@Override
			public boolean onBlockActivated(LittleTileBlock tile, World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
			{
				if (worldIn.isRemote)
		        {
		            return true;
		        }
		        else
		        {
		            playerIn.displayGui(new BlockWorkbench.InterfaceCraftingTable(worldIn, pos){
		            	
		            	@Override
		            	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn)
		                {
		                    return new ContainerWorkbench(playerInventory, worldIn, pos){
		                    	
		                    	@Override
		                    	public boolean canInteractWith(EntityPlayer playerIn)
		                        {
		                    		return true;
		                        }
		                    };
		                }
		            });
		            playerIn.addStat(StatList.CRAFTING_TABLE_INTERACTION);
		            return true;
		        }
			}
			
		});
	}
	
}
