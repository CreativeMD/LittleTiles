package team.creative.littletiles.common.placement.selection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import team.creative.creativecore.common.util.registry.NamedHandlerRegistry;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.animation.entity.LittleLevelEntity;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.level.LittleAnimationHandlers;
import team.creative.littletiles.common.mod.chiselsandbits.ChiselsAndBitsManager;

public abstract class SelectionMode {
    
    public static final NamedHandlerRegistry<SelectionMode> REGISTRY = new NamedHandlerRegistry<>(null);
    
    static {
        REGISTRY.registerDefault("area", new AreaSelectionMode());
    }
    
    public SelectionMode() {}
    
    public String getName() {
        return REGISTRY.getId(this);
    }
    
    public TranslatableComponent getTranslation() {
        return new TranslatableComponent("mode.selection." + REGISTRY.getId(this));
    }
    
    public abstract SelectionResult generateResult(Level level, ItemStack stack);
    
    public abstract void leftClick(Player player, ItemStack stack, BlockPos pos);
    
    public abstract void rightClick(Player player, ItemStack stack, BlockPos pos);
    
    public abstract void clear(ItemStack stack);
    
    public abstract LittleGroup getGroup(Level world, ItemStack stack, boolean includeVanilla, boolean includeCB, boolean includeLT, boolean rememberStructure);
    
    public void save(ItemStack stack) {}
    
    public static class SelectionResult {
        
        public final Level level;
        
        public SelectionResult(Level level) {
            this.level = level;
        }
        
        private void addBlockDirectly(Level level, BlockPos pos) {
            BlockEntity te = level.getBlockEntity(pos);
            if (te instanceof BETiles) {
                ltBlocks++;
                ltTiles += ((BETiles) te).tilesCount();
                if (minLtGrid == null)
                    minLtGrid = ((BETiles) te).getGrid();
                else
                    minLtGrid = LittleGrid.max(minLtGrid, ((BETiles) te).getGrid());
            }
            
            LittleGroup specialPreviews = ChiselsAndBitsManager.getGroup(te);
            if (specialPreviews != null) {
                cbBlocks++;
                cbTiles += specialPreviews.size();
                if (minCBGrid == null)
                    minCBGrid = specialPreviews.getGrid();
                else
                    minCBGrid = LittleGrid.max(minCBGrid, specialPreviews.getGrid());
            }
            
            if (LittleAction.isBlockValid(level.getBlockState(pos)))
                blocks++;
        }
        
        public void addBlock(BlockPos pos) {
            if (min == null) {
                min = pos.mutable();
                max = pos.mutable();
            } else {
                min.set(Math.min(min.getX(), pos.getX()), Math.min(min.getY(), pos.getY()), Math.min(min.getZ(), pos.getZ()));
                max.set(Math.max(max.getX(), pos.getX()), Math.max(max.getY(), pos.getY()), Math.max(max.getZ(), pos.getZ()));
            }
            addBlockDirectly(level, pos);
        }
        
        protected void addBlocksWorld(Level level, BlockPos pos, BlockPos pos2) {
            int minX = Math.min(pos.getX(), pos2.getX());
            int minY = Math.min(pos.getY(), pos2.getY());
            int minZ = Math.min(pos.getZ(), pos2.getZ());
            int maxX = Math.max(pos.getX(), pos2.getX());
            int maxY = Math.max(pos.getY(), pos2.getY());
            int maxZ = Math.max(pos.getZ(), pos2.getZ());
            
            if (min == null) {
                min = new MutableBlockPos(minX, minY, minZ);
                max = new MutableBlockPos(maxX, maxY, maxZ);
            } else {
                min.set(Math.min(min.getX(), minX), Math.min(min.getY(), minY), Math.min(min.getZ(), minZ));
                max.set(Math.max(max.getX(), minX), Math.max(max.getY(), minY), Math.max(max.getZ(), minZ));
            }
            
            MutableBlockPos mutPos = new MutableBlockPos();
            for (int posX = minX; posX <= maxX; posX++)
                for (int posY = minY; posY <= maxY; posY++)
                    for (int posZ = minZ; posZ <= maxZ; posZ++)
                        addBlockDirectly(level, mutPos.set(posX, posY, posZ));
        }
        
        public void addBlocks(BlockPos pos, BlockPos pos2) {
            int minX = Math.min(pos.getX(), pos2.getX());
            int minY = Math.min(pos.getY(), pos2.getY());
            int minZ = Math.min(pos.getZ(), pos2.getZ());
            int maxX = Math.max(pos.getX(), pos2.getX());
            int maxY = Math.max(pos.getY(), pos2.getY());
            int maxZ = Math.max(pos.getZ(), pos2.getZ());
            
            addBlocksWorld(level, pos, pos2);
            
            for (LittleLevelEntity entity : LittleAnimationHandlers.get(level).find(new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1)))
                addBlocksWorld(entity.getFakeLevel(), pos, pos2);
        }
        
        public MutableBlockPos min = null;
        public MutableBlockPos max = null;
        
        public Vec3i getSize() {
            return new Vec3i(max.getX() - min.getX(), max.getY() - min.getY(), max.getZ() - min.getZ());
        }
        
        public int blocks;
        
        public int ltBlocks = 0;
        public int ltTiles = 0;
        public LittleGrid minLtGrid = null;
        
        public int cbBlocks = 0;
        public int cbTiles = 0;
        public LittleGrid minCBGrid = null;
        
    }
    
}