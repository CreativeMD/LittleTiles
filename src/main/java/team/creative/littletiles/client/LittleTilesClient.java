package team.creative.littletiles.client;

import java.lang.reflect.Field;
import java.util.UUID;

import org.lwjgl.glfw.GLFW;

import com.creativemd.creativecore.client.rendering.model.CreativeBlockRenderHelper;
import com.creativemd.littletiles.client.gui.controls.GuiAxisIndicatorControl;
import com.creativemd.littletiles.client.render.entity.RenderSizedTNTPrimed;
import com.creativemd.littletiles.client.render.overlay.LittleTilesProfilerOverlay;
import com.creativemd.littletiles.client.render.overlay.OverlayControl;
import com.creativemd.littletiles.client.render.overlay.OverlayRenderer;
import com.creativemd.littletiles.client.render.overlay.OverlayRenderer.OverlayPositionType;
import com.creativemd.littletiles.client.render.overlay.PreviewRenderer;
import com.creativemd.littletiles.client.render.overlay.TooltipOverlay;
import com.creativemd.littletiles.client.render.world.LittleChunkDispatcher;
import com.creativemd.littletiles.client.tooltip.CompiledActionMessage;
import com.creativemd.littletiles.common.block.BlockLittleDyeable;
import com.creativemd.littletiles.common.block.BlockLittleDyeable2;
import com.creativemd.littletiles.common.block.BlockLittleDyeableTransparent;
import com.creativemd.littletiles.common.util.tooltip.ActionMessage;
import com.google.common.base.Function;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.internal.FMLMessage.EntitySpawnMessage;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.fmlclient.registry.ClientRegistry;
import net.minecraftforge.fmlclient.registry.IRenderFactory;
import net.minecraftforge.fmlclient.registry.RenderingRegistry;
import team.creative.creativecore.client.CreativeCoreClient;
import team.creative.creativecore.client.command.ClientCommandRegistry;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.level.LevelHandlersClient;
import team.creative.littletiles.client.render.block.BETilesRenderer;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.entity.EntityAnimation;
import team.creative.littletiles.common.entity.PrimedSizedTnt;
import team.creative.littletiles.common.item.ItemLittleChisel;
import team.creative.littletiles.common.item.ItemLittleGrabber;
import team.creative.littletiles.common.item.ItemLittlePaintBrush;
import team.creative.littletiles.common.item.ItemLittleRecipe;
import team.creative.littletiles.common.item.ItemLittleRecipeAdvanced;
import team.creative.littletiles.common.level.WorldAnimationHandler;

@OnlyIn(Dist.CLIENT)
public class LittleTilesClient {
    
    private static Field entityUUIDField = ObfuscationReflectionHelper.findField(ClientboundAddEntityPacket.class, "entityUUID");
    private static Field entityIdField = ObfuscationReflectionHelper.findField(ClientboundAddEntityPacket.class, "entityId");
    private static Field rawXField = ObfuscationReflectionHelper.findField(ClientboundAddEntityPacket.class, "rawX");
    private static Field rawYField = ObfuscationReflectionHelper.findField(ClientboundAddEntityPacket.class, "rawY");
    private static Field rawZField = ObfuscationReflectionHelper.findField(ClientboundAddEntityPacket.class, "rawZ");
    private static Field scaledYawField = ObfuscationReflectionHelper.findField(ClientboundAddEntityPacket.class, "scaledYaw");
    private static Field scaledPitchField = ObfuscationReflectionHelper.findField(ClientboundAddEntityPacket.class, "scaledPitch");
    
    public static final Minecraft mc = Minecraft.getInstance();
    
    public static final LevelHandlersClient LEVEL_HANDLERS = new LevelHandlersClient();
    
    public static KeyMapping flip;
    public static KeyMapping mark;
    public static KeyMapping configure;
    public static KeyMapping configureAdvanced;
    public static KeyMapping up;
    public static KeyMapping down;
    public static KeyMapping right;
    public static KeyMapping left;
    
    public static KeyMapping undo;
    public static KeyMapping redo;
    
    public static BETilesRenderer blockEntityRenderer;
    
    public static OverlayRenderer overlay;
    
    public static void displayActionMessage(ActionMessage message) {
        overlay.addMessage(new CompiledActionMessage(message));
    }
    
