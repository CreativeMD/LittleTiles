package com.creativemd.littletiles.client.render.optifine;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import shadersmod.client.SVertexBuilder;

public class OptifineVertexBuffer extends BufferBuilder {
	
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

	public OptifineVertexBuffer(int bufferSizeIn) {
		super(bufferSizeIn);
	}
	
	@Override
	public void addVertexData(int[] vertexData)
    {
		try{
			SVertexBuilder builder = (SVertexBuilder) this.getClass().getField("sVertexBuilder").get(this);
			System.out.println(Arrays.toString((long[]) ReflectionHelper.getPrivateValue(SVertexBuilder.class, builder, "entityData")));
			System.out.println((int) ReflectionHelper.getPrivateValue(SVertexBuilder.class, builder, "entityDataIndex"));
			System.out.println((int) ReflectionHelper.getPrivateValue(SVertexBuilder.class, builder, "vertexSize"));
		}catch(Exception e){
			e.printStackTrace();
		}
		
        //if(isShaders())
        	//SVertexBuilder.beginAddVertexData(this, vertexData);
        super.addVertexData(vertexData);
        
        //if(isShaders())
        	//SVertexBuilder.endAddVertexData(this);
    }
	
	@Override
    public void endVertex()
    {
    	super.endVertex();
    	//if(isShaders())
    		//SVertexBuilder.endAddVertex(this);
    	
    }
	
	@Override
    public BufferBuilder pos(double x, double y, double z)
    {
        //if(isShaders())
        	//SVertexBuilder.beginAddVertex(this);
        
        return super.pos(x, y, z);
    }

}
