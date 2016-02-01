package com.creativemd.littletiles.client.render;

import java.util.ArrayList;

import com.creativemd.creativecore.common.utils.CubeObject;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;

public class LittleBlockVertex {
	
	
	//public CubeObject cube;
	
	public boolean enableAO;
	
	public LittleVertex XPos;
	public LittleVertex XNeg;
	public LittleVertex YPos;
	public LittleVertex YNeg;
	public LittleVertex ZPos;
	public LittleVertex ZNeg;
	
	public ArrayList<LittleVertex> getAllSides()
	{
		ArrayList<LittleVertex> sides = new ArrayList<>();
		if(XPos != null)
			sides.add(XPos);
		if(XNeg != null)
			sides.add(XNeg);
		if(YPos != null)
			sides.add(YPos);
		if(YNeg != null)
			sides.add(YNeg);
		if(ZPos != null)
			sides.add(ZPos);
		if(ZNeg != null)
			sides.add(ZNeg);
		return sides;
	}
	
	public void renderVertex()
	{
		Tessellator tessellator = Tessellator.instance;
        tessellator.setBrightness(983055);
        
        ArrayList<LittleVertex> sides = getAllSides();
        for (int i = 0; i < sides.size(); i++) {
        	LittleVertex vertex = sides.get(i);
        	if(enableAO)
            {      
            	tessellator.setColorOpaque_F(vertex.colorRedTopLeft, vertex.colorGreenTopLeft, vertex.colorBlueTopLeft);
                tessellator.setBrightness(vertex.brightnessTopLeft);
                tessellator.addVertexWithUV(vertex.coords[0][0], vertex.coords[0][1], vertex.coords[0][2], vertex.coords[0][3], vertex.coords[0][4]);
                tessellator.setColorOpaque_F(vertex.colorRedBottomLeft, vertex.colorGreenBottomLeft, vertex.colorBlueBottomLeft);
                tessellator.setBrightness(vertex.brightnessBottomLeft);
                tessellator.addVertexWithUV(vertex.coords[1][0], vertex.coords[1][1], vertex.coords[1][2], vertex.coords[1][3], vertex.coords[1][4]);
                tessellator.setColorOpaque_F(vertex.colorRedBottomRight, vertex.colorGreenBottomRight, vertex.colorBlueBottomRight);
                tessellator.setBrightness(vertex.brightnessBottomRight);
                tessellator.addVertexWithUV(vertex.coords[2][0], vertex.coords[2][1], vertex.coords[2][2], vertex.coords[2][3], vertex.coords[2][4]);
                tessellator.setColorOpaque_F(vertex.colorRedTopRight, vertex.colorGreenTopRight, vertex.colorBlueTopRight);
                tessellator.setBrightness(vertex.brightnessTopRight);
                tessellator.addVertexWithUV(vertex.coords[3][0], vertex.coords[3][1], vertex.coords[3][2], vertex.coords[3][3], vertex.coords[3][4]);
            }else{
        		tessellator.setColorOpaque_I(vertex.color);
        		tessellator.setBrightness(vertex.brightness);
            	tessellator.addVertexWithUV(vertex.coords[0][0], vertex.coords[0][1], vertex.coords[0][2], vertex.coords[0][3], vertex.coords[0][4]);
            	tessellator.addVertexWithUV(vertex.coords[1][0], vertex.coords[1][1], vertex.coords[1][2], vertex.coords[1][3], vertex.coords[1][4]);
            	tessellator.addVertexWithUV(vertex.coords[2][0], vertex.coords[2][1], vertex.coords[2][2], vertex.coords[2][3], vertex.coords[2][4]);
            	tessellator.addVertexWithUV(vertex.coords[3][0], vertex.coords[3][1], vertex.coords[3][2], vertex.coords[3][3], vertex.coords[3][4]);
            }
		}
        
        
	}
	
	public static class LittleVertex {
		
		public LittleVertex() {
			
		}
		
		//public IIcon icon;
		
		public int color;
		public int brightness;
		
		/** Brightness top left */
	    public int brightnessTopLeft;
	    /** Brightness bottom left */
	    public int brightnessBottomLeft;
	    /** Brightness bottom right */
	    public int brightnessBottomRight;
	    /** Brightness top right */
	    public int brightnessTopRight;
	    /** Red color value for the top left corner */
	    public float colorRedTopLeft;
	    /** Red color value for the bottom left corner */
	    public float colorRedBottomLeft;
	    /** Red color value for the bottom right corner */
	    public float colorRedBottomRight;
	    /** Red color value for the top right corner */
	    public float colorRedTopRight;
	    /** Green color value for the top left corner */
	    public float colorGreenTopLeft;
	    /** Green color value for the bottom left corner */
	    public float colorGreenBottomLeft;
	    /** Green color value for the bottom right corner */
	    public float colorGreenBottomRight;
	    /** Green color value for the top right corner */
	    public float colorGreenTopRight;
	    /** Blue color value for the top left corner */
	    public float colorBlueTopLeft;
	    /** Blue color value for the bottom left corner */
	    public float colorBlueBottomLeft;
	    /** Blue color value for the bottom right corner */
	    public float colorBlueBottomRight;
	    /** Blue color value for the top right corner */
	    public float colorBlueTopRight;
	    
	    public double[][] coords;
		
	}
}
