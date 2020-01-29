package com.creativemd.littletiles.common.util.tooltip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.creativemd.creativecore.common.gui.GuiRenderHelper;
import com.creativemd.littletiles.common.action.LittleAction;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ActionMessage {
	
	private static HashMap<Class, ActionMessageObjectType> typeClasses = new HashMap<>();
	private static List<ActionMessageObjectType> types = new ArrayList<>();
	
	public static void registerType(ActionMessageObjectType type) {
		type.index = types.size();
		
		typeClasses.put(type.clazz, type);
		types.add(type);
	}
	
	public static ActionMessageObjectType getType(int index) {
		return types.get(index);
	}
	
	public static ActionMessageObjectType getType(Object object) {
		ActionMessageObjectType type = typeClasses.get(object.getClass());
		if (type == null)
			throw new RuntimeException("Invalid object in action message " + object + " clazz: " + object.getClass());
		return type;
	}
	
	public String text;
	public Object[] objects;
	
	public ActionMessage(String text, Object[] objects) {
		this.text = text;
		this.objects = objects;
	}
	
	static {
		registerType(new ActionMessageObjectType<ItemStack>(ItemStack.class) {
			
			@Override
			public void write(ItemStack object, ByteBuf buf) {
				LittleAction.writeItemStack(buf, object);
			}
			
			@Override
			public ItemStack read(ByteBuf buf) {
				return LittleAction.readItemStack(buf);
			}
			
			@Override
			@SideOnly(Side.CLIENT)
			public int width(ItemStack object, GuiRenderHelper helper) {
				return 20;
			}
			
			@Override
			@SideOnly(Side.CLIENT)
			public void render(ItemStack object, GuiRenderHelper helper, int color, float alpha) {
				GlStateManager.color(1, 1, 1, alpha);
				helper.drawItemStack(object, 2, -4, 16, 16, 0, color);
			}
			
		});
	}
	
	public static abstract class ActionMessageObjectType<T> {
		
		public final Class<T> clazz;
		private int index;
		
		public ActionMessageObjectType(Class<T> clazz) {
			this.clazz = clazz;
		}
		
		public int index() {
			return index;
		}
		
		public abstract void write(T object, ByteBuf buf);
		
		public abstract T read(ByteBuf buf);
		
		@SideOnly(Side.CLIENT)
		public abstract int width(T object, GuiRenderHelper helper);
		
		@SideOnly(Side.CLIENT)
		public abstract void render(T object, GuiRenderHelper helper, int color, float alpha);
		
	}
	
}
