package com.creativemd.littletiles.common.structure.connection;

import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StructureLinkTile extends StructureLinkBaseRelative<LittleTile> {
	
	public StructureLinkTile(TileEntity te, BlockPos coord, LittleGridContext context, int[] identifier, LittleStructureAttribute attribute, LittleTile parent) {
		super(te, coord, context, identifier, attribute, parent);
	}
	
	public StructureLinkTile(BlockPos origin, BlockPos coord, LittleGridContext context, int[] identifier, LittleStructureAttribute attribute, LittleTile parent) {
		super(origin, coord, context, identifier, attribute, parent);
	}
	
	public StructureLinkTile(int baseX, int baseY, int baseZ, BlockPos coord, LittleGridContext context, int[] identifier, LittleStructureAttribute attribute, LittleTile parent) {
		super(baseX, baseY, baseZ, coord, context, identifier, attribute, parent);
	}
	
	public StructureLinkTile(NBTTagCompound nbt, LittleTile parent) {
		super(nbt, parent);
	}
	
	protected StructureLinkTile(int relativeX, int relativeY, int relativeZ, LittleGridContext context, int[] identifier, LittleStructureAttribute attribute, LittleTile parent) {
		super(relativeX, relativeY, relativeZ, context, identifier, attribute, parent);
	}
	
	@Override
	public BlockPos getStructurePosition() {
		return getAbsolutePosition(parent.te);
	}
	
	@Override
	protected void connect(World world, LittleTile mainTile) {
		
		this.connectedStructure = mainTile.connection.getStructureWithoutLoading();
		
		if (connectedStructure == null || !this.connectedStructure.LoadList()) {
			new RuntimeException("Failed to connect to structure because the list cannot be loaded!").printStackTrace();
			connectedStructure = null;
			return;
		}
		
		if (!this.connectedStructure.containsTile(parent))
			this.connectedStructure.addTile(parent);
	}
	
	@Override
	protected void failedConnect(World world) {
		new RuntimeException("Failed to connect to structure coord " + this + "!").printStackTrace();
		parent.te.updateTiles((tilesTe) -> tilesTe.removeTile(parent));
		parent.te.updateBlock();
	}
	
	@Override
	public StructureLinkTile copy(LittleTile parent) {
		return new StructureLinkTile(coord.getX(), coord.getY(), coord.getZ(), context, identifier.clone(), attribute, parent);
	}
	
}