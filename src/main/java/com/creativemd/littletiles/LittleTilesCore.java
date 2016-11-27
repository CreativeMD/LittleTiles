package com.creativemd.littletiles;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.creativemd.creativecore.CreativeCore;
import com.creativemd.creativecore.client.avatar.AvatarItemStack;
import com.creativemd.creativecore.command.GuiCommand;
import com.creativemd.creativecore.common.packet.BlockUpdatePacket;
import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.packet.CreativeMessageHandler;
import com.creativemd.creativecore.common.packet.PacketReciever;
import com.creativemd.creativecore.common.packet.gui.ContainerControlUpdatePacket;
import com.creativemd.creativecore.common.packet.gui.GuiLayerPacket;
import com.creativemd.creativecore.common.packet.gui.GuiUpdatePacket;
import com.creativemd.creativecore.common.packet.gui.OpenGuiPacket;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.common.utils.stack.StackInfo;
import com.creativemd.creativecore.event.GuiTickHandler;
import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiAvatarLabel;
import com.creativemd.creativecore.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.gui.controls.gui.GuiProgressBar;
import com.creativemd.creativecore.gui.controls.gui.GuiScrollBox;
import com.creativemd.creativecore.gui.controls.gui.GuiStateButton;
import com.creativemd.creativecore.gui.controls.gui.GuiTextfield;
import com.creativemd.creativecore.gui.event.gui.GuiControlClickEvent;
import com.creativemd.creativecore.gui.opener.CustomGuiHandler;
import com.creativemd.creativecore.gui.opener.GuiHandler;
import com.creativemd.creativecore.gui.premade.SubContainerEmpty;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.ModListHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleTilesCore extends DummyModContainer {
	
	public static final String modid = "littletilescore";
	public static final String version = "1.0.0";
	
	public LittleTilesCore() {

		super(new ModMetadata());
		ModMetadata meta = getMetadata();
		meta.modId = modid;
		meta.name = "LittleTilesCore";
		meta.version = version; //String.format("%d.%d.%d.%d", majorVersion, minorVersion, revisionVersion, buildVersion);
		meta.credits = "CreativeMD";
		meta.authorList = Arrays.asList("CreativeMD");
		meta.description = "";
		meta.url = "";
		meta.screenshots = new String[0];
		meta.logoFile = "";
		//CreativeCore.loadMod();
	}
	
	@Override
	public boolean registerBus(EventBus bus, LoadController controller) {
		
		bus.register(this);
		//bus.register(CreativeCore.instance);
		return true;
	}
}
