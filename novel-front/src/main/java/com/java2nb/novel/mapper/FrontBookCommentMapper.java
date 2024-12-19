package com.java2nb.novel.mapper;

import com.java2nb.novel.vo.BookCommentVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Administrator
 */
public interface FrontBookCommentMapper extends BookCommentMapper {

    List<BookCommentVO> listCommentByPage(@Param("userId") Long userId, @Param("bookId") Long bookId);

    @Select("select book_comment.create_time, user.username, user.user_photo from book_comment " +
            "inner join user " +
            "on book_comment.create_user_id = user.id " +
            "and book_comment.book_id = #{bookId} " +
            "limit #{offset}, #{limit}")
    @Results(id = "UserResultMap", value = {
            @Result(column = "username", property = "createUserName"),
            @Result(column = "user_photo", property = "createUserPhoto"),
            @Result(column = "create_time", property = "createTime")
    })
    List<BookCommentVO> listCommentByPageOnlyUseBookId(@Param("bookId") Long bookId, Long offset, Long limit);

    @Select("select book_comment.comment_content, book_comment.create_time, user.username, user.user_photo from book_comment " +
            "inner join user " +
            "on book_comment.create_user_id = user.id " +
            "and book_comment.book_id = #{bookId} " +
            "limit #{offset}, #{limit}")
    @Results(id = "UserResultMap2", value = {
            @Result(column = "username", property = "createUserName"),
            @Result(column = "user_photo", property = "createUserPhoto"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "comment_content", property = "commentContent")
    })
    List<BookCommentVO> listCommentByPageOnlyUseBookIdWithContent(@Param("bookId") Long bookId, Long offset, Long limit);



    @Select("select count(id) from book_comment where book_id = #{bookId}")
    long countBookComment(Long bookId);
}
