package com.java2nb.novel.mapper;

import com.java2nb.novel.entity.Book;
import com.java2nb.novel.entity.BookIndex;
import com.java2nb.novel.vo.SearchDataVO;
import com.java2nb.novel.vo.BookVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author Administrator
 */
public interface FrontBookMapper extends BookMapper {

    List<BookVO> searchByPage(SearchDataVO params);

    void addVisitCount(@Param("bookId") Long bookId, @Param("visitCount") Integer visitCount);

    List<Book> listRecBookByCatId(@Param("catId") Integer catId);

    void addCommentCount(@Param("bookId") Long bookId);

    List<Book> queryNetworkPicBooks(@Param("localPicPrefix") String localPicPrefix, @Param("limit") Integer limit);

    /**
     * 按评分随机查询小说集合
     * @param limit 查询条数
     * @return 小说集合
     * */
    List<Book> selectIdsByScoreAndRandom(@Param("limit") int limit);

    int searchByPageTotal(SearchDataVO searchData);

    @Update("update book set comment_count = comment_count + #{increment} where id = #{bookId}")
    void updateCommentCount(int increment, long bookId);

    @Select("select count(book.id) from book where author_id = #{userId}")
    long countTotalAuthorBookNumber(Long userId);

    @Select("select book.id, book.pic_url, book.book_name, book.cat_name, book.visit_count, book.yesterday_buy, book.last_index_update_time, book.update_time, book.word_count " +
            "from book where author_id = #{authorId} " +
            "limit #{offset}, #{limit}")
    List<Book> listAuthorBook(long offset, long limit, Long authorId);

    @Select("select count(book_index.id) from book_index where book_index.book_id = #{bookId}")
    long queryChapterNumber(Long bookId);

    @Select("select book_index.id, book_index.index_name, book_index.update_time, book_index.is_vip " +
            "from book_index where book_id = #{bookId} " +
            "order by #{orderBy} " +
            "limit #{offset}, #{limit}")
    List<BookIndex> queryChapter(long offset, long limit, String orderBy, Long bookId);

}
