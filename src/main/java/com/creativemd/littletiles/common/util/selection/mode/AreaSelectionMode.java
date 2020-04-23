package com.creativemd.littletiles.common.util.selection.mode;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.mod.chiselsandbits.ChiselsAndBitsManager;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class AreaSelectionMode extends SelectionMode {
	
	public AreaSelectionMode() {
		super("area");
	}
	
	@Override
	public SelectionResult generateResult(World world, ItemStack stack) {
		BlockPos pos = null;
		if (stack.getTagCompound().hasKey("pos1")) {
			int[] array = stack.getTagCompound().getIntArray("pos1");
			pos = new BlockPos(array[0], array[1], array[2]);
		}
		
		BlockPos pos2 = null;
		if (stack.getTagCompound().hasKey("pos2")) {
			int[] array = stack.getTagCompound().getIntArray("pos2");
			pos2 = new BlockPos(array[0], array[1], array[2]);
		}
		
		if (pos == null && pos2 == null)
			return null;
		
		if (pos == null)
			pos = pos2;
		else if (pos2 == null)
			pos2 = pos;
		
		SelectionResult result = new SelectionResult(world);
		result.addBlocks(pos, pos2);
		return result;
	}
	
	@Override
	public void onLeftClick(EntityPlayer player, ItemStack stack, BlockPos pos) {
		stack.getTagCompound().setIntArray("pos1", new int[] { pos.getX(), pos.getY(), pos.getZ() });
		if (!player.world.isRemote)
			player.sendMessage(new TextComponentTranslation("selection.mode.area.pos.first", pos.getX(), pos.getY(), pos.getZ()));
	}
	
	@Override
	public void onRightClick(EntityPlayer player, ItemStack stack, BlockPos pos) {
		stack.getTagCompound().setIntArray("pos2", new int[] { pos.getX(), pos.getY(), pos.getZ() });
		if (!player.world.isRemote)
			player.sendMessage(new TextComponentTranslation("selection.mode.area.pos.second", pos.getX(), pos.getY(), pos.getZ()));
	}
	
	@Override
	public void clearSelection(ItemStack stack) {
		stack.getTagCompound().removeTag("pos1");
		stack.getTagCompound().removeTag("pos2");
	}
	
	@Override
	public LittlePreviews getPreviews(World world, ItemStack stack, boolean includeVanilla, boolean includeCB, boolean includeLT, boolean rememberStructure) {
		
		BlockPos pos = null;
		if (stack.getTagCompound().hasKey("pos1")) {
			int[] array = stack.getTagCompound().getIntArray("pos1");
			pos = new BlockPos(array[0], array[1], array[2]);
		}
		
		BlockPos pos2 = null;
		if (stack.getTagCompound().hasKey("pos2")) {
			int[] array = stack.getTagCompound().getIntArray("pos2");
			pos2 = new BlockPos(array[0], array[1], array[2]);
		}
		
		if (pos == null && pos2 == null)
			return null;
		
		if (pos == null)
			pos = pos2;
		else if (pos2 == null)
			pos2 = pos;
		
		int minX = Math.min(pos.getX(), pos2.getX());
		int minY = Math.min(pos.getY(), pos2.getY());
		int minZ = Math.min(pos.getZ(), pos2.getZ());
		int maxX = Math.max(pos.getX(), pos2.getX());
		int maxY = Math.max(pos.getY(), pos2.getY());
		int maxZ = Math.max(pos.getZ(), pos2.getZ());
		
		LittlePreviews previews = new LittlePreviews(LittleGridContext.getMin());
		
		boolean includeTE = includeCB || includeLT;
		
		BlockPos center = new BlockPos(minX, minY, minZ);
		MutableBlockPos newPos = new MutableBlockPos();
		
		List<LittleStructure> structures = null;
		if (rememberStructure)
			structures = new ArrayList<>();
		
		for (int posX = minX; posX <= maxX; posX++) {
			for (int posY = minY; posY <= maxY; posY++) {
				for (int posZ = minZ; posZ <= maxZ; posZ++) {
					newPos.setPos(posX, posY, posZ);
					if (includeTE) {
						TileEntity tileEntity = world.getTileEntity(newPos);
						
						if (includeLT) {
							if (tileEntity instanceof TileEntityLittleTiles) {
								TileEntityLittleTiles te = (TileEntityLittleTiles) tileEntity;
								for (LittleTile tile : te) {
									if (rememberStructure && tile.isConnectedToStructure()) {
										LittleStructure structure = tile.connection.getStructure(world);
										while (structure.parent != null) {
											if (!structure.parent.isConnected(world))
												continue;
											structure = structure.parent.getStructure(world);
										}
										
										if (structure.load() && structure.loadChildren() && !structures.contains(structure)) {
											previews.addChild(structure.getPreviews(center));
											structures.add(structure);
										}
									} else {
										LittlePreview preview = previews.addPreview(null, tile.getPreviewTile(), te.getContext());
										preview.box.add(new LittleVec((posX - minX) * previews.getContext().size, (posY - minY) * previews.getContext().size, (posZ - minZ) * previews.getContext().size));
									}
								}
								continue;
							}
						}
						
						if (includeCB) {
							LittlePreviews specialPreviews = ChiselsAndBitsManager.getPreviews(tileEntity);
							if (specialPreviews != null) {
								for (int i = 0; i < specialPreviews.size(); i++) {
									LittlePreview preview = previews.addPreview(null, specialPreviews.get(i), LittleGridContext.get(ChiselsAndBitsManager.convertingFrom));
									preview.box.add(new LittleVec((posX - minX) * previews.getContext().size, (posY - minY) * previews.getContext().size, (posZ - minZ) * previews.getContext().size));
								}
								continue;
							}
						}
					}
					
					if (includeVanilla) {
						IBlockState state = world.getBlockState(newPos);
						if (LittleAction.isBlockValid(state)) {
							LittleTile tile = new LittleTile(state.getBlock(), state.getBlock().getMetaFromState(state));
							tile.box = new LittleBox(0, 0, 0, LittleGridContext.getMin().size, LittleGridContext.getMin().size, LittleGridContext.getMin().size);
							LittlePreview preview = previews.addPreview(null, tile.getPreviewTile(), LittleGridContext.getMin());
							preview.box.add(new LittleVec((posX - minX) * previews.getContext().size, (posY - minY) * previews.getContext().size, (posZ - minZ) * previews.getContext().size));
						}
					}
				}
			}
		}
		return previews;
	}
	
}
