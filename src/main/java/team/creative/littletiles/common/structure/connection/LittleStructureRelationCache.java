package team.creative.littletiles.common.structure.connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.connection.children.StructureChildConnection;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;

public class LittleStructureRelationCache {
    
    private final LittleStructure structure;
    private final boolean differentLevel;
    private final List<LittleStructureRelationCache> children = new ArrayList<>();
    private final Object2ObjectMap<String, LittleStructureRelationCache> extensions = new Object2ObjectArrayMap<>();
    
    public LittleStructureRelationCache(LittleStructure structure) {
        this.structure = structure;
        this.differentLevel = false;
        cacheAllChildren();
    }
    
    public LittleStructureRelationCache(IStructureConnection connection) throws CorruptedConnectionException, NotYetConnectedException {
        this.structure = connection.getStructure();
        this.differentLevel = connection.isLinkToAnotherWorld();
    }
    
    protected void cacheAllChildren() {
        for (int i = 0; i < structure.children.sizeChildren(); i++)
            try {
                children.add(new LittleStructureRelationCache(structure.children.getChild(i)));
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        
        for (Entry<String, StructureChildConnection> entry : structure.children.extensionEntries())
            try {
                extensions.put(entry.getKey(), new LittleStructureRelationCache(entry.getValue()));
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
    }
    
    public void forEachSameLevel(LittleStructure against, LittleStructureTransfer transfer) {
        transfer.transfer(structure, against);
        
        for (int i = 0; i < children.size(); i++)
            try {
                var thisChild = children.get(i);
                if (thisChild.differentLevel)
                    continue;
                thisChild.forEach(against.children.getChild(i).getStructure(), transfer);
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        
        for (Entry<String, LittleStructureRelationCache> entry : extensions.entrySet())
            try {
                var thisChild = entry.getValue();
                if (thisChild.differentLevel)
                    continue;
                thisChild.forEach(against.children.getExtension(entry.getKey()).getStructure(), transfer);
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
    }
    
    public void forEach(LittleStructure against, LittleStructureTransfer transfer) {
        transfer.transfer(structure, against);
        
        for (int i = 0; i < children.size(); i++)
            try {
                var thisChild = children.get(i);
                thisChild.forEach(against.children.getChild(i).getStructure(), transfer);
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        
        for (Entry<String, LittleStructureRelationCache> entry : extensions.entrySet())
            try {
                var thisChild = entry.getValue();
                thisChild.forEach(against.children.getExtension(entry.getKey()).getStructure(), transfer);
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
    }
    
    @FunctionalInterface
    public static interface LittleStructureTransfer {
        
        public void transfer(LittleStructure oldStructure, LittleStructure newStructure);
        
    }
    
}
