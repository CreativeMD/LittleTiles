package team.creative.littletiles.common.placement.mode;

import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.registry.NamedHandlerRegistry;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.placement.PlacementContext;
import team.creative.littletiles.common.structure.LittleStructure;

public abstract class PlacementMode {
    
    private static final NamedHandlerRegistry<PlacementMode> REGISTRY = new NamedHandlerRegistry<PlacementMode>(null);
    
    public static final PlacementMode NORMAL = new PlacementModeNormal(PreviewMode.PREVIEWS, false);
    
    /** Tries to fill in the tiles where it is possible. **/
    public static final PlacementMode FILL = new PlacementModeFill(PreviewMode.PREVIEWS);
    
    /** Used for placing structures, should fail if it cannot place all tiles. **/
    public static final PlacementMode ALL = new PlacementModeAll(PreviewMode.PREVIEWS);
    
    /** Places all tiles no matter what is in the way. **/
    public static final PlacementMode OVERWRITE = new PlacementModeOverwrite(PreviewMode.PREVIEWS);
    
    /** Places all tiles no matter what is in the way. **/
    public static final PlacementMode OVERWRITE_ALL = new PlacementModeOverwriteAll(PreviewMode.PREVIEWS);
    
    /** Similar to overwrite only that replace will not place any tiles in the air. **/
    public static final PlacementMode REPLACE = new PlacementModeReplace(PreviewMode.LINES);
    
    /** Will not place anything but just remove the shape, basically like replace without the placing part **/
    public static final PlacementMode STENCIL = new PlacementModeStencil(PreviewMode.LINES);
    
    /** Will not place anything but just remove the shape, basically like replace without the placing part **/
    public static final PlacementMode COLORIZE = new PlacementModeColorize(PreviewMode.LINES);
    
    private static final TextMapBuilder<PlacementMode> MAP = new TextMapBuilder<>();
    
    public static PlacementMode getStructureDefault() {
        return PlacementMode.ALL;
    }
    
    public static PlacementMode getDefault() {
        return REGISTRY.getDefault();
    }
    
    public static PlacementMode getMode(String name) {
        return REGISTRY.get(name);
    }
    
    public static PlacementMode getMode(String name, boolean structure) {
        PlacementMode mode = REGISTRY.get(name);
        if (mode.canPlaceStructures() || !structure)
            return mode;
        return getStructureDefault();
    }
    
    public static void register(String id, PlacementMode handler) {
        REGISTRY.register(id, handler);
        MAP.addComponent(handler, Component.translatable("placement.mode." + id));
    }
    
    private static void registerDefault(String id, PlacementMode handler) {
        REGISTRY.registerDefault(id, handler);
        MAP.addComponent(handler, Component.translatable("placement.mode." + id));
    }
    
    public static TextMapBuilder<PlacementMode> map() {
        return MAP;
    }
    
    static {
        register("normal", NORMAL);
        registerDefault("fill", FILL);
        register("all", ALL);
        register("overwrite", OVERWRITE);
        register("overwrite_all", OVERWRITE_ALL);
        register("replace", REPLACE);
        register("stencil", STENCIL);
        register("colorize", COLORIZE);
    }
    
    public final boolean placeInside;
    private final PreviewMode mode;
    
    public PlacementMode(PreviewMode mode, boolean placeInside) {
        this.mode = mode;
        this.placeInside = placeInside;
    }
    
    public String getId() {
        return REGISTRY.getId(this);
    }
    
    public PreviewMode getPreviewMode() {
        if (LittleTiles.CONFIG.rendering.previewLines)
            return PreviewMode.LINES;
        return mode;
    }
    
    public abstract List<BlockPos> getCoordsToCheck(Set<BlockPos> splittedTiles, BlockPos pos);
    
    public abstract boolean placeTile(PlacementContext context, LittleStructure structure, LittleTile tile) throws LittleActionException;
    
    @OnlyIn(Dist.CLIENT)
    public PlacementMode place() {
        return this;
    }
    
    public boolean canPlaceStructures() {
        return false;
    }
    
    public boolean checkAll() {
        return true;
    }
    
    public boolean shouldConvertBlock() {
        return false;
    }
    
    public void prepareBlock(PlacementContext context) {}
    
    public String translatableKey() {
        return "placement.mode." + REGISTRY.getId(this);
    }
    
    public Component translatable() {
        return Component.translatable(translatableKey());
    }
    
    public LittleIngredients getBeforePlaceIngredients(LittleGroup previews) {
        return LittleAction.getIngredients(previews);
    }
    
    public static enum PreviewMode {
        LINES,
        PREVIEWS;
    }
    
}