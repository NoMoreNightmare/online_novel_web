package com.java2nb.novel.core.cache;

/**
 * @author 11797
 */
public interface CacheKey {

    /**
     * 首页小说设置
     * */
    String INDEX_BOOK_SETTINGS_KEY = "indexBookSettingsKey";

    /**
     * 首页新闻
     * */
    String INDEX_NEWS_KEY = "indexNewsKey";

    /**
     * 首页点击榜单
     * */
    String INDEX_CLICK_RANK_BOOK_KEY = "indexClickRankBookKey";

    /**
     * 首页友情链接
     * */
    String INDEX_LINK_KEY = "indexLinkKey";

    /**
     * 首页新书榜单
     * */
    String INDEX_NEW_BOOK_KEY = "indexNewBookKey";


    /**
     * 首页更新榜单
     * */
    String INDEX_UPDATE_BOOK_KEY = "indexUpdateBookKey";

    /**
     * 模板目录保存key
     * */
    String TEMPLATE_DIR_KEY =  "templateDirKey";;

    /**
     * 正在运行的爬虫线程存储KEY前缀
     * */
    String RUNNING_CRAWL_THREAD_KEY_PREFIX = "runningCrawlTreadDataKeyPrefix";

    /**
     * 上一次搜索引擎更新的时间
     * */
    String ES_LAST_UPDATE_TIME = "esLastUpdateTime";

    /**
     * 搜索引擎转换锁
     * */
    String ES_TRANS_LOCK = "esTransLock";

    /**
     * 上一次搜索引擎是否更新过小说点击量
     * */
    String ES_IS_UPDATE_VISIT = "esIsUpdateVisit";

    /**
     * 累积的小说点击量
     * */
    String BOOK_ADD_VISIT_COUNT = "bookAddVisitCount";
    /**
     * 测试爬虫规则缓存
     */
    String BOOK_TEST_PARSE = "testParse";

    /**
     * redisson的生成id的分布式锁
     */
    String REDISSON_ID_GENERATOR = "redissonIdGenerator";

    /**
     * 排行榜页点击榜
     */
    String RANKING_LIST_CLICK = "rankingListClick";

    /**
     * 排行榜新书榜
     */
    String RANKING_LIST_NEW = "rankingListNew";

    /**
     * 排行榜更新榜
     */
    String RANKING_LIST_UPDATE = "rankingListUpdate";

    /**
     * 排行榜评论榜
     */
    String RANKING_LIST_COMMENT = "rankingListComment";
}
