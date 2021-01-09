package com.creativemd.littletiles.common.command;

import com.creativemd.creativecore.common.gui.opener.GuiHandler;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

public class ImportCommand extends CommandBase {
    
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        GuiHandler.openGui("lt-import", new NBTTagCompound(), (EntityPlayer) sender.getCommandSenderEntity());
    }
    
    @Override
    public String getName() {
        return "lt-import";
    }
    
    @Override
    public String getUsage(ICommandSender sender) {
        return "/lt-import can be used to import little structures";
    }
    
}
