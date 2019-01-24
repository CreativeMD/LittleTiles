package com.creativemd.littletiles.common.action.block;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.creativecore.common.utils.type.HashMapList;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionCombined;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.config.SpecialServerConfig;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.LittleTileBlockColored;
import com.creativemd.littletiles.common.tiles.vec.LittleBoxes;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.ingredients.BlockIngredient;
import com.creativemd.littletiles.common.utils.ingredients.ColorUnit;
import com.creativemd.littletiles.common.utils.selection.selector.TileSelector;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;

public class LittleActionColorBoxes extends LittleActionBoxes {
	
	public int color;
	public boolean toVanilla;
	
	public LittleActionColorBoxes(LittleBoxes boxes, int color, boolean toVanilla) {
		super(boxes);
		this.color = color;
		this.toVanilla = toVanilla;
	}
	
	public LittleActionColorBoxes() {
		
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		super.writeBytes(buf);
		buf.writeInt(color);
		buf.writeBoolean(toVanilla);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		super.readBytes(buf);
		color = buf.readInt();
		toVanilla = buf.readBoolean();
	}
	
	public HashMapList<Integer, LittleBoxes> revertList;
	
	public void addRevert(int color, BlockPos pos, LittleGridContext context, List<LittleTileBox> boxes) {
		LittleBoxes newBoxes = new LittleBoxes(pos, context);
		for (LittleTileBox box : boxes) {
			newBoxes.add(box.copy());
		}
		revertList.add(color, newBoxes);
	}
	
	public boolean shouldSkipTile(LittleTile tile) {
		return false;
	}
	
	public boolean doneSomething;
	
	public ColorUnit action(TileEntityLittleTiles te, List<LittleTileBox> boxes, ColorUnit gained, boolean simulate, LittleGridContext context) {
		doneSomething = false;
		double colorVolume = 0;
		
		for (Iterator<LittleTile> iterator = te.getTiles().iterator(); iterator.hasNext();) {
			LittleTile tile = iterator.next();
			
			if (shouldSkipTile(tile))
				continue;
			
			LittleTileBox intersecting = null;
			boolean intersects = false;
			for (int j = 0; j < boxes.size(); j++) {
				if (tile.intersectsWith(boxes.get(j))) {
					intersects = true;
					intersecting = boxes.get(j);
					break;
				}
			}
			
			if (!intersects || !(tile.getClass() == LittleTileBlock.class || tile instanceof LittleTileBlockColored) || (tile.isConnectedToStructure() && (!tile.isConnectedToStructure() || !tile.connection.getStructure(te.getWorld()).hasLoaded())))
				continue;
			
			if (!LittleTileBlockColored.needsToBeRecolored((LittleTileBlock) tile, color))
				continue;
			
			doneSomething = true;
			
			if (tile.canBeSplitted() && !tile.equalsBox(intersecting)) {
				if (simulate) {
					double volume = 0;
					List<LittleTileBox> cutout = new ArrayList<>();
					tile.cutOut(boxes, cutout);
					for (LittleTileBox box2 : cutout) {
						colorVolume += box2.getPercentVolume(context);
						volume += box2.getPercentVolume(context);
					}
					
					gained.addColorUnit(ColorUnit.getColors(tile.getPreviewTile(), volume));
					
				} else {
					List<LittleTileBox> cutout = new ArrayList<>();
					List<LittleTileBox> newBoxes = tile.cutOut(boxes, cutout);
					
					if (newBoxes != null) {
						addRevert(LittleTileBlockColored.getColor((LittleTileBlock) tile), te.getPos(), context, cutout);
						
						LittleTile tempTile = tile.copy();
						LittleTile changedTile = LittleTileBlockColored.setColor((LittleTileBlock) tempTile, color);
						if (changedTile == null)
							changedTile = tempTile;
						
						if (tile.isConnectedToStructure())
							tile.connection.getStructure(te.getWorld()).removeTile(tile);
						
						for (int i = 0; i < newBoxes.size(); i++) {
							LittleTile newTile = tile.copy();
							newTile.box = newBoxes.get(i);
							newTile.place();
							if (tile.isConnectedToStructure())
								tile.connection.getStructure(te.getWorld()).addTile(newTile);
						}
						
						for (int i = 0; i < cutout.size(); i++) {
							LittleTile newTile = changedTile.copy();
							newTile.box = cutout.get(i);
							newTile.place();
							if (tile.isConnectedToStructure())
								tile.connection.getStructure(te.getWorld()).addTile(newTile);
						}
						
						if (tile.isConnectedToStructure()) {
							if (tile.connection.isLink())
								tile.connection.getStructure(te.getWorld()).updateStructure();
							else
								tile.connection.getStructureWithoutLoading().selectMainTile();
						}
						
						tile.connection = null;
						tile.destroy();
					}
				}
			} else {
				if (simulate) {
					colorVolume += tile.getPercentVolume();
					gained.addColorUnit(ColorUnit.getColors(tile.getPreviewTile(), tile.getPercentVolume()));
				} else {
					List<LittleTileBox> oldBoxes = new ArrayList<>();
					oldBoxes.add(tile.box);
					
					addRevert(LittleTileBlockColored.getColor((LittleTileBlock) tile), te.getPos(), context, oldBoxes);
					
					LittleTile changedTile = LittleTileBlockColored.setColor((LittleTileBlock) tile, color);
					if (changedTile != null) {
						changedTile.place();
						
						if (tile.isChildOfStructure()) {
							changedTile.connection = tile.connection.copy(changedTile);
							LittleStructure structure = tile.connection.getStructure(te.getWorld());
							structure.removeTile(tile);
							structure.addTile(changedTile);
							structure.updateStructure();
							
							if (!tile.connection.isLink())
								structure.setMainTile(changedTile);
						}
						
						tile.connection = null;
						tile.destroy();
					}
				}
			}
		}
		ColorUnit toDrain = ColorUnit.getColors(color);
		toDrain.scale(colorVolume);
		
		gained.drain(toDrain);
		
		return toDrain;
	}
	
