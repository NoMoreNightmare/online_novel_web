package com.java2nb.novel.mapper;

import com.java2nb.novel.vo.BookReadHistoryVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author Administrator
 */
public interface FrontUserReadHistoryMapper extends UserReadHistoryMapper {

    List<BookReadHistoryVO> listReadHistory(@Param("userId") Long userId);

    @Insert("insert into user_read_history (user_id, book_id, pre_content_id, create_time, update_time) values (#{userId}, #{bookId}, #{preContentId}, NOW(), NOW())")
    void insertNewHistory(@Param("userId") long userId,@Param("bookId") long bookId,@Param("preContentId") long preContentId);

    @Update("update user_read_history set update_time = NOW() and pre_content_id = #{preContentId} where user_id = #{userId} and book_id = #{bookId}")
    void updateBookHistory(@Param("userId") long userId,@Param("bookId") long bookId,@Param("preContentId") long preContentId);

}
