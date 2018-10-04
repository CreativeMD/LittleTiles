package com.creativemd.littletiles.common.structure.connection;

import java.util.ArrayList;
import java.util.Map.Entry;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StructureLink extends StructureLinkBaseRelative<LittleStructure> implements IStructureChildConnector<LittleStructure> {
	
	public final boolean isChild;
	public final int childID;
	
	public StructureLink(TileEntity te, BlockPos coord, LittleGridContext context, int[] identifier, LittleStructureAttribute attribute, LittleStructure parent, int childID, boolean isChild) {
		super(te, coord, context, identifier, attribute, parent);
		this.childID = childID;
		this.isChild = isChild;
	}
	
	public StructureLink(BlockPos origin, BlockPos coord, LittleGridContext context, int[] identifier, LittleStructureAttribute attribute, LittleStructure parent, int childID, boolean isChild) {
		super(origin, coord, context, identifier, attribute, parent);
		this.childID = childID;
		this.isChild = isChild;
	}
	
	public StructureLink(int baseX, int baseY, int baseZ, BlockPos coord, LittleGridContext context, int[] identifier, LittleStructureAttribute attribute, LittleStructure parent, int childID, boolean isChild) {
		super(baseX, baseY, baseZ, coord, context, identifier, attribute, parent);
		this.childID = childID;
		this.isChild = isChild;
	}
	
	public StructureLink(NBTTagCompound nbt, LittleStructure parent, boolean isChild) {
		super(nbt, parent);
		this.childID = nbt.getInteger("childID");
		this.isChild = isChild;
	}
	
	protected StructureLink(int relativeX, int relativeY, int relativeZ, LittleGridContext context, int[] identifier, LittleStructureAttribute attribute, LittleStructure parent, int childID, boolean isChild) {
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
		
		this.structure = mainTile.connection.getStructureWithoutLoading();
		
		if (isChild) {
			IStructureChildConnector link = this.structure.children.get(childID);
			if (link == null) {
				new RuntimeException("Parent does not remember child! coord=" + this).printStackTrace();
				return;
			}
			
			link.setLoadedStructure(parent, parent.attribute);
		} else
			this.structure.parent.setLoadedStructure(parent, parent.attribute); // Yeah it looks confusing ... it loads the parent for the child
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
		if (structure.hasLoaded() && structure.loadChildren()) {
			for (Entry<TileEntityLittleTiles, ArrayList<LittleTile>> entry : structure.getEntrySet()) {
				entry.getKey().removeTiles(entry.getValue());
			}
			for (IStructureChildConnector child : structure.children.values())
				child.destroyStructure();
		}
	}
}
