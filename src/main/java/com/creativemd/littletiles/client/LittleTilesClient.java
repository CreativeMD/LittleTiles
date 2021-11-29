package com.creativemd.littletiles.client;

import java.lang.reflect.Field;
import java.util.UUID;

import org.lwjgl.input.Keyboard;

import com.creativemd.creativecore.client.CreativeCoreClient;
import com.creativemd.creativecore.client.key.ExtendedKeyBinding;
import com.creativemd.creativecore.client.rendering.model.CreativeBlockRenderHelper;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.gui.controls.GuiAxisIndicatorControl;
import com.creativemd.littletiles.client.interact.LittleInteractionHandlerClient;
import com.creativemd.littletiles.client.render.entity.RenderSizedTNTPrimed;
import com.creativemd.littletiles.client.render.overlay.LittleTilesProfilerOverlay;
import com.creativemd.littletiles.client.render.overlay.OverlayControl;
import com.creativemd.littletiles.client.render.overlay.OverlayRenderer;
import com.creativemd.littletiles.client.render.overlay.OverlayRenderer.OverlayPositionType;
import com.creativemd.littletiles.client.render.overlay.PreviewRenderer;
import com.creativemd.littletiles.client.render.overlay.TooltipOverlay;
import com.creativemd.littletiles.client.render.world.LittleChunkDispatcher;
import com.creativemd.littletiles.client.render.world.TileEntityTilesRenderer;
import com.creativemd.littletiles.client.tooltip.CompiledActionMessage;
import com.creativemd.littletiles.common.block.BlockLittleDyeable;
import com.creativemd.littletiles.common.block.BlockLittleDyeable2;
import com.creativemd.littletiles.common.block.BlockLittleDyeableTransparent;
import com.creativemd.littletiles.common.block.BlockTile;
import com.creativemd.littletiles.common.command.DebugCommand;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.entity.EntitySizedTNTPrimed;
import com.creativemd.littletiles.common.item.ItemLittleChisel;
import com.creativemd.littletiles.common.item.ItemLittleGrabber;
import com.creativemd.littletiles.common.item.ItemLittlePaintBrush;
import com.creativemd.littletiles.common.item.ItemLittleRecipe;
import com.creativemd.littletiles.common.item.ItemLittleRecipeAdvanced;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTilesRendered;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTilesTickingRendered;
import com.creativemd.littletiles.common.util.tooltip.ActionMessage;
import com.creativemd.littletiles.common.world.WorldAnimationHandler;
import com.creativemd.littletiles.server.LittleTilesServer;
import com.google.common.base.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ClientCommandHandler;
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
public class LittleTilesClient extends LittleTilesServer {
    
    Minecraft mc = Minecraft.getMinecraft();
    
    public static KeyBinding flip;
    public static KeyBinding mark;
    public static KeyBinding configure;
    public static KeyBinding configureAdvanced;
    public static ExtendedKeyBinding up;
    public static ExtendedKeyBinding down;
    public static ExtendedKeyBinding right;
    public static ExtendedKeyBinding left;
    
    public static KeyBinding undo;
    public static KeyBinding redo;
    
    public static TileEntityTilesRenderer tileEntityRenderer;
    
    public static OverlayRenderer overlay;
    public static LittleInteractionHandlerClient INTERACTION;
    
    private static Field entityUUIDField = ReflectionHelper.findField(EntitySpawnMessage.class, "entityUUID");
    private static Field entityIdField = ReflectionHelper.findField(EntityMessage.class, "entityId");
    private static Field rawXField = ReflectionHelper.findField(EntitySpawnMessage.class, "rawX");
    private static Field rawYField = ReflectionHelper.findField(EntitySpawnMessage.class, "rawY");
    private static Field rawZField = ReflectionHelper.findField(EntitySpawnMessage.class, "rawZ");
    private static Field scaledYawField = ReflectionHelper.findField(EntitySpawnMessage.class, "scaledYaw");
    private static Field scaledPitchField = ReflectionHelper.findField(EntitySpawnMessage.class, "scaledPitch");
    
    public static void displayActionMessage(ActionMessage message) {
        overlay.addMessage(new CompiledActionMessage(message));
    }
    
