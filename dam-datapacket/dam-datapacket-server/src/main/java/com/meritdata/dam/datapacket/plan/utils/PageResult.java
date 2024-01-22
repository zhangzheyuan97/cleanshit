package com.meritdata.dam.datapacket.plan.utils;

import java.util.LinkedList;
import java.util.List;

/**
 * 分页结果类
 *
 * @author weijh
 * @date 2023/2/8
 */
public class PageResult<T> {
    private static final long serialVersionUID = -3186109799527111580L;
    private boolean hasNextPage;
    private int pageSize;
    private int pageNumber;
    private List<T> rows;

    public PageResult(int pageSize, int pageNum) {
        this.pageSize = pageSize;
        this.pageNumber = pageNum;
        rows = new LinkedList<T>();
    }

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    public void setHasNextPage(boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public List<T> getRows() {
        return rows;
    }

    public void addRows(List<T> rows) {
        this.rows.addAll(rows);
    }
}

