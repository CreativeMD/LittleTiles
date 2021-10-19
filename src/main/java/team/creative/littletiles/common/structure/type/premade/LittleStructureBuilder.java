package team.creative.littletiles.common.structure.type.premade;

import java.util.HashMap;
import java.util.Set;

import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import team.creative.creativecore.common.util.mc.InventoryUtils;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.gui.handler.LittleStructureGuiHandler;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.structure.LittleStructureType;

public class LittleStructureBuilder extends LittleStructurePremade {
    
    private static HashMap<String, LittleStructureBuilderType> types = new HashMap<>();
    
    public static void register(LittleStructureBuilderType type) {
        types.put(type.type.id, type);
    }
    
    public static Set<String> getNames() {
        return types.keySet();
    }
    
    public static LittleStructureBuilderType get(String id) {
        return types.get(id);
    }
    
    public SimpleContainer inventory = new SimpleContainer(1);
    public int lastSizeX = 16;
    public int lastSizeY = 16;
    public int lastThickness = 1;
    public BlockState lastBlockState;
    public String lastStructureType;
    public int lastGrid = 16;
    
    public LittleStructureBuilder(LittleStructureType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    public boolean canInteract() {
        return true;
    }
    
    @Override
    public InteractionResult use(Level level, LittleTile tile, LittleBox box, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        LittleStructureGuiHandler.openGui("structure_builder", new CompoundTag(), player, this);
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
        if (nbt.contains("block")) {
            String[] parts = nbt.getString("block").split(":");
            Block block = Block.getBlockFromName(parts[0] + ":" + parts[1]);
            int meta;
            if (parts.length == 3)
                meta = Integer.parseInt(parts[2]);
            else
                meta = 0;
            lastBlockState = block.getStateFromMeta(meta);
        } else
            lastBlockState = Blocks.OAK_PLANKS.defaultBlockState();
        inventory = InventoryUtils.load(nbt, 1);
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt) {
        nbt.putInt("sizeX", lastSizeX);
        nbt.putInt("sizeY", lastSizeY);
        nbt.putInt("thickness", lastThickness);
        InventoryUtils.save(inventory);
        Block block = lastBlockState.getBlock();
        int meta = block.getMetaFromState(lastBlockState);
        nbt.putString("block", block.getRegistryName().toString() + (meta != 0 ? ":" + meta : ""));
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
        
        public LittlePreviews construct(LittleGridContext context, int width, int height, int thickness, NBTTagCompound tileData) {
            NBTTagCompound structureNBT = new NBTTagCompound();
            structureNBT.setString("id", type.id);
            structureNBT.setIntArray("topRight", new int[] { Float.floatToIntBits(0), Float.floatToIntBits(1), Float.floatToIntBits(1) });
            structureNBT.setIntArray(frameVariableName, new int[] { thickness, 0, 0, thickness + 1, height, width, context.size });
            LittlePreviews previews = new LittlePreviews(structureNBT, context);
            for (int x = 0; x < thickness; x += context.size)
                for (int y = 0; y < height; y += context.size)
                    for (int z = 0; z < width; z += context.size)
                        previews.addWithoutCheckingPreview(new LittlePreview(new LittleBox(x, y, z, Math.min(x + 16, thickness), Math.min(y + 16, height), Math
                                .min(z + 16, width)), tileData.copy()));
            return previews;
        }
        
    }
    
}