    @Override
    public void loadSidePre() {
        RenderingRegistry.registerEntityRenderingHandler(EntitySizedTNTPrimed.class, new IRenderFactory<EntitySizedTNTPrimed>() {
            
            @Override
            public Render<? super EntitySizedTNTPrimed> createRenderFor(RenderManager manager) {
                return new RenderSizedTNTPrimed(manager);
            }
        });
        
        RenderingRegistry.registerEntityRenderingHandler(EntityAnimation.class, new IRenderFactory<EntityAnimation>() {
            
            @Override
            public Render<? super EntityAnimation> createRenderFor(RenderManager manager) {
                return new Render<EntityAnimation>(manager) {
                    
                    @Override
                    protected ResourceLocation getEntityTexture(EntityAnimation entity) {
                        return TextureMap.LOCATION_BLOCKS_TEXTURE;
                    }
                    
                };
            }
        });
    }
    
    @Override
    public void loadSidePost() {
        mc.getItemColors().registerItemColorHandler(new IItemColor() {
            
            @Override
            @SideOnly(Side.CLIENT)
            public int colorMultiplier(ItemStack stack, int color) {
                if (color == 0)
                    return ColorUtils.WHITE;
                return ItemLittlePaintBrush.getColor(stack);
            }
            
        }, LittleTiles.colorTube);
        
        BlockTile.mc = mc;
        
        MinecraftForge.EVENT_BUS.register(overlay = new OverlayRenderer());
        MinecraftForge.EVENT_BUS.register(new PreviewRenderer());
        
        up = new ExtendedKeyBinding("key.rotateup", Keyboard.KEY_UP, "key.categories.littletiles");
        down = new ExtendedKeyBinding("key.rotatedown", Keyboard.KEY_DOWN, "key.categories.littletiles");
        right = new ExtendedKeyBinding("key.rotateright", Keyboard.KEY_RIGHT, "key.categories.littletiles");
        left = new ExtendedKeyBinding("key.rotateleft", Keyboard.KEY_LEFT, "key.categories.littletiles");
        
        flip = new KeyBinding("key.little.flip", Keyboard.KEY_G, "key.categories.littletiles");
        mark = new KeyBinding("key.little.mark", Keyboard.KEY_M, "key.categories.littletiles");
        mark = new KeyBinding("key.little.mark", Keyboard.KEY_M, "key.categories.littletiles");
        configure = new KeyBinding("key.little.config.item", net.minecraftforge.client.settings.KeyConflictContext.UNIVERSAL, KeyModifier.NONE, Keyboard.KEY_C, "key.categories.littletiles");
        configureAdvanced = new KeyBinding("key.little.config", net.minecraftforge.client.settings.KeyConflictContext.UNIVERSAL, KeyModifier.CONTROL, Keyboard.KEY_C, "key.categories.littletiles");
        
        undo = new KeyBinding("key.little.undo", net.minecraftforge.client.settings.KeyConflictContext.UNIVERSAL, KeyModifier.CONTROL, Keyboard.KEY_Z, "key.categories.littletiles");
        redo = new KeyBinding("key.little.redo", net.minecraftforge.client.settings.KeyConflictContext.UNIVERSAL, KeyModifier.CONTROL, Keyboard.KEY_Y, "key.categories.littletiles");
        
        ClientRegistry.registerKeyBinding(up);
        ClientRegistry.registerKeyBinding(down);
        ClientRegistry.registerKeyBinding(right);
        ClientRegistry.registerKeyBinding(left);
        ClientRegistry.registerKeyBinding(flip);
        ClientRegistry.registerKeyBinding(mark);
        ClientRegistry.registerKeyBinding(configure);
        
        ClientRegistry.registerKeyBinding(undo);
        ClientRegistry.registerKeyBinding(redo);
        
        EntityRegistry.instance().lookupModSpawn(EntityAnimation.class, false).setCustomSpawning(new Function<EntitySpawnMessage, Entity>() {
            @Override
            public Entity apply(EntitySpawnMessage input) {
                try {
                    UUID uuid = (UUID) entityUUIDField.get(input);
                    EntityAnimation animation = WorldAnimationHandler.getHandlerClient().findAnimation(uuid);
                    boolean alreadyExisted = animation != null;
                    if (animation == null) {
                        animation = new EntityAnimation(mc.world);
                        animation.setUniqueId(uuid);
                    } else
                        animation.spawnedInWorld = true;
                    
                    if (animation != null) {
                        animation.setEntityId(entityIdField.getInt(input));
                        double rawX = rawXField.getDouble(input);
                        double rawY = rawYField.getDouble(input);
                        double rawZ = rawZField.getDouble(input);
                        float scaledYaw = scaledYawField.getFloat(input);
                        float scaledPitch = scaledPitchField.getFloat(input);
                        if (!alreadyExisted)
                            animation.setInitialPosition(rawX, rawY, rawZ);
                    }
                    
                    return animation;
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                
            }
            
        }, false);
        
        CreativeCoreClient.registerItemColorHandler(LittleTiles.recipe);
        CreativeCoreClient.registerItemColorHandler(LittleTiles.recipeAdvanced);
        CreativeCoreClient.registerItemColorHandler(LittleTiles.chisel);
        CreativeCoreClient.registerItemColorHandler(LittleTiles.multiTiles);
        CreativeCoreClient.registerItemColorHandler(LittleTiles.grabber);
        CreativeCoreClient.registerItemColorHandler(LittleTiles.premade);
        CreativeCoreClient.registerItemColorHandler(LittleTiles.blockIngredient);
        CreativeCoreClient.registerBlockColorHandler(LittleTiles.blockTileNoTicking);
        CreativeCoreClient.registerBlockColorHandler(LittleTiles.blockTileTicking);
        CreativeCoreClient.registerBlockColorHandler(LittleTiles.blockTileNoTickingRendered);
        CreativeCoreClient.registerBlockColorHandler(LittleTiles.blockTileTickingRendered);
        
        ClientCommandHandler.instance.registerCommand(new DebugCommand());
        
        // Init overlays
        MinecraftForge.EVENT_BUS.register(LittleTilesProfilerOverlay.class);
        MinecraftForge.EVENT_BUS.register(TooltipOverlay.class);
        
        overlay.add(new OverlayControl(new GuiAxisIndicatorControl("axis", 0, 0), OverlayPositionType.CENTER).setShouldRender(() -> PreviewRenderer.marked != null));
        
        IReloadableResourceManager reloadableResourceManager = (IReloadableResourceManager) mc.getResourceManager();
        reloadableResourceManager.registerReloadListener(new IResourceManagerReloadListener() {
            @Override
            public void onResourceManagerReload(IResourceManager resourceManager) {
                LittleChunkDispatcher.currentRenderState++;
                ItemLittleChisel.model = null;
                ItemLittleGrabber.model = null;
                ItemLittleRecipe.model = null;
                ItemLittleRecipeAdvanced.model = null;
            }
        });
        
        INTERACTION = new LittleInteractionHandlerClient();
    }
    
    @Override
    public void loadSide() {
        tileEntityRenderer = new TileEntityTilesRenderer();
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLittleTilesRendered.class, tileEntityRenderer);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLittleTilesTickingRendered.class, tileEntityRenderer);
        
