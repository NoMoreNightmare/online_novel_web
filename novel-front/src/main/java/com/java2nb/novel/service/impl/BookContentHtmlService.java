package com.java2nb.novel.service.impl;

import com.java2nb.novel.entity.News;
import com.java2nb.novel.service.MyBookService;
import com.java2nb.novel.service.MyNewsService;
import com.java2nb.novel.vo.BookSettingVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Thymeleaf生成静态页面并保存到本地
 */

/*
@Service
public class BookContentHtmlService {

        // themleaf 模板引擎
        @Autowired
        private TemplateEngine templateEngine;

        @Autowired
        private MyBookService myBookService;
        @Autowired
        private MyNewsService myNewsService;

        public void createHTML() {

            // 初始化运行上下文: org.thymeleaf.context 包下
            Context context = new Context();

            // 设置数据模板
            // 从goodsService.loadData(spuId) 方法中获取模板页面中需要渲染的数据

            Map<Byte, List<BookSettingVO>> map= myBookService.listBookSettingVO();
            //加载首页新闻线程
            context.setVariable("bookMap", map);

            //newList新闻
            List<News> list = myNewsService.queryTop3NewsInThisWeek();

            context.setVariable("newsList", list);

            // 获取输出流
            // 将需要输出的html 文件地址设置为服务器中的nginx 目录下的html 目录中的item文件夹下（我这里nginx 部署的是win10本地！）
            File file = new File("index/index.html");
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(file);
                // 模板引擎生成静态html 页面
                templateEngine.process("index", context, writer);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                // 判断并关闭流
                if (writer != null) {
                    writer.close();
                }
            }
        }
    }
*/

