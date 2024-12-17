package com.java2nb.novel.service.impl;

import com.java2nb.novel.core.cache.CacheKey;
import com.java2nb.novel.core.cache.CacheService;
import com.java2nb.novel.core.result.Result;
import com.java2nb.novel.entity.Book;
import com.java2nb.novel.mapper.BookDynamicSqlSupport;
import com.java2nb.novel.mapper.BookMapper;
import com.java2nb.novel.service.MyBookService;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;

import javax.annotation.Resource;
import java.util.List;

import static com.java2nb.novel.mapper.BookDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.select.SelectDSL.select;

public class MyBookServiceImpl implements MyBookService {
    @Resource
    BookMapper bookMapper;

    @Resource
    CacheService cacheService;

    @Override
    public Result<?> listClickRank() {
        List<Book> books = (List<Book>) cacheService.getObject(CacheKey.INDEX_CLICK_RANK_BOOK_KEY);

        if(books == null || books.size() == 0){
            //查询数据库，并缓存
            SelectStatementProvider select = select(id, picUrl, bookName, bookDesc)
                    .from(book)
                    .orderBy(visitCount.descending())
                    .limit(10L)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            books = bookMapper.selectMany(select);
        }

        return Result.ok(books);
    }
}