        CreativeBlockRenderHelper.registerCreativeRenderedBlock(LittleTiles.blockTileNoTicking);
        CreativeBlockRenderHelper.registerCreativeRenderedBlock(LittleTiles.blockTileTicking);
        CreativeBlockRenderHelper.registerCreativeRenderedBlock(LittleTiles.blockTileNoTickingRendered);
        CreativeBlockRenderHelper.registerCreativeRenderedBlock(LittleTiles.blockTileTickingRendered);
        
        CreativeCoreClient.registerBlockItem(LittleTiles.storageBlock);
        
        CreativeCoreClient.registerBlockModels(LittleTiles.dyeableBlock, LittleTiles.modid, "colored_block_", BlockLittleDyeable.LittleDyeableType.values());
        CreativeCoreClient
            .registerBlockModels(LittleTiles.dyeableBlockTransparent, LittleTiles.modid, "colored_transparent_block_", BlockLittleDyeableTransparent.LittleDyeableTransparent
                .values());
        CreativeCoreClient.registerBlockModels(LittleTiles.dyeableBlock2, LittleTiles.modid, "colored_block_", BlockLittleDyeable2.LittleDyeableType2.values());
        
        CreativeCoreClient.registerBlockItem(LittleTiles.signalConverter);
        
        CreativeCoreClient.registerBlockItem(LittleTiles.flowingWater);
        CreativeCoreClient.registerBlockItem(LittleTiles.whiteFlowingWater);
        CreativeCoreClient.registerBlockItem(LittleTiles.flowingLava);
        CreativeCoreClient.registerBlockItem(LittleTiles.whiteFlowingLava);
        
