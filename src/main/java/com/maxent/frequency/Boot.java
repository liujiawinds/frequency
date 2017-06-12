package com.maxent.frequency;

import com.alibaba.fastjson.JSON;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.maxent.frequency.util.ConfigHelper;
import com.maxent.frequency.util.KafkaProducer;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by liujia on 2017/6/9.
 * control kafka producer send speed in every second.
 */
public class Boot {
    private static Logger logger = LoggerFactory.getLogger(Boot.class);
    private static List<String> testDataFiles = ConfigHelper.getStringList("test-cases.test-data-files");
    private static int frequency = ConfigHelper.getInt("test-cases.frequency");
    private static int senderNumber = ConfigHelper.getInt("test-cases.senderNumber");
    private static KafkaProducer kafkaProducer = new KafkaProducer();
    private static AtomicInteger msgCnt = new AtomicInteger(1);

    private ScheduledExecutorService sheduler = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder().setNameFormat("scheduler-%d").build());
    private ExecutorService sendService = Executors.newFixedThreadPool(senderNumber, new ThreadFactoryBuilder().setNameFormat("sender-%d").build());

    private static String testMsg = null;

    static {
        try {
            System.out.println(testDataFiles.get(0));
            testMsg = IOUtils.toString(Boot.class.getClassLoader().getResourceAsStream(testDataFiles.get(0)));
            System.out.println(JSON.parseObject(testMsg).toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void start() {
        sheduler.scheduleAtFixedRate(() -> {
            msgCnt.set(0);
            logger.info("reset!!! this second have sent %s messages.", msgCnt.intValue());
        }, 1, 1, TimeUnit.SECONDS);

        sheduler.scheduleAtFixedRate(() -> {
            Config config = ConfigFactory.parseFile(new File("conf/application.conf")).resolve();
            synchronized (Boot.class) {
                frequency = config.getInt("test-cases.frequency");
            }
        }, 1, 1, TimeUnit.SECONDS);

        for (int i = 0; i < senderNumber; i++) {
            sendService.submit(() -> {
                while (true) {
                    try {
                        synchronized (Boot.class) {
                            if (msgCnt.intValue() < frequency) {
                                String msg = compose();
                                kafkaProducer.send(JSON.parseObject(msg).toJSONString());
                                msgCnt.incrementAndGet();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }


    private String compose() {
        String tempMaxent_id = UUID.randomUUID().toString().replace("-", "");
        String tick = UUID.randomUUID().toString().replace("-", "");
        String tempEventId = UUID.randomUUID().toString();
        String tempUserId = UUID.randomUUID().toString();

        String[] eventTypeArr = {"ACT", "Transaction", "Login", "Logout", "Customize"};
        String eventType = eventTypeArr[new Random().nextInt(5)];
        return testMsg
                .replaceAll("8BF21FA65F111BF84291A7B6718327C5", tempMaxent_id)
                .replaceAll("a3c3fa89-64cd-4be9-ac5d-730625f273df", tempEventId)
                .replaceAll("user123", tempUserId)
                .replaceAll("1455862416652", String.valueOf(System.currentTimeMillis()))
                .replaceAll("I'm a tick", tick)
                .replaceAll("Transaction", eventType);
    }


    public static void main(String[] args) {
        new Boot().start();
        try {
            while (true) {
                TimeUnit.SECONDS.sleep(1);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
