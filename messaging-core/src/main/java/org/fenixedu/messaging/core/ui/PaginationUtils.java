package org.fenixedu.messaging.core.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ui.Model;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class PaginationUtils {

    private PaginationUtils() {
    }

    public static <T extends Comparable<T>> List<T> paginate(Model model, String path, String property, Collection<T> items,
            int nr, int page) {
        List<T> list = items.stream().sorted().collect(Collectors.toList());
        return paginateAux(model, path, property, list, nr, page);
    }

    public static <T> List<T> paginate(Model model, String path, String property, Collection<T> items, Comparator<T> comparator,
            int nr, int page) {
        List<T> list =
                comparator != null ? items.stream().sorted(comparator).collect(Collectors.toList()) : new ArrayList<T>(items);
        return paginateAux(model, path, property, list, nr, page);
    }

    private static <T> List<T> paginateAux(Model model, String path, String property, List<T> items, int nr, int page) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        nr = itemsClip(nr, items.size());
        List<List<T>> pages = Lists.partition(items, nr);
        page = pageClip(page, pages.size());
        List<T> selected = pages.get(page - 1);
        if (model != null) {
            if (!Strings.isNullOrEmpty(property)) {
                model.addAttribute(property, selected);
            }
            model.addAttribute("path", path);
            model.addAttribute("page", page);
            model.addAttribute("items", nr);
            model.addAttribute("pages", pages.size());
        }
        return selected;

    }

    private static int itemsClip(int val, int max) {
        if (val < 1) {
            return max;
        }
        return val;
    }

    private static int pageClip(int val, int max) {
        val = val % max;
        if (val < 1) {
            return max + val;
        }
        return val;
    }
}
