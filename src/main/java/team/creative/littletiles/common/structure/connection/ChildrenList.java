package team.creative.littletiles.common.structure.connection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.MissingChildException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;

public class ChildrenList implements Iterable<StructureChildConnection> {
    
    private int size = 0;
    private List<StructureChildConnection> content = new ArrayList<>();
    
    public ChildrenList() {
        
    }
    
    public StructureChildConnection get(int index) throws CorruptedConnectionException, NotYetConnectedException {
        if (index >= content.size())
            throw new MissingChildException(index);
        StructureChildConnection child = content.get(index);
        if (child != null)
            return child;
        throw new MissingChildException(index);
    }
    
    public void set(StructureChildConnection child) {
        while (content.size() <= child.childId)
            content.add(null);
        content.set(child.childId, child);
        size = countSize();
    }
    
    public void remove(int index) throws CorruptedConnectionException, NotYetConnectedException {
        StructureChildConnection child = get(index);
        if (!child.dynamic)
            throw new RuntimeException("Cannot remove non dynamic child");
        child.getStructure().removeParent();
        content.set(index, null);
        size = countSize();
    }
    
    @Override
    public Iterator<StructureChildConnection> iterator() {
        return new Iterator<StructureChildConnection>() {
            
            private Iterator<StructureChildConnection> iter = content.iterator();
            private StructureChildConnection next = findNext();
            
            StructureChildConnection findNext() {
                while (iter.hasNext()) {
                    StructureChildConnection child = iter.next();
                    if (child != null)
                        return child;
                }
                return null;
            }
            
            @Override
            public boolean hasNext() {
                return next != null;
            }
            
            @Override
            public StructureChildConnection next() {
                StructureChildConnection toReturn = next;
                next = findNext();
                return toReturn;
            }
        };
    }
    
    private int countSize() {
        int size = 0;
        for (StructureChildConnection child : content)
            if (child != null)
                size++;
        return size;
    }
    
    public int size() {
        return size;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    public int findFreeIndex() {
        for (int i = 0; i < content.size(); i++)
            if (content.get(i) == null)
                return i;
        return content.size();
    }
    
}
