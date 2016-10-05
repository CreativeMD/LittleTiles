package com.creativemd.littletiles.common.command;

import com.creativemd.creativecore.gui.opener.GuiHandler;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

public class ImportCommand extends CommandBase {

	@Override
	public String getCommandName() {
		return "lt-import";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "import little structures";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		GuiHandler.openGui("lt-import", new NBTTagCompound(), (EntityPlayer) sender.getCommandSenderEntity());
	}

}
