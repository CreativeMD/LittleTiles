package cr0s.warpdrive.api;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IBlockTransformer {
    // Return true if this transformer is applicable to that block.
    boolean isApplicable(final Block block, final int metadata, final TileEntity tileEntity);
    
    // Called when preparing to save a ship structure.
    // Use this to prevent jump during critical events/animations.
    boolean isJumpReady(final Block block, final int metadata, final TileEntity tileEntity, final WarpDriveText reason);
    
    // Called when saving a ship structure.
    // Use this to save external data in the ship schematic.
    // You don't need to save Block and TileEntity data here, it's already covered.
    // Warning: do NOT assume that the ship will be removed!
    NBTBase saveExternals(final World world, final int x, final int y, final int z, final Block block, final int blockMeta, final TileEntity tileEntity);
    
    // Called when removing the original ship structure, if saveExternals() returned non-null for that block.
    // Use this to prevents drops, clear energy networks, etc.
    // Block and TileEntity will be removed right after this call. 
    // When moving, the new ship is placed first.
    void removeExternals(final World world, final int x, final int y, final int z, final Block block, final int blockMeta, final TileEntity tileEntity);
    
    // Called when restoring a ship in the world.
    // Use this to apply metadata & NBT rotation, right before block & tile entity placement.
    // Use priority placement to ensure dependent blocks are placed first.
    // Warning: do NOT place the block or tile entity!
    int rotate(final Block block, final int metadata, final NBTTagCompound nbtTileEntity, final ITransformation transformation);
    
    // Called when placing back a ship in the world, if saveExternals() returned non-null for that block.
    // Use this to restore external data from the ship schematic, right after block & tile entity placement.
    // Use priority placement to ensure dependent blocks are placed first.
    void restoreExternals(final World world, final BlockPos blockPos, final IBlockState blockState, final TileEntity tileEntity, final ITransformation transformation, final NBTBase nbtBase);
    
    // Support method to disable compatibility module on error
    static Block getBlockOrThrowException(final String blockId) {
        final ResourceLocation resourceLocation = new ResourceLocation(blockId);
        final Block block = Block.REGISTRY.getObject(resourceLocation);
        if (block == Blocks.AIR) {
            //throw new RuntimeException(String.format("Invalid %s version, please report to mod author, %s is missing", resourceLocation.getNamespace(), blockId));
        }
        return block;
    }
    
    // default rotation using EnumFacing properties used in many blocks
    static int rotateFirstEnumFacingProperty(final Block block, final int metadata, final byte rotationSteps) {
        // find first property using EnumFacing
        final IBlockState blockState = block.getStateFromMeta(metadata);
        PropertyEnum<EnumFacing> propertyFacing = null;
        for (final IProperty<?> propertyKey : blockState.getPropertyKeys()) {
            if (propertyKey instanceof PropertyEnum<?> && propertyKey.getValueClass() == EnumFacing.class) {
                propertyFacing = (PropertyEnum<EnumFacing>) propertyKey;
                break;
            }
        }
        if (propertyFacing != null) {
            final EnumFacing facingOld = blockState.getValue(propertyFacing);
            // skip vertical facings
            if (facingOld == EnumFacing.DOWN || facingOld == EnumFacing.UP) {
                return metadata;
            }
            
            // turn horizontal facings
            final EnumFacing facingNew;
            switch (rotationSteps) {
            case 1:
                facingNew = facingOld.rotateY();
                break;
            case 2:
                facingNew = facingOld.rotateY().rotateY();
                break;
            case 3:
                facingNew = facingOld.rotateY().rotateY().rotateY();
                break;
            default:
                facingNew = facingOld;
                break;
            }
            final IBlockState blockStateNew = blockState.withProperty(propertyFacing, facingNew);
            return block.getMetaFromState(blockStateNew);
        }
        
        return metadata;
    }
}