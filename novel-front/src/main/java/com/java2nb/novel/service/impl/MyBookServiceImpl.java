package com.java2nb.novel.service.impl;

import com.java2nb.novel.controller.page.PageBean;
import com.java2nb.novel.core.cache.CacheKey;
import com.java2nb.novel.core.cache.CacheService;
import com.java2nb.novel.core.result.Result;
import com.java2nb.novel.entity.Book;
import com.java2nb.novel.entity.BookComment;
import com.java2nb.novel.entity.BookIndex;
import com.java2nb.novel.mapper.*;
import com.java2nb.novel.service.MyBookService;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static com.java2nb.novel.mapper.BookDynamicSqlSupport.*;

import static com.java2nb.novel.mapper.BookDynamicSqlSupport.id;
import static org.mybatis.dynamic.sql.SqlBuilder.*;
import static org.mybatis.dynamic.sql.select.SelectDSL.select;


@Service
public class MyBookServiceImpl implements MyBookService {
    @Resource
    BookMapper bookMapper;

    @Resource
    CacheService cacheService;
    @Autowired
    private BookCommentMapper bookCommentMapper;
    @Autowired
    private BookIndexMapper bookIndexMapper;

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

    @Override
    public Book queryBook(long bookId) {
        SelectStatementProvider select = select(id, bookName, catId, catName, picUrl, authorName, bookStatus, visitCount, wordCount, bookDesc, lastIndexId, lastIndexUpdateTime, lastIndexName)
                .from(book)
                .where(id, isEqualTo(bookId))
                .build()
                .render(RenderingStrategy.MYBATIS3);

        Optional<Book> book = bookMapper.selectOne(select);
        return book.get();
    }

    @Override
    public PageBean<BookComment> queryBookComment(long bookId, long page, long pageSize) {
        SelectStatementProvider select = select(BookCommentDynamicSqlSupport.commentContent, BookCommentDynamicSqlSupport.createUserId)
                .from(BookCommentDynamicSqlSupport.bookComment)
                .where(BookCommentDynamicSqlSupport.id, isEqualTo(bookId))
                .limit(pageSize)
                .offset((long) (page - 1) * pageSize)
                .build()
                .render(RenderingStrategy.MYBATIS3);

        SelectStatementProvider countSelect = select(count(BookCommentDynamicSqlSupport.id))
                .from(BookCommentDynamicSqlSupport.bookComment)
                .where(BookCommentDynamicSqlSupport.bookId, isEqualTo(bookId))
                .build()
                .render(RenderingStrategy.MYBATIS3);
        long total = bookMapper.count(countSelect);

        List<BookComment> bookComments = bookCommentMapper.selectMany(select);
        PageBean<BookComment> pageBean = new PageBean<>(page, pageSize, total, true, bookComments);

        return pageBean;
    }

    @Override
    public List<Book> queryRecommendedBooks(long bookId) {
        SelectStatementProvider select = select(id, picUrl, bookDesc, bookName)
                .from(book)
                .limit(4L)
                .build()
                .render(RenderingStrategy.MYBATIS3);

        List<Book> books = bookMapper.selectMany(select);
        return books;
    }

    @Override
    public Long queryBookFirstChapter(long id) {
        SelectStatementProvider select = select(BookIndexDynamicSqlSupport.id)
                .from(BookIndexDynamicSqlSupport.bookIndex)
                .where(BookIndexDynamicSqlSupport.bookId, isEqualTo(id))
                .orderBy(BookIndexDynamicSqlSupport.createTime.descending())
                .limit(1)
                .build()
                .render(RenderingStrategy.MYBATIS3);

        Optional<BookIndex> bookIndex = bookIndexMapper.selectOne(select);
        if(bookIndex.isPresent()){
            return bookIndex.get().getId();
        }else{
            return null;
        }

    }

    private Date getTimeOneMonthAgo(){
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1).minusDays(4);
        return Date.from(oneMonthAgo.atZone(ZoneId.systemDefault()).toInstant());
    }
}
