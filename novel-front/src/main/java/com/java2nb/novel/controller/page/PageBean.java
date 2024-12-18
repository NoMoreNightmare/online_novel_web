package com.java2nb.novel.controller.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageBean<T> {
    private long page = 1;

    private long pageSize = 10;

    private long total = 0;

    private boolean pagination = true;

    private List<T> list;


}
