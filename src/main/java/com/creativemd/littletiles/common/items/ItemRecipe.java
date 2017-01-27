package com.creativemd.littletiles.common.items;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.client.rendering.model.CreativeBakedQuad;
import com.creativemd.creativecore.client.rendering.model.ICreativeRendered;
import com.creativemd.creativecore.client.rendering.model.IExtendedCreativeRendered;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.opener.GuiHandler;
import com.creativemd.creativecore.gui.opener.IGuiCreator;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.LittleTilesClient;
import com.creativemd.littletiles.common.gui.SubContainerStructure;
import com.creativemd.littletiles.common.gui.SubGuiStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTilePreview;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;
import com.creativemd.littletiles.utils.TileList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemRecipe extends Item implements IExtendedCreativeRendered, IGuiCreator{
	
	public ItemRecipe(){
		setCreativeTab(CreativeTabs.TOOLS);
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
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
		ItemStack stack = player.getHeldItem(hand);
		if(player.isSneaking())
		{
			if(!world.isRemote)
			{
				stack.setTagCompound(null);
				stack.setItemDamage(0);
			}
				
			return EnumActionResult.SUCCESS;
		}
		
		if(stack.hasTagCompound() && stack.getTagCompound().hasKey("x"))
		{
			if(!world.isRemote)
			{
				int firstX = stack.getTagCompound().getInteger("x");
				int firstY = stack.getTagCompound().getInteger("y");
				int firstZ = stack.getTagCompound().getInteger("z");
				int minX = Math.min(firstX, pos.getX());
				int maxX = Math.max(firstX, pos.getX());
				int minY = Math.min(firstY, pos.getY());
				int maxY = Math.max(firstY, pos.getY());
				int minZ = Math.min(firstZ, pos.getZ());
				int maxZ = Math.max(firstZ, pos.getZ());
				
				ArrayList<LittleTile> tiles = new ArrayList<LittleTile>();
				
				stack.getTagCompound().removeTag("x");
				stack.getTagCompound().removeTag("y");
				stack.getTagCompound().removeTag("z");
				
				for (int posX = minX; posX <= maxX; posX++) {
					for (int posY = minY; posY <= maxY; posY++) {
						for (int posZ = minZ; posZ <= maxZ; posZ++) {
							BlockPos newPos = new BlockPos(posX, posY, posZ);
							TileEntity tileEntity = world.getTileEntity(newPos);
							if(tileEntity instanceof TileEntityLittleTiles)
							{
								LittleTileVec offset = new LittleTileVec((posX-minX)*LittleTile.gridSize, (posY-minY)*LittleTile.gridSize, (posZ-minZ)*LittleTile.gridSize);
								TileEntityLittleTiles te = (TileEntityLittleTiles) tileEntity;
								for (Iterator iterator = te.getTiles().iterator(); iterator.hasNext();) {
									LittleTile tile = ((LittleTile) iterator.next()).copy();
									for (int j = 0; j < tile.boundingBoxes.size(); j++) {
										tile.boundingBoxes.get(j).addOffset(offset);
									}
									tiles.add(tile);
								}
							}
						}
					}
				}
				player.sendMessage(new TextComponentTranslation("Second position: x=" + pos.getX() + ",y=" + pos.getY() + ",z=" + pos.getZ()));
				saveTiles(world, tiles, stack);
				stack.setItemDamage(1);
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
	
	/*public static void flipPreview(ItemStack stack, EnumFacing direction)
	{
		int tiles = stack.getTagCompound().getInteger("tiles");
		for (int i = 0; i < tiles; i++) {
			NBTTagCompound nbt = stack.getTagCompound().getCompoundTag("tile" + i);
			LittleTilePreview.flipPreview(nbt, direction);
			stack.getTagCompound().setTag("tile" + i, nbt);
		}
	}
	
	public static void rotatePreview(ItemStack stack, EnumFacing direction)
	{
		int tiles = stack.getTagCompound().getInteger("tiles");
		for (int i = 0; i < tiles; i++) {
			NBTTagCompound nbt = stack.getTagCompound().getCompoundTag("tile" + i);
			LittleTilePreview.rotatePreview(nbt, direction);
			stack.getTagCompound().setTag("tile" + i, nbt);
		}
	}*/
	
	public static ArrayList<LittleTilePreview> getPreview(ItemStack stack)
	{
		ArrayList<LittleTilePreview> result = new ArrayList<LittleTilePreview>();
		int tiles = stack.getTagCompound().getInteger("tiles");
		for (int i = 0; i < tiles; i++) {
			NBTTagCompound nbt = stack.getTagCompound().getCompoundTag("tile" + i);
			LittleTilePreview preview = LittleTilePreview.loadPreviewFromNBT(nbt);
			if(preview != null)
				result.add(preview);
		}
		return result;
	}
	
	public static LittleTileSize getSize(ItemStack stack)
	{
		ArrayList<LittleTilePreview> tiles = getPreview(stack);
		int minX = LittleTile.maxPos;
		int minY = LittleTile.maxPos;
		int minZ = LittleTile.maxPos;
		int maxX = LittleTile.minPos;
		int maxY = LittleTile.minPos;
		int maxZ = LittleTile.minPos;
		for (int i = 0; i < tiles.size(); i++) {
			LittleTilePreview tile = tiles.get(i);
			minX = Math.min(minX, tile.box.minX);
			minY = Math.min(minY, tile.box.minY);
			maxX = Math.max(maxX, tile.box.maxX);
			maxY = Math.max(maxY, tile.box.maxY);
			maxZ = Math.max(maxZ, tile.box.maxZ);
		}
		return new LittleTileSize(maxX-minX, maxY-minY, maxZ-minZ);
	}
	
	public static ArrayList<LittleTile> loadTiles(TileEntityLittleTiles te, ItemStack stack)
	{
		ArrayList<LittleTile> result = new ArrayList<LittleTile>();
		int tiles = stack.getTagCompound().getInteger("tiles");
		for (int i = 0; i < tiles; i++) {
			NBTTagCompound nbt = stack.getTagCompound().getCompoundTag("tile" + i);
			LittleTile tile = LittleTile.CreateandLoadTile(te, te.getWorld(), nbt);
			//if(tile != null && tile.isValid())
			result.add(tile);
		}
		return result;
	}
	
	public static void savePreviewTiles(List<LittleTilePreview> previews, ItemStack stack)
	{
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setInteger("tiles", previews.size());
		for (int i = 0; i < previews.size(); i++) {
			NBTTagCompound nbt = new NBTTagCompound();
			previews.get(i).writeToNBT(nbt);			
			stack.getTagCompound().setTag("tile" + i, nbt);
		}
	}
	
	public static void saveTiles(World world, List<LittleTile> tiles, ItemStack stack)
	{
		stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setInteger("tiles", tiles.size());
		List cubes = null;
		if(world.isRemote)
			cubes = new ArrayList<RenderCubeObject>();
		for (int i = 0; i < tiles.size(); i++) {
			NBTTagCompound nbt = new NBTTagCompound();
			tiles.get(i).getPreviewTile().writeToNBT(nbt);;
			if(world.isRemote)
				cubes.addAll(tiles.get(i).getRenderingCubes());
			stack.getTagCompound().setTag("tile" + i, nbt);
		}
		if(world.isRemote)
			updateSize((ArrayList<RenderCubeObject>) cubes, stack);
	}
	
	@SideOnly(Side.CLIENT)
	public static void updateSize(ArrayList<RenderCubeObject> cubes, ItemStack stack)
	{
		if(stack.hasTagCompound())
		{
			Vec3d size = CubeObject.getSizeOfCubes(cubes);
			stack.getTagCompound().setDouble("size", Math.max(1, Math.max(size.xCoord, Math.max(size.yCoord, size.zCoord))));
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static ArrayList<RenderCubeObject> getCubes(ItemStack stack)
	{
		ArrayList<LittleTilePreview> preview = getPreview(stack);
		ArrayList<RenderCubeObject> cubes = new ArrayList<RenderCubeObject>();
		for (int i = 0; i < preview.size(); i++) {
			cubes.add(preview.get(i).getCubeBlock());
		}
		return cubes;
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
				list.add("contains " + stack.getTagCompound().getInteger("tiles") + " tiles");
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
			return getCubes(stack);
		return new ArrayList<RenderCubeObject>();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void applyCustomOpenGLHackery(ItemStack stack, TransformType cameraTransformType)
	{
		Minecraft mc = Minecraft.getMinecraft();
		GlStateManager.pushMatrix();
		
		//if(cameraTransformType == TransformType.GUI)
			//GlStateManager.translate(0, 0, -10);
		
		if(cameraTransformType == TransformType.GUI || !stack.hasTagCompound() || stack.getTagCompound().getInteger("tiles") == 0)
		{
			if(cameraTransformType == TransformType.GUI)
				GlStateManager.disableDepth();
			IBakedModel model = mc.getRenderItem().getItemModelMesher().getModelManager().getModel(new ModelResourceLocation(LittleTiles.modid + ":recipe_background", "inventory"));
			ForgeHooksClient.handleCameraTransforms(model, cameraTransformType, false);
			
			mc.getRenderItem().renderItem(new ItemStack(Items.PAPER), model);
			if(cameraTransformType == TransformType.GUI)
				GlStateManager.enableDepth();
		}
		GlStateManager.popMatrix();
		
		if(stack.hasTagCompound() && !stack.getTagCompound().hasKey("x"))
		{
			if(!stack.getTagCompound().hasKey("size"))
				updateSize(getCubes(stack), stack);
			double scaler = 1/Math.max(1, stack.getTagCompound().getDouble("size"));
			GlStateManager.scale(scaler, scaler, scaler);
		}
		
	}

	@Override
	@SideOnly(Side.CLIENT)
	public List<BakedQuad> getSpecialBakedQuads(IBlockState state, TileEntity te, EnumFacing side, long rand,
			ItemStack stack) {
		/*IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager().getModel(new ModelResourceLocation(LittleTiles.modid + ":recipe_background", "inventory"));
		if(model != null)
		{
			List<BakedQuad> quads = model.getQuads(state, side, rand);
			ArrayList<BakedQuad> newQuads = new ArrayList<>();
			for (int i = 0; i < quads.size(); i++) {
				newQuads.add(new CreativeBakedQuad(quads.get(i), null, -1, true, side));
			}
			return newQuads;
		}*/
		return new ArrayList<>();
	}
}
