package com.creativemd.littletiles.common.api.blocks;

import javax.annotation.Nullable;

import com.creativemd.littletiles.common.entity.EntitySizedTNTPrimed;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;

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
		SpecialBlockHandler.registerSpecialHandler(BlockTNT.class, new ISpecialBlockHandler() {
			
			@Override
			public boolean onBlockActivated(LittleTileBlock tile, World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
			{
				if (heldItem != null && (heldItem.getItem() == Items.FLINT_AND_STEEL || heldItem.getItem() == Items.FIRE_CHARGE))
		        {
		            if (!worldIn.isRemote)
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
		                heldItem.shrink(1);;
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
				LittleTileSize size = tile.box.getSize();
				LittleTileVec min = tile.box.getMinVec();
		        EntitySizedTNTPrimed entitytntprimed = new EntitySizedTNTPrimed(tile.te.getWorld(), (double)((float)pos.getX() + min.getPosX() + size.getPosX()/2), (double)(pos.getY() + min.getPosY() + size.getPosY()/2), (double)((float)pos.getZ() + min.getPosZ() + size.getPosZ()/2), entity, size);
		        if(randomFuse)
		        	entitytntprimed.setFuse((short)(tile.te.getWorld().rand.nextInt(entitytntprimed.getFuse() / 4) + entitytntprimed.getFuse() / 8));
		        tile.te.getWorld().spawnEntity(entitytntprimed);
		        tile.te.getWorld().playSound((EntityPlayer)null, entitytntprimed.posX, entitytntprimed.posY, entitytntprimed.posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
			}
			
		});
		
		SpecialBlockHandler.registerSpecialHandler(Blocks.CRAFTING_TABLE, new ISpecialBlockHandler() {
			
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
