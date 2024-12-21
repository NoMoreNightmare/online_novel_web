package com.java2nb.novel.controller.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageBean<T> {
    private long pageNum = 1;

    private long pageSize = 10;

    private long total = 0;

    private boolean pagination = true;

    private List<T> list;

    public PageBean(long page, long pageSize) {
        this.pageNum = page;
        this.pageSize = pageSize;
    }


}
