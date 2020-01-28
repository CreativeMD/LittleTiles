package com.creativemd.littletiles.common.structure.connection;

import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StructureLink extends StructureLinkBaseRelative<LittleStructure> implements IStructureChildConnector<LittleStructure> {
	
	public final boolean isChild;
	public final int childID;
	
	public StructureLink(TileEntity te, BlockPos coord, LittleGridContext context, int[] identifier, int attribute, LittleStructure parent, int childID, boolean isChild) {
		super(te, coord, context, identifier, attribute, parent);
		this.childID = childID;
		this.isChild = isChild;
	}
	
	public StructureLink(BlockPos origin, BlockPos coord, LittleGridContext context, int[] identifier, int attribute, LittleStructure parent, int childID, boolean isChild) {
		super(origin, coord, context, identifier, attribute, parent);
		this.childID = childID;
		this.isChild = isChild;
	}
	
	public StructureLink(int baseX, int baseY, int baseZ, BlockPos coord, LittleGridContext context, int[] identifier, int attribute, LittleStructure parent, int childID, boolean isChild) {
		super(baseX, baseY, baseZ, coord, context, identifier, attribute, parent);
		this.childID = childID;
		this.isChild = isChild;
	}
	
	public StructureLink(NBTTagCompound nbt, LittleStructure parent, boolean isChild) {
		super(nbt, parent);
		this.childID = nbt.getInteger("childID");
		this.isChild = isChild;
	}
	
	protected StructureLink(int relativeX, int relativeY, int relativeZ, LittleGridContext context, int[] identifier, int attribute, LittleStructure parent, int childID, boolean isChild) {
		super(relativeX, relativeY, relativeZ, context, identifier, attribute, parent);
		this.childID = childID;
		this.isChild = isChild;
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("childID", childID);
		return nbt;
	}
	
	@Override
	public BlockPos getStructurePosition() {
		return getAbsolutePosition(parent.getMainTile().te);
	}
	
	@Override
	protected void connect(World world, LittleTile mainTile) {
		
		this.connectedStructure = mainTile.connection.getStructureWithoutLoading();
		
		if (isChild) {
			IStructureChildConnector link = this.connectedStructure.children.get(childID);
			if (link == null) {
				new RuntimeException("Parent does not remember child! coord=" + this).printStackTrace();
				return;
			}
			
			link.setLoadedStructure(parent);
		} else {
			if (this.connectedStructure.parent == null)
				this.connectedStructure.updateParentConnection(childID, parent);
			this.connectedStructure.parent.setLoadedStructure(parent); // Yeah it looks confusing ... it loads the parent for the child
		}
	}
	
	@Override
	protected void failedConnect(World world) {
		new RuntimeException("Failed to connect to parent/ child structure! coord=" + this + "").printStackTrace();
	}
	
	@Override
	public StructureLink copy(LittleStructure parent) {
		return new StructureLink(coord.getX(), coord.getY(), coord.getZ(), context, identifier.clone(), attribute, parent, childID, isChild);
	}
	
	@Override
	public boolean isChild() {
		return isChild;
	}
	
	@Override
	public int getChildID() {
		return childID;
	}
	
	public static IStructureChildConnector loadFromNBT(LittleStructure structure, NBTTagCompound nbt, boolean isChild) {
		if (nbt.hasKey("entity"))
			return new StructureLinkToSubWorld(nbt, structure);
		else if (nbt.getBoolean("subWorld"))
			return new StructureLinkFromSubWorld(nbt, structure);
		return new StructureLink(nbt, structure, isChild);
	}
	
	@Override
	public void destroyStructure() {
		if (!isChild() && connectedStructure.load() && connectedStructure.loadChildren())
			connectedStructure.removeStructure();
	}
	
	@Override
	public EntityAnimation getAnimation() {
		return null;
	}
}
