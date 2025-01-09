package com.java2nb.novel.service;

import com.java2nb.novel.controller.page.PageBean;
import com.java2nb.novel.core.result.Result;
import com.java2nb.novel.entity.Book;
import com.java2nb.novel.entity.BookContent;
import com.java2nb.novel.entity.BookIndex;
import com.java2nb.novel.vo.*;

import java.util.List;
import java.util.Map;

public interface MyBookService {
    Result<?> listClickRank();

    Result<?> listNewRank();

    Result<?> listUpdateRank();

    Book queryBook(long id);

    PageBean<BookCommentVO> queryBookComment(long id, long page, long pageSize);

    List<Book> queryRecommendedBooks(long id);

    Long queryBookLastChapter(long id);

    Result<?> queryBookIndexAbout(Long bookId, Long lastBookIndexId);

    long queryBookChapterNumber(Long bookId);

    Result<?> addVisitCount(Long bookId);

    Result<?> addBookComment(Long bookId, String commentContent, Long userId);

    Result<?> listCommentByPage(Long bookId, Long curr, Long limit);

    BookIndex queryAboutCurrentIndex(long bookId, long bookIndexId);

    Long queryBookIndexIdByIndexNum(Long bookId, int IndexNum);

    BookContent queryBookContent(long bookId, long bookIndexId);

    List<BookIndex> queryAllIndex(long bookId);

    List<BookVO> queryWithCondition(SearchDataVO searchData);

    Result<?> queryAllCategory();

    int queryWithConditionTotal(SearchDataVO searchData);

    Result<?> listRank(int rankType, int limit);

    Result<?> listAuthorBookChapter(long curr, long limit, Long bookId, String orderBy, Long userId);

    Map<Byte, List<BookSettingVO>> listBookSettingVO();

    Result<?> listCommentRank(String key, int limit);

    void queryUsingElasticSearch(PageBean<BookDoc> pageBean, SearchDataVO searchData);

//    void recover();
}