	@Override
	public void action(World world, EntityPlayer player, BlockPos pos, IBlockState state, List<LittleTileBox> boxes, LittleGridContext context) throws LittleActionException {
		if (ColorUtils.getAlpha(color) < SpecialServerConfig.getMinimumTransparency(player))
			throw new SpecialServerConfig.NotAllowedToPlaceColorException();
		
		TileEntity tileEntity = loadTe(player, world, pos, true);
		
		if (tileEntity instanceof TileEntityLittleTiles) {
			if (!world.isRemote) {
				BreakEvent event = new BreakEvent(world, tileEntity.getPos(), ((TileEntityLittleTiles) tileEntity).getBlockTileState(), player);
				MinecraftForge.EVENT_BUS.post(event);
				if (event.isCanceled()) {
					sendBlockResetToClient((EntityPlayerMP) player, pos, (TileEntityLittleTiles) tileEntity);
					return;
				}
			}
			
			TileEntityLittleTiles te = (TileEntityLittleTiles) tileEntity;
			
			te.ensureMinContext(context);
			
			if (context != te.getContext()) {
				for (LittleTileBox box : boxes) {
					box.convertTo(context, te.getContext());
				}
				context = te.getContext();
			}
			
			List<BlockIngredient> entries = new ArrayList<>();
			
			te.preventUpdate = true;
			
			ColorUnit gained = new ColorUnit();
			
			ColorUnit toDrain = action(te, boxes, gained, true, context);
			
			if (addIngredients(player, null, gained, true)) {
				drainIngredients(player, null, toDrain);
				addIngredients(player, null, gained);
				
				action(te, boxes, gained, false, context);
			}
			
			te.preventUpdate = false;
			
			te.combineTiles();
			
			if (toVanilla || !doneSomething)
				te.convertBlockToVanilla();
		}
	}
	
	@Override
	protected boolean action(EntityPlayer player) throws LittleActionException {
		revertList = new HashMapList<>();
		return super.action(player);
	}
	
	@Override
	public boolean canBeReverted() {
		return true;
	}
	
	@Override
	public LittleAction revert() {
		List<LittleAction> actions = new ArrayList<>();
		for (Entry<Integer, ArrayList<LittleBoxes>> entry : revertList.entrySet()) {
			for (LittleBoxes boxes : entry.getValue()) {
				boxes.convertToSmallest();
				actions.add(new LittleActionColorBoxes(boxes, entry.getKey(), true));
			}
		}
		return new LittleActionCombined(actions.toArray(new LittleAction[0]));
	}
	
	public static class LittleActionColorBoxesFiltered extends LittleActionColorBoxes {
		
		public TileSelector selector;
		
		public LittleActionColorBoxesFiltered(LittleBoxes boxes, int color, boolean toVanilla, TileSelector selector) {
			super(boxes, color, toVanilla);
			this.selector = selector;
		}
		
		public LittleActionColorBoxesFiltered() {
			
		}
		
		@Override
		public void writeBytes(ByteBuf buf) {
			super.writeBytes(buf);
			writeSelector(selector, buf);
		}
		
		@Override
		public void readBytes(ByteBuf buf) {
			super.readBytes(buf);
			selector = readSelector(buf);
		}
		
		@Override
		public boolean shouldSkipTile(LittleTile tile) {
			return !selector.is(tile);
		}
	}
}
