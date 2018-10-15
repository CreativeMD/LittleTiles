package com.creativemd.littletiles.common.items;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.client.rendering.model.ICreativeRendered;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.gui.opener.GuiHandler;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.ItemModelCache;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.packet.LittleSelectionModePacket;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.placing.PlacementHelper.PositionResult;
import com.creativemd.littletiles.common.utils.placing.PlacementMode;
import com.creativemd.littletiles.common.utils.selection.mode.SelectionMode;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemRecipeAdvanced extends Item implements ILittleTile, ICreativeRendered {
	
	public ItemRecipeAdvanced() {
		setCreativeTab(LittleTiles.littleTab);
		hasSubtypes = true;
	}
	
	@Override
	public boolean hasLittlePreview(ItemStack stack) {
		return stack.hasTagCompound() && (stack.getTagCompound().getInteger("count") > 0 || stack.getTagCompound().hasKey("children"));
	}
	
	@Override
	public void saveLittlePreview(ItemStack stack, LittlePreviews previews) {
		LittleTilePreview.savePreview(previews, stack);
	}
	
	@Override
	public LittlePreviews getLittlePreview(ItemStack stack) {
		return LittleTilePreview.getPreview(stack);
	}
	
	@Override
	public LittlePreviews getLittlePreview(ItemStack stack, boolean allowLowResolution, boolean marked) {
		return LittleTilePreview.getPreview(stack, allowLowResolution);
	}
	
	@Override
	public void onClickAir(EntityPlayer player, ItemStack stack) {
		GuiHandler.openGui("recipeadvanced", new NBTTagCompound(), player);
	}
	
	@Override
	public boolean onClickBlock(EntityPlayer player, ItemStack stack, PositionResult position, RayTraceResult result) {
		GuiHandler.openGui("recipeadvanced", new NBTTagCompound(), player);
		return true;
	}
	
	@Override
	public PlacementMode getPlacementMode(ItemStack stack) {
		return PlacementMode.all;
	}
	
	@Override
	public float getStrVsBlock(ItemStack stack, IBlockState state) {
		return 0F;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		
	}
	
	@Override
	public boolean canDestroyBlockInCreative(World world, BlockPos pos, ItemStack stack, EntityPlayer player) {
		return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public ArrayList<RenderCubeObject> getRenderingCubes(IBlockState state, TileEntity te, ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().getInteger("count") > 0)
			return LittleTilePreview.getCubes(stack);
		return new ArrayList<RenderCubeObject>();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void applyCustomOpenGLHackery(ItemStack stack, TransformType cameraTransformType) {
		Minecraft mc = Minecraft.getMinecraft();
		GlStateManager.pushMatrix();
		
		if (cameraTransformType == TransformType.GUI || !stack.hasTagCompound() || !stack.getTagCompound().hasKey("tiles")) {
			if (cameraTransformType == TransformType.GUI)
				GlStateManager.disableDepth();
			IBakedModel model = mc.getRenderItem().getItemModelMesher().getModelManager().getModel(new ModelResourceLocation(LittleTiles.modid + ":recipeadvanced_background", "inventory"));
			ForgeHooksClient.handleCameraTransforms(model, cameraTransformType, false);
			
			mc.getRenderItem().renderItem(new ItemStack(Items.PAPER), model);
			
			if (cameraTransformType == TransformType.GUI)
				GlStateManager.enableDepth();
		}
		GlStateManager.popMatrix();
		
		if (stack.hasTagCompound() && stack.getTagCompound().getInteger("count") > 0) {
			LittleTileSize size = LittleTilePreview.getSize(stack);
			LittleGridContext context = LittleGridContext.get(stack.getTagCompound());
			double scaler = 1 / Math.max(1, Math.max(1, Math.max(size.getPosX(context), Math.max(size.getPosY(context), size.getPosZ(context)))));
			GlStateManager.scale(scaler, scaler, scaler);
		}
		
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void saveCachedModel(EnumFacing facing, BlockRenderLayer layer, List<BakedQuad> cachedQuads, IBlockState state, TileEntity te, ItemStack stack, boolean threaded) {
		ItemModelCache.cacheModel(stack, facing, cachedQuads);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public List<BakedQuad> getCachedModel(EnumFacing facing, BlockRenderLayer layer, IBlockState state, TileEntity te, ItemStack stack, boolean threaded) {
		return ItemModelCache.getCache(stack, facing);
	}
	
	@Override
	public boolean containsIngredients(ItemStack stack) {
		return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean onRightClick(EntityPlayer player, ItemStack stack, PositionResult position, RayTraceResult result) {
		if (hasLittlePreview(stack))
			return true;
		getSelectionMode(stack).onRightClick(player, stack, result.getBlockPos());
		PacketHandler.sendPacketToServer(new LittleSelectionModePacket(result.getBlockPos()));
		return false;
	}
	
	public static SelectionMode getSelectionMode(ItemStack stack) {
		if (!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		return SelectionMode.getOrDefault(stack.getTagCompound().getString("selmode"));
	}
	
	public static void setSelectionMode(ItemStack stack, SelectionMode mode) {
		if (!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		stack.getTagCompound().setString("selmode", mode.name);
	}
}