        CreativeCoreClient.registerItemRenderer(LittleTiles.hammer);
        CreativeCoreClient.registerItemRenderer(LittleTiles.recipe);
        CreativeCoreClient.registerItemRenderer(LittleTiles.recipeAdvanced);
        CreativeCoreClient.registerItemRenderer(LittleTiles.saw);
        CreativeCoreClient.registerItemRenderer(LittleTiles.container);
        CreativeCoreClient.registerItemRenderer(LittleTiles.wrench);
        CreativeCoreClient.registerItemRenderer(LittleTiles.chisel);
        CreativeCoreClient.registerItemRenderer(LittleTiles.screwdriver);
        CreativeCoreClient.registerItemRenderer(LittleTiles.colorTube);
        CreativeCoreClient.registerItemRenderer(LittleTiles.rubberMallet);
        CreativeCoreClient.registerItemRenderer(LittleTiles.utilityKnife);
        CreativeCoreClient.registerItemRenderer(LittleTiles.grabber);
        CreativeCoreClient.registerItemRenderer(LittleTiles.premade);
        
        CreativeCoreClient.registerItemRenderer(LittleTiles.blockIngredient);
        
        for (int i = 0; i <= 5; i++) {
            ModelLoader.setCustomModelResourceLocation(LittleTiles.blackColorIngredient, i, new ModelResourceLocation(LittleTiles.blackColorIngredient.getRegistryName()
                .toString() + i, "inventory"));
            ModelLoader.setCustomModelResourceLocation(LittleTiles.cyanColorIngredient, i, new ModelResourceLocation(LittleTiles.cyanColorIngredient.getRegistryName()
                .toString() + i, "inventory"));
            ModelLoader.setCustomModelResourceLocation(LittleTiles.magentaColorIngredient, i, new ModelResourceLocation(LittleTiles.magentaColorIngredient.getRegistryName()
                .toString() + i, "inventory"));
            ModelLoader.setCustomModelResourceLocation(LittleTiles.yellowColorIngredient, i, new ModelResourceLocation(LittleTiles.yellowColorIngredient.getRegistryName()
                .toString() + i, "inventory"));
        }
        
        CreativeBlockRenderHelper.registerCreativeRenderedItem(LittleTiles.multiTiles);
        
        CreativeBlockRenderHelper.registerCreativeRenderedItem(LittleTiles.recipeAdvanced);
        ModelLoader.setCustomModelResourceLocation(LittleTiles.recipeAdvanced, 0, new ModelResourceLocation(LittleTiles.modid + ":recipeadvanced", "inventory"));
        ModelLoader.setCustomModelResourceLocation(LittleTiles.recipeAdvanced, 1, new ModelResourceLocation(LittleTiles.modid + ":recipeadvanced_background", "inventory"));
        
        CreativeBlockRenderHelper.registerCreativeRenderedItem(LittleTiles.recipe);
        ModelLoader.setCustomModelResourceLocation(LittleTiles.recipe, 0, new ModelResourceLocation(LittleTiles.modid + ":recipe", "inventory"));
        ModelLoader.setCustomModelResourceLocation(LittleTiles.recipe, 1, new ModelResourceLocation(LittleTiles.modid + ":recipe_background", "inventory"));
        
        CreativeBlockRenderHelper.registerCreativeRenderedItem(LittleTiles.chisel);
        ModelLoader.setCustomModelResourceLocation(LittleTiles.chisel, 0, new ModelResourceLocation(LittleTiles.modid + ":chisel", "inventory"));
        ModelLoader.setCustomModelResourceLocation(LittleTiles.chisel, 1, new ModelResourceLocation(LittleTiles.modid + ":chisel_background", "inventory"));
        
        CreativeBlockRenderHelper.registerCreativeRenderedItem(LittleTiles.grabber);
        ModelLoader.setCustomModelResourceLocation(LittleTiles.grabber, 0, new ModelResourceLocation(LittleTiles.modid + ":grabber", "inventory"));
        ModelLoader.setCustomModelResourceLocation(LittleTiles.grabber, 1, new ModelResourceLocation(LittleTiles.modid + ":grabber_background", "inventory"));
        
        CreativeBlockRenderHelper.registerCreativeRenderedItem(LittleTiles.premade);
        CreativeBlockRenderHelper.registerCreativeRenderedItem(LittleTiles.blockIngredient);
    }
    
}
