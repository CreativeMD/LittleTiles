package team.creative.littletiles.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.creativemd.creativecore.client.rendering.model.CreativeBlockRenderHelper;
import com.creativemd.littletiles.common.block.BlockLittleDyeable;
import com.creativemd.littletiles.common.block.BlockLittleDyeable2;
import com.creativemd.littletiles.common.block.BlockLittleDyeableTransparent;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import team.creative.creativecore.client.CreativeCoreClient;
import team.creative.creativecore.client.command.ClientCommandRegistry;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.client.render.model.CreativeRenderBlock;
import team.creative.creativecore.client.render.model.CreativeRenderItem;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.client.level.LevelHandlersClient;
import team.creative.littletiles.client.level.LittleAnimationHandlerClient;
import team.creative.littletiles.client.render.block.BETilesRenderer;
import team.creative.littletiles.client.render.entity.RenderSizedTNTPrimed;
import team.creative.littletiles.client.render.item.LittleRenderToolBackground;
import team.creative.littletiles.client.render.item.LittleRenderToolBig;
import team.creative.littletiles.client.render.item.LittleRenderToolPreview;
import team.creative.littletiles.client.render.level.LittleChunkDispatcher;
import team.creative.littletiles.client.render.level.LittleClientEventHandler;
import team.creative.littletiles.client.render.overlay.LittleTilesProfilerOverlay;
import team.creative.littletiles.client.render.overlay.OverlayControl;
import team.creative.littletiles.client.render.overlay.OverlayRenderer;
import team.creative.littletiles.client.render.overlay.OverlayRenderer.OverlayPositionType;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.client.render.overlay.TooltipOverlay;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.controls.GuiAxisIndicatorControl;
import team.creative.littletiles.common.ingredient.BlockIngredientEntry;
import team.creative.littletiles.common.item.ItemBlockIngredient;
import team.creative.littletiles.common.item.ItemLittleChisel;
import team.creative.littletiles.common.item.ItemLittleGlove;
import team.creative.littletiles.common.item.ItemLittlePaintBrush;
import team.creative.littletiles.common.item.ItemPremadeStructure;
import team.creative.littletiles.common.structure.type.premade.LittleStructurePremade;
import team.creative.littletiles.common.structure.type.premade.LittleStructurePremade.LittleStructureTypePremade;

@OnlyIn(Dist.CLIENT)
public class LittleTilesClient {
    
    public static final Minecraft mc = Minecraft.getInstance();
    
    public static final LevelHandlersClient LEVEL_HANDLERS = new LevelHandlersClient();
    public static LittleActionHandlerClient ACTION_HANDLER;
    public static LittleAnimationHandlerClient ANIMATION_HANDLER;
    
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
    
    public static void displayActionMessage(List<Component> message) {
        overlay.addMessage(message);
    }
    
