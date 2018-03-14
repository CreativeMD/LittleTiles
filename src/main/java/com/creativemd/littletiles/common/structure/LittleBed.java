package com.creativemd.littletiles.common.structure;

import static org.lwjgl.opengl.GL11.glRotatef;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.Rotation;
import com.creativemd.creativecore.common.utils.RotationUtils;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiStateButton;
import com.creativemd.creativecore.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.creativecore.gui.event.gui.GuiControlClickEvent;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.gui.SubGuiStructure;
import com.creativemd.littletiles.common.gui.controls.GuiDirectionIndicator;
import com.creativemd.littletiles.common.gui.controls.GuiTileViewer;
import com.creativemd.littletiles.common.items.ItemRecipe;
import com.creativemd.littletiles.common.packet.LittleBedPacket;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileIdentifierAbsolute;
import com.creativemd.littletiles.common.tiles.vec.LittleTilePos;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.SleepResult;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Biomes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketUseBed;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeManager.BiomeType;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleBed extends LittleStructure{
	
	public EntityLivingBase sleepingPlayer = null;
	@SideOnly(Side.CLIENT)
	public LittleTilePos playerPostion;
	public EnumFacing direction;
	
	@SideOnly(Side.CLIENT)
	public boolean hasBeenActivated;

	@Override
	protected void loadFromNBTExtra(NBTTagCompound nbt) {
		direction = EnumFacing.getHorizontal(nbt.getInteger("direction"));
	}

	@Override
	protected void writeToNBTExtra(NBTTagCompound nbt) {
		nbt.setInteger("direction", direction.getHorizontalIndex());
	}
	
	@SideOnly(Side.CLIENT)
	@CustomEventSubscribe
	public void buttonClicked(GuiControlClickEvent event)
	{		
		if(event.source.parent instanceof SubGuiStructure)
		{
			GuiTileViewer viewer = (GuiTileViewer) event.source.parent.get("tileviewer");
			GuiDirectionIndicator relativeDirection = (GuiDirectionIndicator) event.source.parent.get("relativeDirection");
			
			EnumFacing direction = EnumFacing.getHorizontal(((GuiStateButton) event.source.parent.get("direction")).getState());
			
			LittleSlidingDoor.updateDirection(viewer, direction.getOpposite(), relativeDirection);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void createControls(SubGui gui, LittleStructure structure) {
		GuiTileViewer tile = new GuiTileViewer("tileviewer", 0, 30, 100, 100, ((SubGuiStructure) gui).stack);
		tile.viewDirection = EnumFacing.UP;
		gui.addControl(tile);
		
		LittleTileSize size = LittleTilePreview.getSize(((SubGuiStructure) gui).stack);
		int index = EnumFacing.EAST.getHorizontalIndex();
		if(size.sizeX < size.sizeZ)
			index = EnumFacing.SOUTH.getHorizontalIndex();
		if(structure instanceof LittleBed)
			index = ((LittleBed) structure).direction.getHorizontalIndex();
		gui.addControl(new GuiStateButton("direction", index, 110, 30, 37, RotationUtils.getHorizontalFacingNames()));
		
		GuiDirectionIndicator relativeDirection = new GuiDirectionIndicator("relativeDirection", 155, 30, EnumFacing.UP);
		gui.addControl(relativeDirection);
		LittleSlidingDoor.updateDirection(tile, EnumFacing.getHorizontal(index).getOpposite(), relativeDirection);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public LittleStructure parseStructure(SubGui gui) {
		EnumFacing direction = EnumFacing.getHorizontal(((GuiStateButton) gui.get("direction")).getState());
		LittleBed bed = new LittleBed();
		bed.direction = direction;
		return bed;
	}
	
	@Override
	public boolean isBed(IBlockAccess world, BlockPos pos, EntityLivingBase player)
	{
		return true;
	}
	
	@SideOnly(Side.CLIENT)
    public static float getBedOrientationInDegrees(EntityPlayer player)
    {		
		try {
			LittleStructure bed = (LittleStructure) littleBed.get(player);
			if(bed instanceof LittleBed)
				switch (((LittleBed) bed).direction)
	            {
	                case SOUTH:
	                    return 90.0F;
	                case WEST:
	                    return 0.0F;
	                case NORTH:
	                    return 270.0F;
	                case EAST:
	                    return 180.0F;
                    default:
                    	return 0;
	            }
			
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
        IBlockState state = player.bedLocation == null ? null : player.world.getBlockState(player.bedLocation);
        if (state != null && state.getBlock().isBed(state, player.world, player.bedLocation, player))
        {
            EnumFacing enumfacing = state.getBlock().getBedDirection(state, player.world, player.bedLocation);

            switch (enumfacing)
            {
                case SOUTH:
                    return 90.0F;
                case WEST:
                    return 0.0F;
                case NORTH:
                    return 270.0F;
                case EAST:
                    return 180.0F;
            }
        }

        return 0.0F;
    }
	
	public static Method setSize = ReflectionHelper.findMethod(Entity.class, "setSize", "func_70105_a", float.class, float.class);
	
	public static Field sleeping = ReflectionHelper.findField(EntityPlayer.class, "sleeping", "field_71083_bS");
	public static Field sleepTimer = ReflectionHelper.findField(EntityPlayer.class, "sleepTimer", "field_71076_b");
	
	public static Field littleBed = ReflectionHelper.findField(EntityPlayer.class, "littleBed");;
	
	public SleepResult trySleep(EntityPlayer player, LittleTilePos highest)
	{		
		BlockPos center = highest.pos;

        if (!player.world.isRemote)
        {
            if (player.isPlayerSleeping() || !player.isEntityAlive())
            {
                return EntityPlayer.SleepResult.OTHER_PROBLEM;
            }

            if (!player.world.provider.isSurfaceWorld())
            {
                return EntityPlayer.SleepResult.NOT_POSSIBLE_HERE;
            }

            if (player.world.isDaytime())
            {
                return EntityPlayer.SleepResult.NOT_POSSIBLE_NOW;
            }

            double d0 = 8.0D;
            double d1 = 5.0D;
            List<EntityMob> list = player.world.<EntityMob>getEntitiesWithinAABB(EntityMob.class, new AxisAlignedBB((double)center.getX() - 8.0D, (double)center.getY() - 5.0D, (double)center.getZ() - 8.0D, (double)center.getX() + 8.0D, (double)center.getY() + 5.0D, (double)center.getZ() + 8.0D));

            if (!list.isEmpty())
            {
                return EntityPlayer.SleepResult.NOT_SAFE;
            }
        }

        if (player.isRiding())
        {
            player.dismountRidingEntity();
        }
        if(player.world.isRemote)
        	playerPostion = highest;
        sleepingPlayer = player;
        
        try {
			LittleBed.littleBed.set(player, this);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
        
        try {
			setSize.invoke(player, 0.2F, 0.2F);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
        
        float f1 = 0.5F + (float)direction.getFrontOffsetX() * 0.8F;
        float f = 0.5F + (float)direction.getFrontOffsetZ() * 0.8F;
        
        player.renderOffsetX = -1.8F * (float)direction.getFrontOffsetX();
        player.renderOffsetZ = -1.8F * (float)direction.getFrontOffsetZ();
        player.setPosition((double)((float)highest.getPosX() - 0.5F + f1), (double)((float)highest.getPosY()), (double)((float)highest.getPosZ() - 0.5F + f));
        
        try {
        	sleeping.setBoolean(player, true);
			sleepTimer.setInt(player, 0);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
        
        player.bedLocation = getMainTile().te.getPos();
        player.motionX = 0.0D;
        player.motionY = 0.0D;
        player.motionZ = 0.0D;

        if (!player.world.isRemote)
        {
            player.world.updateAllPlayersSleepingFlag();
        }
        return EntityPlayer.SleepResult.OK;
	}
	
	@Override
	public void onLittleTileDestroy()
	{
		super.onLittleTileDestroy();
		if(sleepingPlayer != null)
		{
			try {
	        	littleBed.set(sleepingPlayer, null);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static void setBedDirection(Entity player)
	{
		if(player instanceof EntityPlayer)
		{
			try {
				LittleStructure bed = (LittleStructure) littleBed.get(player);
				if(bed instanceof LittleBed)
				{
					int i = ((LittleBed) bed).direction.getHorizontalIndex();
					
					GlStateManager.rotate(i * 90, 0.0F, 1.0F, 0.0F);
					
					if(player == Minecraft.getMinecraft().player)
					{
						double height = 0.2;
						double forward = 0;
						
						GlStateManager.translate(((LittleBed) bed).direction.getDirectionVec().getX() * forward, height, ((LittleBed) bed).direction.getDirectionVec().getZ() * forward);
					}
					//GlStateManager.translate(0, ((LittleBed) bed).playerPostion.getPosY() - player.posY, 0);
					
					//Minecraft.getMinecraft().getRenderManager().playerViewY = (float)(((LittleBed) bed).direction.getHorizontalAngle() * 90 + 180);
					//Minecraft.getMinecraft().getRenderManager().playerViewX = 0.0F;
					
				}
			}catch(IllegalArgumentException | IllegalAccessException e){
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public boolean onBlockActivated(World world, LittleTile tile, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if(world.isRemote)
		{
			hasBeenActivated = true;
			return true;
		}
        if (world.provider.canRespawnHere() && world.getBiome(pos) != Biomes.HELL)
        {
        	LittleTilePos vec = getHighestCenterPoint();
        	if(this.sleepingPlayer != null)
        	{
        		player.sendStatusMessage(new TextComponentTranslation("tile.bed.occupied", new Object[0]), true);
        		return true;
        	}
        	
            SleepResult enumstatus = trySleep(player, vec);
            if (enumstatus == SleepResult.OK)
            {
            	player.addStat(StatList.SLEEP_IN_BED);
            	PacketHandler.sendPacketToPlayer(new LittleBedPacket(new LittleTileIdentifierAbsolute(getMainTile())), (EntityPlayerMP) player);
            	PacketHandler.sendPacketToTrackingPlayers(new LittleBedPacket(new LittleTileIdentifierAbsolute(getMainTile()), player), (EntityPlayerMP) player);
                return true;
            }
            else
            {
                if (enumstatus == SleepResult.NOT_POSSIBLE_NOW)
                {
                    player.sendStatusMessage(new TextComponentTranslation("tile.bed.noSleep"), true);
                }
                else if (enumstatus == SleepResult.NOT_SAFE)
                {
                	player.sendStatusMessage(new TextComponentTranslation("tile.bed.notSafe"), true);
                }
                
                return true;
            }
        }
        return true;
	}
	
	@Override
	public void onFlip(World world, EntityPlayer player, ItemStack stack, Axis axis, LittleTileVec doubledCenter)
	{
		if(axis == this.direction.getAxis())
			this.direction = this.direction.getOpposite();
	}
	
	
	@Override
	public void onRotate(World world, EntityPlayer player, ItemStack stack, Rotation rotation, LittleTileVec doubledCenter) 
	{
		this.direction = RotationUtils.rotateFacing(this.direction, rotation);
	}

}
