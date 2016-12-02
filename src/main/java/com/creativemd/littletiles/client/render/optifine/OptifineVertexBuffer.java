package com.creativemd.littletiles.client.render.optifine;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.client.renderer.VertexBuffer;
import shadersmod.client.SVertexBuilder;

public class OptifineVertexBuffer extends VertexBuffer {
	
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
	
	private static boolean isShaders()
	{
		try {
			return (boolean) isShadersMethod.invoke(null);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return false;
	}

	public OptifineVertexBuffer(int bufferSizeIn) {
		super(bufferSizeIn);
	}
	
	@Override
	public void addVertexData(int[] vertexData)
    {
        if(isShaders())
    	{
    		System.out.println("adding data");
        	SVertexBuilder.beginAddVertexData(this, vertexData);
    	}else
    		System.out.println("No shaders active");
        super.addVertexData(vertexData);
        if(isShaders())
        	SVertexBuilder.endAddVertexData(this);
    }
	
	@Override
    public void endVertex()
    {
    	super.endVertex();
    	if(isShaders())
    		SVertexBuilder.endAddVertex(this);
    	
    }
	
	@Override
    public VertexBuffer pos(double x, double y, double z)
    {
        if(isShaders())
        	SVertexBuilder.beginAddVertex(this);
        
        return super.pos(x, y, z);
    }

}
