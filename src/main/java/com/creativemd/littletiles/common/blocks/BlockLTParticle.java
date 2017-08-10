package com.creativemd.littletiles.common.blocks;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.opener.GuiHandler;
import com.creativemd.creativecore.gui.opener.IGuiCreator;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.action.block.NotEnoughIngredientsException;
import com.creativemd.littletiles.common.container.SubContainerParticle;
import com.creativemd.littletiles.common.gui.SubGuiParticle;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityParticle;
import com.creativemd.littletiles.common.tiles.LittleTileTE;
import com.creativemd.littletiles.common.tiles.advanced.LittleTileParticle;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockLTParticle extends BlockContainer implements IGuiCreator ,ILittleTile {

	public BlockLTParticle() {
		super(Material.IRON);
		setHardness(0.4F);
		setCreativeTab(LittleTiles.littleTab);
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

	@Override
	public ArrayList<LittleTilePreview> getLittlePreview(ItemStack stack) {
		ArrayList<LittleTilePreview> previews = new ArrayList<>();
		NBTTagCompound nbt = new NBTTagCompound();
		new LittleTileParticle(LittleTiles.particleBlock, 0, new TileEntityParticle()).saveTile(nbt);;
		nbt.removeTag("bSize");
		LittleTilePreview preview = new LittleTilePreview(new LittleTileSize(1, 1, 1), nbt);
		previews.add(preview);
		return previews;
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
	}

	@Override
	public void saveLittlePreview(ItemStack stack, List<LittleTilePreview> previews) {
		
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced)
    {
		tooltip.add("Particles will not spawn if you are holding the wrench.");
    }

	@Override
	public boolean hasLittlePreview(ItemStack stack) {
		return true;
	}

}
