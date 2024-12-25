package com.java2nb.novel.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java2nb.novel.controller.page.PageBean;
import com.java2nb.novel.core.cache.CacheKey;
import com.java2nb.novel.core.cache.CacheService;
import com.java2nb.novel.core.result.BookConstant;
import com.java2nb.novel.core.result.Result;
import com.java2nb.novel.entity.Book;
import com.java2nb.novel.entity.BookCategory;
import com.java2nb.novel.entity.BookContent;
import com.java2nb.novel.entity.BookIndex;
import com.java2nb.novel.mapper.*;
import com.java2nb.novel.service.BookContentService;
import com.java2nb.novel.service.MyBookService;
import com.java2nb.novel.vo.BookCommentVO;
import com.java2nb.novel.vo.BookVO;
import com.java2nb.novel.vo.SearchDataVO;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static com.java2nb.novel.mapper.BookDynamicSqlSupport.*;

import static com.java2nb.novel.mapper.BookDynamicSqlSupport.id;
import static org.mybatis.dynamic.sql.SqlBuilder.*;
import static org.mybatis.dynamic.sql.select.SelectDSL.select;


@Service
public class MyBookServiceImpl implements MyBookService {
    @Autowired
    FrontBookMapper bookMapper;

    @Resource
    CacheService cacheService;


    @Autowired
    private FrontBookCommentMapper bookCommentMapper;
    @Autowired
    private BookIndexMapper bookIndexMapper;
    @Autowired
    private MyBookService myBookService;
    @Resource(name = "db")
    private BookContentService bookContentService;
    @Autowired
    private BookContentMapper bookContentMapper;
    @Autowired
    private BookCategoryMapper bookCategoryMapper;

    @Override
    public Result<?> listClickRank() {
        return listClickRank(CacheKey.INDEX_CLICK_RANK_BOOK_KEY, 10);
    }

    private Result<?> listClickRank(String key, int limit){
        String bookJson = cacheService.get(key);
        ObjectMapper objectMapper = new ObjectMapper();
        List<Book> books = null;


        if(bookJson == null || bookJson.isEmpty()) {
            //查询数据库，并缓存
            SelectStatementProvider select = select(id, picUrl, bookName, bookDesc, catName, lastIndexName, authorName, wordCount)
                    .from(book)
                    .orderBy(visitCount.descending())
                    .limit(limit)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            books = bookMapper.selectMany(select);
            String jsonStr;
            try {
                jsonStr = objectMapper.writeValueAsString(books);
            } catch (JsonProcessingException e) {
                return Result.customError("序列化错误", 2020);
            }
            cacheService.set(key, jsonStr, 1800);
        }else{
            try {
                books = objectMapper.readValue(bookJson, new TypeReference<List<Book>>() {});
            } catch (JsonProcessingException e) {
                return Result.customError("反序列化错误", 2021);
            }
        }



        return Result.ok(books);
    }

    @Override
    public Result<?> listNewRank() {
        return listNewRank(CacheKey.INDEX_NEW_BOOK_KEY, 10);
    }

    private Result<?> listNewRank(String key, int limit){
        String booksJson = cacheService.get(key);
        ObjectMapper objectMapper = new ObjectMapper();
        List<Book> books = null;


        if(booksJson == null || "".equals(booksJson)) {
            //查询数据库，并缓存
            SelectStatementProvider select = select(id, picUrl, bookName, bookDesc, catName, lastIndexName, authorName, wordCount)
                    .from(book)
                    .where(createTime, isGreaterThanOrEqualTo(getTimeTwoMonthAgo()))
                    .orderBy(visitCount.descending())
                    .limit(limit)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            books = bookMapper.selectMany(select);
            try {
                booksJson = objectMapper.writeValueAsString(books);
            } catch (JsonProcessingException e) {
                return Result.customError("序列化错误", 2020);
            }
            cacheService.set(key, booksJson, 1800);
        }else{
            try {
                books = objectMapper.readValue(booksJson, new TypeReference<List<Book>>() {
                });
            } catch (JsonProcessingException e) {
                return Result.customError("反序列化错误", 2021);
            }
        }



        return Result.ok(books);
    }

