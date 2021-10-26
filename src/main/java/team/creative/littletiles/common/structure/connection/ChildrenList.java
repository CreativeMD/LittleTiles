package team.creative.littletiles.common.structure.connection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public abstract class ChildrenList<T> {
    
    protected T parent;
    private Iterable<T> all;
    private List<T> children;
    protected HashMap<String, T> extensions;
    
    public ChildrenList(List<T> children) {
        set(children);
    }
    
    public T getParent() {
        return parent;
    }
    
    public boolean hasParent() {
        return parent != null;
    }
    
    protected void set(int index, T child) {
        this.children.set(index, child);
    }
    
    protected void set(List<T> children) {
        this.children = children != null ? new UnmodifiableList(children) : Collections.EMPTY_LIST;
    }
    
    protected T getChildDirectly(int index) {
        return children.get(index);
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
    
    public Iterable<T> all() {
        if (all == null)
            all = new Iterable<T>() {
                
                @Override
                public Iterator<T> iterator() {
                    return ChildrenList.this.iteratorAll();
                }
            };
        return all;
    }
    
    public Iterator<T> iteratorAll() {
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
    
    public Iterable<Entry<String, T>> extensionEntries() {
        return extensions.entrySet();
    }
    
    public boolean hasChildren() {
        return !children.isEmpty();
    }
    
    public int sizeChildren() {
        return children.size();
    }
    
    public boolean hasExtensions() {
        return !extensions.isEmpty();
    }
    
    public int sizeExtensions() {
        return extensions.size();
    }
    
    public int size() {
        return children.size() + extensions.size();
    }
    
    public boolean isEmpty() {
        return children.isEmpty() && extensions.isEmpty();
    }
    
    static class UnmodifiableList<E> extends ArrayList<E> {
        
        UnmodifiableList(List<? extends E> list) {
            super(list);
        }
        
        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public Spliterator<E> spliterator() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public boolean add(E e) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void add(int index, E element) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public E remove(int index) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        protected void removeRange(int fromIndex, int toIndex) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public boolean addAll(Collection<? extends E> c) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public boolean addAll(int index, Collection<? extends E> c) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void replaceAll(UnaryOperator<E> operator) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void sort(Comparator<? super E> c) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public ListIterator<E> listIterator() {
            return listIterator(0);
        }
        
        @Override
        public ListIterator<E> listIterator(final int index) {
            return new ListIterator<E>() {
                private final ListIterator<? extends E> i = UnmodifiableList.super.listIterator(index);
                
                @Override
                public boolean hasNext() {
                    return i.hasNext();
                }
                
                @Override
                public E next() {
                    return i.next();
                }
                
                @Override
                public boolean hasPrevious() {
                    return i.hasPrevious();
                }
                
                @Override
                public E previous() {
                    return i.previous();
                }
                
                @Override
                public int nextIndex() {
                    return i.nextIndex();
                }
                
                @Override
                public int previousIndex() {
                    return i.previousIndex();
                }
                
                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
                
                @Override
                public void set(E e) {
                    throw new UnsupportedOperationException();
                }
                
                @Override
                public void add(E e) {
                    throw new UnsupportedOperationException();
                }
                
                @Override
                public void forEachRemaining(Consumer<? super E> action) {
                    i.forEachRemaining(action);
                }
            };
        }
        
        @Override
        public List<E> subList(int fromIndex, int toIndex) {
            return new UnmodifiableList<>(super.subList(fromIndex, toIndex));
        }
    }
    
}
