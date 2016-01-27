package org.fenixedu.messaging.ui;

import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class Sorter {

    public static <T> SortedSet<T> uniqueSort(Collection<T> collection) {
        return collection != null ? new TreeSet<>(collection) : new TreeSet<>();
    }

    public static <K, T> SortedMap<K, T> mapSort(Map<K, T> map) {
        return map != null ? new TreeMap<>(map) : new TreeMap<>();
    }
}
