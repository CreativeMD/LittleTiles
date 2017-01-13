package com.creativemd.littletiles.common.blocks;

import java.util.ArrayList;

import javax.annotation.Nullable;

import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.opener.GuiHandler;
import com.creativemd.creativecore.gui.opener.IGuiCreator;
import com.creativemd.littletiles.common.gui.SubContainerParticle;
import com.creativemd.littletiles.common.gui.SubGuiParticle;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityParticle;
import com.creativemd.littletiles.common.utils.LittleTilePreview;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockLTParticle extends BlockContainer implements IGuiCreator /*,ILittleTile*/ {

	public BlockLTParticle() {
		super(Material.IRON);
		setHardness(0.4F);
		setCreativeTab(CreativeTabs.DECORATIONS);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityParticle();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public SubGui getGui(EntityPlayer player, ItemStack stack, World world, BlockPos pos, IBlockState state) {
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityParticle)
			return new SubGuiParticle((TileEntityParticle) te);
		return null;
	}

	@Override
	public SubContainer getContainer(EntityPlayer player, ItemStack stack, World world, BlockPos pos,
			IBlockState state) {
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityParticle)
			return new SubContainerParticle(player, (TileEntityParticle) te);
		return null;
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
		if(!worldIn.isRemote)
			GuiHandler.openGui(playerIn, worldIn, pos);
		return true;
    }

	/*@Override
	public ArrayList<LittleTilePreview> getLittlePreview(ItemStack stack) {
		return null;
	}

	@Override
	public void rotateLittlePreview(ItemStack stack, EnumFacing facing) {
		
	}

	@Override
	public void flipLittlePreview(ItemStack stack, EnumFacing facing) {
		
	}

	@Override
	public LittleStructure getLittleStructure(ItemStack stack) {
		return null;
	}*/

}
