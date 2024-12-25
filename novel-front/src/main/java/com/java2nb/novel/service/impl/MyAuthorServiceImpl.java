package com.java2nb.novel.service.impl;

import com.java2nb.novel.controller.page.PageBean;
import com.java2nb.novel.core.result.BookConstant;
import com.java2nb.novel.core.result.Result;
import com.java2nb.novel.entity.*;
import com.java2nb.novel.entity.Book;
import com.java2nb.novel.mapper.*;
import com.java2nb.novel.service.MyAuthorService;
import com.java2nb.novel.vo.BookContentVO;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.java2nb.novel.mapper.BookDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.*;
import static org.mybatis.dynamic.sql.select.SelectDSL.select;

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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<?> deleteIndex(long indexId, Long userId) {
        //查询它的bookid
        SelectStatementProvider select = select(BookIndexDynamicSqlSupport.bookId, BookIndexDynamicSqlSupport.indexNum, BookIndexDynamicSqlSupport.wordCount)
                .from(BookIndexDynamicSqlSupport.bookIndex)
                .where(BookIndexDynamicSqlSupport.id, isEqualTo(indexId))
                .build()
                .render(RenderingStrategy.MYBATIS3);
        Optional<BookIndex> bookIndex = bookIndexMapper.selectOne(select);
        long bookId = bookIndex.get().getBookId();
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

        //查询上一个章节的最新的index信息
        SelectStatementProvider select1 = select(BookIndexDynamicSqlSupport.id, BookIndexDynamicSqlSupport.indexName, BookIndexDynamicSqlSupport.updateTime, BookIndexDynamicSqlSupport.isVip, BookIndexDynamicSqlSupport.wordCount)
                .from(BookIndexDynamicSqlSupport.bookIndex)
                .where(BookIndexDynamicSqlSupport.bookId, isEqualTo(bookId))
                .orderBy(BookIndexDynamicSqlSupport.indexNum.descending())
                .limit(1)
                .build()
                .render(RenderingStrategy.MYBATIS3);

        Optional<Book> currLastIndex = bookMapper.selectOne(select1);
        Optional<Book> originBook = bookMapper.selectOne(
                select(wordCount, authorId)
                .from(BookDynamicSqlSupport.book)
                .where(id, isEqualTo(bookId))
                .build()
                .render(RenderingStrategies.MYBATIS3));

        if(currLastIndex.isPresent()) {
                    //最新的index id

            UpdateStatementProvider update = update(BookDynamicSqlSupport.book)
                    .set(BookDynamicSqlSupport.lastIndexId)
                    .equalTo(currLastIndex.get().getLastIndexId())
                    .set(BookDynamicSqlSupport.lastIndexName)
                    .equalTo(currLastIndex.get().getLastIndexName())
                    .set(BookDynamicSqlSupport.updateTime)
                    .equalTo(new Date())
                    .set(BookDynamicSqlSupport.isVip)
                    .equalTo(currLastIndex.get().getIsVip())
                    .set(BookDynamicSqlSupport.wordCount)
                    .equalTo(originBook.get().getWordCount() - currLastIndex.get().getWordCount())
                    .where(BookDynamicSqlSupport.id, isEqualTo(bookId))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            bookMapper.update(update);
        }



        return Result.ok();
    }

    @Override
    public Result<?> queryIndexContent(long indexId) {
        SelectStatementProvider select = SqlBuilder.select(BookContentDynamicSqlSupport.content)
                .from(BookContentDynamicSqlSupport.bookContent)
                .where(BookContentDynamicSqlSupport.indexId, isEqualTo(indexId))
                .build()
                .render(RenderingStrategy.MYBATIS3);
        Optional<BookContent> bookContent = bookContentMapper.selectOne(select);
        if(bookContent.isPresent()) {
            return Result.ok(bookContent.get().getContent());
        }else{
            return Result.customError(BookConstant.CONTENT_NOT_EXIST_MSG, BookConstant.CONTENT_NOT_EXIST);
        }
    }

    @Transactional
    @Override
    public Result<?> updateBookContent(BookContentVO bookContent) {
        //修改里面的wordcount
        int newWordCount = bookContent.getContent().length();
        SelectStatementProvider selectWordCount = select(BookIndexDynamicSqlSupport.wordCount, BookIndexDynamicSqlSupport.bookId)
                .from(BookIndexDynamicSqlSupport.bookIndex)
                .where(BookIndexDynamicSqlSupport.id, isEqualTo(bookContent.getIndexId()))
                .build()
                .render(RenderingStrategy.MYBATIS3);

        BookIndex bookIndex = bookIndexMapper.selectOne(selectWordCount).get();
        int preWordCount = bookIndex.getWordCount();
        long bookId = bookContent.getId();
        int originalTotalWordCount = bookMapper.selectOne(select(wordCount)
                .from(book)
                .where(id, isEqualTo(bookId))
                .build()
                .render(RenderingStrategy.MYBATIS3)).get().getWordCount();

        int finalTotalWordCount = originalTotalWordCount - preWordCount + newWordCount;
        UpdateStatementProvider updateBookWordCount = update(book)
                .set(wordCount)
                .equalTo(finalTotalWordCount)
                .where(id, isEqualTo(bookId))
                .build()
                .render(RenderingStrategy.MYBATIS3);
        bookMapper.update(updateBookWordCount);


        //修改bookcontent
        UpdateStatementProvider updateBookContent = update(BookContentDynamicSqlSupport.bookContent)
                .set(BookContentDynamicSqlSupport.content)
                .equalTo(bookContent.getContent())
                .where(BookContentDynamicSqlSupport.indexId, isEqualTo(bookContent.getIndexId()))
                .build()
                .render(RenderingStrategy.MYBATIS3);

        bookContentMapper.update(updateBookContent);

        //修改bookindex里的index name和word count
        UpdateStatementProvider updateBookIndex = update(BookIndexDynamicSqlSupport.bookIndex)
                .set(BookIndexDynamicSqlSupport.indexName)
                .equalTo(bookContent.getIndexName())
                .set(BookIndexDynamicSqlSupport.wordCount)
                .equalTo(bookContent.getContent().length())
                .set(BookIndexDynamicSqlSupport.updateTime)
                .equalTo(new Date())
                .where(BookIndexDynamicSqlSupport.id, isEqualTo(bookContent.getIndexId()))
                .build()
                .render(RenderingStrategy.MYBATIS3);

        bookIndexMapper.update(updateBookIndex);

        //如果indexId是book表中最新的index，那么修改里面的indexname
        UpdateStatementProvider updateBookIndexName = update(book)
                .set(lastIndexName)
                .equalTo(bookContent.getIndexName())
                .set(lastIndexUpdateTime)
                .equalTo(new Date())
                .where(lastIndexId, isEqualTo(bookContent.getIndexId()))
                .build()
                .render(RenderingStrategy.MYBATIS3);
        bookMapper.update(updateBookIndexName);

        return Result.ok();

    }

    @Override
    public Result<?> addBook(Book book, Long userId) {
        book.setUpdateTime(new Date());
        book.setCreateTime(new Date());

        //查询用户的author id

        //查询用户的author name
        SelectStatementProvider select = select(AuthorDynamicSqlSupport.id, AuthorDynamicSqlSupport.penName)
                .from(AuthorDynamicSqlSupport.author)
                .where(AuthorDynamicSqlSupport.userId, isEqualTo(userId))
                .build()
                .render(RenderingStrategy.MYBATIS3);
        Optional<Author> author = authorMapper.selectOne(select);
        if(author.isPresent()) {
            book.setScore(0f);
            book.setBookStatus((byte)0);
            book.setVisitCount(0L);
            book.setWordCount(0);

            book.setAuthorId(author.get().getId());
            book.setAuthorName(author.get().getPenName());
            bookMapper.insert(book);
        }else{
            return Result.customError(BookConstant.ADD_BOOK_FAIL_MSG, BookConstant.ADD_BOOK_FAIL);
        }

        return Result.ok();

    }

    @Override
    public Result<?> updateBookPic(Long bookId, String bookPic) {
        UpdateStatementProvider updateBookPic = update(book)
                .set(picUrl)
                .equalTo(bookPic)
                .where(id, isEqualTo(bookId))
                .build()
                .render(RenderingStrategy.MYBATIS3);

        bookMapper.update(updateBookPic);
        return Result.ok();
    }


}
