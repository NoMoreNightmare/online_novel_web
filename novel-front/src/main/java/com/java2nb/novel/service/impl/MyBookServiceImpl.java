package com.java2nb.novel.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java2nb.novel.controller.page.PageBean;
import com.java2nb.novel.core.cache.CacheKey;
import com.java2nb.novel.core.cache.CacheService;
import com.java2nb.novel.core.result.BookConstant;
import com.java2nb.novel.core.result.RabbitMQConstant;
import com.java2nb.novel.core.result.RedisConstant;
import com.java2nb.novel.core.result.Result;
import com.java2nb.novel.core.utils.Constants;
import com.java2nb.novel.core.utils.MQManager;
import com.java2nb.novel.entity.*;
import com.java2nb.novel.entity.Book;
import com.java2nb.novel.mapper.*;
import com.java2nb.novel.service.BookContentService;
import com.java2nb.novel.service.MyBookService;
import com.java2nb.novel.vo.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Collectors;

import static com.java2nb.novel.mapper.BookDynamicSqlSupport.*;

import static com.java2nb.novel.mapper.BookDynamicSqlSupport.id;
import static com.java2nb.novel.mapper.BookSettingDynamicSqlSupport.bookSetting;
import static org.mybatis.dynamic.sql.SqlBuilder.*;
import static org.mybatis.dynamic.sql.select.SelectDSL.select;


@Slf4j
@Service
public class MyBookServiceImpl implements MyBookService {
    @Autowired
    FrontBookMapper bookMapper;

    @Autowired
    FrontBookSettingMapper bookSettingMapper;
    @Autowired
    private BookCacheServiceImpl bookCacheService;

    @Resource
    CacheService cacheService;

    @Resource
    private RestHighLevelClient client;

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
    @Autowired
    private MQManager mqManager;

    Random random = new Random();

    @Override
    public Result<?> listClickRank() {
        return listClickRank(CacheKey.INDEX_CLICK_RANK_BOOK_KEY, 10);
    }

    private Result<?> queryLocalCache(String key){
        String bookJson = bookCacheService.getIndexByKey(key);
        ObjectMapper objectMapper = new ObjectMapper();
        List<Book> books = null;
        if(bookJson != null){
            try {
                books = objectMapper.readValue(bookJson, new TypeReference<List<Book>>() {});
            } catch (JsonProcessingException e) {
                return Result.customError("反序列化错误", 2021);
            }
            return Result.ok(books);
        }

        return null;
    }

    private Result<?> listClickRank(String key, int limit){
        Result<?> result = queryLocalCache(key);
        if(result != null){
            return result;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        List<Book> books = null;
        String bookJson = cacheService.get(key);



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
            cacheService.set(key, jsonStr, RedisConstant.INDEX_MAX_TTL);
        }else{
            try {
                books = objectMapper.readValue(bookJson, new TypeReference<List<Book>>() {});
            } catch (JsonProcessingException e) {
                return Result.customError("反序列化错误", 2021);
            }
        }

        bookCacheService.putIndexByKey(key, bookJson);

        return Result.ok(books);
    }

    @Override
    public Result<?> listNewRank() {
        return listNewRank(CacheKey.INDEX_NEW_BOOK_KEY, 10);
    }

    private Result<?> listNewRank(String key, int limit){
        Result<?> result = queryLocalCache(key);
        if(result != null){
            return result;
        }

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
            cacheService.set(key, booksJson, RedisConstant.INDEX_MAX_TTL);
        }else{
            try {
                books = objectMapper.readValue(booksJson, new TypeReference<List<Book>>() {
                });
            } catch (JsonProcessingException e) {
                return Result.customError("反序列化错误", 2021);
            }
        }

        bookCacheService.putIndexByKey(key, booksJson);

