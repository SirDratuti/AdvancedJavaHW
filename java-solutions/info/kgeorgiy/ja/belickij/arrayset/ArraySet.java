package info.kgeorgiy.ja.belickij.arrayset;

import java.util.*;

public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {
    // :NOTE: final
    private List<T> array;
    private Comparator<? super T> comparator;

    // :NOTE: this()
    public ArraySet() {
        array = Collections.emptyList();
    }

    public ArraySet(Collection<? extends T> collection) {
        array = new ArrayList<>(new TreeSet<T>(collection));
    }

    public ArraySet(Comparator<? super T> comparator) {
        array = Collections.emptyList();
        this.comparator = comparator;
    }

    public ArraySet(Collection<? extends T> collection, Comparator<? super T> comparator) {
        array = new ArrayList<>(convert(collection, comparator));
        this.comparator = comparator;
    }

    private ArraySet(List<T> collection, Comparator<? super T> newComparator, boolean isDescending) {
        // :NOTE: redundant isDescending
        if (!isDescending) {
            array = collection;
            this.comparator = newComparator;
        } else {
            DescendingList<T> temp = new DescendingList<>(collection);
            temp.descend();
            array = temp;
            this.comparator = Collections.reverseOrder(newComparator);
        }
    }

    private ArraySet(DescendingList<T> collection, Comparator<? super T> newComparator, boolean needDescend) {
        if (needDescend) {
            collection.descend();
        }
        array = collection;
        comparator = newComparator;
    }

    @Override
    public NavigableSet<T> descendingSet() {

        if (array instanceof DescendingList) {
            // :NOTE: array
            return new ArraySet<>(new DescendingList<>(((DescendingList<T>) array).getAll()),
                    Collections.reverseOrder(comparator),
                    !((DescendingList) array).getDescend());
        } else {
            return new ArraySet<>(new DescendingList<>(array),
                    Collections.reverseOrder(comparator),
                    true);
        }
    }


    private TreeSet<T> convert(Collection<? extends T> collection, Comparator<? super T> comparator) {
        TreeSet<T> tempArray = new TreeSet<>(comparator);
        tempArray.addAll(collection);
        return tempArray;
    }

    private int findIndex(T element, boolean inc, boolean fromStart) {
        int x = 0;
        if (fromStart) {
            x++;
        }
        int ind = Collections.binarySearch(array, element, comparator);
        if (ind < 0) {
            return ~ind - 1 + x;
        }

        if (inc) {
            return ind;
        }
        return ind - 1 + 2 * x;
    }

    @Override
    public T lower(T element) {
        return elementAt(findIndex(element, false, false));
    }

    @Override
    public T floor(T element) {
        return elementAt(findIndex(element, true, false));
    }

    @Override
    public T ceiling(T element) {
        return elementAt(findIndex(element, true, true));
    }

    @Override
    public T higher(T element) {
        return elementAt(findIndex(element, false, true));
    }

    private T elementAt(int index) {
        if (index >= 0 && index < array.size()) {
            return array.get(index);
        }
        return null;
    }

    @Override
    public NavigableSet<T> subSet(T firstElement, boolean firstInc, T secondElement, boolean secondInc) {
        if (comparator != null && this.comparator.compare(firstElement, secondElement) > 0) {
            throw new IllegalArgumentException();
        }
        if (comparator == null && firstElement instanceof Comparable && secondElement instanceof Comparable) {
            if (((Comparable<T>) firstElement).compareTo(secondElement) > 0) {
                throw new IllegalArgumentException();
            }
        }

        int firstIndex = findIndex(firstElement, firstInc, true);
        int secondIndex = findIndex(secondElement, secondInc, false) + 1;
        return new ArraySet<>(array.subList(firstIndex, Math.max(firstIndex, secondIndex)), comparator, false);
    }

    @Override
    public NavigableSet<T> headSet(T element, boolean inc) {
        return new ArraySet<>(array.subList(0, findIndex(element, inc, false) + 1), comparator, false);
    }

    @Override
    public NavigableSet<T> tailSet(T element, boolean inc) {
        return new ArraySet<>(array.subList(findIndex(element, inc, true), size()), comparator, false);
    }

    @Override
    public Iterator<T> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<T> subSet(T firstElement, T secondElement) {
        return subSet(firstElement, true, secondElement, false);
    }

    @Override
    public SortedSet<T> headSet(T element) {
        return headSet(element, false);
    }

    @Override
    public SortedSet<T> tailSet(T element) {
        return tailSet(element, true);
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(array).iterator();
    }

    @Override
    public T first() {
        if (!array.isEmpty()) {
            return array.get(0);
        }
        throw new NoSuchElementException();
    }

    @Override
    public T last() {
        if (!array.isEmpty()) {
            return array.get(size() - 1);
        }
        throw new NoSuchElementException();
    }

    @Override
    public int size() {
        return array.size();
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object object) {
        return Collections.binarySearch(array, (T) object, comparator) >= 0;
    }

}

