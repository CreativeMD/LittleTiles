package com.creativemd.littletiles.client;

import java.util.Iterator;
import java.util.UUID;

import org.lwjgl.input.Keyboard;

import com.creativemd.creativecore.client.rendering.model.CreativeBlockRenderHelper;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.core.CreativeCoreClient;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.OverlayRenderer;
import com.creativemd.littletiles.client.render.PreviewRenderer;
import com.creativemd.littletiles.client.render.RenderUploader;
import com.creativemd.littletiles.client.render.TileEntityTilesRenderer;
import com.creativemd.littletiles.client.render.entity.RenderAnimation;
import com.creativemd.littletiles.client.render.entity.RenderSizedTNTPrimed;
import com.creativemd.littletiles.common.blocks.BlockLTColored;
import com.creativemd.littletiles.common.blocks.BlockLTTransparentColored;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.entity.EntityDoorAnimation;
import com.creativemd.littletiles.common.entity.EntitySizedTNTPrimed;
import com.creativemd.littletiles.common.items.ItemColorTube;
import com.creativemd.littletiles.common.packet.LittleEntityRequestPacket;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.server.LittleTilesServer;
import com.google.common.base.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.network.internal.FMLMessage.EntityMessage;
import net.minecraftforge.fml.common.network.internal.FMLMessage.EntitySpawnMessage;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LittleTilesClient extends LittleTilesServer{
	
	public static KeyBinding flip;	
	public static KeyBinding mark;
	public static KeyBinding up;
	public static KeyBinding down;
	public static KeyBinding right;
	public static KeyBinding left;
	
	public static KeyBinding undo;
	public static KeyBinding redo;	
	
	@Override
	public void loadSidePre()
	{
		RenderingRegistry.registerEntityRenderingHandler(EntitySizedTNTPrimed.class, new IRenderFactory<EntitySizedTNTPrimed>() {

			@Override
			public Render<? super EntitySizedTNTPrimed> createRenderFor(RenderManager manager) {
				return new RenderSizedTNTPrimed(manager);
			}
		});
		
		RenderingRegistry.registerEntityRenderingHandler(EntityDoorAnimation.class, new IRenderFactory<EntityDoorAnimation>() {

			@Override
			public Render<? super EntityDoorAnimation> createRenderFor(RenderManager manager) {
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
		CreativeCoreClient.registerItemRenderer(LittleTiles.screwdriver);
		CreativeCoreClient.registerItemRenderer(LittleTiles.colorTube);
		CreativeCoreClient.registerItemRenderer(LittleTiles.rubberMallet);
		CreativeCoreClient.registerItemRenderer(LittleTiles.utilityKnife);
		
		CreativeBlockRenderHelper.registerCreativeRenderedItem(LittleTiles.multiTiles);
		
		CreativeBlockRenderHelper.registerCreativeRenderedItem(LittleTiles.recipe);	
		ModelLoader.setCustomModelResourceLocation(LittleTiles.recipe, 0, new ModelResourceLocation(LittleTiles.modid + ":recipe", "inventory"));
		ModelLoader.setCustomModelResourceLocation(LittleTiles.recipe, 1, new ModelResourceLocation(LittleTiles.modid + ":recipe_background", "inventory"));
		
		CreativeBlockRenderHelper.registerCreativeRenderedItem(LittleTiles.chisel);	
		ModelLoader.setCustomModelResourceLocation(LittleTiles.chisel, 0, new ModelResourceLocation(LittleTiles.modid + ":chisel", "inventory"));
		ModelLoader.setCustomModelResourceLocation(LittleTiles.chisel, 1, new ModelResourceLocation(LittleTiles.modid + ":chisel_background", "inventory"));
		
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
		
		MinecraftForge.EVENT_BUS.register(new OverlayRenderer());
		MinecraftForge.EVENT_BUS.register(new PreviewRenderer());
		MinecraftForge.EVENT_BUS.register(new RenderUploader());
		
		up = new KeyBinding("key.rotateup", Keyboard.KEY_UP, "key.categories.littletiles");
		down = new KeyBinding("key.rotatedown", Keyboard.KEY_DOWN, "key.categories.littletiles");
		right = new KeyBinding("key.rotateright", Keyboard.KEY_RIGHT, "key.categories.littletiles");
		left = new KeyBinding("key.rotateleft", Keyboard.KEY_LEFT, "key.categories.littletiles");
		
		flip = new KeyBinding("key.flip", Keyboard.KEY_G, "key.categories.littletiles");
		mark = new KeyBinding("key.little.mark", Keyboard.KEY_M, "key.categories.littletiles");
		
		undo = new KeyBinding("key.little.undo", net.minecraftforge.client.settings.KeyConflictContext.UNIVERSAL, KeyModifier.CONTROL, Keyboard.KEY_Z, "key.categories.littletiles");
		redo = new KeyBinding("key.little.redo", net.minecraftforge.client.settings.KeyConflictContext.UNIVERSAL, KeyModifier.CONTROL, Keyboard.KEY_Y, "key.categories.littletiles");
		
		ClientRegistry.registerKeyBinding(up);
		ClientRegistry.registerKeyBinding(down);
		ClientRegistry.registerKeyBinding(right);
		ClientRegistry.registerKeyBinding(left);
		ClientRegistry.registerKeyBinding(flip);
		ClientRegistry.registerKeyBinding(mark);
		
		ClientRegistry.registerKeyBinding(undo);
		ClientRegistry.registerKeyBinding(redo);
		
		EntityRegistry.instance().lookupModSpawn(EntityDoorAnimation.class, false).setCustomSpawning(new Function<EntitySpawnMessage, Entity>(){

			@Override
			public Entity apply(EntitySpawnMessage input) {
				//entity = cls.getConstructor(World.class).newInstance(wc);
				
				UUID uuid = ReflectionHelper.getPrivateValue(EntitySpawnMessage.class, input, "entityUUID");
				EntityDoorAnimation animation = null;
				for (Iterator<Entity> iterator = mc.world.getLoadedEntityList().iterator(); iterator.hasNext();) {
					Entity entity = iterator.next();
					if(entity instanceof EntityDoorAnimation && entity.getUniqueID().equals(uuid))
					{
						animation = (EntityDoorAnimation) entity;
						break;
					}
				}
				
				if(animation == null)
				{
					animation = new EntityDoorAnimation(mc.world);
					
					animation.setUniqueId(uuid);
					//PacketHandler.sendPacketToServer(new LittleEntityRequestPacket(uuid, new NBTTagCompound(), true));
				}else{
					mc.world.removeEntity(animation);
					animation = animation.copy();
					PacketHandler.sendPacketToServer(new LittleEntityRequestPacket(uuid, new NBTTagCompound(), false));
					animation.isDead = true;
					mc.world.loadedEntityList.remove(animation);
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
