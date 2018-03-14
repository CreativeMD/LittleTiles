package com.creativemd.littletiles.common.items;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.client.rendering.model.IExtendedCreativeRendered;
import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.opener.GuiHandler;
import com.creativemd.creativecore.gui.opener.IGuiCreator;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.ItemModelCache;
import com.creativemd.littletiles.common.container.SubContainerStructure;
import com.creativemd.littletiles.common.gui.SubGuiStructure;
import com.creativemd.littletiles.common.mods.chiselsandbits.ChiselsAndBitsManager;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;

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
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemRecipe extends Item implements IExtendedCreativeRendered, IGuiCreator {
	
	public ItemRecipe(){
		setCreativeTab(LittleTiles.littleTab);
		hasSubtypes = true;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
		ItemStack stack = player.getHeldItem(hand);
		if(hand == EnumHand.OFF_HAND)
			return new ActionResult(EnumActionResult.PASS, stack); 
		if(!player.isSneaking() && stack.hasTagCompound() && !stack.getTagCompound().hasKey("x"))
		{
			if(!world.isRemote)
				GuiHandler.openGuiItem(player, world);
			return new ActionResult(EnumActionResult.SUCCESS, stack);
		}
		return new ActionResult(EnumActionResult.PASS, stack);
    }
	
	public LittlePreviews saveBlocks(World world, ItemStack stack, int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
	{
		LittlePreviews previews = new LittlePreviews(LittleGridContext.getMin());
		
		for (int posX = minX; posX <= maxX; posX++) {
			for (int posY = minY; posY <= maxY; posY++) {
				for (int posZ = minZ; posZ <= maxZ; posZ++) {
					BlockPos newPos = new BlockPos(posX, posY, posZ);
					TileEntity tileEntity = world.getTileEntity(newPos);
					if(tileEntity instanceof TileEntityLittleTiles)
					{
						TileEntityLittleTiles te = (TileEntityLittleTiles) tileEntity;
						for (Iterator iterator = te.getTiles().iterator(); iterator.hasNext();) {
							LittleTilePreview preview = previews.addPreview(null, ((LittleTile) iterator.next()).getPreviewTile(), te.getContext());
							preview.box.addOffset(new LittleTileVec((posX-minX)*previews.context.size, (posY-minY)*previews.context.size, (posZ-minZ)*previews.context.size));
						}
					}
					LittlePreviews specialPreviews = ChiselsAndBitsManager.getPreviews(tileEntity);
					if(specialPreviews != null)
					{
						for (int i = 0; i < specialPreviews.size(); i++) {
							LittleTilePreview preview = previews.addPreview(null, specialPreviews.get(i), LittleGridContext.get(ChiselsAndBitsManager.convertingFrom));
							preview.box.addOffset(new LittleTileVec((posX-minX)*previews.context.size, (posY-minY)*previews.context.size, (posZ-minZ)*previews.context.size));
						}
					}
				}
			}
		}
		return previews;
	}
	
	public void saveRecipe(World world, EntityPlayer player, ItemStack stack, BlockPos second)
	{
		int firstX = stack.getTagCompound().getInteger("x");
		int firstY = stack.getTagCompound().getInteger("y");
		int firstZ = stack.getTagCompound().getInteger("z");
		
		stack.getTagCompound().removeTag("x");
		stack.getTagCompound().removeTag("y");
		stack.getTagCompound().removeTag("z");
		
		LittleTilePreview.savePreviewTiles(saveBlocks(world, stack, Math.min(firstX, second.getX()), Math.min(firstY, second.getY()), Math.min(firstZ, second.getZ()),
				Math.max(firstX, second.getX()), Math.max(firstY, second.getY()), Math.max(firstZ, second.getZ())), stack);	
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
		ItemStack stack = player.getHeldItem(hand);
		if(player.isSneaking())
		{
			if(!world.isRemote)
			{
				stack.setTagCompound(null);
			}
				
			return EnumActionResult.SUCCESS;
		}
		
		if(stack.hasTagCompound() && stack.getTagCompound().hasKey("x"))
		{
			if(!world.isRemote)
			{
				saveRecipe(world, player, stack, pos);
				player.sendMessage(new TextComponentTranslation("Second position: x=" + pos.getX() + ",y=" + pos.getY() + ",z=" + pos.getZ()));
			}
				
			return EnumActionResult.SUCCESS;
		}else if(!stack.hasTagCompound()){
			if(!world.isRemote)
			{
				stack.setTagCompound(new NBTTagCompound());
				stack.getTagCompound().setInteger("x", pos.getX());
				stack.getTagCompound().setInteger("y", pos.getY());
				stack.getTagCompound().setInteger("z", pos.getZ());
				player.sendMessage(new TextComponentTranslation("First position: x=" + pos.getX() + ",y=" + pos.getY() + ",z=" + pos.getZ()));
			}
			return EnumActionResult.SUCCESS;
		}
        return EnumActionResult.PASS;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced)
	{
		if(stack.hasTagCompound())
		{
			if(stack.getTagCompound().hasKey("x"))
				list.add("First pos: x=" + stack.getTagCompound().getInteger("x") + ",y=" + stack.getTagCompound().getInteger("y")+ ",z=" + stack.getTagCompound().getInteger("z"));
			else{
				String id = "none";
				if(stack.getTagCompound().hasKey("structure"))
					id = stack.getTagCompound().getCompoundTag("structure").getString("id");
				list.add("structure: " + id);
				list.add("contains " + stack.getTagCompound().getInteger("count") + " tiles");
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public SubGui getGui(EntityPlayer player, ItemStack stack, World world, BlockPos pos, IBlockState state) {
		return new SubGuiStructure(stack);
	}

	@Override
	public SubContainer getContainer(EntityPlayer player, ItemStack stack, World world, BlockPos pos, IBlockState state) {
		return new SubContainerStructure(player, stack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ArrayList<RenderCubeObject> getRenderingCubes(IBlockState state, TileEntity te, ItemStack stack) {
		if(stack.hasTagCompound() && !stack.getTagCompound().hasKey("x"))
			return LittleTilePreview.getCubes(stack);
		return new ArrayList<RenderCubeObject>();
	}
	
	public ModelResourceLocation getBackgroundLocation()
	{
		return new ModelResourceLocation(LittleTiles.modid + ":recipe_background", "inventory");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void applyCustomOpenGLHackery(ItemStack stack, TransformType cameraTransformType)
	{
		Minecraft mc = Minecraft.getMinecraft();
		GlStateManager.pushMatrix();
		
		if(cameraTransformType == TransformType.GUI || !stack.hasTagCompound() || !stack.getTagCompound().hasKey("tiles"))
		{
			if(cameraTransformType == TransformType.GUI)
				GlStateManager.disableDepth();
			IBakedModel model = mc.getRenderItem().getItemModelMesher().getModelManager().getModel(getBackgroundLocation());
			ForgeHooksClient.handleCameraTransforms(model, cameraTransformType, false);
			
			mc.getRenderItem().renderItem(new ItemStack(Items.PAPER), model);
			
			if(cameraTransformType == TransformType.GUI)
				GlStateManager.enableDepth();
		}
		GlStateManager.popMatrix();
		
		if(stack.hasTagCompound() && !stack.getTagCompound().hasKey("x"))
		{
			LittleTileSize size = LittleTilePreview.getSize(stack);
			LittleGridContext context = LittleGridContext.get(stack.getTagCompound());
			double scaler = 1/Math.max(1, Math.max(1, Math.max(size.getPosX(context), Math.max(size.getPosY(context), size.getPosZ(context)))));
			GlStateManager.scale(scaler, scaler, scaler);
		}
		
	}

	@Override
	@SideOnly(Side.CLIENT)
	public List<BakedQuad> getSpecialBakedQuads(IBlockState state, TileEntity te, EnumFacing side, long rand,
			ItemStack stack) {
		return new ArrayList<>();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void saveCachedModel(EnumFacing facing, BlockRenderLayer layer, List<BakedQuad> cachedQuads, IBlockState state, TileEntity te, ItemStack stack, boolean threaded)
	{
		ItemModelCache.cacheModel(stack, facing, cachedQuads);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public List<BakedQuad> getCachedModel(EnumFacing facing, BlockRenderLayer layer, IBlockState state, TileEntity te, ItemStack stack, boolean threaded)
	{
		return ItemModelCache.getCache(stack, facing);
	}
}
