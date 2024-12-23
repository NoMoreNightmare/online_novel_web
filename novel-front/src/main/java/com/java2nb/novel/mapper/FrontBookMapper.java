package com.java2nb.novel.mapper;

import com.java2nb.novel.entity.Book;
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
            "from book where author_id = #{userId} " +
            "limit #{offset}, #{limit}")
    List<Book> listAuthorBook(long offset, long limit, Long userId);
}
