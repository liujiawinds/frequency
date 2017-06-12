package com.maxent.frequency.util;

/**
 * Created by kevin on 3/7/16.
 */

import com.typesafe.config.Config;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;

/**
 * This is a producer client which can be used to send the message or the pair
 * of key and message.
 * <p/>
 * It can be used to send one message once or multiple messages once. When
 * multiple messages, it will send only 20 messages in one batch.
 * <p/>
 * Created by kevin on 3/7/16.
 */
public class KafkaProducer {
    protected static Logger log = LoggerFactory.getLogger(KafkaProducer.class);

    // If the number of one batch is over 20, use 20 instead
    protected static int MULTI_MSG_ONCE_SEND_NUM = 20;

    private Producer<String, String> producer;

    private String defaultTopic;

    private Properties properties;

    public KafkaProducer() {
        Config kafkaConf = ConfigHelper.getConfig("kafka");
        properties = new Properties();
        properties.put("metadata.broker.list", kafkaConf.getString("brokers"));
        //properties.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        properties.put("serializer.class", "kafka.serializer.StringEncoder");
        properties.put("key.serializer.class", "kafka.serializer.StringEncoder");
        properties.put("request.required.acks", "-1");
//        properties.put("retry.backoff.ms", "10000");

        defaultTopic = kafkaConf.getString("test-topic");
        init();
    }

    public KafkaProducer(Properties properties, String defaultTopic) {
        this.properties = properties;
        this.defaultTopic = defaultTopic;

        init();
    }

    protected void init() {
        if (properties == null) {
            throw new IllegalArgumentException("Failed to init the Kafka producer due to the properties is null.");
        }
        log.info("Producer properties:" + properties);

        ProducerConfig config = new ProducerConfig(properties);
        producer = new Producer<String, String>(config);
    }

    // send string message

    public void send(String message) {
        send2Topic(null, message);
    }

    public void send2Topic(String topicName, String message) {
        if (message == null) {
            return;
        }

        if (topicName == null)
            topicName = defaultTopic;

        KeyedMessage<String, String> km = new KeyedMessage<String, String>(
                topicName, message);
        producer.send(km);
    }

    public void send(String key, String message) {
        send2Topic(null, key, message);
    }

    public void send2Topic(String topicName, String key, String message) {
        if (message == null) {
            return;
        }

        if (topicName == null)
            topicName = defaultTopic;

        KeyedMessage<String, String> km = new KeyedMessage<String, String>(
                topicName, key, message);
        producer.send(km);
    }

    public void send(Collection<String> messages) {
        send2Topic(null, messages);
    }

    public void send2Topic(String topicName, Collection<String> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        if (topicName == null)
            topicName = defaultTopic;

        List<KeyedMessage<String, String>> kms = new ArrayList<KeyedMessage<String, String>>();
        int i = 0;
        for (String entry : messages) {
            KeyedMessage<String, String> km = new KeyedMessage<String, String>(
                    topicName, entry);
            kms.add(km);
            i++;
            // Send the messages 20 at most once
            if (i % MULTI_MSG_ONCE_SEND_NUM == 0) {
                producer.send(kms);
                kms.clear();
            }
        }

        if (!kms.isEmpty()) {
            producer.send(kms);
        }
    }

    public void send(Map<String, String> messages) {
        send2Topic(null, messages);
    }

    public void send2Topic(String topicName, Map<String, String> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        if (topicName == null)
            topicName = defaultTopic;

        List<KeyedMessage<String, String>> kms = new ArrayList<KeyedMessage<String, String>>();

        int i = 0;
        for (Entry<String, String> entry : messages.entrySet()) {
            KeyedMessage<String, String> km = new KeyedMessage<String, String>(
                    topicName, entry.getKey(), entry.getValue());
            kms.add(km);
            i++;
            // Send the messages 20 at most once
            if (i % MULTI_MSG_ONCE_SEND_NUM == 0) {
                producer.send(kms);
                kms.clear();
            }
        }

        if (!kms.isEmpty()) {
            producer.send(kms);
        }
    }

    // send bean message

    public <T> void sendBean(T bean) {
        sendBean2Topic(null, bean);
    }

    public <T> void sendBean2Topic(String topicName, T bean) {
        send2Topic(topicName, GsonUtils.toJson(bean));
    }

    public <T> void sendBean(String key, T bean) {
        sendBean2Topic(null, key, bean);
    }

    public <T> void sendBean2Topic(String topicName, String key, T bean) {
        send2Topic(topicName, key, GsonUtils.toJson(bean));
    }

    public <T> void sendBeans(Collection<T> beans) {
        sendBeans2Topic(null, beans);
    }

    public <T> void sendBeans2Topic(String topicName, Collection<T> beans) {
        Collection<String> beanStrs = new ArrayList<String>();
        for (T bean : beans) {
            beanStrs.add(GsonUtils.toJson(bean));
        }

        send2Topic(topicName, beanStrs);
    }

    public <T> void sendBeans(Map<String, T> beans) {
        sendBeans2Topic(null, beans);
    }

    public <T> void sendBeans2Topic(String topicName, Map<String, T> beans) {
        Map<String, String> beansStr = new HashMap<String, String>();
        for (Entry<String, T> entry : beans.entrySet()) {
            beansStr.put(entry.getKey(), GsonUtils.toJson(entry.getValue()));
        }

        send2Topic(topicName, beansStr);
    }

    public void close() {
        producer.close();
    }

    public String getDefaultTopic() {
        return defaultTopic;
    }

    public void setDefaultTopic(String defaultTopic) {
        this.defaultTopic = defaultTopic;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}