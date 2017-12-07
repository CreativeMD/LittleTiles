package com.creativemd.littletiles.common.items;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.util.Color;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.client.rendering.model.CreativeBakedModel;
import com.creativemd.creativecore.client.rendering.model.ICreativeRendered;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.common.utils.Rotation;
import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.opener.GuiHandler;
import com.creativemd.creativecore.gui.opener.IGuiCreator;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.container.SubContainerChisel;
import com.creativemd.littletiles.common.container.SubContainerGrabber;
import com.creativemd.littletiles.common.gui.SubGuiChisel;
import com.creativemd.littletiles.common.gui.SubGuiGrabber;
import com.creativemd.littletiles.common.items.geo.DragShape;
import com.creativemd.littletiles.common.packet.LittleBlockPacket;
import com.creativemd.littletiles.common.packet.LittleVanillaBlockPacket;
import com.creativemd.littletiles.common.packet.LittleBlockPacket.BlockPacketAction;
import com.creativemd.littletiles.common.packet.LittleVanillaBlockPacket.VanillaBlockAction;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.LittleTileBlockColored;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemLittleGrabber extends Item implements ICreativeRendered, ILittleTile {
	
	public ItemLittleGrabber()
	{
		setCreativeTab(LittleTiles.littleTab);
		hasSubtypes = true;
		setMaxStackSize(1);
	}
	
	@Override
	public float getDestroySpeed(ItemStack stack, IBlockState state)
    {
        return 0F;
    }
	
	@Override
	public boolean canDestroyBlockInCreative(World world, BlockPos pos, ItemStack stack, EntityPlayer player)
    {
        return false;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public List<RenderCubeObject> getRenderingCubes(IBlockState state, TileEntity te, ItemStack stack) {
		return Collections.emptyList();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void applyCustomOpenGLHackery(ItemStack stack, TransformType cameraTransformType)
	{
		Minecraft mc = Minecraft.getMinecraft();
		GlStateManager.pushMatrix();
		
		IBakedModel model = mc.getRenderItem().getItemModelMesher().getModelManager().getModel(new ModelResourceLocation(LittleTiles.modid + ":grabber_background", "inventory"));
		ForgeHooksClient.handleCameraTransforms(model, cameraTransformType, false);
		
		mc.getRenderItem().renderItem(new ItemStack(Items.PAPER), model);
		
		if(cameraTransformType == TransformType.GUI)
		{
			GlStateManager.scale(0.9, 0.9, 0.9);
			
			LittleTilePreview preview = getPreview(stack);
			ItemStack blockStack = new ItemStack(preview.getPreviewBlock(), 1, preview.getPreviewBlockMeta());
			model =  mc.getRenderItem().getItemModelWithOverrides(blockStack, mc.world, mc.player); //getItemModelMesher().getItemModel(blockStack);
			if(!(model instanceof CreativeBakedModel))
				ForgeHooksClient.handleCameraTransforms(model, cameraTransformType, false);
			
			GlStateManager.disableDepth();
			GlStateManager.pushMatrix();
	        GlStateManager.translate(-0.5F, -0.5F, -0.5F);
	        
	        
			try {
				if (model.isBuiltInRenderer())
	            {
	                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	                GlStateManager.enableRescaleNormal();
	                TileEntityItemStackRenderer.instance.renderByItem(blockStack);
	            }else{
					Color color = preview.hasColor() ? ColorUtils.IntToRGBA(preview.getColor()) : ColorUtils.IntToRGBA(ColorUtils.WHITE);
					color.setAlpha(255);
					ReflectionHelper.findMethod(RenderItem.class, "renderModel", "func_191967_a", IBakedModel.class, int.class, ItemStack.class).invoke(mc.getRenderItem(), model, ColorUtils.RGBAToInt(color), blockStack);
	            }
	        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
			
			GlStateManager.popMatrix();
			
			GlStateManager.enableDepth();
		}
		
		GlStateManager.popMatrix();
		
	}
	
	@Override
	public boolean hasLittlePreview(ItemStack stack)
	{
		return true;
	}
	
	@Override
	public List<LittleTilePreview> getLittlePreview(ItemStack stack)
	{
		List<LittleTilePreview> previews = new ArrayList<>();
		previews.add(getPreview(stack));
		return previews;
	}
	
	@Override
	public void saveLittlePreview(ItemStack stack, List<LittleTilePreview> previews)
	{
		setPreview(stack, previews.get(0));
	}
	
	@Override
	public LittleStructure getLittleStructure(ItemStack stack)
	{
		return null;
	}
	
	@Override
	public boolean containsIngredients(ItemStack stack)
	{
		return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void onClickBlock(EntityPlayer player, ItemStack stack, RayTraceResult result)
	{
		if(player.isSneaking())
		{
			GuiHandler.openGui("grabber", new NBTTagCompound(), player);
		}else{
			IBlockState state = player.world.getBlockState(result.getBlockPos());
			if(SubContainerGrabber.isBlockValid(state.getBlock()))
			{
				PacketHandler.sendPacketToServer(new LittleVanillaBlockPacket(result.getBlockPos(), VanillaBlockAction.GRABBER));
			}
			else if(state.getBlock() instanceof BlockTile)
				PacketHandler.sendPacketToServer(new LittleBlockPacket(result.getBlockPos(), player, BlockPacketAction.GRABBER, new NBTTagCompound()));
		}
	}
	
	/*public static void setSize(ItemStack stack, LittleTileSize size)
	{
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		stack.getTagCompound().setInteger("sizeX", size.sizeX);
		stack.getTagCompound().setInteger("sizeY", size.sizeY);
		stack.getTagCompound().setInteger("sizeZ", size.sizeZ);
	}
	
	public static LittleTileSize getSize(ItemStack stack)
	{
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		if(stack.getTagCompound().hasKey("sizeX"))
			return new LittleTileSize(stack.getTagCompound().getInteger("sizeX"), stack.getTagCompound().getInteger("sizeY"), stack.getTagCompound().getInteger("sizeZ"));
		return new LittleTileSize(1, 1, 1);
	}*/
	
	public static LittleTilePreview getPreview(ItemStack stack)
	{
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		LittleTilePreview preview = null;
		if(stack.getTagCompound().hasKey("preview"))
			preview = LittleTilePreview.loadPreviewFromNBT(stack.getTagCompound().getCompoundTag("preview"));
		else
		{
			IBlockState state = stack.getTagCompound().hasKey("state") ? Block.getStateById(stack.getTagCompound().getInteger("state")) : Blocks.STONE.getDefaultState();
			LittleTile tile = stack.getTagCompound().hasKey("color") ? new LittleTileBlockColored(state.getBlock(), state.getBlock().getMetaFromState(state), stack.getTagCompound().getInteger("color")) : new LittleTileBlock(state.getBlock(), state.getBlock().getMetaFromState(state));
			tile.box = new LittleTileBox(LittleTile.minPos, LittleTile.minPos, LittleTile.minPos, 1, 1, 1);
			preview = tile.getPreviewTile();
			setPreview(stack, preview);
		}
		//LittleTileSize size = getSize(stack);
		//preview.box = new LittleTileBox(0, 0, 0, size.sizeX, size.sizeY, size.sizeZ);		
		return preview;
	}
	
	public static void setPreview(ItemStack stack, LittleTilePreview preview)
	{
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		NBTTagCompound nbt = new NBTTagCompound();	
		preview.writeToNBT(nbt);
		stack.getTagCompound().setTag("preview", nbt);
	}
}