    public static void setup(final FMLClientSetupEvent event) {
        mc.getItemColors().register(new ItemColor() {
            
            @Override
            public int getColor(ItemStack stack, int color) {
                if (color == 0)
                    return ColorUtils.WHITE;
                return ItemLittlePaintBrush.getColor(stack);
            }
        }, LittleTiles.colorTube);
        
        BlockTile.mc = mc;
        
        MinecraftForge.EVENT_BUS.register(overlay = new OverlayRenderer());
        MinecraftForge.EVENT_BUS.register(new PreviewRenderer());
        
        up = new KeyMapping("key.rotateup", GLFW.GLFW_KEY_UP, "key.categories.littletiles");
        down = new KeyMapping("key.rotatedown", GLFW.GLFW_KEY_DOWN, "key.categories.littletiles");
        right = new KeyMapping("key.rotateright", GLFW.GLFW_KEY_RIGHT, "key.categories.littletiles");
        left = new KeyMapping("key.rotateleft", GLFW.GLFW_KEY_LEFT, "key.categories.littletiles");
        
        flip = new KeyMapping("key.little.flip", GLFW.GLFW_KEY_G, "key.categories.littletiles");
        mark = new KeyMapping("key.little.mark", GLFW.GLFW_KEY_M, "key.categories.littletiles");
        mark = new KeyMapping("key.little.mark", GLFW.GLFW_KEY_M, "key.categories.littletiles");
        configure = new KeyMapping("key.little.config.item", KeyConflictContext.UNIVERSAL, KeyModifier.NONE, InputConstants.Type.KEYSYM, InputConstants.KEY_C, "key.categories.littletiles");
        configureAdvanced = new KeyMapping("key.little.config", KeyConflictContext.UNIVERSAL, KeyModifier.CONTROL, InputConstants.Type.KEYSYM, InputConstants.KEY_C, "key.categories.littletiles");
        
        undo = new KeyMapping("key.little.undo", KeyConflictContext.UNIVERSAL, KeyModifier.CONTROL, InputConstants.Type.KEYSYM, InputConstants.KEY_Z, "key.categories.littletiles");
        redo = new KeyMapping("key.little.redo", KeyConflictContext.UNIVERSAL, KeyModifier.CONTROL, InputConstants.Type.KEYSYM, InputConstants.KEY_Y, "key.categories.littletiles");
        
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
                    } else {
                        animation.spawnedInWorld = true;
                        animation.controller.onServerApproves();
                    }
                    
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
        
        CreativeCoreClient.registerClientConfig(LittleTiles.MODID);
        RenderingRegistry.registerEntityRenderingHandler(PrimedSizedTnt.class, new IRenderFactory<PrimedSizedTnt>() {
            
            @Override
            public Render<? super PrimedSizedTnt> createRenderFor(RenderManager manager) {
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
        
        blockEntityRenderer = new BETilesRenderer();
        BlockEntityRenderers.register(LittleTiles.BE_TILES_TYPE_RENDERED, x -> blockEntityRenderer);
        
        CreativeBlockRenderHelper.registerCreativeRenderedBlock(LittleTiles.BLOCK_TILES);
        CreativeBlockRenderHelper.registerCreativeRenderedBlock(LittleTiles.BLOCK_TILES_TICKING);
        CreativeBlockRenderHelper.registerCreativeRenderedBlock(LittleTiles.BLOCK_TILES_RENDERED);
        CreativeBlockRenderHelper.registerCreativeRenderedBlock(LittleTiles.BLOCK_TILES_TICKING_RENDERED);
        
        CreativeCoreClient.registerBlockItem(LittleTiles.STORAGE_BLOCK);
        
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
        
        for (
                
                int i = 0; i <= 5; i++) {
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
        
        event.enqueueWork(() -> {
            ClientCommandRegistry.register(LiteralArgumentBuilder.<SharedSuggestionProvider>literal("lt-debug").executes(x -> {
                if (LittleTilesProfilerOverlay.isActive())
                    LittleTilesProfilerOverlay.stop();
                else
                    LittleTilesProfilerOverlay.start();
                return Command.SINGLE_SUCCESS;
            }));
        });
    }
    
}
