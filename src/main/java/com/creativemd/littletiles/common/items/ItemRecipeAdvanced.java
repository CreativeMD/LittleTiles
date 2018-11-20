package com.creativemd.littletiles.common.items;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.client.rendering.model.ICreativeRendered;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.ItemModelCache;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.container.SubContainerConfigure;
import com.creativemd.littletiles.common.container.SubContainerRecipeAdvanced;
import com.creativemd.littletiles.common.gui.SubGuiRecipeAdvancedSelection;
import com.creativemd.littletiles.common.gui.SubGuiRecipeAdvancedStructure;
import com.creativemd.littletiles.common.gui.configure.SubGuiConfigure;
import com.creativemd.littletiles.common.gui.configure.SubGuiModeSelector;
import com.creativemd.littletiles.common.packet.LittleSelectionModePacket;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
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
import net.minecraft.client.util.ITooltipFlag;
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
	public String getItemStackDisplayName(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("structure") && stack.getTagCompound().getCompoundTag("structure").hasKey("name"))
			return stack.getTagCompound().getCompoundTag("structure").getString("name");
		return super.getItemStackDisplayName(stack);
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
	@SideOnly(Side.CLIENT)
	public SubGuiConfigure getConfigureGUI(EntityPlayer player, ItemStack stack) {
		if (!((ItemRecipeAdvanced) stack.getItem()).hasLittlePreview(stack))
			return new SubGuiRecipeAdvancedSelection(stack);
		return new SubGuiRecipeAdvancedStructure(stack);
	}
	
	@Override
	public SubContainerConfigure getConfigureContainer(EntityPlayer player, ItemStack stack) {
		return new SubContainerRecipeAdvanced(player, stack);
	}
	
	@Override
	public float getDestroySpeed(ItemStack stack, IBlockState state) {
		return 0F;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		
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
	
	@SideOnly(Side.CLIENT)
	IBakedModel model;
	
	@Override
	@SideOnly(Side.CLIENT)
	public void applyCustomOpenGLHackery(ItemStack stack, TransformType cameraTransformType) {
		Minecraft mc = Minecraft.getMinecraft();
		GlStateManager.pushMatrix();
		
		if (cameraTransformType == TransformType.GUI || !stack.hasTagCompound() || !stack.getTagCompound().hasKey("tiles")) {
			if (cameraTransformType == TransformType.GUI)
				GlStateManager.disableDepth();
			if (model == null)
				model = mc.getRenderItem().getItemModelMesher().getModelManager().getModel(new ModelResourceLocation(LittleTiles.modid + ":recipeadvanced_background", "inventory"));
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
		PacketHandler.sendPacketToServer(new LittleSelectionModePacket(result.getBlockPos(), true));
		return true;
	}
	
	@Override
	public boolean onClickBlock(EntityPlayer player, ItemStack stack, PositionResult position, RayTraceResult result) {
		if (hasLittlePreview(stack))
			return true;
		getSelectionMode(stack).onLeftClick(player, stack, result.getBlockPos());
		PacketHandler.sendPacketToServer(new LittleSelectionModePacket(result.getBlockPos(), false));
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public SubGuiConfigure getConfigureGUIAdvanced(EntityPlayer player, ItemStack stack) {
		return new SubGuiModeSelector(stack, ItemMultiTiles.currentContext, ItemMultiTiles.currentMode) {
			
			@Override
			public void saveConfiguration(LittleGridContext context, PlacementMode mode) {
				ItemMultiTiles.currentContext = context;
				ItemMultiTiles.currentMode = mode;
			}
			
		};
	}
	
	@Override
	public PlacementMode getPlacementMode(ItemStack stack) {
		if (!ItemMultiTiles.currentMode.canPlaceStructures() && stack.getTagCompound().hasKey("structure"))
			return PlacementMode.getStructureDefault();
		return ItemMultiTiles.currentMode;
	}
	
	@Override
	public LittleGridContext getPositionContext(ItemStack stack) {
		return ItemMultiTiles.currentContext;
	}
	
	@Override
	public LittleTileSize getCachedSize(ItemStack stack) {
		if (stack.getTagCompound().hasKey("size"))
			return LittleTilePreview.getSize(stack);
		return null;
	}
	
	@Override
	public LittleTileVec getCachedOffset(ItemStack stack) {
		return LittleTilePreview.getOffset(stack);
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
