package team.creative.littletiles.common.item;

import java.util.List;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.littletiles.common.block.BlockTile.TEResult;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.core.BlockPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.creativecore.common.gui.handler.GuiHandler;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.animation.entity.EntityAnimation;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.gui.handler.LittleTileGuiHandler;
import team.creative.littletiles.common.packet.LittleBlockPacket;
import team.creative.littletiles.common.packet.LittleBlockPacket.BlockPacketAction;

public class ItemLittleWrench extends Item {
    
    public ItemLittleWrench() {
        setCreativeTab(LittleTiles.littleTab);
        setMaxStackSize(1);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        
    }
    
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityLittleTiles) {
            
            if (world.isRemote) {
                TEResult result = BlockTile.loadTeAndTile(world, pos, player);
                if (player.isSneaking()) {
                    if (result.isComplete() && result.parent.isStructure())
                        LittleTileGuiHandler.openGui("structureoverview", new NBTTagCompound(), player, result.parent, result.tile);
                    else
                        PacketHandler.sendPacketToServer(new LittleBlockPacket(world, pos, player, BlockPacketAction.WRENCH));
                } else
                    PacketHandler.sendPacketToServer(new LittleBlockPacket(world, pos, player, BlockPacketAction.WRENCH_INFO));
            }
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }
    
    public static void rightClickAnimation(EntityAnimation animation, EntityPlayer player) {
        if (player.world.isRemote) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("uuid", animation.getCachedUniqueIdString());
            GuiHandler.openGui("diagnose", nbt, player);
        }
    }
}
