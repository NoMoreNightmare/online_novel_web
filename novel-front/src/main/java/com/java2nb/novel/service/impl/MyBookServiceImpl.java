package com.java2nb.novel.service.impl;

import cn.hutool.json.JSONUtil;
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
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

        String bookJson = cacheService.get(CacheKey.INDEX_CLICK_RANK_BOOK_KEY);
        ObjectMapper objectMapper = new ObjectMapper();
        List<Book> books = null;


        if(bookJson == null || "".equals(bookJson)) {
            //查询数据库，并缓存
            SelectStatementProvider select = select(id, picUrl, bookName, bookDesc)
                    .from(book)
                    .orderBy(visitCount.descending())
                    .limit(10L)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            books = bookMapper.selectMany(select);
            String jsonStr;
            try {
                jsonStr = objectMapper.writeValueAsString(books);
            } catch (JsonProcessingException e) {
                return Result.customError("序列化错误", 2020);
            }
            cacheService.set(CacheKey.INDEX_CLICK_RANK_BOOK_KEY, jsonStr, 1800);
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
        String booksJson = cacheService.get(CacheKey.INDEX_NEW_BOOK_KEY);
        ObjectMapper objectMapper = new ObjectMapper();
        List<Book> books = null;


        if(booksJson == null || "".equals(booksJson)) {
            //查询数据库，并缓存
            SelectStatementProvider select = select(id, picUrl, bookName, bookDesc)
                    .from(book)
                    .where(createTime, isGreaterThanOrEqualTo(getTimeTwoMonthAgo()))
                    .orderBy(visitCount.descending())
                    .limit(10L)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            books = bookMapper.selectMany(select);
            try {
                booksJson = objectMapper.writeValueAsString(books);
            } catch (JsonProcessingException e) {
                return Result.customError("序列化错误", 2020);
            }
            cacheService.set(CacheKey.INDEX_NEW_BOOK_KEY, booksJson, 1800);
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
        String booksJson = cacheService.get(CacheKey.INDEX_NEW_BOOK_KEY);

        ObjectMapper objectMapper = new ObjectMapper();
        List<Book> books = null;


        if(booksJson == null || "".equals(booksJson)) {
            //查询数据库，并缓存
            SelectStatementProvider select = select(id, picUrl, bookName, bookDesc, catId, catName, lastIndexName, authorName, lastIndexUpdateTime)
                    .from(book)
                    .where(updateTime, isGreaterThanOrEqualTo(getTimeTwoMonthAgo()))
                    .orderBy(visitCount.descending())
                    .limit(10L)
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            books = bookMapper.selectMany(select);
            try {
                booksJson = objectMapper.writeValueAsString(books);
            } catch (JsonProcessingException e) {
                return Result.customError("序列化错误", 2020);
            }

            cacheService.set(CacheKey.INDEX_NEW_BOOK_KEY, booksJson, 1800);
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

    @Override
    public Result<?> addBookComment(Long bookId, String commentContent, Long userId) {

        int success = bookCommentMapper.addBookComment(bookId, commentContent, userId);
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
                BookIndexDynamicSqlSupport.indexNum, BookIndexDynamicSqlSupport.isVip, BookIndexDynamicSqlSupport.bookPrice)
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
        SelectStatementProvider select = select(BookContentDynamicSqlSupport.content)
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

    private Date getTimeTwoMonthAgo(){
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(2);
        return Date.from(oneMonthAgo.atZone(ZoneId.systemDefault()).toInstant());
    }
}
