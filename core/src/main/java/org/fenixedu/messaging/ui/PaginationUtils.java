package org.fenixedu.messaging.ui;

import java.util.List;

import org.springframework.ui.Model;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class PaginationUtils {

    private PaginationUtils() {
    }

    public static <T> List<T> paginate(Model model, String path, String property, List<T> list, int items, int page) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        items = itemsClip(items, list.size());
        List<List<T>> pages = Lists.partition(list, items);
        page = pageClip(page, pages.size());
        List<T> selected = pages.get(page - 1);
        if (model != null) {
            if (!Strings.isNullOrEmpty(property)) {
                model.addAttribute(property, selected);
            }
            model.addAttribute("path", path);
            model.addAttribute("page", page);
            model.addAttribute("items", items);
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
