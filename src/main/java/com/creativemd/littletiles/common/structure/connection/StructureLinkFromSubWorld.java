package com.creativemd.littletiles.common.structure.connection;

import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StructureLinkFromSubWorld extends StructureLinkBaseAbsolute<EntityAnimation> implements IStructureChildConnector<EntityAnimation> {
	
	public final int childID;
	
	public StructureLinkFromSubWorld(LittleTile tile, LittleStructureAttribute attribute, EntityAnimation parent, int childID) {
		super(tile, attribute, parent);
		this.childID = childID;
	}
	
	public StructureLinkFromSubWorld(TileEntity te, LittleGridContext context, int[] identifier, LittleStructureAttribute attribute, EntityAnimation parent, int childID) {
		super(te, context, identifier, attribute, parent);
		this.childID = childID;
	}
	
	public StructureLinkFromSubWorld(BlockPos pos, LittleGridContext context, int[] identifier, LittleStructureAttribute attribute, EntityAnimation parent, int childID) {
		super(pos, context, identifier, attribute, parent);
		this.childID = childID;
	}
	
	public StructureLinkFromSubWorld(NBTTagCompound nbt, EntityAnimation parent) {
		super(nbt, parent);
		this.childID = nbt.getInteger("childID");
	}
	
	@Override
	protected World getWorld(World world) {
		return parent.world;
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt = super.writeToNBT(nbt);
		nbt.setInteger("childID", childID);
		nbt.setBoolean("subWorld", true);
		return nbt;
	}
	
	@Override
	protected void connect(World world, LittleTile mainTile) {
		
		this.structure = mainTile.connection.getStructureWithoutLoading();
		
		IStructureChildConnector link = this.structure.children.get(childID);
		if (link == null) {
			new RuntimeException("Parent does not remember child! coord=" + this).printStackTrace();
			return;
		}
		
		link.setLoadedStructure(parent.structure, parent.structure.attribute);
	}
	
	@Override
	protected void failedConnect(World world) {
		new RuntimeException("Failed to connect to parent/ child structure! coord=" + this + "").printStackTrace();
	}
	
	@Override
	public StructureLinkFromSubWorld copy(EntityAnimation parent) {
		return new StructureLinkFromSubWorld(pos, context, identifier.clone(), attribute, parent, childID);
	}
	
	@Override
	public boolean isChild() {
		return true;
	}
	
	@Override
	public int getChildID() {
		return childID;
	}
	
	@Override
	public void destroyStructure() {
		parent.isDead = true;
	}
}
