package com.creativemd.littletiles.client.render.world;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.creativemd.creativecore.common.world.CreativeWorld;
import com.creativemd.creativecore.common.world.IOrientatedWorld;
import com.creativemd.littletiles.client.render.entity.LittleRenderChunk;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderUtils {
	
	// ViewFrustum
	private static Field viewFrustumField;
	
	public static ViewFrustum getViewFrustum() {
		if (viewFrustumField == null)
			viewFrustumField = ReflectionHelper.findField(RenderGlobal.class, "viewFrustum", "field_175008_n");
		try {
			return (ViewFrustum) viewFrustumField.get(mc.renderGlobal);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static Minecraft mc = Minecraft.getMinecraft();
	
	public static BlockPos getRenderChunkPos(BlockPos pos) {
		int i = MathHelper.intFloorDiv(pos.getX(), 16);
		int j = MathHelper.intFloorDiv(pos.getY(), 16);
		int k = MathHelper.intFloorDiv(pos.getZ(), 16);
		return new BlockPos(i, j, k);
	}
	
	private static Method getRenderChunk = ReflectionHelper.findMethod(ViewFrustum.class, "getRenderChunk", "func_178161_a", BlockPos.class);
	
	public static RenderChunk getRenderChunk(ViewFrustum frustum, BlockPos pos) {
		try {
			return (RenderChunk) getRenderChunk.invoke(frustum, pos);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static LittleRenderChunk getRenderChunk(IOrientatedWorld world, BlockPos pos) {
		if (world instanceof CreativeWorld && ((CreativeWorld) world).renderChunkSupplier != null)
			return (LittleRenderChunk) ((CreativeWorld) world).renderChunkSupplier.getRenderChunk((World) world, pos);
		return null;
	}
	
}
