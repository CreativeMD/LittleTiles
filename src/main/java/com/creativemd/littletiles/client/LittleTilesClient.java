package com.creativemd.littletiles.client;

import java.util.Iterator;
import java.util.UUID;

import org.lwjgl.input.Keyboard;

import com.creativemd.creativecore.CreativeCore;
import com.creativemd.creativecore.client.rendering.model.CreativeBlockRenderHelper;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.core.CreativeCoreClient;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.PreviewRenderer;
import com.creativemd.littletiles.client.render.RenderUploader;
import com.creativemd.littletiles.client.render.TileEntityTilesRenderer;
import com.creativemd.littletiles.client.render.entity.RenderAnimation;
import com.creativemd.littletiles.client.render.entity.RenderSizedTNTPrimed;
import com.creativemd.littletiles.client.render.optifine.OptifineVertexBuffer;
import com.creativemd.littletiles.common.blocks.BlockLTColored;
import com.creativemd.littletiles.common.blocks.BlockLTTransparentColored;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.entity.EntitySizedTNTPrimed;
import com.creativemd.littletiles.common.items.ItemColorTube;
import com.creativemd.littletiles.common.packet.LittleEntityRequestPacket;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.server.LittleTilesServer;
import com.google.common.base.Function;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderEnderman;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.internal.FMLMessage.EntityMessage;
import net.minecraftforge.fml.common.network.internal.FMLMessage.EntitySpawnMessage;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LittleTilesClient extends LittleTilesServer{
	
	public static KeyBinding flip;
	public static boolean pressedFlip = false;
	
	public static KeyBinding mark;
	public static boolean pressedMark = false;
	
	public static KeyBinding up;
	public static boolean pressedUp = false;
	public static KeyBinding down;
	public static boolean pressedDown = false;
	public static KeyBinding right;
	public static boolean pressedRight = false;
	public static KeyBinding left;
	public static boolean pressedLeft = false;
	/*private static VertexFormat optifineFormat = createOptifineBlockFormat();
	
	private static VertexFormat createOptifineBlockFormat()
	{
		VertexFormat format = new VertexFormat(DefaultVertexFormats.BLOCK);
		format.addElement(DefaultVertexFormats.NORMAL_3B);
		format.addElement(DefaultVertexFormats.PADDING_1B);
		format.addElement(new VertexFormatElement(0, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.PADDING, 2));
		VertexFormatElement PADDING_4S = new VertexFormatElement(0, VertexFormatElement.EnumType.SHORT, VertexFormatElement.EnumUsage.PADDING, 4);
		format.addElement(PADDING_4S);
		format.addElement(PADDING_4S);
		return format;
	}*/
	
	public static VertexFormat getBlockVertexFormat()
	{
		//if(optifineMode)
			//return optifineFormat;
		return DefaultVertexFormats.BLOCK;
	}
	
	public static VertexBuffer createVertexBuffer(int size)
	{
		//if(FMLClientHandler.instance().hasOptifine())
			//return new OptifineVertexBuffer(size);
		return new VertexBuffer(size);
	}
	
	@Override
	public void loadSidePre()
	{
		RenderingRegistry.registerEntityRenderingHandler(EntitySizedTNTPrimed.class, new IRenderFactory<EntitySizedTNTPrimed>() {

			@Override
			public Render<? super EntitySizedTNTPrimed> createRenderFor(RenderManager manager) {
				return new RenderSizedTNTPrimed(manager);
			}
		});
		
		RenderingRegistry.registerEntityRenderingHandler(EntityAnimation.class, new IRenderFactory<EntityAnimation>() {

			@Override
			public Render<? super EntityAnimation> createRenderFor(RenderManager manager) {
				return new RenderAnimation(manager);
			}
		});
	}
	
	@Override
	public void loadSide()
	{		
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLittleTiles.class, new TileEntityTilesRenderer());
		
		Minecraft mc = Minecraft.getMinecraft();
		
		CreativeBlockRenderHelper.registerCreativeRenderedBlock(LittleTiles.blockTile);
		
		CreativeCoreClient.registerBlockItem(LittleTiles.storageBlock);
		CreativeCoreClient.registerBlockItem(LittleTiles.particleBlock);
		
		CreativeCoreClient.registerBlockModels(LittleTiles.coloredBlock, LittleTiles.modid, "colored_block_", BlockLTColored.EnumType.values());
		CreativeCoreClient.registerBlockModels(LittleTiles.transparentColoredBlock, LittleTiles.modid, "colored_transparent_block_", BlockLTTransparentColored.EnumType.values());
		
		CreativeCoreClient.registerItemRenderer(LittleTiles.hammer);
		CreativeCoreClient.registerItemRenderer(LittleTiles.recipe);
		CreativeCoreClient.registerItemRenderer(LittleTiles.saw);
		CreativeCoreClient.registerItemRenderer(LittleTiles.container);
		CreativeCoreClient.registerItemRenderer(LittleTiles.wrench);
		CreativeCoreClient.registerItemRenderer(LittleTiles.chisel);
		CreativeCoreClient.registerItemRenderer(LittleTiles.colorTube);
		CreativeCoreClient.registerItemRenderer(LittleTiles.rubberMallet);
		CreativeCoreClient.registerItemRenderer(LittleTiles.utilityKnife);
		
		CreativeBlockRenderHelper.registerCreativeRenderedItem(LittleTiles.multiTiles);
		
		CreativeBlockRenderHelper.registerCreativeRenderedItem(LittleTiles.recipe);	
		ModelLoader.setCustomModelResourceLocation(LittleTiles.recipe, 0, new ModelResourceLocation(LittleTiles.modid + ":recipe", "inventory"));
		ModelLoader.setCustomModelResourceLocation(LittleTiles.recipe, 1, new ModelResourceLocation(LittleTiles.modid + ":recipe_background", "inventory"));
		
		
		mc.getRenderItem().getItemModelMesher().register(LittleTiles.colorTube, new ItemMeshDefinition()
        {
            public ModelResourceLocation getModelLocation(ItemStack stack)
            {
                return new ModelResourceLocation("LTChisel", "inventory");
            }
        });
		mc.getItemColors().registerItemColorHandler(new IItemColor() {
			
			@Override
			@SideOnly(Side.CLIENT)
		    public int getColorFromItemstack(ItemStack stack, int color)
		    {
				if (color == 0)
		        	return ColorUtils.WHITE;
		        return ItemColorTube.getColor(stack);
		    }
			
		}, LittleTiles.colorTube);
		
		BlockTile.mc = mc;
		
		MinecraftForge.EVENT_BUS.register(new PreviewRenderer());
		MinecraftForge.EVENT_BUS.register(new RenderUploader());
		
		up = new KeyBinding("key.rotateup", Keyboard.KEY_UP, "key.categories.littletiles");
		down = new KeyBinding("key.rotatedown", Keyboard.KEY_DOWN, "key.categories.littletiles");
		right = new KeyBinding("key.rotateright", Keyboard.KEY_RIGHT, "key.categories.littletiles");
		left = new KeyBinding("key.rotateleft", Keyboard.KEY_LEFT, "key.categories.littletiles");
		
		flip = new KeyBinding("key.flip", Keyboard.KEY_G, "key.categories.littletiles");
		mark = new KeyBinding("key.mark", Keyboard.KEY_M, "key.categories.littletiles");
		
		ClientRegistry.registerKeyBinding(up);
		ClientRegistry.registerKeyBinding(down);
		ClientRegistry.registerKeyBinding(right);
		ClientRegistry.registerKeyBinding(left);
		ClientRegistry.registerKeyBinding(flip);
		ClientRegistry.registerKeyBinding(mark);
		
		EntityRegistry.instance().lookupModSpawn(EntityAnimation.class, false).setCustomSpawning(new Function<EntitySpawnMessage, Entity>(){

			@Override
			public Entity apply(EntitySpawnMessage input) {
				//entity = cls.getConstructor(World.class).newInstance(wc);
				
				UUID uuid = ReflectionHelper.getPrivateValue(EntitySpawnMessage.class, input, "entityUUID");
				EntityAnimation animation = null;
				for (Iterator<Entity> iterator = mc.theWorld.getLoadedEntityList().iterator(); iterator.hasNext();) {
					Entity entity = iterator.next();
					if(entity instanceof EntityAnimation && entity.getUniqueID().equals(uuid))
					{
						animation = (EntityAnimation) entity;
						break;
					}
				}
				
				if(animation == null)
				{
					animation = new EntityAnimation(mc.theWorld);
					animation.setUniqueId(uuid);
					PacketHandler.sendPacketToServer(new LittleEntityRequestPacket(uuid, new NBTTagCompound(), true));
				}else{
					mc.theWorld.removeEntity(animation);
					animation = animation.copy();
					PacketHandler.sendPacketToServer(new LittleEntityRequestPacket(uuid, new NBTTagCompound(), false));
					mc.theWorld.loadedEntityList.remove(animation);
					//mc.world.removeEntity(animation);
					//animation.isDead = false;
				}
				
				if(animation != null)
				{
					animation.setEntityId(ReflectionHelper.getPrivateValue(EntityMessage.class, input, "entityId"));
					double rawX = ReflectionHelper.getPrivateValue(EntitySpawnMessage.class, input, "rawX");
					double rawY = ReflectionHelper.getPrivateValue(EntitySpawnMessage.class, input, "rawY");
					double rawZ = ReflectionHelper.getPrivateValue(EntitySpawnMessage.class, input, "rawZ");
					float scaledYaw = ReflectionHelper.getPrivateValue(EntitySpawnMessage.class, input, "scaledYaw");
					float scaledPitch = ReflectionHelper.getPrivateValue(EntitySpawnMessage.class, input, "scaledPitch");
					animation.setLocationAndAngles(rawX, rawY, rawZ, scaledYaw, scaledPitch);
				}

				return animation;
			}
			
		}, false);
	}
	
}
