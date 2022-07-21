package team.creative.littletiles.common.placement.mode;

import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.registry.NamedHandlerRegistry;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.placement.PlacementContext;
import team.creative.littletiles.common.structure.LittleStructure;

public abstract class PlacementMode {
    
    /** Tries to place all tiles, fails if the main block pos (the player aimed at)
     * cannot be placed entirely. **/
    public static final PlacementMode normal = new PlacementModeNormal("placement.mode.default", PreviewMode.PREVIEWS, false);
    
    private static final NamedHandlerRegistry<PlacementMode> REGISTRY = new NamedHandlerRegistry<PlacementMode>(normal);
    
    /** Tries to fill in the tiles where it is possible. **/
    public static final PlacementMode fill = new PlacementModeFill("placement.mode.fill", PreviewMode.PREVIEWS);
    
    /** Used for placing structures, should fail if it cannot place all tiles. **/
    public static final PlacementMode all = new PlacementModeAll("placement.mode.all", PreviewMode.PREVIEWS);
    
    /** Places all tiles no matter what is in the way. **/
    public static final PlacementMode overwrite = new PlacementModeOverwrite("placement.mode.overwrite", PreviewMode.PREVIEWS);
    
    /** Places all tiles no matter what is in the way. **/
    public static final PlacementMode overwrite_all = new PlacementModeOverwriteAll("placement.mode.overwriteall", PreviewMode.PREVIEWS);
    
    /** Similar to overwrite only that replace will not place any tiles in the air. **/
    public static final PlacementMode replace = new PlacementModeReplace("placement.mode.replace", PreviewMode.LINES);
    
    /** Will not place anything but just remove the shape, basically like replace without the placing part **/
    public static final PlacementMode stencil = new PlacementModeStencil("placement.mode.stencil", PreviewMode.LINES);
    
    /** Will not place anything but just remove the shape, basically like replace without the placing part **/
    public static final PlacementMode colorize = new PlacementModeColorize("placement.mode.colorize", PreviewMode.LINES);
    
    private static final TextMapBuilder<PlacementMode> map = new TextMapBuilder<>();
    
    public static PlacementMode getStructureDefault() {
        return PlacementMode.all;
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
    
    public void register(String id, PlacementMode handler) {
        REGISTRY.register(id, handler);
        map.addComponent(handler, new TranslatableComponent(handler.name));
    }
    
    public static TextMapBuilder<PlacementMode> map() {
        return map;
    }
    
    static {
        REGISTRY.register("fill", fill);
        REGISTRY.register("all", all);
        REGISTRY.register("overwrite", overwrite);
        REGISTRY.register("overwrite_all", overwrite_all);
        REGISTRY.register("replace", replace);
        REGISTRY.register("stencil", stencil);
        REGISTRY.register("colorize", colorize);
    }
    
    public final String name;
    public final boolean placeInside;
    private final PreviewMode mode;
    
    public PlacementMode(String name, PreviewMode mode, boolean placeInside) {
        this.name = name;
        this.mode = mode;
        this.placeInside = placeInside;
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
    
    public static enum PreviewMode {
        LINES,
        PREVIEWS;
    }
    
}