package com.creativemd.littletiles.common.items;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.creativemd.creativecore.client.rendering.model.ICreativeRendered;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.creativecore.common.utils.TickUtils;
import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.creativecore.core.CreativeCoreClient;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.ITilesRenderer;
import com.creativemd.littletiles.client.render.PreviewRenderer;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.blocks.ILittleTile;
import com.creativemd.littletiles.common.packet.LittlePlacePacket;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTile.LittleTilePosition;
import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.creativemd.littletiles.common.utils.LittleTilePreview;
import com.creativemd.littletiles.common.utils.PlacementHelper;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileCoord;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;
import com.creativemd.littletiles.utils.PreviewTile;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockTiles extends ItemBlock implements ILittleTile, ICreativeRendered, ITilesRenderer{

	public ItemBlockTiles(Block block, ResourceLocation location) {
		super(block);
		setUnlocalizedName(location.getResourcePath());
		hasSubtypes = true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public String getItemStackDisplayName(ItemStack stack)
    {
		String result = super.getItemStackDisplayName(stack);
		//LittleTile tile = getLittleTile(Minecraft.getMinecraft().theWorld, stack);
		if(stack.hasTagCompound())
		{
			result += " (x=" + stack.getTagCompound().getByte("sizex") + ",y=" + stack.getTagCompound().getByte("sizey") + "z=" + stack.getTagCompound().getByte("sizez") + ")";
		}
		return result;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public String getUnlocalizedName(ItemStack stack)
    {
		if(stack.hasTagCompound())
		{
			Block block = Block.getBlockFromName(stack.getTagCompound().getString("block"));
			return block.getUnlocalizedName();
		}
        return super.getUnlocalizedName(stack);
    }
	
	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			return EnumActionResult.FAIL;
		
		PlacementHelper helper = PlacementHelper.getInstance(playerIn);
		
		RayTraceResult moving = Minecraft.getMinecraft().objectMouseOver;
		if(PreviewRenderer.markedHit != null)
			moving = PreviewRenderer.markedHit;
		
		//LittleTileSize size = helper.size;
		//size.rotateSize(PreviewRenderer.direction);
		//size.rotateSize(PreviewRenderer.direction2);
		//Vec3 center = helper.getCenterPos(size);
		
		/*x = moving.blockX;
		y = moving.blockY;
		z = moving.blockZ;*/
		
		facing = moving.sideHit;
		
		if(PreviewRenderer.markedHit == null)
		{
			if(!helper.canBePlacedInside(pos, moving.hitVec, facing))
			{
				pos = pos.offset(facing);
			}
		}
		
		if (stack.stackSize != 0 && playerIn.canPlayerEdit(pos, facing, stack)) //&& worldIn.canBlockBePlaced(this.block, pos, false, facing, (Entity)null, stack))
        {
            /*int i = this.getMetadata(stack.getMetadata());
            IBlockState iblockstate1 = this.block.onBlockPlaced(worldIn, pos, facing, hitX, hitY, hitZ, i, playerIn);*/
            
            if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
    			PacketHandler.sendPacketToServer(new LittlePlacePacket(stack, playerIn.getPositionEyes(TickUtils.getPartialTickTime()), moving.hitVec, pos, facing, PreviewRenderer.markedHit != null)); //, RotationUtils.getIndex(PreviewRenderer.direction), RotationUtils.getIndex(PreviewRenderer.direction2)));
    		
            if(placeBlockAt(playerIn, stack, worldIn, playerIn.getPositionEyes(TickUtils.getPartialTickTime()), moving.hitVec, helper, pos, facing, PreviewRenderer.markedHit != null)) //, PreviewRenderer.direction, PreviewRenderer.direction2);
	            PreviewRenderer.markedHit = null;
            
            /*if (placeBlockAt(stack, playerIn, worldIn, pos, facing, hitX, hitY, hitZ, iblockstate1))
            {
                SoundType soundtype = this.block.getSoundType();
                worldIn.playSound(playerIn, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                --stack.stackSize;
            }*/

            return EnumActionResult.SUCCESS;
        }
        else
        {
            return EnumActionResult.FAIL;
        }
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public void getSubItems(Item stack, CreativeTabs tab, List list)
    {
        
    }

	
	/*@Override
	@SideOnly(Side.CLIENT)
    public boolean func_150936_a(World world, int x, int y, int z, int side, EntityPlayer player, ItemStack stack)
    {
		Block block = world.getBlockState(x, y, z);
		
		Minecraft mc = Minecraft.getMinecraft();
		
		MovingObjectPosition moving = Minecraft.getMinecraft().objectMouseOver;
		if(PreviewRenderer.markedHit != null)
			moving = PreviewRenderer.markedHit;
		
		x = moving.blockX;
		y = moving.blockY;
		z = moving.blockZ;
		
		
		
        if (block == Blocks.snow_layer)
        {
            side = 1;
        }
        else if (block != Blocks.vine && block != Blocks.tallgrass && block != Blocks.deadbush && !block.isReplaceable(world, x, y, z) && !PlacementHelper.getInstance(player).canBePlacedInside(x, y, z, moving.hitVec, ForgeDirection.getOrientation(side)))
        {
            if (side == 0)
            {
                --y;
            }

            if (side == 1)
            {
                ++y;
            }

            if (side == 2)
            {
                --z;
            }

            if (side == 3)
            {
                ++z;
            }

            if (side == 4)
            {
                --x;
            }

            if (side == 5)
            {
                ++x;
            }
        }
        block = world.getBlockState(x, y, z);
        //helper.posX = x;
        //helper.posY = y;
        //helper.posZ = z;
        return block.isReplaceable(world, x, y, z) || PlacementHelper.getInstance(player).canBePlacedInsideBlock(x, y, z);
    }*/
	
	public static HashMapList<BlockPos, PreviewTile> getSplittedTiles(ArrayList<PreviewTile> tiles, BlockPos pos)
	{
		HashMapList<BlockPos, PreviewTile> splitted = new HashMapList<BlockPos, PreviewTile>();
		for (int i = 0; i < tiles.size(); i++) {
			if(!tiles.get(i).split(splitted, pos))
				return null;
		}
		return splitted;
	}
	
	public static boolean canPlaceTiles(World world, HashMapList<BlockPos, PreviewTile> splitted, ArrayList<BlockPos> coordsToCheck)
	{
		for (BlockPos pos : coordsToCheck) {
			TileEntity mainTile = world.getTileEntity(pos);
			if(mainTile instanceof TileEntityLittleTiles)
			{
				
				ArrayList<PreviewTile> tiles = splitted.getValues(pos);
				if(tiles != null)
				{
					for (int j = 0; j < tiles.size(); j++)
						if(tiles.get(j).needsCollisionTest() && !((TileEntityLittleTiles) mainTile).isSpaceForLittleTile(tiles.get(j).box))
							return false;
				}
			}else if(!(world.getBlockState(pos).getBlock() instanceof BlockTile) && !world.getBlockState(pos).getMaterial().isReplaceable())
				return false;
		}
		return true;
	}
	
	public static boolean placeTiles(World world, EntityPlayer player, ArrayList<PreviewTile> previews, LittleStructure structure, BlockPos pos, ItemStack stack, ArrayList<LittleTile> unplaceableTiles)
	{
		//if(!(world.getBlock(x, y, z) instanceof BlockTile))
			//if (!world.setBlock(x, y, z, LittleTiles.blockTile, 0, 3))
				//return false;
		
		HashMapList<BlockPos, PreviewTile> splitted = getSplittedTiles(previews, pos);
		if(splitted == null)
			return false;
		
		/*int counting = 0;
		for (int i = 0; i < splitted.size(); i++) {
			counting += splitted.getValues(i).size();
		}
		
		System.out.println("Created " + counting + " of " + previews.size() + " tiles");*/
		ArrayList<BlockPos> coordsToCheck = new ArrayList<BlockPos>();
		if(structure != null)
		{
			coordsToCheck.addAll(splitted.getKeys());
		}else{
			coordsToCheck.add(pos);
		}
		ArrayList<SoundType> soundsToBePlayed = new ArrayList<>();
		if(canPlaceTiles(world, splitted, coordsToCheck))
		{
			LittleTilePosition littlePos = null;
			//LittleTileCoord pos = null;
			
			for (BlockPos coord : splitted.getKeys()) {
				ArrayList<PreviewTile> placeTiles = splitted.getValues(coord);
				boolean hascollideBlock = false;
				for (int j = 0; j < placeTiles.size(); j++) {
					if(placeTiles.get(j).needsCollisionTest())
					{
						hascollideBlock = true;
						break;
					}
				}
				if(hascollideBlock)
				{
					if(!(world.getBlockState(coord).getBlock() instanceof BlockTile) && world.getBlockState(coord).getMaterial().isReplaceable())
						world.setBlockState(coord, LittleTiles.blockTile.getDefaultState());
					
					TileEntity te = world.getTileEntity(coord);
					if(te instanceof TileEntityLittleTiles)
					{
						//int tiles = 0;
						TileEntityLittleTiles teLT = (TileEntityLittleTiles) te;
						
						for (int j = 0; j < placeTiles.size(); j++) {
							LittleTile LT = placeTiles.get(j).placeTile(player, stack, teLT, structure, unplaceableTiles);
							if(LT != null)
							{
								if(!soundsToBePlayed.contains(LT.getSound()))
									soundsToBePlayed.add(LT.getSound());
								if(structure != null)
								{
									if(pos == null)
									{
										structure.mainTile = LT;
										LT.isMainBlock = true;
										LT.updateCorner();
										//pos = new LittleTileCoord(baseX, baseY, baseZ, coord, LT.cornerVec.copy());
										littlePos = new LittleTilePosition(coord, LT.cornerVec.copy());
									}else
										LT.coord = new LittleTileCoord(teLT, littlePos.coord, littlePos.position);
								}
							}
						}
						
						if(structure != null)
							teLT.combineTiles(structure);
					}
					//System.out.println("Placed " + tiles + "/" + placeTiles.size());
				}//else
					//System.out.println("Couldn't create te at x=" + coord.posX + ",y=" + coord.posY + ",z=" + coord.posZ);
			}
			for (int i = 0; i < soundsToBePlayed.size(); i++) {
				world.playSound((EntityPlayer)null, pos, soundsToBePlayed.get(i).getPlaceSound(), SoundCategory.BLOCKS, (soundsToBePlayed.get(i).getVolume() + 1.0F) / 2.0F, soundsToBePlayed.get(i).getPitch() * 0.8F);
			}
			return true;
		}
		return false;
	}
	
	public boolean placeBlockAt(EntityPlayer player, ItemStack stack, World world, Vec3d playerPos, Vec3d hitVec, PlacementHelper helper, BlockPos pos, EnumFacing facing, boolean customPlacement) //, ForgeDirection direction, ForgeDirection direction2)
    {
		ArrayList<PreviewTile> previews = helper.getPreviewTiles(stack, pos, playerPos, hitVec, facing, customPlacement, true); //, direction, direction2);
		
		LittleStructure structure = null;
		if(stack.getItem() instanceof ILittleTile)
		{
			structure = ((ILittleTile)stack.getItem()).getLittleStructure(stack);
		}else if(Block.getBlockFromItem(stack.getItem()) instanceof ILittleTile){
			structure = ((ILittleTile)Block.getBlockFromItem(stack.getItem())).getLittleStructure(stack);
		}
		
		if(structure != null)
		{
			structure.dropStack = stack.copy();
			structure.setTiles(new ArrayList<LittleTile>());
		}
		//System.out.println("Creating " + previews.size() + " tiles");
		
		ArrayList<LittleTile> unplaceableTiles = new ArrayList<LittleTile>();
		if(placeTiles(world, player, previews, structure, pos, stack, unplaceableTiles))
		{			
			if(!player.capabilities.isCreativeMode)
			{
				player.inventory.mainInventory[player.inventory.currentItem].stackSize--;
				if(player.inventory.mainInventory[player.inventory.currentItem].stackSize == 0)
					player.inventory.mainInventory[player.inventory.currentItem] = null;
			}
			
			if(!world.isRemote)
			{
				for (int j = 0; j < unplaceableTiles.size(); j++) {
					if(!(unplaceableTiles.get(j) instanceof LittleTileBlock) && !ItemTileContainer.addBlock(player, ((LittleTileBlock)unplaceableTiles.get(j)).block, ((LittleTileBlock)unplaceableTiles.get(j)).meta, (float)((LittleTileBlock)unplaceableTiles.get(j)).getPercentVolume()))
						WorldUtils.dropItem(world, unplaceableTiles.get(j).getDrops(), pos);
				}
			}
			
			/*TileEntity tileEntity = world.getTileEntity(x, y, z);
			if(tileEntity instanceof TileEntityLittleTiles)
			{
				for (int i = 0; i < previews.size(); i++) {
					PreviewTile tile = previews.get(i);
					
				}
			}*/
			return true;
		}
       return false;
    }

	@Override
	public ArrayList<LittleTilePreview> getLittlePreview(ItemStack stack) {
		ArrayList<LittleTilePreview> previews = new ArrayList<LittleTilePreview>();
		previews.add(LittleTilePreview.getPreviewFromNBT(stack.getTagCompound()));
		return previews;
	}
	
	@Override
	public void rotateLittlePreview(ItemStack stack, EnumFacing direction) {
		LittleTilePreview.rotatePreview(stack.getTagCompound(), direction);
	}
	
	public static ArrayList<CubeObject> getItemRenderingCubes(ItemStack stack) {
		ArrayList<CubeObject> cubes = new ArrayList<CubeObject>();
		Block block = Block.getBlockFromName(stack.getTagCompound().getString("block"));
		int meta = stack.getTagCompound().getInteger("meta");
		LittleTileSize size = new LittleTileSize("size", stack.getTagCompound());
		if(!(block instanceof BlockAir))
		{
			CubeObject cube = new LittleTileBox(new LittleTileVec(8, 8, 8), size).getCube();
			cube.block = block;
			cube.meta = meta;
			if(stack.getTagCompound().hasKey("color"))
				cube.color = stack.getTagCompound().getInteger("color");
			cubes.add(cube);
		}
		return cubes;
	}
	
	@Override
	public ArrayList<CubeObject> getRenderingCubes(ItemStack stack) {
		return getItemRenderingCubes(stack);
	}

	@Override
	public boolean hasBackground(ItemStack stack) {
		return false;
	}

	@Override
	public LittleStructure getLittleStructure(ItemStack stack) {
		return null;
	}

	@Override
	public void flipLittlePreview(ItemStack stack, EnumFacing direction) {
		//No need to flip one single tile!
	}

	@Override
	public ArrayList<CubeObject> getRenderingCubes(IBlockState state, TileEntity te, ItemStack stack) {
		if(stack != null)
			return getRenderingCubes(stack);
		return new ArrayList<>();
	}

}
