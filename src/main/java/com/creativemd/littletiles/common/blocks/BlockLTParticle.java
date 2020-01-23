package com.creativemd.littletiles.common.blocks;

import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.gui.container.SubContainer;
import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.opener.GuiHandler;
import com.creativemd.creativecore.common.gui.opener.IGuiCreator;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.gui.SubGuiParticle;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.container.SubContainerParticle;
import com.creativemd.littletiles.common.tile.LittleTileParticle;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.tileentity.TileEntityParticle;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockLTParticle extends BlockContainer implements IGuiCreator, ILittleTile {
	
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
		if (te instanceof TileEntityParticle)
			return new SubGuiParticle((TileEntityParticle) te);
		return null;
	}
	
	@Override
	public SubContainer getContainer(EntityPlayer player, ItemStack stack, World world, BlockPos pos, IBlockState state) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityParticle)
			return new SubContainerParticle(player, (TileEntityParticle) te);
		return null;
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!worldIn.isRemote)
			GuiHandler.openGui(playerIn, worldIn, pos);
		return true;
	}
	
	@Override
	public LittlePreviews getLittlePreview(ItemStack stack) {
		LittlePreviews previews = new LittlePreviews(LittleGridContext.get());
		NBTTagCompound nbt = new NBTTagCompound();
		LittleTileParticle particle = new LittleTileParticle(LittleTiles.particleBlock, 0, new TileEntityParticle());
		particle.box = new LittleBox(0, 0, 0, 1, 1, 1);
		previews.addWithoutCheckingPreview(particle.getPreviewTile());
		return previews;
	}
	
	@Override
	public void rotateLittlePreview(EntityPlayer player, ItemStack stack, Rotation rotation) {
		
	}
	
	@Override
	public void flipLittlePreview(EntityPlayer player, ItemStack stack, Axis axis) {
	}
	
	@Override
	public void saveLittlePreview(ItemStack stack, LittlePreviews previews) {
		
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
		tooltip.add("Particles will not spawn if you are holding the wrench.");
	}
	
	@Override
	public boolean hasLittlePreview(ItemStack stack) {
		return true;
	}
	
	@Override
	public boolean containsIngredients(ItemStack stack) {
		return true;
	}
	
}
