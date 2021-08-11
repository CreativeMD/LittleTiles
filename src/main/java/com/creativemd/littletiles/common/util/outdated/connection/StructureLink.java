package com.creativemd.littletiles.common.util.outdated.connection;

import java.util.UUID;

import com.creativemd.littletiles.common.util.outdated.identifier.LittleIdentifierRelative;

import net.minecraft.nbt.NBTTagCompound;
import team.creative.littletiles.common.structure.connection.ILevelPositionProvider;
import team.creative.littletiles.common.structure.connection.StructureChildConnection;
import team.creative.littletiles.common.structure.connection.StructureChildFromSubWorldConnection;
import team.creative.littletiles.common.structure.connection.StructureChildToSubWorldConnection;

@Deprecated
public class StructureLink {
    
    @Deprecated
    public static StructureChildConnection loadFromNBTOld(ILevelPositionProvider structure, NBTTagCompound nbt, boolean isChild) {
        int childID = nbt.getInteger("childID");
        int attribute = nbt.getInteger("type");
        LittleIdentifierRelative identifier = new LittleIdentifierRelative(nbt);
        int index = identifier.generateIndex(structure.getPos());
        if (nbt.hasKey("entity"))
            return new StructureChildToSubWorldConnection(structure, false, childID, identifier.coord, index, attribute, UUID.fromString(nbt.getString("entity")));
        else if (nbt.getBoolean("subWorld"))
            return new StructureChildFromSubWorldConnection(structure, false, childID, identifier.coord, index, attribute);
        return new StructureChildConnection(structure, isChild, false, childID, identifier.coord, index, attribute);
    }
}
