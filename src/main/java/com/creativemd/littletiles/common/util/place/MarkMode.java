package com.creativemd.littletiles.common.util.place;

import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.mc.GuiContainerSub;
import com.creativemd.creativecore.common.gui.premade.SubContainerEmpty;
import com.creativemd.littletiles.client.gui.SubGuiMarkMode;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.math.vec.LittleVecContext;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MarkMode {
	
	public static Minecraft mc = Minecraft.getMinecraft();
	
	public PlacementPosition position = null;
	
	public boolean allowLowResolution = true;
	
	public MarkMode() {
		
	}
	
	public static PlacementPosition loadPosition(PlacementPosition position, PlacementPreview preview) {
		if (!preview.previews.isAbsolute()) {
			position.setVecContext(new LittleVecContext(preview.box.getCenter(), preview.context));
			
			LittleVec center = preview.size.calculateCenter();
			LittleVec centerInv = preview.size.calculateInvertedCenter();
			
			switch (position.facing) {
			case EAST:
				position.getVec().x -= center.x;
				break;
			case WEST:
				position.getVec().x += centerInv.x;
				break;
			case UP:
				position.getVec().y -= center.y;
				break;
			case DOWN:
				position.getVec().y += centerInv.y;
				break;
			case SOUTH:
				position.getVec().z -= center.z;
				break;
			case NORTH:
				position.getVec().z += centerInv.z;
				break;
			default:
				break;
			}
			
			if (preview.previews.size() > 1 && preview.fixed)
				position.addVec(preview.cachedOffset);
		}
		return position;
	}
	
	public boolean allowLowResolution() {
		return allowLowResolution;
	}
	
	public SubGui getConfigurationGui() {
		return new SubGuiMarkMode(this);
	}
	
	public boolean processPosition(EntityPlayer player, PlacementPosition position, PlacementPreview preview) {
		if (this.position == null) {
			this.position = loadPosition(position, preview);
			return false;
		}
		if (GuiScreen.isCtrlKeyDown()) {
			FMLClientHandler.instance().displayGuiScreen(player, new GuiContainerSub(player, getConfigurationGui(), new SubContainerEmpty(player)));
			return false;
		}
		return true;
	}
	
	public void renderWorld(float renderTickTime) {
		
	}
	
	public void move(LittleGridContext context, EnumFacing facing) {
		LittleVec vec = new LittleVec(facing);
		vec.scale(GuiScreen.isCtrlKeyDown() ? context.size : 1);
		position.subVec(vec);
	}
	
	public void renderBlockHighlight(EntityPlayer player, float renderTickTime) {
		double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * renderTickTime;
		double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * renderTickTime;
		double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * renderTickTime;
		
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);
		AxisAlignedBB box = position.getBox().grow(0.0020000000949949026D).offset(-d0, -d1, -d2);
		
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
	
}
