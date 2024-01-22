package com.meritdata.dam.datapacket.plan.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author： lt.liu
 * 时间：2023/3/16
 * @description:
 **/
public class PageUtil<T> {
    private List<T> list = new ArrayList<>();
    private int pageSize;
    private int pageNumber;

    public PageUtil(List<T> list, int pageSize, int pageNumber) {
        this.list.clear();
        this.list = list;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    public List<T> getList() {
        int startIndex = (pageNumber - 1) * pageSize;
        if (0 == pageSize && 0 == pageNumber) {
            return list;
        } else if (0 >= pageNumber || 0 >= pageSize || null == list || list.size() == 0 || startIndex >= list.size()) {
            return new ArrayList<>();
        } else {

            int endIndex = (pageNumber - 1) * pageSize + pageSize;
            if (endIndex > list.size()) {
                endIndex = list.size();
            }
            List<T> ts = list.subList(startIndex, endIndex);
            return ts;
        }
    }
}
