package com.creativemd.littletiles.common.util.selection.mode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.mod.chiselsandbits.ChiselsAndBitsManager;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public abstract class SelectionMode {
	
	private static LinkedHashMap<String, SelectionMode> modes = new LinkedHashMap<>();
	
	public static SelectionMode getMode(String id) {
		return modes.get(id);
	}
	
	public static SelectionMode getOrDefault(String id) {
		return modes.getOrDefault(id, area);
	}
	
	public static List<String> names() {
		return new ArrayList<>(modes.keySet());
	}
	
	public static SelectionMode area = new AreaSelectionMode();
	// public static SelectionMode individual = new SelectionMode("individual");
	
	public final String name;
	
	public SelectionMode(String name) {
		this.name = "mode.selection." + name;
		modes.put(this.name, this);
	}
	
	public abstract SelectionResult generateResult(World world, ItemStack stack);
	
	public abstract void onLeftClick(EntityPlayer player, ItemStack stack, BlockPos pos);
	
	public abstract void onRightClick(EntityPlayer player, ItemStack stack, BlockPos pos);
	
	public abstract void clearSelection(ItemStack stack);
	
	public abstract LittlePreviews getPreviews(World world, ItemStack stack, boolean includeVanilla, boolean includeCB, boolean includeLT, boolean rememberStructure);
	
	public void saveSelection(ItemStack stack) {}
	
	public static class SelectionResult {
		
		public final World world;
		
		public SelectionResult(World world) {
			this.world = world;
		}
		
		private void addBlockDirectly(BlockPos pos) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityLittleTiles) {
				ltBlocks++;
				ltTiles += ((TileEntityLittleTiles) te).size();
				if (minLtContext == null)
					minLtContext = ((TileEntityLittleTiles) te).getContext();
				else
					minLtContext = LittleGridContext.max(minLtContext, ((TileEntityLittleTiles) te).getContext());
			}
			
			LittlePreviews specialPreviews = ChiselsAndBitsManager.getPreviews(te);
			if (specialPreviews != null) {
				cbBlocks++;
				cbTiles += specialPreviews.size();
				if (minCBContext == null)
					minCBContext = specialPreviews.getContext();
				else
					minCBContext = LittleGridContext.max(minCBContext, specialPreviews.getContext());
			}
			
			IBlockState state = world.getBlockState(pos);
			if (LittleAction.isBlockValid(state))
				blocks++;
		}
		
		public void addBlock(BlockPos pos) {
			if (min == null) {
				min = new MutableBlockPos(pos);
				max = new MutableBlockPos(pos);
			} else {
				min.setPos(Math.min(min.getX(), pos.getX()), Math.min(min.getY(), pos.getY()), Math.min(min.getZ(), pos.getZ()));
				max.setPos(Math.max(max.getX(), pos.getX()), Math.max(max.getY(), pos.getY()), Math.max(max.getZ(), pos.getZ()));
			}
			addBlockDirectly(pos);
		}
		
		public void addBlocks(BlockPos pos, BlockPos pos2) {
			int minX = Math.min(pos.getX(), pos2.getX());
			int minY = Math.min(pos.getY(), pos2.getY());
			int minZ = Math.min(pos.getZ(), pos2.getZ());
			int maxX = Math.max(pos.getX(), pos2.getX());
			int maxY = Math.max(pos.getY(), pos2.getY());
			int maxZ = Math.max(pos.getZ(), pos2.getZ());
			
			if (min == null) {
				min = new MutableBlockPos(minX, minY, minZ);
				max = new MutableBlockPos(maxX, maxY, maxZ);
			} else {
				min.setPos(Math.min(min.getX(), minX), Math.min(min.getY(), minY), Math.min(min.getZ(), minZ));
				max.setPos(Math.max(max.getX(), minX), Math.max(max.getY(), minY), Math.max(max.getZ(), minZ));
			}
			
			MutableBlockPos mutPos = new MutableBlockPos();
			for (int posX = minX; posX <= maxX; posX++)
				for (int posY = minY; posY <= maxY; posY++)
					for (int posZ = minZ; posZ <= maxZ; posZ++)
						addBlockDirectly(mutPos.setPos(posX, posY, posZ));
		}
		
		public MutableBlockPos min = null;
		public MutableBlockPos max = null;
		
		public Vec3i getSize() {
			return new Vec3i(max.getX() - min.getX(), max.getY() - min.getY(), max.getZ() - min.getZ());
		}
		
		public int blocks;
		
		public int ltBlocks = 0;
		public int ltTiles = 0;
		public LittleGridContext minLtContext = null;
		
		public int cbBlocks = 0;
		public int cbTiles = 0;
		public LittleGridContext minCBContext = null;
		
	}
	
}