package team.creative.littletiles.common.structure.connection.children;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.level.ISubLevel;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.connection.ILevelPositionProvider;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;

public class LevelChildrenList extends ChildrenList<StructureChildConnection> {
    
    public final LittleStructure owner;
    
    public LevelChildrenList(LittleStructure owner) {
        super(null);
        this.owner = owner;
    }
    
    public void initAfterPlacing(int children) {
        List<StructureChildConnection> list = new ArrayList<>(children);
        for (int i = 0; i < children; i++)
            list.add(null);
        set(list);
    }
    
    public StructureChildConnection removeExtension(String key) throws CorruptedConnectionException, NotYetConnectedException {
        StructureChildConnection ex = super.removeExt(key);
        if (ex != null) {
            LittleStructure parentStructure = ex.getStructure();
            if (!ex.extension)
                throw new RuntimeException("Cannot remove non dynamic child");
            parentStructure.children.parent = null;
        }
        return ex;
    }
    
    @Override
    protected void added(StructureChildConnection child) {}
    
    public void load(CompoundTag nbt) {
        if (nbt.contains(LittleGroup.PARENT_KEY))
            parent = StructureChildConnection.load(owner, nbt.getCompound(LittleGroup.PARENT_KEY), true);
        else
            parent = null;
        
        ListTag list = nbt.getList(LittleGroup.CHILDREN_KEY, Tag.TAG_COMPOUND);
        List<StructureChildConnection> children = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++)
            children.add(StructureChildConnection.load(owner, list.getCompound(i), false));
        set(children);
        
        extensions.clear();
        list = nbt.getList(LittleGroup.EXTENSION_KEY, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag extension = list.getCompound(i);
            extensions.put(extension.getString(LittleGroup.EXTENSION_ID_KEY), StructureChildConnection.load(owner, extension, false));
        }
    }
    
    public void save(CompoundTag nbt) {
        if (hasParent())
            nbt.put(LittleGroup.PARENT_KEY, parent.save(new CompoundTag()));
        
        if (hasChildren()) {
            ListTag list = new ListTag();
            for (StructureChildConnection child : children())
                list.add(child.save(new CompoundTag()));
            nbt.put(LittleGroup.CHILDREN_KEY, list);
        }
        if (hasExtensions()) {
            ListTag list = new ListTag();
            for (Entry<String, StructureChildConnection> pair : extensionEntries()) {
                CompoundTag extensionNBT = pair.getValue().save(new CompoundTag());
                extensionNBT.putString(LittleGroup.EXTENSION_ID_KEY, pair.getKey());
                list.add(extensionNBT);
            }
            nbt.put(LittleGroup.EXTENSION_KEY, list);
        }
    }
    
    public void connectToChild(int i, LittleStructure child) {
        Level level = owner.getLevel();
        Level childLevel = child.getLevel();
        
        StructureChildConnection connector;
        if (childLevel == level)
            connector = new StructureChildConnection(owner, false, false, i, child.getPos().subtract(owner.getPos()), child.getIndex(), child.getAttribute());
        else if (childLevel instanceof ISubLevel)
            connector = new StructureChildToSubLevelConnection(owner, false, i, child.getPos().subtract(owner.getPos()), child.getIndex(), child
                    .getAttribute(), ((ISubLevel) childLevel).getHolder().getUUID());
        else
            throw new RuntimeException("Invalid connection between to structures!");
        
        set(i, connector);
    }
    
    public void connectToExtension(String extension, LittleStructure child) {
        Level level = owner.getLevel();
        Level childLevel = child.getLevel();
        
        StructureChildConnection connector;
        if (childLevel == level)
            connector = new StructureChildConnection(owner, false, false, -1, child.getPos().subtract(owner.getPos()), child.getIndex(), child.getAttribute());
        else if (childLevel instanceof ISubLevel)
            connector = new StructureChildToSubLevelConnection(owner, false, -1, child.getPos().subtract(owner.getPos()), child.getIndex(), child
                    .getAttribute(), ((ISubLevel) childLevel).getHolder().getUUID());
        else
            throw new RuntimeException("Invalid connection between to structures!");
        
        extensions.put(extension, connector);
    }
    
    public void connectToParentAsChild(int i, LittleStructure parent) {
        Level level = owner.getLevel();
        Level parentLevel = parent.getLevel();
        
        StructureChildConnection connector;
        if (parentLevel == level)
            connector = new StructureChildConnection(owner, true, false, i, parent.getPos().subtract(owner.getPos()), parent.getIndex(), parent.getAttribute());
        else if (level instanceof ISubLevel)
            connector = new StructureChildFromSubLevelConnection(owner, false, i, parent.getPos().subtract(owner.getPos()), parent.getIndex(), parent.getAttribute());
        else
            throw new RuntimeException("Invalid connection between to structures!");
        
        this.parent = connector;
    }
    
    public void connectToParentAsExtension(LittleStructure parent) {
        Level level = owner.getLevel();
        Level parentLevel = parent.getLevel();
        
        StructureChildConnection connector;
        if (parentLevel == level)
            connector = new StructureChildConnection(owner, true, true, -1, parent.getPos().subtract(owner.getPos()), parent.getIndex(), parent.getAttribute());
        else if (level instanceof ISubLevel)
            connector = new StructureChildFromSubLevelConnection(owner, true, -1, parent.getPos().subtract(owner.getPos()), parent.getIndex(), parent.getAttribute());
        else
            throw new RuntimeException("Invalid connection between to structures!");
        
        this.parent = connector;
    }
    
    public StructureChildConnection generateConnection(ILevelPositionProvider parent) {
        Level level = owner.getLevel();
        Level parentLevel = parent.getLevel();
        
        StructureChildConnection connector;
        if (parentLevel == level)
            connector = new StructureChildConnection(parent, true, false, 0, owner.getPos().subtract(parent.getPos()), owner.getIndex(), owner.getAttribute());
        else if (level instanceof ISubLevel)
            connector = new StructureChildToSubLevelConnection(parent, false, 0, owner.getPos().subtract(parent.getPos()), owner.getIndex(), owner
                    .getAttribute(), ((ISubLevel) level).getHolder().getUUID());
        else
            throw new RuntimeException("Invalid connection between to structures!");
        return connector;
    }
    
    public boolean hasChild(int child) {
        return child >= 0 && child < sizeChildren();
    }
    
    public StructureChildConnection getChild(int child) throws CorruptedConnectionException, NotYetConnectedException {
        if (child >= 0 && child < sizeChildren())
            return getChildDirectly(child);
        throw new CorruptedConnectionException("child with id " + child + " does not exist");
    }
    
}
