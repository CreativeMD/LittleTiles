package com.creativemd.littletiles.common.structure.connection;

import java.util.UUID;

import com.creativemd.creativecore.common.world.SubWorld;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.events.LittleDoorHandler;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StructureLinkToSubWorld extends StructureLinkBaseAbsolute<LittleStructure> implements IStructureChildConnector<LittleStructure> {
	
	public final UUID entityUUID;
	public final int childID;
	
	public StructureLinkToSubWorld(LittleTile tile, LittleStructureAttribute attribute, LittleStructure parent, int childID, UUID entityUUID) {
		super(tile, attribute, parent);
		this.childID = childID;
		this.entityUUID = entityUUID;
	}
	
	public StructureLinkToSubWorld(TileEntity te, LittleGridContext context, int[] identifier, LittleStructureAttribute attribute, LittleStructure parent, int childID, UUID entityUUID) {
		super(te, context, identifier, attribute, parent);
		this.childID = childID;
		this.entityUUID = entityUUID;
	}
	
	public StructureLinkToSubWorld(BlockPos pos, LittleGridContext context, int[] identifier, LittleStructureAttribute attribute, LittleStructure parent, int childID, UUID entityUUID) {
		super(pos, context, identifier, attribute, parent);
		this.childID = childID;
		this.entityUUID = entityUUID;
	}
	
	public StructureLinkToSubWorld(NBTTagCompound nbt, LittleStructure parent) {
		super(nbt, parent);
		this.childID = nbt.getInteger("childID");
		this.entityUUID = UUID.fromString(nbt.getString("entity"));
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt = super.writeToNBT(nbt);
		nbt.setInteger("childID", childID);
		nbt.setString("entity", entityUUID.toString());
		return nbt;
	}
	
	@Override
	protected World getWorld(World world) {
		EntityAnimation animation = LittleDoorHandler.getHandler(world).findDoor(entityUUID);
		if (animation != null)
			return animation.fakeWorld;
		return null;
	}
	
	@Override
	protected void connect(World world, LittleTile mainTile) {
		
		this.connectedStructure = mainTile.connection.getStructureWithoutLoading();
		
		this.connectedStructure.parent.setLoadedStructure(parent, parent.attribute);
	}
	
	@Override
	protected void failedConnect(World world) {
		new RuntimeException("Failed to connect to parent/ child structure! coord=" + this + "").printStackTrace();
	}
	
	@Override
	public StructureLinkToSubWorld copy(LittleStructure parent) {
		return new StructureLinkToSubWorld(pos, context, identifier.clone(), attribute, parent, childID, entityUUID);
	}
	
	@Override
	public boolean isChild() {
		return false;
	}
	
	@Override
	public int getChildID() {
		return childID;
	}
	
	@Override
	public void destroyStructure() {
		EntityAnimation animation = LittleDoorHandler.getHandler((SubWorld) connectedStructure.getWorld()).findDoor(entityUUID);
		if (animation != null)
			animation.isDead = true;
		for (IStructureChildConnector child : connectedStructure.children.values())
			child.destroyStructure();
	}
	
	@Override
	public boolean isLinkToAnotherWorld() {
		return true;
	}
	
	@Override
	public EntityAnimation getAnimation() {
		return null;
	}
	
}
