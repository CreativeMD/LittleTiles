package com.creativemd.littletiles.common.structure.connection;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.tiles.LittleTile;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.model.ITransformation;

public interface IStructureConnector<T> {
	
	public BlockPos getStructurePosition();
	
	public LittleStructure getStructure(World world);
	
	public LittleStructure getStructureWithoutLoading();
	
	public boolean isConnected(World world);
	
	public void setLoadedStructure(LittleStructure structure, LittleStructureAttribute attribute);
	
	public LittleStructureAttribute getAttribute();
	
	public boolean isLink();
	
	public boolean is(LittleTile mainTile);
	
	public default boolean isLinkToAnotherWorld() {
		return false;
	}
	
	public void reset();
	
	public IStructureConnector copy(T parent);
	
	public void transform(ITransformation transformation);
	
}
