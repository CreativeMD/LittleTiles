package com.creativemd.littletiles.common.packet;

import java.util.UUID;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.creativecore.common.utils.mc.TickUtils;
import com.creativemd.creativecore.common.utils.type.Pair;
import com.creativemd.creativecore.common.world.CreativeWorld;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.item.ItemLittleChisel;
import com.creativemd.littletiles.common.item.ItemLittleGrabber;
import com.creativemd.littletiles.common.item.ItemLittlePaintBrush;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.LittleTileColored;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.world.WorldAnimationHandler;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleBlockPacket extends CreativeCorePacket {
    
    public static enum BlockPacketAction {
        
        COLOR_TUBE(true) {
            @Override
            public void action(World world, TileEntityLittleTiles te, IParentTileList parent, LittleTile tile, ItemStack stack, EntityPlayer player, RayTraceResult moving, BlockPos pos, NBTTagCompound nbt) {
                if (parent.isStructure()) {
                    try {
                        LittleStructure structure = parent.getStructure();
                        if (structure.hasStructureColor()) {
                            ItemLittlePaintBrush.setColor(player.getHeldItemMainhand(), structure.getStructureColor());
                            return;
                        }
                    } catch (CorruptedConnectionException | NotYetConnectedException e) {}
                    
                }
                if ((tile.getClass() == LittleTile.class || tile instanceof LittleTileColored)) {
                    int color = ColorUtils.WHITE;
                    if (tile instanceof LittleTileColored)
                        color = ((LittleTileColored) tile).color;
                    ItemLittlePaintBrush.setColor(player.getHeldItemMainhand(), color);
                }
            }
        },
        CHISEL(false) {
            @Override
            public void action(World world, TileEntityLittleTiles te, IParentTileList parent, LittleTile tile, ItemStack stack, EntityPlayer player, RayTraceResult moving, BlockPos pos, NBTTagCompound nbt) {
                LittlePreview preview = tile.getPreviewTile();
                preview.box = new LittleBox(0, 0, 0, LittleGridContext.get().size, LittleGridContext.get().size, LittleGridContext.get().size);
                ItemLittleChisel.setPreview(stack, preview);
            }
        },
        GRABBER(false) {
            @Override
            public void action(World world, TileEntityLittleTiles te, IParentTileList parent, LittleTile tile, ItemStack stack, EntityPlayer player, RayTraceResult moving, BlockPos pos, NBTTagCompound nbt) {
                ItemLittleGrabber.getMode(stack).littleBlockAction(world, te, tile, stack, pos, nbt);
            }
        },
        WRENCH(true) {
            
            @Override
            public void action(World world, TileEntityLittleTiles te, IParentTileList parent, LittleTile tile, ItemStack stack, EntityPlayer player, RayTraceResult moving, BlockPos pos, NBTTagCompound nbt) {
                player.sendStatusMessage(new TextComponentString("grid:" + te.getContext()), true);
                te.combineTiles();
                te.convertBlockToVanilla();
                te.updateTiles();
            }
            
        },
        WRENCH_INFO(true) {
            
            @Override
            public void action(World world, TileEntityLittleTiles te, IParentTileList parent, LittleTile tile, ItemStack stack, EntityPlayer player, RayTraceResult moving, BlockPos pos, NBTTagCompound nbt) {
                if (parent.isStructure())
                    try {
                        String info = parent.getStructure().info();
                        if (!info.isEmpty())
                            player.sendMessage(new TextComponentString(info));
                    } catch (CorruptedConnectionException | NotYetConnectedException e) {}
            }
            
        },
        RECIPE(false) {
            @Override
            public void action(World world, TileEntityLittleTiles te, IParentTileList parent, LittleTile tile, ItemStack stack, EntityPlayer player, RayTraceResult moving, BlockPos pos, NBTTagCompound nbt) {
                LittlePreviews previews = new LittlePreviews(te.getContext());
                if (parent.isStructure()) {
                    if (nbt.getBoolean("secondMode"))
                        try {
                            previews = parent.getStructure().getPreviews(parent.getStructure().getPos());
                        } catch (CorruptedConnectionException | NotYetConnectedException e) {
                            return;
                        }
                    else
                        try {
                            previews = parent.getStructure().findTopStructure().getPreviews(parent.getStructure().getPos());
                        } catch (CorruptedConnectionException | NotYetConnectedException e) {
                            return;
                        }
                } else {
                    if (nbt.getBoolean("secondMode"))
                        for (Pair<IParentTileList, LittleTile> pair : te.allTiles())
                            previews.addWithoutCheckingPreview(pair.value.getPreviewTile());
                    else
                        previews.addWithoutCheckingPreview(tile.getPreviewTile());
                }
                
                LittlePreview.savePreview(previews, stack);
            }
        };
        
        public final boolean rightClick;
        
        private BlockPacketAction(boolean rightClick) {
            this.rightClick = rightClick;
        }
        
        public abstract void action(World world, TileEntityLittleTiles te, IParentTileList parent, LittleTile tile, ItemStack stack, EntityPlayer player, RayTraceResult moving, BlockPos pos, NBTTagCompound nbt);
    }
    
    public BlockPos blockPos;
    public Vec3d pos;
    public Vec3d look;
    public BlockPacketAction action;
    public World world;
    public NBTTagCompound nbt;
    public UUID uuid;
    
    public LittleBlockPacket() {
        
    }
    
    public LittleBlockPacket(World world, BlockPos blockPos, EntityPlayer player, BlockPacketAction action) {
        this(world, blockPos, player, action, new NBTTagCompound());
    }
    
    public LittleBlockPacket(World world, BlockPos blockPos, EntityPlayer player, BlockPacketAction action, NBTTagCompound nbt) {
        this.blockPos = blockPos;
        this.action = action;
        this.pos = player.getPositionEyes(TickUtils.getPartialTickTime());
        double d0 = player.capabilities.isCreativeMode ? 5.0F : 4.5F;
        Vec3d look = player.getLook(TickUtils.getPartialTickTime());
        this.look = pos.addVector(look.x * d0, look.y * d0, look.z * d0);
        this.nbt = nbt;
        if (world instanceof CreativeWorld)
            uuid = ((CreativeWorld) world).parent.getUniqueID();
    }
    
    @Override
    public void writeBytes(ByteBuf buf) {
        writePos(buf, blockPos);
        writeVec3d(pos, buf);
        writeVec3d(look, buf);
        buf.writeInt(action.ordinal());
        writeNBT(buf, nbt);
        if (uuid != null) {
            buf.writeBoolean(true);
            writeString(buf, uuid.toString());
        } else
            buf.writeBoolean(false);
    }
    
    @Override
    public void readBytes(ByteBuf buf) {
        blockPos = readPos(buf);
        pos = readVec3d(buf);
        look = readVec3d(buf);
        action = BlockPacketAction.values()[buf.readInt()];
        nbt = readNBT(buf);
        if (buf.readBoolean())
            uuid = UUID.fromString(readString(buf));
        else
            uuid = null;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void executeClient(EntityPlayer player) {
        
    }
    
    @Override
    public void executeServer(EntityPlayer player) {
        
        World world = player.world;
        
        if (uuid != null) {
            EntityAnimation animation = WorldAnimationHandler.findAnimation(false, uuid);
            if (animation == null)
                return;
            
            if (!LittleAction.isAllowedToInteract(player, animation, action.rightClick))
                return;
            
            world = animation.fakeWorld;
            pos = animation.origin.transformPointToFakeWorld(pos);
            look = animation.origin.transformPointToFakeWorld(look);
        }
        
        TileEntity tileEntity = world.getTileEntity(blockPos);
        if (tileEntity instanceof TileEntityLittleTiles) {
            TileEntityLittleTiles te = (TileEntityLittleTiles) tileEntity;
            Pair<IParentTileList, LittleTile> pair = te.getFocusedTile(pos, look);
            
            if (!LittleAction.isAllowedToInteract(world, player, blockPos, action.rightClick, EnumFacing.EAST)) {
                LittleAction.sendBlockResetToClient(world, player, te);
                return;
            }
            
            if (pair != null) {
                ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
                RayTraceResult moving = te.rayTrace(pos, look);
                action.action(world, te, pair.key, pair.value, stack, player, moving, blockPos, nbt);
                
                if (!player.world.isRemote) {
                    EntityPlayerMP playerMP = (EntityPlayerMP) player;
                    Slot slot = playerMP.openContainer.getSlotFromInventory(playerMP.inventory, playerMP.inventory.currentItem);
                    playerMP.connection.sendPacket(new SPacketSetSlot(playerMP.openContainer.windowId, slot.slotNumber, playerMP.inventory.getCurrentItem()));
                }
            }
        }
    }
    
}
