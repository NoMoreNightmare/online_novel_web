package com.java2nb.novel.core.cache;

import org.springframework.data.redis.core.ZSetOperations;

import java.util.Map;
import java.util.Set;

/**
 * @author 11797
 */
public interface CacheService {

	/**
	 * 根据key获取缓存的String类型数据
	 */
	String get(String key);

	/**
	 * 设置String类型的缓存
	 */
	void set(String key, String value);

	/**
	 * 设置一个有过期时间的String类型的缓存,单位秒
	 */
	void set(String key, String value, long timeout);
	
	/**
	 * 根据key获取缓存的Object类型数据
	 */
	Object getObject(String key);
	
	/**
	 * 设置Object类型的缓存
	 */
	void setObject(String key, Object value);
	
	/**
	 * 设置一个有过期时间的Object类型的缓存,单位秒
	 */
    void setObject(String key, Object value, long timeout);

	/**
	 * 根据key删除缓存的数据
	 */
	void del(String key);

	
	/**
	 * 判断是否存在一个key
	 * */
	boolean contains(String key);
	
	/**
	 * 设置key过期时间
	 * */
	void expire(String key, long timeout);


    long getMQUUID(String mqKey) throws InterruptedException;

//	Object hmGet(String key, String field);
//
//	void hmSet(String key, String value, Object object);
//
//	Long incrHmKeyFieldByOne(String key, String field);
//
//	Map<Object, Object> hmGetAll(String key) throws InterruptedException;

	public double incrZetByOne(String key, String value);

	public double zsetIncrBy(String key, String value, int score);

	public Set<String> zsetRankBy(String key, long start, long end);

	public Set<ZSetOperations.TypedTuple<String>> zetGetAll(String key);

}
