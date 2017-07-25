package com.creativemd.littletiles.client.render.optifine;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class OptifineHelper {
	
	private static Method isShadersMethod = getIsShaderMethod();
	
	private static Method getIsShaderMethod()
	{
		try {
			return Class.forName("Config").getMethod("isShaders");
		} catch (NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static boolean isShaders()
	{
		try {
			return (boolean) isShadersMethod.invoke(null);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return false;
	}

}
