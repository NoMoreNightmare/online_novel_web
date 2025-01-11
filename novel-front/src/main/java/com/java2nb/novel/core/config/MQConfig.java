package com.java2nb.novel.core.config;

import com.java2nb.novel.core.cache.CacheKey;
import com.java2nb.novel.core.cache.CacheService;
import com.java2nb.novel.core.result.RabbitMQConstant;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfig {
    @Autowired
    private CacheService cacheService;

    /**
     * book的Topic交换机配置
     * @return book的Topic交换机
     */
    @Bean
    public TopicExchange bookTopicExchange() {
        return new TopicExchange(RabbitMQConstant.RABBITMQ_BOOK_EXCHANGE);
    }

    /**
     * redis监听book的队列
     * @return 监听book的队列
     */
    @Bean
    public Queue bookRedisQueue() {
        return new Queue(RabbitMQConstant.RABBITMQ_BOOK_REDIS_QUEUE);
    }

    /**
     * 将book的更新行为与redis队列绑定（新书的阅读量一般较少）
     * @return
     */
    @Bean
    public Binding bookRedisBinding() {
        return BindingBuilder.bind(bookRedisQueue()).to(bookTopicExchange()).with(RabbitMQConstant.RABBITMQ_BOOK_UPDATE_BINDING_KEY);
    }

    /**
     * ES监听book更新队列
     * @return
     */
    @Bean
    public Queue bookESUpdateQueue(){
        return new Queue(RabbitMQConstant.RABBITMQ_BOOK_ES_UPDATE_QUEUE);
    }

    /**
     * ES监听book新增队列
     * @return
     */
    @Bean
    public Queue bookESAddQueue(){
        return new Queue(RabbitMQConstant.RABBITMQ_BOOK_ES_ADD_QUEUE);
    }

    /**
     * 将book的新增行为与es的新增队列绑定
     * @return
     */
    @Bean
    public Binding bookESAddBinding() {
        return BindingBuilder.bind(bookESAddQueue()).to(bookTopicExchange()).with(RabbitMQConstant.RABBITMQ_BOOK_ADD_BINDING_KEY);
    }

    /**
     * 将book的更新行为与es的更新队列绑定
     * @return
     */
    @Bean
    public Binding bookESUpdateBinding() {
        return BindingBuilder.bind(bookESUpdateQueue()).to(bookTopicExchange()).with(RabbitMQConstant.RABBITMQ_BOOK_UPDATE_BINDING_KEY);
    }

    @Bean
    public String bookCaffeineQueueName(){
        long id = 0;
        try {
            id = cacheService.getMQUUID(CacheKey.MQ_BOOK);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return RabbitMQConstant.RABBITMQ_BOOK_CAFFEINE_QUEUE_PREFIX + id + ".queue";
    }

    @Bean
    public Queue bookCaffeineQueue(){
        return new Queue(bookCaffeineQueueName());
    }

    @Bean
    public Binding bookCaffeineBinding() {
        return BindingBuilder.bind(bookCaffeineQueue()).to(bookTopicExchange()).with(RabbitMQConstant.RABBITMQ_BOOK_UPDATE_BINDING_KEY);
    }


    @Bean
    public TopicExchange bookContentTopicExchange() {
        return new TopicExchange(RabbitMQConstant.RABBITMQ_BOOK_CONTENT_EXCHANGE);
    }


    @Bean
    public Queue bookContentRedisQueue() {
        return new Queue(RabbitMQConstant.RABBITMQ_BOOK_CONTENT_REDIS_QUEUE);
    }


    @Bean
    public String bookContentCaffeineQueueName(){
        //TODO 雪花算法生成的id
        long id = 0;
        try {
            id = cacheService.getMQUUID(CacheKey.MQ_BOOK_CONTENT);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return RabbitMQConstant.RABBITMQ_BOOK_CONTENT_CAFFEINE_QUEUE_PREFIX + id + ".queue";
    }

    @Bean
    public Queue bookContentCaffeineQueue() {

        return new Queue(bookContentCaffeineQueueName());
    }

    @Bean
    public Binding bookContentRedisBinding() {
        return BindingBuilder.bind(bookContentRedisQueue()).to(bookContentTopicExchange()).with(RabbitMQConstant.RABBITMQ_BOOK_CONTENT_UPDATE_OR_ADD);
    }


    @Bean
    public Binding bookContentCaffeineBinding() {
        return BindingBuilder.bind(bookContentCaffeineQueue()).to(bookContentTopicExchange()).with(RabbitMQConstant.RABBITMQ_BOOK_CONTENT_UPDATE_OR_ADD);
    }


    @Bean
    public Binding bookContentDeleteRedisBinding() {
        return BindingBuilder.bind(bookContentDeleteRedisQueue()).to(bookContentTopicExchange()).with(RabbitMQConstant.RABBITMQ_BOOK_CONTENT_DELETE);
    }

    @Bean
    public Queue bookContentDeleteRedisQueue() {
        return new Queue(RabbitMQConstant.RABBITMQ_BOOK_CONTENT_REDIS_DELETE_QUEUE);
    }



    @Bean
    public String bookContentDeleteCaffeineQueueName(){
        //TODO 雪花算法生成的id
        long id = 0;
        try {
            id = cacheService.getMQUUID(CacheKey.MQ_BOOK_CONTENT_DELETE);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return RabbitMQConstant.RABBITMQ_BOOK_CONTENT_DELETE_CAFFEINE_QUEUE_PREFIX + id + ".queue";
    }

    @Bean
    public Queue bookContentDeleteCaffeineQueue() {

        return new Queue(bookContentDeleteCaffeineQueueName());
    }

    @Bean
    public Binding bookContentDeleteCaffeineBinding() {
        return BindingBuilder.bind(bookContentDeleteCaffeineQueue()).to(bookContentTopicExchange()).with(RabbitMQConstant.RABBITMQ_BOOK_CONTENT_DELETE);
    }
}
