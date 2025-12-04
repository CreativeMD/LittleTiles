package team.creative.littletiles.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent.RegisterAdditional;
import net.neoforged.neoforge.client.event.ModelEvent.RegisterGeometryLoaders;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.NeoForge;
import team.creative.creativecore.client.CreativeCoreClient;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.client.render.model.CreativeBlockModel;
import team.creative.creativecore.client.render.model.CreativeItemBoxModel;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.matrix.IntMatrix3c;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.api.common.tool.ILittleTool;
import team.creative.littletiles.client.action.LittleActionHandlerClient;
import team.creative.littletiles.client.action.interact.LittleInteractionHandlerClient;
import team.creative.littletiles.client.level.LevelHandlersClient;
import team.creative.littletiles.client.level.LittleAnimationHandlerClient;
import team.creative.littletiles.client.level.LittleVanillaInteractionHandlerClient;
import team.creative.littletiles.client.mod.iris.IrisManager;
import team.creative.littletiles.client.mod.sodium.SodiumManager;
import team.creative.littletiles.client.player.LittleClientPlayerConnection;
import team.creative.littletiles.client.render.block.BETilesRenderer;
import team.creative.littletiles.client.render.block.BlockTileRenderProperties;
import team.creative.littletiles.client.render.entity.LittleEntityRenderer;
import team.creative.littletiles.client.render.entity.LittleSitRenderer;
import team.creative.littletiles.client.render.entity.RenderSizedTNTPrimed;
import team.creative.littletiles.client.render.item.ItemRenderCache;
import team.creative.littletiles.client.render.item.LittleModelItemBackground;
import team.creative.littletiles.client.render.item.LittleModelItemPreview;
import team.creative.littletiles.client.render.item.LittleModelItemTilesBig;
import team.creative.littletiles.client.render.level.LittleClientEventHandler;
import team.creative.littletiles.client.render.overlay.LittleTilesProfilerOverlay;
import team.creative.littletiles.client.render.overlay.OverlayRenderer;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.ingredient.BlockIngredientEntry;
import team.creative.littletiles.common.ingredient.ColorIngredient;
import team.creative.littletiles.common.item.ItemBlockIngredient;
import team.creative.littletiles.common.item.ItemColorIngredient;
import team.creative.littletiles.common.item.ItemLittleBlueprint;
import team.creative.littletiles.common.item.ItemPremadeStructure;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.structure.type.premade.LittleStructurePremade.LittlePremadeType;

@OnlyIn(Dist.CLIENT)
public class LittleTilesClient {
    
    public static final Minecraft MC = Minecraft.getInstance();
    
