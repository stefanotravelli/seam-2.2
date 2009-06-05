package org.jboss.seam.util;

import java.util.ArrayList;
import java.util.List;

public class Sorter<T> {

    private List<SortItem<T>> list = null; // new ArrayList();

    public List<SortItem<T>> sort(List<SortItem<T>> lst) {

        this.list = lst;
        List<SortItem<T>> res = new ArrayList<SortItem<T>>();
        SortItem<T> inmost = null;

        do {
            inmost = getInmost();
            if (inmost != null) {
                res.add(inmost);
                remove(inmost);
            }
        } while (!list.isEmpty() && inmost != null);

        if (!list.isEmpty()) {
            throw new IllegalArgumentException("Can not sort list:" + list);
        }

        return res;
    }

    private void remove(SortItem<T> item) {
        list.remove(item);
        for (SortItem<T> o : list) {
            o.getWithin().remove(item);
        }
    }

    private SortItem<T> getInmost() {
        SortItem<T> res = null;
        for (SortItem<T> o : list) {
            if (o.getWithin().isEmpty() && nobodyWantsAround(o)) {
                res = o;
                break;
            }
        }
        return res;
    }

    private boolean nobodyWantsAround(SortItem<T> item) {
        boolean res = true;
        for (SortItem<T> o : list) {
            if (o.getAround().contains(item)) {
                res = false;
                break;
            }
        }
        return res;
    }

}
