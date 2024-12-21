package com.java2nb.novel.mapper;

import com.java2nb.novel.vo.BookShelfVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author Administrator
 */
public interface FrontUserMapper extends UserMapper {



    void addUserBalance(@Param("userId") Long userId, @Param("amount") Integer amount);


    @Select("select book.cat_id, book.cat_name,book.book_name, book.last_index_id, book.last_index_name, book.last_index_update_time, user_bookshelf.book_id, user_bookshelf.pre_content_id " +
            "from book inner join user_bookshelf " +
            "on book.id = user_bookshelf.book_id " +
            "where user_id = #{userId} " +
            "limit #{offset}, #{limit}")
    List<BookShelfVO> selectBookShelfList(Long userId, int offset, Long limit);

    @Update("update user set user_sex = #{userSex} where id = #{userId}")
    void updateUserSex(Long userId, byte userSex);

    @Update("update user set password = #{newPassword} where id = #{userId}")
    int updatePassword(Long userId, String newPassword);

    @Update("update user set nick_name = #{nickName} where id = #{userId}")
    int updateNickName(Long userId, String nickName);

    @Update("update user set user_photo = #{userPhoto} where id = #{userId}")
    void updateUserPhoto(Long userId, String userPhoto);

    @Select("select user.user_photo from user where id = #{userId}")
    String queryUserPhoto(Long userId);
}
