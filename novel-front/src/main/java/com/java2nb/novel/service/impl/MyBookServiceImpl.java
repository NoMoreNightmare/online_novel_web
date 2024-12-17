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
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.java2nb.novel.mapper.BookDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.isGreaterThan;
import static org.mybatis.dynamic.sql.SqlBuilder.isGreaterThanOrEqualTo;
import static org.mybatis.dynamic.sql.select.SelectDSL.select;

@Service
public class MyBookServiceImpl implements MyBookService {
    @Resource
    BookMapper bookMapper;

    @Resource
    CacheService cacheService;

    @Override
    public Result<?> listClickRank() {
        List<Book> books = (List<Book>) cacheService.getObject(CacheKey.INDEX_CLICK_RANK_BOOK_KEY);

        if(books == null || books.isEmpty()){
            //查询数据库，并缓存
            SelectStatementProvider select = select(id, picUrl, bookName, bookDesc)
                    .from(book)
                    .orderBy(visitCount.descending())
                    .limit(10L)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            books = bookMapper.selectMany(select);
            cacheService.setObject(CacheKey.INDEX_CLICK_RANK_BOOK_KEY, books, 1800);
        }

        return Result.ok(books);
    }

    @Override
    public Result<?> listNewRank() {
        List<Book> books = (List<Book>) cacheService.getObject(CacheKey.INDEX_NEW_BOOK_KEY);

        if(books == null || books.isEmpty()){
            //查询数据库，并缓存
            SelectStatementProvider select = select(id, picUrl, bookName, bookDesc)
                    .from(book)
                    .where(createTime, isGreaterThanOrEqualTo(getTimeOneMonthAgo()))
                    .orderBy(visitCount.descending())
                    .limit(10L)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            books = bookMapper.selectMany(select);
            cacheService.setObject(CacheKey.INDEX_NEW_BOOK_KEY, books, 1800);
        }

        return Result.ok(books);
    }

    @Override
    public Result<?> listUpdateRank() {
        List<Book> books = (List<Book>) cacheService.getObject(CacheKey.INDEX_NEW_BOOK_KEY);

        if(books == null || books.isEmpty()){
            //查询数据库，并缓存
            SelectStatementProvider select = select(id, picUrl, bookName, bookDesc, catId, catName, lastIndexName, authorName)
                    .from(book)
                    .where(updateTime, isGreaterThanOrEqualTo(getTimeOneMonthAgo()))
                    .orderBy(visitCount.descending())
                    .limit(10L)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            books = bookMapper.selectMany(select);
            cacheService.setObject(CacheKey.INDEX_NEW_BOOK_KEY, books, 1800);
        }

        return Result.ok(books);
    }

    private Date getTimeOneMonthAgo(){
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1).minusDays(4);
        return Date.from(oneMonthAgo.atZone(ZoneId.systemDefault()).toInstant());
    }
}
