package team.creative.littletiles.common.structure.connection;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class ChildrenList<T> implements Iterable<T> {
    
    private List<T> children;
    private HashMap<String, T> extensions;
    
    public ChildrenList(List<T> children) {
        this.children = children != null ? Collections.unmodifiableList(children) : Collections.EMPTY_LIST;
    }
    
    protected abstract void added(T child);
    
    public void addExtension(String key, T extension) {
        if (extensions.containsKey(key))
            throw new RuntimeException("Extension " + key + " already exists");
        extensions.put(key, extension);
    }
    
    public void addExtensions(Map<String, T> extensions) {
        for (Entry<String, T> pair : extensions.entrySet())
            addExtension(pair.getKey(), pair.getValue());
    }
    
    protected T removeExt(String key) {
        return extensions.remove(key);
    }
    
    public T getExtension(String key) {
        return extensions.get(key);
    }
    
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            
            private Iterator<T> itr = children.iterator();
            private boolean firstItr = true;
            
            @Override
            public boolean hasNext() {
                if (!itr.hasNext() && firstItr) {
                    itr = extensions.values().iterator();
                    firstItr = false;
                }
                return itr.hasNext();
            }
            
            @Override
            public T next() {
                return itr.next();
            }
            
        };
    }
    
    public Iterable<T> children() {
        return children;
    }
    
    public Iterable<T> extensions() {
        return extensions.values();
    }
    
    public int size() {
        return children.size() + extensions.size();
    }
    
    public boolean isEmpty() {
        return children.isEmpty() && extensions.isEmpty();
    }
    
}
