package com.java2nb.novel.service.impl;

import com.java2nb.novel.entity.News;
import com.java2nb.novel.mapper.AuthorDynamicSqlSupport;
import com.java2nb.novel.mapper.NewsDynamicSqlSupport;
import com.java2nb.novel.mapper.NewsMapper;
import com.java2nb.novel.service.MyNewsService;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.SelectDSL;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.mybatis.dynamic.sql.SqlBuilder.*;
//import static org.mybatis.dynamic.sql.select.SelectDSL.select;

@Service
class MyNewsServiceIml implements MyNewsService{
    @Autowired
    NewsMapper newsMapper;

    @Override
    public List<News> queryTop3NewsInThisWeek() {
        Date today = new Date();

        // 使用 Calendar 计算7天前的日期
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DAY_OF_MONTH, -3000);
        Date sevenDaysAgo = calendar.getTime();


        SelectStatementProvider selectNews = select(NewsDynamicSqlSupport.catName, NewsDynamicSqlSupport.id, NewsDynamicSqlSupport.title)
                .from(NewsDynamicSqlSupport.news)
                .where(AuthorDynamicSqlSupport.createTime, isGreaterThan(sevenDaysAgo))
                .orderBy(NewsDynamicSqlSupport.readCount)
                .limit(3)
                .build()
                .render(RenderingStrategy.MYBATIS3);

        return newsMapper.selectMany(selectNews);

    }
}
