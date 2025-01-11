package com.java2nb.novel.core.utils;

import com.java2nb.novel.core.result.RabbitMQConstant;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class MQManager {
    @Autowired
    private AmqpTemplate amqpTemplate;

    public void sendBookMessage(Long bookId, String routeKey){
        sendMQMessage(RabbitMQConstant.RABBITMQ_BOOK_EXCHANGE, routeKey, bookId);
    }

    public void sendBookContentMessage(Long bookIndexId, String routeKey){
        sendMQMessage(RabbitMQConstant.RABBITMQ_BOOK_CONTENT_EXCHANGE, routeKey, bookIndexId);
    }

    public void sendBookContentMessage(Long bookIndexId){
        sendMQMessage(RabbitMQConstant.RABBITMQ_BOOK_CONTENT_EXCHANGE, RabbitMQConstant.RABBITMQ_BOOK_CONTENT_UPDATE_OR_ADD, bookIndexId);
    }

    public void sendBookContentDeleteMessage(Long bookIndexId){
        sendMQMessage(RabbitMQConstant.RABBITMQ_BOOK_CONTENT_EXCHANGE, RabbitMQConstant.RABBITMQ_BOOK_CONTENT_DELETE, bookIndexId);
    }

    private void sendMQMessage(String exchange, String routeKey, Object message) {
        if(TransactionSynchronizationManager.isActualTransactionActive()){
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    amqpTemplate.convertAndSend(exchange, routeKey, message);
                }
            });
            return;
        }
        amqpTemplate.convertAndSend(exchange, routeKey, message);
    }
}
