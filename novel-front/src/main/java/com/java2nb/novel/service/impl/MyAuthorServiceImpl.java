package com.java2nb.novel.service.impl;

import cn.hutool.db.Page;
import com.java2nb.novel.controller.page.PageBean;
import com.java2nb.novel.core.result.Result;
import com.java2nb.novel.entity.AuthorIncome;
import com.java2nb.novel.entity.AuthorIncomeDetail;
import com.java2nb.novel.entity.Book;
import com.java2nb.novel.entity.BookIndex;
import com.java2nb.novel.mapper.*;
import com.java2nb.novel.service.MyAuthorService;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mybatis.dynamic.sql.SqlBuilder.*;

@Service
@Slf4j
public class MyAuthorServiceImpl implements MyAuthorService {
    @Autowired
    private FrontBookMapper frontBookMapper;
    @Autowired
    private AuthorMapper authorMapper;
    @Autowired
    private BookMapper bookMapper;
    @Autowired
    private BookIndexMapper bookIndexMapper;
    @Autowired
    private BookContentMapper bookContentMapper;

    @Override
    public Result<?> listAuthorBook(long curr, long limit, Long userId) {
        long authorId = authorMapper.queryAuthorId(userId);
        PageBean<Book> pageBean = new PageBean<>(curr, limit);
        long total = frontBookMapper.countTotalAuthorBookNumber(userId);
        List<Book> books = frontBookMapper.listAuthorBook((curr - 1) * limit, limit, authorId);

        pageBean.setTotal(total);
        pageBean.setList(books);
        return Result.ok(pageBean);
    }

    @Override
    public Result<?> listIncomeDailyByPage(long curr, long limit, long bookId, Long userId) {
        PageBean<AuthorIncomeDetail> pageBean = new PageBean<>(curr, limit);

        long total = authorMapper.queryIncomeDetailsNumber(bookId, userId);
        List<AuthorIncomeDetail> authorIncomeDetails = authorMapper.queryIncomeDetails((curr - 1) * limit, limit, bookId, userId);
        pageBean.setTotal(total);
        pageBean.setList(authorIncomeDetails);

        return Result.ok(pageBean);
    }

    @Override
    public Result<?> listIncomeMonthByPage(long curr, long limit, Long userId) {
        PageBean<AuthorIncome> pageBean = new PageBean<>(curr, limit);

        long total = authorMapper.queryIncomeNumber(userId);
        List<AuthorIncome> authorIncome = authorMapper.queryIncome((curr - 1) * limit, limit, userId);
        pageBean.setTotal(total);
        pageBean.setList(authorIncome);

        return Result.ok(pageBean);
    }

    @Override
    public Result<?> deleteIndex(long indexId, Long userId) {
        //查询它的bookid
        SelectStatementProvider select = select(BookIndexDynamicSqlSupport.bookId, BookIndexDynamicSqlSupport.indexNum)
                .from(BookIndexDynamicSqlSupport.bookIndex)
                .where(BookIndexDynamicSqlSupport.id, isEqualTo(indexId))
                .build()
                .render(RenderingStrategy.MYBATIS3);
        Optional<BookIndex> bookIndex = bookIndexMapper.selectOne(select);
        long bookId = bookIndex.get().getBookId();
        int indexNum = bookIndex.get().getIndexNum();
        //删除index
        bookIndexMapper.deleteIndex(indexId);

        //TODO 这里也可以多线程优化
        //删除对应的content
        DeleteStatementProvider delete = deleteFrom(BookContentDynamicSqlSupport.bookContent)
                .where(BookContentDynamicSqlSupport.indexId, isEqualTo(indexId))
                .build()
                .render(RenderingStrategy.MYBATIS3);
        bookContentMapper.delete(delete);

        //如果删除的是最新的index，更新book表里的内容
            if(indexNum > 0) {
                //查询上一个章节的最新的index信息
                SelectStatementProvider select1 = select(BookIndexDynamicSqlSupport.id, BookIndexDynamicSqlSupport.indexName, BookIndexDynamicSqlSupport.updateTime, BookIndexDynamicSqlSupport.isVip)
                        .from(BookIndexDynamicSqlSupport.bookIndex)
                        .where(BookIndexDynamicSqlSupport.id, isEqualTo(indexId))
                        .and(BookIndexDynamicSqlSupport.indexNum, isEqualTo(indexNum - 1))
                        .build()
                        .render(RenderingStrategy.MYBATIS3);

                Optional<Book> book = bookMapper.selectOne(select1);

                UpdateStatementProvider update = update(BookDynamicSqlSupport.book)
                        .set(BookDynamicSqlSupport.lastIndexId)
                        .equalTo(book.get().getLastIndexId())
                        .set(BookDynamicSqlSupport.lastIndexName)
                        .equalTo(book.get().getLastIndexName())
                        .set(BookDynamicSqlSupport.updateTime)
                        .equalTo(new Date())
                        .set(BookDynamicSqlSupport.isVip)
                        .equalTo(book.get().getIsVip())
                        .where(BookDynamicSqlSupport.id, isEqualTo(bookId))
                        .build()
                        .render(RenderingStrategy.MYBATIS3);

                bookMapper.update(update);
            }

            return Result.ok();
    }

}