    @Override
    public Result<?> listUpdateRank() {
        return listUpdateRank(CacheKey.INDEX_NEW_BOOK_KEY, 10);
    }

    private Result<?> listUpdateRank(String key, int limit){
        String booksJson = cacheService.get(key);

        ObjectMapper objectMapper = new ObjectMapper();
        List<Book> books = null;


        if(booksJson == null || "".equals(booksJson)) {
            //查询数据库，并缓存
            SelectStatementProvider select = select(id, picUrl, bookName, bookDesc, catId, catName, lastIndexName, authorName, lastIndexUpdateTime, wordCount)
                    .from(book)
                    .where(updateTime, isGreaterThanOrEqualTo(getTimeTwoMonthAgo()))
                    .orderBy(visitCount.descending())
                    .limit(limit)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            books = bookMapper.selectMany(select);
            try {
                booksJson = objectMapper.writeValueAsString(books);
            } catch (JsonProcessingException e) {
                return Result.customError("序列化错误", 2020);
            }

            cacheService.set(key, booksJson, 1800);
        }else{
            try {
                books = objectMapper.readValue(booksJson, new TypeReference<List<Book>>() {
                });
            } catch (JsonProcessingException e) {
                return Result.customError("反序列化错误", 2021);
            }

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
    public PageBean<BookCommentVO> queryBookComment(long bookId, long page, long pageSize) {
        SelectStatementProvider select = select(BookCommentDynamicSqlSupport.commentContent, BookCommentDynamicSqlSupport.createUserId)
                .from(BookCommentDynamicSqlSupport.bookComment)
                .where(BookCommentDynamicSqlSupport.id, isEqualTo(bookId))
                .limit(pageSize)
                .offset((long) (page - 1) * pageSize)
                .build()
                .render(RenderingStrategy.MYBATIS3);

        List<BookCommentVO> bookComments = bookCommentMapper.listCommentByPageOnlyUseBookIdWithContent(bookId, (page - 1) * pageSize, pageSize);

        SelectStatementProvider countSelect = select(count(BookCommentDynamicSqlSupport.id))
                .from(BookCommentDynamicSqlSupport.bookComment)
                .where(BookCommentDynamicSqlSupport.bookId, isEqualTo(bookId))
                .build()
                .render(RenderingStrategy.MYBATIS3);
        long total = bookMapper.count(countSelect);

        return new PageBean<>(page, pageSize, total, true, bookComments);
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

    @Override
    public Result<?> queryBookIndexAbout(Long bookId, Long lastBookIndexId) {
        //查询章节数量
        long numberOfChapter = myBookService.queryBookChapterNumber(bookId);

        //查询最后一章的内容
        BookContent lastChapterContent = bookContentService.queryBookContent(bookId, lastBookIndexId);

        Map<String, Object> map = new HashMap<>();
        map.put("bookIndexCount", numberOfChapter);
        String content = "";
        if(lastChapterContent.getContent().length() > 50){
            content = lastChapterContent.getContent().substring(0, 50);
        }else{
            content = lastChapterContent.getContent();
        }
        map.put("lastBookContent", content);
        return Result.ok(map);
    }

    @Override
    public long queryBookChapterNumber(Long bookId) {
        SelectStatementProvider select = select(count(BookIndexDynamicSqlSupport.id))
                .from(BookIndexDynamicSqlSupport.bookIndex)
                .where(BookIndexDynamicSqlSupport.bookId, isEqualTo(bookId))
                .build()
                .render(RenderingStrategy.MYBATIS3);

        return bookIndexMapper.count(select);
    }

    @Override
    public Result<?> addVisitCount(Long bookId) {
        boolean res = bookMapper.addVisitCountByOne(bookId);
        if(res){
            return Result.ok();
        }else{
            return Result.error();
        }
    }

    @Transactional
    @Override
    public Result<?> addBookComment(Long bookId, String commentContent, Long userId) {

        int success = bookCommentMapper.addBookComment(bookId, commentContent, userId);
        bookMapper.updateCommentCount(1, bookId);
        if(success == 1){
            return Result.ok();
        }else{
            return Result.customError(BookConstant.ADD_COMMENT_TO_BOOK_MSG, BookConstant.ADD_COMMENT_TO_BOOK);
        }
    }

    @Override
    public Result<?> listCommentByPage(Long bookId, Long curr, Long limit) {
        PageBean<BookCommentVO> pageBean = new PageBean<>(curr, limit);

        List<BookCommentVO> bookCommentVOS = bookCommentMapper.listCommentByPageOnlyUseBookIdWithContent(bookId, (curr - 1) * limit, limit);

        long total = bookCommentMapper.countBookComment(bookId);

        pageBean.setList(bookCommentVOS);
        pageBean.setTotal(total);

        return Result.ok(pageBean);
    }

    @Override
    public BookIndex queryAboutCurrentIndex(long bookId, long bookIndexId) {
        SelectStatementProvider select = select(BookIndexDynamicSqlSupport.id, BookIndexDynamicSqlSupport.indexName,
                BookIndexDynamicSqlSupport.indexNum, BookIndexDynamicSqlSupport.isVip, BookIndexDynamicSqlSupport.bookPrice, BookIndexDynamicSqlSupport.updateTime, BookIndexDynamicSqlSupport.wordCount)
                .from(BookIndexDynamicSqlSupport.bookIndex)
                .where(BookIndexDynamicSqlSupport.bookId, isEqualTo(bookId))
                .and(BookIndexDynamicSqlSupport.id, isEqualTo(bookIndexId))
                .build()
                .render(RenderingStrategy.MYBATIS3);
        Optional<BookIndex> bookIndex = bookIndexMapper.selectOne(select);
        if(bookIndex.isPresent()){
            return bookIndex.get();
        }
        return null;
    }

    @Override
    public Long queryBookIndexIdByIndexNum(Long bookId, int IndexNum) {
        SelectStatementProvider select = select(BookIndexDynamicSqlSupport.id)
                .from(BookIndexDynamicSqlSupport.bookIndex)
                .where(BookIndexDynamicSqlSupport.bookId, isEqualTo(bookId))
                .and(BookIndexDynamicSqlSupport.indexNum, isEqualTo(IndexNum))
                .build()
                .render(RenderingStrategy.MYBATIS3);
        Optional<BookIndex> bookIndex = bookIndexMapper.selectOne(select);
        if(bookIndex.isPresent()){
            return bookIndex.get().getId();
        }
        return null;
    }

    @Override
    public BookContent queryBookContent(long bookId, long bookIndexId) {
        //TODO 应该根据bookId和bookIndexId来查询的，但这里只用了bookIndexId，看看能不能改数据库表的DDL
        SelectStatementProvider select = select(BookContentDynamicSqlSupport.content, BookContentDynamicSqlSupport.id)
                .from(BookContentDynamicSqlSupport.bookContent)
                .where(BookContentDynamicSqlSupport.indexId, isEqualTo(bookIndexId))
                .build()
                .render(RenderingStrategy.MYBATIS3);

        Optional<BookContent> bookContent = bookContentMapper.selectOne(select);
        return bookContent.orElse(null);

    }

    @Override
    public List<BookIndex> queryAllIndex(long bookId) {
        SelectStatementProvider select = select(BookIndexDynamicSqlSupport.id, BookIndexDynamicSqlSupport.indexName, BookIndexDynamicSqlSupport.isVip)
                .from(BookIndexDynamicSqlSupport.bookIndex)
                .where(BookIndexDynamicSqlSupport.bookId, isEqualTo(bookId))
                .build()
                .render(RenderingStrategy.MYBATIS3);

        return bookIndexMapper.selectMany(select);
    }

    @Override
    public List<BookVO> queryWithCondition(SearchDataVO searchData) {
        searchData.calculateOffset();
        return bookMapper.searchByPage(searchData);
    }

    @Override
    public Result<?> queryAllCategory() {
        SelectStatementProvider select = select(BookCategoryDynamicSqlSupport.id, BookCategoryDynamicSqlSupport.name, BookCategoryDynamicSqlSupport.workDirection)
                .from(BookCategoryDynamicSqlSupport.bookCategory)
                .build()
                .render(RenderingStrategy.MYBATIS3);
        List<BookCategory> bookCategories = bookCategoryMapper.selectMany(select);
        return Result.ok(bookCategories);
    }

    @Override
    public int queryWithConditionTotal(SearchDataVO searchData) {
        return bookMapper.searchByPageTotal(searchData);
    }

    @Override
    public Result<?> listRank(int rankType, int limit) {
        switch (rankType){
            case 0:
                return listClickRank(CacheKey.RANKING_LIST_CLICK, limit);
            case 1:
                return listNewRank(CacheKey.RANKING_LIST_NEW, limit);
            case 2:
                return listUpdateRank(CacheKey.RANKING_LIST_UPDATE, limit);
            case 3:
                return listCommentRank(CacheKey.RANKING_LIST_COMMENT, limit);
        }
        return Result.error();
    }

    @Override
    public Result<?> listAuthorBookChapter(long curr, long limit, Long bookId, String orderBy, Long userId) {
        PageBean<BookIndex> pageBean = new PageBean<>(curr, limit);

        long total = bookMapper.queryChapterNumber(bookId);
        List<BookIndex> list = bookMapper.queryChapter((curr - 1) * limit, limit, orderBy, bookId);
        pageBean.setList(list);
        pageBean.setTotal(total);

        return Result.ok(pageBean);
    }

    private Result<?> listCommentRank(String key, int limit) {
        String bookJson = cacheService.get(key);
        ObjectMapper objectMapper = new ObjectMapper();
        List<Book> books = null;


        if(bookJson == null || bookJson.isEmpty()) {
            //查询数据库，并缓存
            SelectStatementProvider select = select(id, picUrl, bookName, bookDesc, catName, lastIndexName, authorName, wordCount)
                    .from(book)
                    .orderBy(commentCount.descending())
                    .limit(limit)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            books = bookMapper.selectMany(select);
            String jsonStr;
            try {
                jsonStr = objectMapper.writeValueAsString(books);
            } catch (JsonProcessingException e) {
                return Result.customError("序列化错误", 2020);
            }
            cacheService.set(key, jsonStr, 1800);
        }else{
            try {
                books = objectMapper.readValue(bookJson, new TypeReference<List<Book>>() {});
            } catch (JsonProcessingException e) {
                return Result.customError("反序列化错误", 2021);
            }
        }



        return Result.ok(books);
    }

    private Date getTimeTwoMonthAgo(){
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(2);
        return Date.from(oneMonthAgo.atZone(ZoneId.systemDefault()).toInstant());
    }

    @Override
    public void recover(){
        SelectStatementProvider bookSelect = SqlBuilder.select(BookDynamicSqlSupport.id)
                .from(BookDynamicSqlSupport.book)
                .build()
                .render(RenderingStrategy.MYBATIS3);

        List<Book> books = bookMapper.selectMany(bookSelect);
        for (Book book : books) {
            SelectStatementProvider select = select(BookIndexDynamicSqlSupport.id, BookIndexDynamicSqlSupport.indexName, BookIndexDynamicSqlSupport.updateTime)
                    .from(BookIndexDynamicSqlSupport.bookIndex)
                    .where(BookIndexDynamicSqlSupport.bookId, isEqualTo(book.getId()))
                    .orderBy(BookIndexDynamicSqlSupport.indexNum.descending())
                    .limit(1)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);
            Optional<BookIndex> bookIndex = bookIndexMapper.selectOne(select);
            if(bookIndex.isPresent()){
                UpdateStatementProvider render = update(BookDynamicSqlSupport.book)
                        .set(BookDynamicSqlSupport.lastIndexId)
                        .equalTo(bookIndex.get().getId())
                        .set(BookDynamicSqlSupport.lastIndexName)
                        .equalTo(bookIndex.get().getIndexName())
                        .set(BookDynamicSqlSupport.lastIndexUpdateTime)
                        .equalTo(bookIndex.get().getUpdateTime())
                        .where(BookDynamicSqlSupport.id, isEqualTo(book.getId()))
                        .build()
                        .render(RenderingStrategy.MYBATIS3);
                bookMapper.update(render);
            }

        }
    }
}