    public static void setup(final FMLClientSetupEvent event) {
        mc.getItemColors().register((stack, color) -> {
            if (color == 0)
                return ColorUtils.WHITE;
            return ItemLittlePaintBrush.getColor(stack);
        }, LittleTiles.PAINT_BRUSH);
        
        BlockTile.mc = mc;
        
        MinecraftForge.EVENT_BUS.register(overlay = new OverlayRenderer());
        MinecraftForge.EVENT_BUS.register(new PreviewRenderer());
        MinecraftForge.EVENT_BUS.register(new LittleClientEventHandler());
        
        LEVEL_HANDLERS.register(LittleActionHandlerClient::new, x -> ACTION_HANDLER = x);
        LEVEL_HANDLERS.register(LittleAnimationHandlerClient::new, x -> ANIMATION_HANDLER = x);
        
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
        
        CreativeCoreClient.registerItem(new LittleRenderToolBig(), LittleTiles.ITEM_TILES);
        CreativeCoreClient.registerItem(new LittleRenderToolBig() {
            @Override
            public List<? extends RenderBox> getBoxes(ItemStack stack, RenderType layer) {
                if (!stack.getOrCreateTag().contains("structure"))
                    return Collections.EMPTY_LIST;
                
                LittleStructureTypePremade premade = LittleStructurePremade.getType(stack.getOrCreateTagElement("structure").getString("id"));
                if (premade == null)
                    return Collections.EMPTY_LIST;
                LittleGroup previews = ((ItemPremadeStructure) stack.getItem()).getTiles(stack);
                if (previews == null)
                    return Collections.EMPTY_LIST;
                List<RenderBox> cubes = premade.getItemPreview(previews, layer == Sheets.translucentCullBlockSheet());
                if (cubes == null) {
                    cubes = previews.getRenderingBoxes(layer == Sheets.translucentCullBlockSheet());
                    LittleGroup.shrinkCubesToOneBlock(cubes);
                }
                
                return cubes;
            }
        }, LittleTiles.PREMADE);
        
        CreativeCoreClient.registerItem(new LittleRenderToolPreview(new ResourceLocation(LittleTiles.MODID, "glove_background"), stack -> ItemLittleGlove.getMode(stack)
                .getSeparateRenderingPreview(stack)) {
            @Override
            public boolean shouldRenderPreview(ItemStack stack) {
                return ItemLittleGlove.getMode(stack).renderBlockSeparately(stack);
            }
        }, LittleTiles.GLOVE);
        CreativeCoreClient.registerItem(new LittleRenderToolPreview(new ResourceLocation(LittleTiles.MODID, "chisel_background"), stack -> ItemLittleChisel
                .getElement(stack)), LittleTiles.CHISEL);
        CreativeCoreClient.registerItem(new LittleRenderToolBackground(new ResourceLocation(LittleTiles.MODID, "blueprint_background")), LittleTiles.BLUEPRINT);
        
        CreativeCoreClient.registerItem(new CreativeRenderItem() {
            
            @Override
            public List<? extends RenderBox> getBoxes(ItemStack stack, RenderType layer) {
                List<RenderBox> cubes = new ArrayList<>();
                BlockIngredientEntry ingredient = ItemBlockIngredient.loadIngredient(stack);
                if (ingredient == null)
                    return null;
                
                double volume = Math.min(1, ingredient.value);
                LittleGrid context = LittleGrid.defaultGrid();
                int pixels = (int) (volume * context.count3d);
                if (pixels < context.count * context.count)
                    cubes.add(new RenderBox(0.4F, 0.4F, 0.4F, 0.6F, 0.6F, 0.6F, ingredient.block.getState()));
                else {
                    int remainingPixels = pixels;
                    int planes = pixels / context.count2d;
                    remainingPixels -= planes * context.count2d;
                    int rows = remainingPixels / context.count;
                    remainingPixels -= rows * context.count;
                    
                    float height = (float) (planes * context.pixelLength);
                    
                    if (planes > 0)
                        cubes.add(new RenderBox(0.0F, 0.0F, 0.0F, 1.0F, height, 1.0F, ingredient.block.getState()));
                    
                    float width = (float) (rows * context.pixelLength);
                    
                    if (rows > 0)
                        cubes.add(new RenderBox(0.0F, height, 0.0F, 1.0F, height + (float) context.pixelLength, width, ingredient.block.getState()));
                    
                    if (remainingPixels > 0)
                        cubes.add(new RenderBox(0.0F, height, width, 1.0F, height + (float) context.pixelLength, width + (float) context.pixelLength, ingredient.block.getState()));
                }
                return cubes;
            }
        }, LittleTiles.BLOCK_INGREDIENT);
        
        // Init overlays
        MinecraftForge.EVENT_BUS.register(LittleTilesProfilerOverlay.class);
        MinecraftForge.EVENT_BUS.register(TooltipOverlay.class);
        
        overlay.add(new OverlayControl(new GuiAxisIndicatorControl("axis"), OverlayPositionType.CENTER).setShouldRender(() -> PreviewRenderer.marked != null));
        
        ReloadableResourceManager reloadableResourceManager = (ReloadableResourceManager) mc.getResourceManager();
        reloadableResourceManager.registerReloadListener(new SimplePreparableReloadListener() {
            
            @Override
            protected void apply(Object p_10793_, ResourceManager p_10794_, ProfilerFiller p_10795_) {
                LittleChunkDispatcher.currentRenderState++;
            }
            
            @Override
            protected Object prepare(ResourceManager p_10796_, ProfilerFiller p_10797_) {
                return null;
            }
        });
        
        CreativeCoreClient.registerClientConfig(LittleTiles.MODID);
        
        EntityRenderers.register(LittleTiles.SIZED_TNT_TYPE, RenderSizedTNTPrimed::new);
        
        blockEntityRenderer = new BETilesRenderer();
        BlockEntityRenderers.register(LittleTiles.BE_TILES_TYPE_RENDERED, x -> blockEntityRenderer);
        
        CreativeCoreClient.registerBlocks(new CreativeRenderBlock() {
            
            @Override
            public List<? extends RenderBox> getBoxes(BlockState state) {
                return Collections.EMPTY_LIST;
            }
            
        }, LittleTiles.BLOCK_TILES, LittleTiles.BLOCK_TILES_TICKING, LittleTiles.BLOCK_TILES_RENDERED, LittleTiles.BLOCK_TILES_TICKING_RENDERED);
        
        CreativeCoreClient.registerBlockItem(LittleTiles.STORAGE_BLOCK);
        
        CreativeCoreClient.registerBlockModels(LittleTiles.dyeableBlock, LittleTiles.MODID, "colored_block_", BlockLittleDyeable.LittleDyeableType.values());
        CreativeCoreClient
                .registerBlockModels(LittleTiles.dyeableBlockTransparent, LittleTiles.MODID, "colored_transparent_block_", BlockLittleDyeableTransparent.LittleDyeableTransparent
                        .values());
        CreativeCoreClient.registerBlockModels(LittleTiles.dyeableBlock2, LittleTiles.MODID, "colored_block_", BlockLittleDyeable2.LittleDyeableType2.values());
        
        CreativeCoreClient.registerBlockItem(LittleTiles.signalConverter);
        
        CreativeCoreClient.registerBlockItem(LittleTiles.flowingWater);
        CreativeCoreClient.registerBlockItem(LittleTiles.whiteFlowingWater);
        CreativeCoreClient.registerBlockItem(LittleTiles.flowingLava);
        CreativeCoreClient.registerBlockItem(LittleTiles.whiteFlowingLava);
        
        CreativeCoreClient.registerItemRenderer(LittleTiles.hammer);
        CreativeCoreClient.registerItemRenderer(LittleTiles.saw);
        CreativeCoreClient.registerItemRenderer(LittleTiles.container);
        CreativeCoreClient.registerItemRenderer(LittleTiles.wrench);
        CreativeCoreClient.registerItemRenderer(LittleTiles.screwdriver);
        CreativeCoreClient.registerItemRenderer(LittleTiles.colorTube);
        
        for (int i = 0; i <= 5; i++) {
            ModelLoader.setCustomModelResourceLocation(LittleTiles.BLOCK_INGREDIENT, i, new ModelResourceLocation(LittleTiles.BLOCK_INGREDIENT.getRegistryName()
                    .toString() + i, "inventory"));
            ModelLoader.setCustomModelResourceLocation(LittleTiles.CYAN_COLOR, i, new ModelResourceLocation(LittleTiles.CYAN_COLOR.getRegistryName().toString() + i, "inventory"));
            ModelLoader.setCustomModelResourceLocation(LittleTiles.MAGENTA_COLOR, i, new ModelResourceLocation(LittleTiles.MAGENTA_COLOR.getRegistryName()
                    .toString() + i, "inventory"));
            ModelLoader
                    .setCustomModelResourceLocation(LittleTiles.YELLOW_COLOR, i, new ModelResourceLocation(LittleTiles.YELLOW_COLOR.getRegistryName().toString() + i, "inventory"));
        }
        
        CreativeBlockRenderHelper.registerCreativeRenderedItem(LittleTiles.recipeAdvanced);
        ModelLoader.setCustomModelResourceLocation(LittleTiles.recipeAdvanced, 0, new ModelResourceLocation(LittleTiles.MODID + ":blueprint", "inventory"));
        ModelLoader.setCustomModelResourceLocation(LittleTiles.recipeAdvanced, 1, new ModelResourceLocation(LittleTiles.MODID + ":blueprint_background", "inventory"));
        
        CreativeBlockRenderHelper.registerCreativeRenderedItem(LittleTiles.chisel);
        ModelLoader.setCustomModelResourceLocation(LittleTiles.chisel, 0, new ModelResourceLocation(LittleTiles.MODID + ":chisel", "inventory"));
        ModelLoader.setCustomModelResourceLocation(LittleTiles.chisel, 1, new ModelResourceLocation(LittleTiles.MODID + ":chisel_background", "inventory"));
        
        CreativeBlockRenderHelper.registerCreativeRenderedItem(LittleTiles.grabber);
        ModelLoader.setCustomModelResourceLocation(LittleTiles.grabber, 0, new ModelResourceLocation(LittleTiles.MODID + ":grabber", "inventory"));
        ModelLoader.setCustomModelResourceLocation(LittleTiles.grabber, 1, new ModelResourceLocation(LittleTiles.MODID + ":grabber_background", "inventory"));
        
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
