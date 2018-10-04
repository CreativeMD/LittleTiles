package com.creativemd.littletiles.common.structure.connection;

import com.creativemd.creativecore.common.world.WorldFake;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StructureLinkToSubWorld extends StructureLinkBaseAbsolute<LittleStructure> implements IStructureChildConnector<LittleStructure> {
	
	public final String entityUUID;
	public final int childID;
	
	public StructureLinkToSubWorld(LittleTile tile, LittleStructureAttribute attribute, LittleStructure parent, int childID, String entityUUID) {
		super(tile, attribute, parent);
		this.childID = childID;
		this.entityUUID = entityUUID;
	}
	
	public StructureLinkToSubWorld(TileEntity te, LittleGridContext context, int[] identifier, LittleStructureAttribute attribute, LittleStructure parent, int childID, String entityUUID) {
		super(te, context, identifier, attribute, parent);
		this.childID = childID;
		this.entityUUID = entityUUID;
	}
	
	public StructureLinkToSubWorld(BlockPos pos, LittleGridContext context, int[] identifier, LittleStructureAttribute attribute, LittleStructure parent, int childID, String entityUUID) {
		super(pos, context, identifier, attribute, parent);
		this.childID = childID;
		this.entityUUID = entityUUID;
	}
	
	public StructureLinkToSubWorld(NBTTagCompound nbt, LittleStructure parent) {
		super(nbt, parent);
		this.childID = nbt.getInteger("childID");
		this.entityUUID = nbt.getString("entity");
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt = super.writeToNBT(nbt);
		nbt.setInteger("childID", childID);
		nbt.setString("entity", entityUUID);
		return nbt;
	}
	
	@Override
	protected World getWorld(World world) {
		for (Entity entity : world.getLoadedEntityList()) {
			if (entity instanceof EntityAnimation && entity.getCachedUniqueIdString().equals(entityUUID))
				return ((EntityAnimation) entity).fakeWorld;
		}
		return null;
	}
	
	@Override
	protected void connect(World world, LittleTile mainTile) {
		
		this.structure = mainTile.connection.getStructureWithoutLoading();
		
		this.structure.parent.setLoadedStructure(parent, parent.attribute);
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
		for (Entity entity : ((WorldFake) structure.getWorld()).parentWorld.getLoadedEntityList())
			if (entity instanceof EntityAnimation && entity.getCachedUniqueIdString().equals(entityUUID)) {
				entity.isDead = true;
				break;
			}
		for (IStructureChildConnector child : structure.children.values())
			child.destroyStructure();
	}
	
	@Override
	public boolean isLinkToAnotherWorld() {
		return true;
	}
}