    public static final IKeyConflictContext LITTLE_KEY_CONTEXT = new IKeyConflictContext() {
        
        @Override
        public boolean isActive() {
            return true;
        }
        
        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return this == other;
        }
    };
    
    public static final LevelHandlersClient LEVEL_HANDLERS = new LevelHandlersClient();
    public static LittleActionHandlerClient ACTION_HANDLER;
    public static LittleAnimationHandlerClient ANIMATION_HANDLER;
    public static LittleVanillaInteractionHandlerClient INTERACTION_HANDLER;
    public static LittleInteractionHandlerClient INTERACTION;
    public static PreviewRenderer PREVIEW_RENDERER;
    public static ItemRenderCache ITEM_RENDER_CACHE;
    public static LittleClientPlayerConnection PLAYER_CONNECTION;
    public static OverlayRenderer OVERLAY_RENDERER;
    
    public static KeyMapping KEY_MIRROR;
    public static KeyMapping KEY_MARK;
    public static KeyMapping KEY_CONFIGURE;
    public static KeyMapping KEY_CONFIGURE_SECONDARY;
    public static KeyMapping KEY_UP;
    public static KeyMapping KEY_DOWN;
    public static KeyMapping KEY_RIGHT;
    public static KeyMapping KEY_LEFT;
    
    public static KeyMapping[] TOOL_KEYS;
    
    public static KeyMapping KEY_UNDO;
    public static KeyMapping KEY_REDO;
    
    public static BETilesRenderer BLOCK_TILES_RENDERER;
    
    public static void grid(LittleGrid grid) {
        ACTION_HANDLER.setting.grid(grid);
    }
    
    public static void placementMode(PlacementMode mode) {
        ACTION_HANDLER.setting.placementMode(mode);
    }
    
    public static void setPlace(LittleGrid grid, PlacementMode mode) {
        ACTION_HANDLER.setting.set(grid, mode);
    }
    
    public static Component arrowKeysTooltip() {
        if (KEY_UP.isDefault() && KEY_DOWN.isDefault() && KEY_RIGHT.isDefault() && KEY_LEFT.isDefault())
            return Component.translatable("gui.tooltip.arrow_keys");
        return Component.empty().append(KEY_UP.getTranslatedKeyMessage()).append(", ").append(KEY_DOWN.getTranslatedKeyMessage()).append(", ").append(KEY_RIGHT
                .getTranslatedKeyMessage()).append(", ").append(KEY_LEFT.getTranslatedKeyMessage());
    }
    
    public static void displayActionMessage(List<Component> message) {
        OVERLAY_RENDERER.displayActionMessage(message);
    }
    
    public static Facing facingFromKeybind(Player player, KeyMapping key) {
        if (key == LittleTilesClient.KEY_UP)
            return LittleActionHandlerClient.isUsingSecondMode() ? Facing.UP : Facing.EAST;
        if (key == LittleTilesClient.KEY_DOWN)
            return LittleActionHandlerClient.isUsingSecondMode() ? Facing.DOWN : Facing.WEST;
        if (key == LittleTilesClient.KEY_RIGHT)
            return Facing.SOUTH;
        if (key == LittleTilesClient.KEY_LEFT)
            return Facing.NORTH;
        return null;
    }
    
    public static IntMatrix3c fromKeybind(Player player, KeyMapping key) {
        if (key == LittleTilesClient.KEY_UP)
            return Rotation.Z_CLOCKWISE.getMatrix();
        if (key == LittleTilesClient.KEY_DOWN)
            return Rotation.Z_COUNTER_CLOCKWISE.getMatrix();
        if (key == LittleTilesClient.KEY_RIGHT)
            return Rotation.Y_COUNTER_CLOCKWISE.getMatrix();
        if (key == LittleTilesClient.KEY_LEFT)
            return Rotation.Y_CLOCKWISE.getMatrix();
        if (key == LittleTilesClient.KEY_MIRROR)
            return Facing.of(player).axis.getMatrix();
        return null;
    }
    
    public static void load(IEventBus bus) {
        bus.addListener(LittleTilesClient::setup);
        NeoForge.EVENT_BUS.addListener(LittleTilesClient::commands);
        bus.addListener(LittleTilesClient::initItemColors);
        bus.addListener(LittleTilesClient::initBlockColors);
        bus.addListener(LittleTilesClient::registerKeys);
        bus.addListener(LittleTilesClient::modelEvent);
        bus.addListener(LittleTilesClient::modelLoader);
        bus.addListener(LittleTilesClient::initBlockClient);
    }
    
    private static void registerKeys(RegisterKeyMappingsEvent event) {
        KEY_UP = new LittleKeyMapping("key.rotateup", LITTLE_KEY_CONTEXT, InputConstants.KEY_UP, "key.categories.littletiles").ignoreModifier();
        KEY_DOWN = new LittleKeyMapping("key.rotatedown", LITTLE_KEY_CONTEXT, InputConstants.KEY_DOWN, "key.categories.littletiles").ignoreModifier();
        KEY_RIGHT = new LittleKeyMapping("key.rotateright", LITTLE_KEY_CONTEXT, InputConstants.KEY_RIGHT, "key.categories.littletiles").ignoreModifier();
        KEY_LEFT = new LittleKeyMapping("key.rotateleft", LITTLE_KEY_CONTEXT, InputConstants.KEY_LEFT, "key.categories.littletiles").ignoreModifier();
        
        KEY_MIRROR = new LittleKeyMapping("key.little.mirror", LITTLE_KEY_CONTEXT, InputConstants.KEY_G, "key.categories.littletiles");
        KEY_MARK = new LittleKeyMapping("key.little.mark", LITTLE_KEY_CONTEXT, InputConstants.KEY_M, "key.categories.littletiles");
        KEY_CONFIGURE = new LittleKeyMapping("key.little.config.item", LITTLE_KEY_CONTEXT, InputConstants.KEY_C, "key.categories.littletiles");
        KEY_CONFIGURE_SECONDARY = new LittleKeyMapping("key.little.config_secondary.item", LITTLE_KEY_CONTEXT, KeyModifier.SHIFT, InputConstants.KEY_C, "key.categories.littletiles");
        
        KEY_UNDO = new LittleKeyMapping("key.little.undo", LITTLE_KEY_CONTEXT, KeyModifier.CONTROL, InputConstants.KEY_Z, "key.categories.littletiles");
        KEY_REDO = new LittleKeyMapping("key.little.redo", LITTLE_KEY_CONTEXT, KeyModifier.CONTROL, InputConstants.KEY_Y, "key.categories.littletiles");
        
        event.register(KEY_UP);
        event.register(KEY_DOWN);
        event.register(KEY_RIGHT);
        event.register(KEY_LEFT);
        
        event.register(KEY_MIRROR);
        event.register(KEY_MARK);
        event.register(KEY_CONFIGURE);
        event.register(KEY_CONFIGURE_SECONDARY);
        
        TOOL_KEYS = new KeyMapping[] { KEY_UP, KEY_DOWN, KEY_RIGHT, KEY_LEFT, KEY_MIRROR, KEY_MARK, KEY_CONFIGURE, KEY_CONFIGURE_SECONDARY };
        
        event.register(KEY_UNDO);
        event.register(KEY_REDO);
    }
    
    private static void setup(final FMLClientSetupEvent event) {
        MC.getItemColors().register((stack, layer) -> {
            if (layer == 0)
                return ColorUtils.WHITE;
            return stack.getOrDefault(LittleTilesRegistry.COLOR, ColorUtils.WHITE);
        }, LittleTilesRegistry.PAINT_BRUSH.value());
        
        MC.getItemColors().register((stack, layer) -> {
            if (layer == 0)
                return stack.getOrDefault(LittleTilesRegistry.COLOR, ItemLittleBlueprint.DEFAULT_COLOR);
            return stack.getOrDefault(LittleTilesRegistry.COLOR_SECONDARY, ItemLittleBlueprint.DEFAULT_COLOR_SECONDARY);
        }, LittleTilesRegistry.BLUEPRINT.value());
        
        // overlay.add(new OverlayControl(new GuiAxisIndicatorControl("axis"), OverlayPositionType.CENTER).setShouldRender(() -> PreviewRenderer.marked != null));
        NeoForge.EVENT_BUS.register(new LittleClientEventHandler());
        
        LEVEL_HANDLERS.register(LittleActionHandlerClient::new, x -> ACTION_HANDLER = x);
        LEVEL_HANDLERS.register(LittleVanillaInteractionHandlerClient::new, x -> INTERACTION_HANDLER = x);
        LEVEL_HANDLERS.register(LittleTiles.ANIMATION_HANDLERS::get, x -> ANIMATION_HANDLER = (LittleAnimationHandlerClient) x);
        LEVEL_HANDLERS.register(PREVIEW_RENDERER = new PreviewRenderer());
        LEVEL_HANDLERS.register(ITEM_RENDER_CACHE = new ItemRenderCache());
        LEVEL_HANDLERS.register(PLAYER_CONNECTION = new LittleClientPlayerConnection());
        LEVEL_HANDLERS.register(INTERACTION = new LittleInteractionHandlerClient());
        
        // Init overlays
        NeoForge.EVENT_BUS.register(LittleTilesProfilerOverlay.class);
        LEVEL_HANDLERS.register(OVERLAY_RENDERER = new OverlayRenderer());
        
        CreativeCoreClient.registerClientConfig(LittleTiles.MODID);
        
        EntityRenderers.register(LittleTilesRegistry.SIZED_TNT_TYPE.get(), RenderSizedTNTPrimed::new);
        EntityRenderers.register(LittleTilesRegistry.ENTITY_LEVEL.get(), LittleEntityRenderer::new);
        EntityRenderers.register(LittleTilesRegistry.ENTITY_ANIMATION.get(), LittleEntityRenderer::new);
        EntityRenderers.register(LittleTilesRegistry.SIT_TYPE.get(), LittleSitRenderer::new);
        
        BLOCK_TILES_RENDERER = new BETilesRenderer();
        BlockEntityRenderers.register(LittleTilesRegistry.BE_TILES_TYPE_RENDERED.get(), x -> BLOCK_TILES_RENDERER);
        
        event.enqueueWork(() -> {
            ResourceLocation filled = ResourceLocation.tryBuild(LittleTiles.MODID, "filled");
            ClampedItemPropertyFunction function = (stack, level, entity, x) -> ((ItemColorIngredient) stack.getItem()).getColor(stack) / (float) ColorIngredient.BOTTLE_SIZE;
            ItemProperties.register(LittleTilesRegistry.BLACK_COLOR.value(), filled, function);
            ItemProperties.register(LittleTilesRegistry.CYAN_COLOR.value(), filled, function);
            ItemProperties.register(LittleTilesRegistry.MAGENTA_COLOR.value(), filled, function);
            ItemProperties.register(LittleTilesRegistry.YELLOW_COLOR.value(), filled, function);
        });
        
        MC.getItemColors().register((stack, layer) -> {
            var entry = ItemBlockIngredient.loadIngredient(stack);
            return MC.getItemColors().getColor(entry.getBlockStack(), layer);
        }, LittleTilesRegistry.BLOCK_INGREDIENT.value());
        
        SodiumManager.init();
        IrisManager.init();
    }
    
    private static void modelLoader(RegisterAdditional event) {
        event.register(new ModelResourceLocation(ResourceLocation.tryBuild(LittleTiles.MODID, "glove_background"), ModelResourceLocation.STANDALONE_VARIANT));
        event.register(new ModelResourceLocation(ResourceLocation.tryBuild(LittleTiles.MODID, "chisel_background"), ModelResourceLocation.STANDALONE_VARIANT));
        event.register(new ModelResourceLocation(ResourceLocation.tryBuild(LittleTiles.MODID, "blueprint_background"), ModelResourceLocation.STANDALONE_VARIANT));
    }
    
    private static void modelEvent(RegisterGeometryLoaders event) {
        CreativeCoreClient.registerBlockModel(ResourceLocation.tryBuild(LittleTiles.MODID, "empty"), new CreativeBlockModel() {
            
            @Override
            public List<? extends RenderBox> getBoxes(BlockState state, ModelData data, RandomSource source) {
                return Collections.EMPTY_LIST;
            }
            
            @Override
            public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
                return modelData;
            }
        });
        
        CreativeCoreClient.registerItemModel(ResourceLocation.tryBuild(LittleTiles.MODID, "tiles"), new LittleModelItemTilesBig());
        CreativeCoreClient.registerItemModel(ResourceLocation.tryBuild(LittleTiles.MODID, "premade"), new LittleModelItemTilesBig() {
            @Override
            public List<? extends RenderBox> getBoxes(ItemStack stack, boolean translucent) {
                if (!ILittleTool.getData(stack).contains(LittleGroup.STRUCTURE_KEY))
                    return Collections.EMPTY_LIST;
                
                LittlePremadeType premade = ItemPremadeStructure.get(stack);
                if (premade == null)
                    return Collections.EMPTY_LIST;
                LittleGroup previews = ((ItemPremadeStructure) stack.getItem()).getTiles(stack);
                if (previews == null)
                    return Collections.EMPTY_LIST;
                List<RenderBox> cubes = premade.getItemPreview(previews, translucent);
                if (cubes == null) {
                    cubes = previews.getRenderingBoxes(translucent);
                    LittleGroup.shrinkCubesToOneBlock(cubes);
                }
                
                return cubes;
            }
        });
        
        CreativeCoreClient.registerItemModel(ResourceLocation.tryBuild(LittleTiles.MODID, "glove"), new LittleModelItemPreview(new ModelResourceLocation(ResourceLocation.tryBuild(
            LittleTiles.MODID, "glove_background"), ModelResourceLocation.STANDALONE_VARIANT), stack -> LittleElement.getOrDefault(stack)));
        
        CreativeCoreClient.registerItemModel(ResourceLocation.tryBuild(LittleTiles.MODID, "chisel"), new LittleModelItemPreview(new ModelResourceLocation(ResourceLocation.tryBuild(
            LittleTiles.MODID, "chisel_background"), ModelResourceLocation.STANDALONE_VARIANT), stack -> LittleElement.getOrDefault(stack)));
        
        CreativeCoreClient.registerItemModel(ResourceLocation.tryBuild(LittleTiles.MODID, "blueprint"), new LittleModelItemBackground(new ModelResourceLocation(ResourceLocation
                .tryBuild(LittleTiles.MODID, "blueprint_background"), ModelResourceLocation.STANDALONE_VARIANT), x -> {
                    CompoundTag contentData = ItemLittleBlueprint.getContent(x);
                    if (!LittleGroup.shouldRenderInHand(contentData))
                        return ItemStack.EMPTY;
                    ItemStack stack = new ItemStack(LittleTilesRegistry.ITEM_TILES.value());
                    ILittleTool.setData(stack, contentData);
                    return stack;
                }));
        
        CreativeCoreClient.registerItemModel(ResourceLocation.tryBuild(LittleTiles.MODID, "blockingredient"), new CreativeItemBoxModel(new ModelResourceLocation(ResourceLocation
                .tryBuild("minecraft", "stone"), ModelResourceLocation.INVENTORY_VARIANT)) {
            
            @Override
            public List<? extends RenderBox> getBoxes(ItemStack stack, boolean translucent) {
                List<RenderBox> cubes = new ArrayList<>();
                BlockIngredientEntry ingredient = ItemBlockIngredient.loadIngredient(stack);
                if (ingredient == null)
                    return null;
                
                double volume = Math.min(1, ingredient.value);
                LittleGrid context = LittleGrid.overallDefault();
                long pixels = (long) (volume * context.count3d);
                if (pixels < context.count * context.count)
                    cubes.add(new RenderBox(0.4F, 0.4F, 0.4F, 0.6F, 0.6F, 0.6F, ingredient.block.getState()));
                else {
                    long remainingPixels = pixels;
                    long planes = pixels / context.count2d;
                    remainingPixels -= planes * context.count2d;
                    long rows = remainingPixels / context.count;
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
        });
    }
    
    private static void initItemColors(RegisterColorHandlersEvent.Item event) {
        CreativeCoreClient.registerItemColor(event.getItemColors(), LittleTilesRegistry.PREMADE.value());
        CreativeCoreClient.registerItemColor(event.getItemColors(), LittleTilesRegistry.ITEM_TILES.value());
        event.register((stack, tint) -> {
            if (stack.getItem() instanceof BlockItem block)
                return event.getBlockColors().getColor(block.getBlock().defaultBlockState(), (BlockAndTintGetter) null, (BlockPos) null, tint);
            return ColorUtils.WHITE;
        }, LittleTilesRegistry.WATER.value(), LittleTilesRegistry.FLOWING_WATER.value());
    }
    
    private static void initBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tint) -> level != null && pos != null ? ColorUtils.setAlpha(BiomeColors.getAverageWaterColor(level, pos), 255) : -12618012,
            LittleTilesRegistry.WATER.value(), LittleTilesRegistry.FLOWING_WATER.value());
    }
    
    private static void commands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(LiteralArgumentBuilder.<CommandSourceStack>literal("lt-debug").executes(x -> {
            if (LittleTilesProfilerOverlay.isActive())
                LittleTilesProfilerOverlay.stop();
            else
                LittleTilesProfilerOverlay.start();
            return Command.SINGLE_SUCCESS;
        }));
    }
    
    private static void initBlockClient(RegisterClientExtensionsEvent event) {
        event.registerBlock(BlockTileRenderProperties.INSTANCE, LittleTilesRegistry.BLOCK_TILES, LittleTilesRegistry.BLOCK_TILES_RENDERED, LittleTilesRegistry.BLOCK_TILES_TICKING,
            LittleTilesRegistry.BLOCK_TILES_TICKING_RENDERED);
    }
    
    public static class LittleKeyMapping extends KeyMapping {
        
        private boolean ignoreModifier = false;
        
        public LittleKeyMapping(String description, IKeyConflictContext keyConflictContext, int keyCode, String category) {
            super(description, keyConflictContext, KeyModifier.NONE, InputConstants.Type.KEYSYM, keyCode, category);
        }
        
        public LittleKeyMapping(String description, IKeyConflictContext keyConflictContext, KeyModifier keyModifier, int keyCode, String category) {
            super(description, keyConflictContext, keyModifier, InputConstants.Type.KEYSYM, keyCode, category);
        }
        
        public LittleKeyMapping ignoreModifier() {
            ignoreModifier = true;
            return this;
        }
        
        @Override
        public boolean isActiveAndMatches(Key keyCode) {
            if (ignoreModifier)
                return keyCode != InputConstants.UNKNOWN && keyCode.equals(getKey());
            return super.isActiveAndMatches(keyCode);
        }
        
        @Override
        public boolean same(KeyMapping other) {
            if (other instanceof LittleKeyMapping && super.same(other))
                return true;
            return false;
        }
    }
    
}
