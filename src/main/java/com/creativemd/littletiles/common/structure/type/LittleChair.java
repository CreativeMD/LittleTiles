package com.creativemd.littletiles.common.structure.type;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.entity.EntitySit;
import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.registry.LittleStructureGuiParser;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.vec.LittleTilePos;
import com.creativemd.littletiles.common.utils.animation.AnimationGuiHandler;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleChair extends LittleStructure {
	
	public LittleChair(LittleStructureType type) {
		super(type);
	}
	
	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		
	}
	
	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		
	}
	
	@Override
	public boolean onBlockActivated(World world, LittleTile tile, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ, LittleActionActivated action) {
		if (!world.isRemote) {
			LittleTilePos vec = getHighestCenterPoint();
			if (vec != null) {
				EntitySit sit = new EntitySit(world, vec.getPosX(), vec.getPosY() - 0.25, vec.getPosZ());
				player.startRiding(sit);
				world.spawnEntity(sit);
			}
			
		}
		return true;
	}
	
	public static class LittleChairParser extends LittleStructureGuiParser {
		
		public LittleChairParser(GuiParent parent, AnimationGuiHandler handler) {
			super(parent, handler);
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public void createControls(LittlePreviews previews, LittleStructure structure) {
			
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public LittleStructure parseStructure(LittlePreviews previews) {
			return createStructure(LittleChair.class);
		}
	}
}
