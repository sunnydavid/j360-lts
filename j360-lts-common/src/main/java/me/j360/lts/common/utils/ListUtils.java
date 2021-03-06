package me.j360.lts.common.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/28/14.
 */
public class ListUtils {

    /**
     * 过滤
     *
     * @param list
     * @param filter
     * @param <E>
     * @return
     */
    public static <E> List<E> filter(final List<E> list, Filter<E> filter) {
        List<E> newList = new ArrayList<E>();
        if (list != null && list.size() != 0) {
            for (E e : list) {
                if (filter.filter(e)) {
                    newList.add(e);
                }
            }
        }
        return newList;
    }

    public interface Filter<E> {

        /**
         * 如果满足要求就返回true
         *
         * @param e
         * @return
         */
        public boolean filter(E e);

    }
}
