package com.creativemd.littletiles.common.structure.type.premade.signal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.signal.ISignalBase;
import com.creativemd.littletiles.common.structure.signal.ISignalOutput;
import com.creativemd.littletiles.common.structure.signal.SignalNetwork;
import com.creativemd.littletiles.common.structure.type.premade.LittleStructurePremade;
import com.creativemd.littletiles.common.structure.type.premade.signal.LittleSignalCable.LittleStructureTypeCable;
import com.creativemd.littletiles.common.structure.type.premade.signal.LittleSignalCable.LittleStructureTypeNetwork;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleSignalOutput extends LittleStructurePremade implements ISignalOutput {
	
	private boolean[] state;
	public EnumFacing facing;
	private SignalNetwork network;
	
	public LittleSignalOutput(LittleStructureType type) {
		super(type);
	}
	
	@Override
	public int getBandwidth() {
		return ((LittleStructureTypeCable) type).bandwidth;
	}
	
	@Override
	public SignalNetwork getNetwork() {
		return network;
	}
	
	@Override
	public void setNetwork(SignalNetwork network) {
		this.network = network;
	}
	
	@Override
	public Iterator<ISignalBase> connections() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean canConnect(EnumFacing facing) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void connect(EnumFacing facing, ISignalBase base, LittleGridContext context, int distance) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void disconnect(EnumFacing facing, ISignalBase base) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean[] getState() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		// TODO Auto-generated method stub
		
	}
	
	public static class LittleStructureTypeOutput extends LittleStructureTypeNetwork {
		
		@SideOnly(Side.CLIENT)
		public List<RenderCubeObject> cubes;
		
		public LittleStructureTypeOutput(String id, String category, Class<? extends LittleStructure> structureClass, int attribute, String modid, int bandwidth) {
			super(id, category, structureClass, attribute, modid, bandwidth);
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public List<RenderCubeObject> getRenderingCubes() {
			if (cubes == null) {
				float size = (float) ((Math.sqrt(bandwidth) * 1F / 32F) * 1.4);
				cubes = new ArrayList<>();
				cubes.add(new RenderCubeObject(0, 0.5F - size, 0.5F - size, size * 2, 0.5F + size, 0.5F + size, LittleTiles.coloredBlock).setColor(-13619152));
				//cubes.add(new RenderCubeObject(0 + size * 2, 0.5F - size * 0.8F, 0.5F - size * 0.8F, 1 - size * 2, 0.5F + size * 0.8F, 0.5F + size * 0.8F, LittleTiles.singleCable).setColor(-13619152).setKeepUV(true));
				//cubes.add(new RenderCubeObject(1 - size * 2, 0.5F - size, 0.5F - size, 1, 0.5F + size, 0.5F + size, LittleTiles.coloredBlock).setColor(-13619152));
			}
			return cubes;
		}
		
	}
	
}
