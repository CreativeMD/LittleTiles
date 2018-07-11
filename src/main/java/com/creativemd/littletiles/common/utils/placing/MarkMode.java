package com.creativemd.littletiles.common.utils.placing;

import java.util.List;

import org.lwjgl.util.Color;

import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.creativecore.gui.GuiRenderHelper;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.mc.GuiContainerSub;
import com.creativemd.creativecore.gui.premade.SubContainerEmpty;
import com.creativemd.littletiles.client.tiles.LittleRenderingCube;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.gui.SubGuiMarkMode;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.placing.PlacementHelper.PositionResult;
import com.creativemd.littletiles.common.utils.placing.PlacementHelper.PreviewResult;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MarkMode {
	
	public static Minecraft mc = Minecraft.getMinecraft();
	
	public PositionResult position = null;
	
	public boolean allowLowResolution =  true;
	
	public MarkMode()
	{
		
	}
	
	public static PositionResult loadPosition(boolean absolute, PositionResult position, PreviewResult preview)
	{
		if(!absolute)
		{
			position.contextVec.vec = preview.box.getCenter();
			position.contextVec.context = preview.context;
			
			LittleTileVec center = preview.size.calculateCenter();
			LittleTileVec centerInv = preview.size.calculateInvertedCenter();
			
			switch(position.facing)
			{
			case EAST:
				position.contextVec.vec.x -= center.x;
				break;
			case WEST:
				position.contextVec.vec.x += centerInv.x;
				break;
			case UP:
				position.contextVec.vec.y -= center.y;
				break;
			case DOWN:
				position.contextVec.vec.y += centerInv.y;
				break;
			case SOUTH:
				position.contextVec.vec.z -= center.z;
				break;
			case NORTH:
				position.contextVec.vec.z += centerInv.z;
				break;
			default:
				break;
			}
			
			if(!preview.singleMode && preview.placedFixed)
			{
				position.contextVec.sub(preview.offset.contextVec);
			}
		}
		return position;
	}
	
	public boolean allowLowResolution()
	{
		return allowLowResolution;
	}
	
	public SubGui getConfigurationGui()
	{
		return new SubGuiMarkMode(this);
	}
	
	public boolean processPosition(EntityPlayer player, PositionResult position, PreviewResult preview, boolean absolute)
	{
		if(this.position == null)
		{
			this.position = loadPosition(absolute, position, preview);
			return false;
		}
		if(GuiScreen.isCtrlKeyDown())
		{
			FMLClientHandler.instance().displayGuiScreen(player, new GuiContainerSub(player, getConfigurationGui(), new SubContainerEmpty(player)));
			return false;
		}
		return true;
	}
	
	public void renderWorld(float renderTickTime)
	{
		
	}
	
	public void move(LittleGridContext context, EnumFacing facing)
	{
		LittleTileVec vec = new LittleTileVec(facing);
		vec.scale(GuiScreen.isCtrlKeyDown() ? context.size : 1);
		position.subVec(vec);
	}
	
	public void renderBlockHighlight(EntityPlayer player, float renderTickTime)
	{
		double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)renderTickTime;
        double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)renderTickTime;
        double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)renderTickTime;
        
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        AxisAlignedBB box = position.getBox().expandXyz(0.0020000000949949026D).offset(-d0, -d1, -d2);
        
        
        GlStateManager.glLineWidth(4.0F);
        RenderGlobal.drawSelectionBoundingBox(box, 0.0F, 0.0F, 0.0F, 1F);
        
        GlStateManager.disableDepth();
        GlStateManager.glLineWidth(1.0F);
        RenderGlobal.drawSelectionBoundingBox(box, 1F, 0.3F, 0.0F, 1F);
        GlStateManager.enableDepth();
        
		GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
	}
	
	public void renderOverlay(float renderTickTime)
	{
		ScaledResolution scaledresolution = new ScaledResolution(this.mc);
		
		int l = scaledresolution.getScaledWidth();
        int i1 = scaledresolution.getScaledHeight();
        
		GlStateManager.pushMatrix();
        GlStateManager.translate((float)(l / 2), (float)(i1 / 2), 0);
        Entity entity = this.mc.getRenderViewEntity();
        float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * renderTickTime;
        float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * renderTickTime;
        GlStateManager.rotate(pitch, -1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(-1.0F, -1.0F, -1.0F);
        {
        	{
        		float direction = pitch % 180;
        		
        		if(LittleAction.isUsingSecondMode(mc.player))
        		{
        			GlStateManager.pushMatrix();
            		GlStateManager.rotate(180, 0, 0, 1);
            		GuiRenderHelper.instance.drawStringWithShadow("up", -15, -50, ColorUtils.RGBAToInt(new Color(255, 255, 255, 255)));
            		GlStateManager.popMatrix();
            		
            		GlStateManager.pushMatrix();
            		GlStateManager.rotate(180, 1, 0, 0);
            		GuiRenderHelper.instance.drawStringWithShadow("up", 15, -50, ColorUtils.RGBAToInt(new Color(255, 255, 255, 255)));
            		GlStateManager.popMatrix();
        			
        		}else{
            		if(direction < 45 && direction > -45)
            		{
	            		GlStateManager.pushMatrix();
	            		GlStateManager.rotate(180, 0, 0, 1);
	            		GuiRenderHelper.instance.drawStringWithShadow("up", -30, -15, ColorUtils.RGBAToInt(new Color(255, 255, 255, 255)));
	            		GlStateManager.popMatrix();
	            		
	            		GlStateManager.pushMatrix();
	            		GlStateManager.rotate(180, 1, 0, 0);
	            		GuiRenderHelper.instance.drawStringWithShadow("up", 30, -15, ColorUtils.RGBAToInt(new Color(255, 255, 255, 255)));
	            		GlStateManager.popMatrix();
            		}else{
	            		GlStateManager.pushMatrix();
	            		GlStateManager.rotate(180, 0, 0, 1);
	            		GlStateManager.rotate(90, 1, 0, 0);
	            		GuiRenderHelper.instance.drawStringWithShadow("up", -30, -15, ColorUtils.RGBAToInt(new Color(255, 255, 255, 255)));
	            		GlStateManager.popMatrix();
	            		
	            		GlStateManager.pushMatrix();
	            		GlStateManager.rotate(180, 0, 0, 1);
	            		GlStateManager.rotate(-90, 1, 0, 0);
	            		GuiRenderHelper.instance.drawStringWithShadow("up", -30, -15, ColorUtils.RGBAToInt(new Color(255, 255, 255, 255)));
	            		GlStateManager.popMatrix();
            		}
        		}
        		
        		GlStateManager.pushMatrix();
        		
        		GlStateManager.rotate(-90, 0, 1, 0);
        		
        		if(direction < 45 && direction > -45)
        		{
            		GlStateManager.pushMatrix();
            		GlStateManager.rotate(180, 0, 0, 1);
            		GuiRenderHelper.instance.drawStringWithShadow("right", -30, -15, ColorUtils.RGBAToInt(new Color(255, 255, 255, 255)));
            		GlStateManager.popMatrix();
            		
            		GlStateManager.pushMatrix();
            		GlStateManager.rotate(180, 1, 0, 0);
            		GuiRenderHelper.instance.drawStringWithShadow("right", 30, -15, ColorUtils.RGBAToInt(new Color(255, 255, 255, 255)));
            		GlStateManager.popMatrix();
        		}else{
            		GlStateManager.pushMatrix();
            		GlStateManager.rotate(180, 0, 0, 1);
            		GlStateManager.rotate(90, 1, 0, 0);
            		GuiRenderHelper.instance.drawStringWithShadow("right", -30, -15, ColorUtils.RGBAToInt(new Color(255, 255, 255, 255)));
            		GlStateManager.popMatrix();
            		
            		GlStateManager.pushMatrix();
            		GlStateManager.rotate(180, 0, 0, 1);
            		GlStateManager.rotate(-90, 1, 0, 0);
            		GuiRenderHelper.instance.drawStringWithShadow("right", -30, -15, ColorUtils.RGBAToInt(new Color(255, 255, 255, 255)));
            		GlStateManager.popMatrix();
        		}
        		
        		GlStateManager.popMatrix();
        	}
        	OpenGlHelper.renderDirections(GuiScreen.isCtrlKeyDown() ? 50 : 30);
        	
        }
        GlStateManager.popMatrix();
	}
	
}
