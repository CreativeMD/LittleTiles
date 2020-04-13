package com.creativemd.littletiles.common.entity;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import com.creativemd.littletiles.common.structure.IAnimatedStructure;
import com.creativemd.littletiles.common.structure.animation.AnimationController;
import com.creativemd.littletiles.common.util.vec.LittleTransformation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public abstract class EntityAnimationController extends AnimationController {
	
	private static HashMap<String, Class<? extends EntityAnimationController>> controllerTypes = new HashMap<>();
	private static HashMap<Class<? extends EntityAnimationController>, String> controllerTypesInv = new HashMap<>();
	
	public static void registerControllerType(String id, Class<? extends EntityAnimationController> controllerType) {
		controllerTypes.put(id, controllerType);
		controllerTypesInv.put(controllerType, id);
	}
	
	static {
		registerControllerType("door", DoorController.class);
	}
	
	public EntityAnimationController() {
		
	}
	
	public EntityAnimation parent;
	
	public void setParent(EntityAnimation parent) {
		this.parent = parent;
		if (parent.structure != null && parent.structure instanceof IAnimatedStructure)
			((IAnimatedStructure) parent.structure).setAnimation(parent);
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setString("id", controllerTypesInv.get(this.getClass()));
		writeToNBTExtra(nbt);
		return nbt;
	}
	
	protected abstract void writeToNBTExtra(NBTTagCompound nbt);
	
	protected abstract void readFromNBT(NBTTagCompound nbt);
	
	public EntityPlayer activator() {
		return null;
	}
	
	public void onServerApproves() {
		
	}
	
	public void onServerPlaces() {
		
	}
	
	public abstract void transform(LittleTransformation transformation);
	
	public static EntityAnimationController parseController(EntityAnimation animation, NBTTagCompound nbt) {
		Class<? extends EntityAnimationController> controllerType = controllerTypes.get(nbt.getString("id"));
		if (controllerType == null)
			throw new RuntimeException("Unkown controller type '" + nbt.getString("id") + "'");
		
		try {
			EntityAnimationController controller = controllerType.getConstructor().newInstance();
			controller.setParent(animation);
			controller.readFromNBT(nbt);
			return controller;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
		
	}
	
}