        return Result.ok(books);
    }

    @Override
    public Result<?> listUpdateRank() {
        return listUpdateRank(CacheKey.INDEX_NEW_BOOK_KEY, 10);
    }

    private Result<?> listUpdateRank(String key, int limit){
        Result<?> result = queryLocalCache(key);
        if(result != null){
            return result;
        }

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

            cacheService.set(key, booksJson, RedisConstant.INDEX_MAX_TTL);
        }else{
            try {
                books = objectMapper.readValue(booksJson, new TypeReference<List<Book>>() {
                });
            } catch (JsonProcessingException e) {
                return Result.customError("反序列化错误", 2021);
            }

        }

        bookCacheService.putIndexByKey(key, booksJson);

        return Result.ok(books);
    }

    @Override
    public Book queryBook(long bookId) {
        String bookJson = bookCacheService.getBookByKey(String.valueOf(bookId));
        ObjectMapper objectMapper = new ObjectMapper();
        if(bookJson != null && !bookJson.isEmpty()) {
            try {
                return objectMapper.readValue(bookJson, new TypeReference<Book>() {});
            } catch (JsonProcessingException e) {
                return null;
            }
        }
        SelectStatementProvider select = select(id, bookName, catId, catName, picUrl, authorName, bookStatus, visitCount, wordCount, bookDesc, lastIndexId, lastIndexUpdateTime, lastIndexName)
                .from(book)
                .where(id, isEqualTo(bookId))
                .build()
                .render(RenderingStrategy.MYBATIS3);



        Book book = bookMapper.selectOne(select).get();
        try {
            bookJson = objectMapper.writeValueAsString(book);
            bookCacheService.putBookByKey(String.valueOf(bookId), bookJson);
        } catch (JsonProcessingException e) {
            return null;
        }
        return book;
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
    public Long queryBookLastChapter(long id) {
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

    @Transactional
    @Override
    public Result<?> addVisitCount(Long bookId) {
//        boolean res = bookMapper.addVisitCountByOne(bookId);
        Long totalCount = cacheService.incrHmKeyFieldByOne(CacheKey.BOOK_ADD_VISIT_COUNT, String.valueOf(bookId));
        if(totalCount > 0){
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
        //刷新Redis，caffeine和ES
        //TODO 后面细分，只更新redis和caffeine，不更新ES
        mqManager.sendBookMessage(bookId, RabbitMQConstant.RABBITMQ_BOOK_UPDATE_BINDING_KEY);
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
        String bookContentJson = bookCacheService.getBookContentByKey(String.valueOf(bookIndexId));
        ObjectMapper objectMapper = new ObjectMapper();
        if(bookContentJson != null && !bookContentJson.isEmpty()){
            try {
                return objectMapper.readValue(bookContentJson, new TypeReference<BookContent>() {
                });
            } catch (JsonProcessingException e) {
                return null;
            }
        }

        //TODO 应该根据bookId和bookIndexId来查询的，但这里只用了bookIndexId，看看能不能改数据库表的DDL
        SelectStatementProvider select = select(BookContentDynamicSqlSupport.content, BookContentDynamicSqlSupport.id)
                .from(BookContentDynamicSqlSupport.bookContent)
                .where(BookContentDynamicSqlSupport.indexId, isEqualTo(bookIndexId))
                .build()
                .render(RenderingStrategy.MYBATIS3);

        Optional<BookContent> bookContent = bookContentMapper.selectOne(select);
        try {
            bookContentJson = objectMapper.writeValueAsString(bookContent.get());
            bookCacheService.putBookContentByKey(String.valueOf(bookIndexId), bookContentJson);
        } catch (JsonProcessingException e) {
            return null;
        }
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

    @SneakyThrows
    @Override
    public Map<Byte, List<BookSettingVO>> listBookSettingVO() {
        String result = cacheService.get(CacheKey.INDEX_BOOK_SETTINGS_KEY);
        if (result == null || result.length() < Constants.OBJECT_JSON_CACHE_EXIST_LENGTH) {
            List<BookSettingVO> list = bookSettingMapper.listVO();
            if (list.size() == 0) {
                //如果首页小说没有被设置，则初始化首页小说设置
                list = initIndexBookSetting();
            }
            result = new ObjectMapper().writeValueAsString(
                    list.stream().collect(Collectors.groupingBy(BookSettingVO::getType)));
            cacheService.set(CacheKey.INDEX_BOOK_SETTINGS_KEY, result, RedisConstant.INDEX_MAX_TTL);
        }
        return new ObjectMapper().readValue(result, Map.class);
    }

    private List<BookSettingVO> initIndexBookSetting() {
        Date currentDate = new Date();
        List<Book> books = bookMapper.selectIdsByScoreAndRandom(Constants.INDEX_BOOK_SETTING_NUM);
        if (books.size() == Constants.INDEX_BOOK_SETTING_NUM) {
            List<BookSetting> bookSettingList = new ArrayList<>(Constants.INDEX_BOOK_SETTING_NUM);
            List<BookSettingVO> bookSettingVOList = new ArrayList<>(Constants.INDEX_BOOK_SETTING_NUM);
            for (int i = 0; i < books.size(); i++) {
                Book book = books.get(i);
                byte type;
                if (i < 4) {
                    type = 0;
                } else if (i < 14) {
                    type = 1;
                } else if (i < 19) {
                    type = 2;
                } else if (i < 25) {
                    type = 3;
                } else {
                    type = 4;
                }
                BookSettingVO bookSettingVO = new BookSettingVO();
                BookSetting bookSetting = new BookSetting();
                bookSetting.setType(type);
                bookSetting.setSort((byte) i);
                bookSetting.setBookId(book.getId());
                bookSetting.setCreateTime(currentDate);
                bookSetting.setUpdateTime(currentDate);
                bookSettingList.add(bookSetting);

                BeanUtils.copyProperties(book, bookSettingVO);
                BeanUtils.copyProperties(bookSetting, bookSettingVO);
                bookSettingVOList.add(bookSettingVO);
            }

            bookSettingMapper.delete(deleteFrom(bookSetting).build()
                    .render(RenderingStrategies.MYBATIS3));
            bookSettingMapper.insertMultiple(bookSettingList);

            return bookSettingVOList;
        }
        return new ArrayList<>(0);
    }


    @Override
    public Result<?> listCommentRank(String key, int limit) {
        Result<?> result = queryLocalCache(key);
        if(result != null){
            return result;
        }

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
            cacheService.set(key, jsonStr, RedisConstant.INDEX_MAX_TTL);
        }else{
            try {
                books = objectMapper.readValue(bookJson, new TypeReference<List<Book>>() {});
            } catch (JsonProcessingException e) {
                return Result.customError("反序列化错误", 2021);
            }
        }

        bookCacheService.putIndexByKey(key, bookJson);

        return Result.ok(books);
    }

    @Override
    public void queryUsingElasticSearch(PageBean<BookDoc> pageBean, SearchDataVO searchData) {
        SearchRequest searchRequest = new SearchRequest("book");

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //关键字
        boolQueryBuilder.must(QueryBuilders.matchQuery("keywordSearch", searchData.getKeyword()));

        //是否完结
        if(searchData.getBookStatus() != null){
            boolQueryBuilder.must(QueryBuilders.matchQuery("bookStatus", searchData.getBookStatus()));
        }

        //字数限制
        if(searchData.getWordCountMax() != null){
            int min = 0;
            if(searchData.getWordCountMin() != null){
                min = searchData.getWordCountMin();
            }
            RangeQueryBuilder wordCount = QueryBuilders.rangeQuery("wordCount").from(min).to(searchData.getWordCountMax());
            boolQueryBuilder.must(wordCount);
        }else{
            int min = 0;
            if(searchData.getWordCountMin() != null){
                min = searchData.getWordCountMin();
                RangeQueryBuilder wordCount = QueryBuilders.rangeQuery("wordCount").from(min);
                boolQueryBuilder.must(wordCount);
            }
        }

        //更新时间限制
        if(searchData.getUpdatePeriod() != null){
            Calendar calendar = Calendar.getInstance();

            // 减去7天
            calendar.add(Calendar.DAY_OF_MONTH, (int) -searchData.getUpdatePeriod());
            Date date  = calendar.getTime();
            boolQueryBuilder.must(QueryBuilders.rangeQuery("updateTime").from(date));
        }

        searchRequest.source().query(boolQueryBuilder);

        //排序
        String sort = searchData.getSort();
        if(sort != null){
            switch (sort) {
                case "last_index_update_time":
                    searchRequest.source().sort("updateTime", SortOrder.DESC);
                    break;
                case "word_count":
                    searchRequest.source().sort("wordCount", SortOrder.DESC);
                    break;
                case "visit_count":
                    searchRequest.source().sort("visitCount", SortOrder.DESC);
                    break;
            }
        }

        //分页
        searchData.calculateOffset();
        int offset = searchData.getOffset();
        int size = searchData.getLimit();
        searchRequest.source().from(offset).size(size);

        //发送请求
        try {
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            //解析响应
            handleResponse(response, pageBean);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    @Override
//    public Result<?> recoverFromES() throws IOException {
//        SearchRequest request = new SearchRequest("book");
//        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
//        sourceBuilder.query(QueryBuilders.matchAllQuery());
//        sourceBuilder.size(1200);
//        request.source(sourceBuilder);
//
//        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        SearchHit[] hits = response.getHits().getHits();
//        for (SearchHit hit : hits) {
//            String sourceAsString = hit.getSourceAsString();
//            BookDoc bookDoc = objectMapper.readValue(sourceAsString, new TypeReference<BookDoc>() {
//            });
//            UpdateStatementProvider update = update(book)
//                    .set(lastIndexId)
//                    .equalTo(bookDoc.getLastIndexId())
//                    .set(lastIndexName)
//                    .equalTo(bookDoc.getLastIndexName())
//                    .set(lastIndexUpdateTime)
//                    .equalTo(bookDoc.getUpdateTime())
//                    .where(id, isEqualTo(bookDoc.getId()))
//                    .build()
//                    .render(RenderingStrategy.MYBATIS3);
//
//            bookMapper.update(update);
//        }
//        return Result.ok();
//    }

    private void handleResponse(SearchResponse response, PageBean<BookDoc> pageBean) {
        SearchHits searchHits = response.getHits();
        long total = searchHits.getTotalHits() == null ? 0 : searchHits.getTotalHits().value;
        SearchHit[] hits = searchHits.getHits();
        ObjectMapper objectMapper = new ObjectMapper();
        List<BookDoc> list = new ArrayList<>();

        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();
            try {
                BookDoc bookDoc = objectMapper.readValue(json, new TypeReference<BookDoc>() {});
                list.add(bookDoc);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        pageBean.setList(list);
        pageBean.setTotal(total);
    }

    private Date getTimeTwoMonthAgo(){
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(2);
        return Date.from(oneMonthAgo.atZone(ZoneId.systemDefault()).toInstant());
    }

//    @Override
//    public void recover(){
//        SelectStatementProvider bookSelect = SqlBuilder.select(BookDynamicSqlSupport.id)
//                .from(BookDynamicSqlSupport.book)
//                .build()
//                .render(RenderingStrategy.MYBATIS3);
//
//        List<Book> books = bookMapper.selectMany(bookSelect);
//        for (Book book : books) {
//            SelectStatementProvider select = select(BookIndexDynamicSqlSupport.id, BookIndexDynamicSqlSupport.indexName, BookIndexDynamicSqlSupport.updateTime)
//                    .from(BookIndexDynamicSqlSupport.bookIndex)
//                    .where(BookIndexDynamicSqlSupport.bookId, isEqualTo(book.getId()))
//                    .orderBy(BookIndexDynamicSqlSupport.indexNum.descending())
//                    .limit(1)
//                    .build()
//                    .render(RenderingStrategy.MYBATIS3);
//            Optional<BookIndex> bookIndex = bookIndexMapper.selectOne(select);
//            if(bookIndex.isPresent()){
//                UpdateStatementProvider render = update(BookDynamicSqlSupport.book)
//                        .set(BookDynamicSqlSupport.lastIndexId)
//                        .equalTo(bookIndex.get().getId())
//                        .set(BookDynamicSqlSupport.lastIndexName)
//                        .equalTo(bookIndex.get().getIndexName())
//                        .set(BookDynamicSqlSupport.lastIndexUpdateTime)
//                        .equalTo(bookIndex.get().getUpdateTime())
//                        .where(BookDynamicSqlSupport.id, isEqualTo(book.getId()))
//                        .build()
//                        .render(RenderingStrategy.MYBATIS3);
//                bookMapper.update(render);
//            }
//
//        }
//    }
}
