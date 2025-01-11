package com.java2nb.novel.core.result;

public class RabbitMQConstant {
    public static final String RABBITMQ_BOOK_EXCHANGE = "novel.book.exchange";
    public static final String RABBITMQ_BOOK_REDIS_QUEUE = "novel.book.redis.queue";
    public static final String RABBITMQ_BOOK_CAFFEINE_QUEUE_PREFIX = "novel.book.caffeine.";
    public static final String RABBITMQ_BOOK_ES_UPDATE_QUEUE = "novel.book.es.update.queue";
    public static final String RABBITMQ_BOOK_ES_ADD_QUEUE = "novel.book.es.add.queue";

    public static final String RABBITMQ_BOOK_CONTENT_EXCHANGE = "novel.bookcontent.exchange";
    public static final String RABBITMQ_BOOK_CONTENT_REDIS_QUEUE = "novel.bookcontent.redis.queue";
    public static final String RABBITMQ_BOOK_CONTENT_CAFFEINE_QUEUE_PREFIX = "novel.bookcontent.caffeine.";

    public static final String RABBITMQ_BOOK_BINDING_KEY = "book.#";
    public static final String RABBITMQ_BOOK_ADD_BINDING_KEY = "book.add";
    public static final String RABBITMQ_BOOK_UPDATE_BINDING_KEY = "book.update";
}
