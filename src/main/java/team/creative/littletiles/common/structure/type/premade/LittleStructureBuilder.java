package team.creative.littletiles.common.structure.type.premade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import team.creative.creativecore.common.util.inventory.InventoryUtils;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.registry.NamedHandlerRegistry;
import team.creative.littletiles.LittleTilesGuiRegistry;
import team.creative.littletiles.common.block.little.registry.LittleBlockRegistry;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.structure.LittleStructureType;

public class LittleStructureBuilder extends LittleStructurePremade {
    
    public static final NamedHandlerRegistry<LittleStructureBuilderType> REGISTRY = new NamedHandlerRegistry<>(null);
    
    public static void register(LittleStructureBuilderType type) {
        REGISTRY.register(type.type.id, type);
    }
    
    public SimpleContainer inventory = new SimpleContainer(1);
    public int lastSizeX = 16;
    public int lastSizeY = 16;
    public int lastThickness = 1;
    public BlockState lastBlockState;
    public String lastStructureType;
    public int lastGrid = 16;
    
    public LittleStructureBuilder(LittlePremadeType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    public boolean canInteract() {
        return true;
    }
    
    @Override
    public InteractionResult use(Level level, LittleTileContext context, BlockPos pos, Player player, BlockHitResult result) {
        LittleTilesGuiRegistry.STRUCTURE_BUILDER.open(player, this);
        return InteractionResult.SUCCESS;
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt) {
        if (nbt.contains("sizeX"))
            lastSizeX = nbt.getInt("sizeX");
        else
            lastSizeX = 16;
        if (nbt.contains("sizeY"))
            lastSizeY = nbt.getInt("sizeY");
        else
            lastSizeY = 16;
        if (nbt.contains("thickness"))
            lastThickness = nbt.getInt("thickness");
        else
            lastThickness = 1;
        if (nbt.contains("grid"))
            lastGrid = nbt.getInt("grid");
        else
            lastGrid = 16;
        lastStructureType = nbt.getString("type");
        if (nbt.contains("state"))
            lastBlockState = LittleBlockRegistry.loadState(nbt.getString("state"));
        else
            lastBlockState = Blocks.OAK_PLANKS.defaultBlockState();
        inventory = InventoryUtils.load(nbt, 1);
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt) {
        nbt.putInt("sizeX", lastSizeX);
        nbt.putInt("sizeY", lastSizeY);
        nbt.putInt("thickness", lastThickness);
        InventoryUtils.save(inventory);
        nbt.putString("state", LittleBlockRegistry.saveState(lastBlockState));
        nbt.putInt("grid", lastGrid);
        nbt.putString("type", lastStructureType);
    }
    
    public static class LittleStructureBuilderType {
        
        public final LittleStructureType type;
        public final String frameVariableName;
        
        public LittleStructureBuilderType(LittleStructureType type, String frameVariableName) {
            this.type = type;
            this.frameVariableName = frameVariableName;
        }
        
        public LittleGroup construct(LittleGrid grid, int width, int height, int thickness, BlockState state) {
            CompoundTag structureNBT = new CompoundTag();
            structureNBT.putString("id", type.id);
            structureNBT.putIntArray("topRight", new int[] { Float.floatToIntBits(0), Float.floatToIntBits(1), Float.floatToIntBits(1) });
            structureNBT.putIntArray(frameVariableName, new int[] { thickness, 0, 0, thickness + 1, height, width, grid.count });
            LittleGroup previews = new LittleGroup(structureNBT, Collections.EMPTY_LIST);
            List<LittleBox> boxes = new ArrayList<>();
            for (int x = 0; x < thickness; x += grid.count)
                for (int y = 0; y < height; y += grid.count)
                    for (int z = 0; z < width; z += grid.count)
                        boxes.add(new LittleBox(x, y, z, Math.min(x + 16, thickness), Math.min(y + 16, height), Math.min(z + 16, width)));
            previews.addTileFast(grid, new LittleTile(state, ColorUtils.WHITE, boxes));
            previews.convertToSmallest();
            return previews;
        }
        
    }
    
}
