package com.maxent.frequency.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;
import java.util.List;

/**
 * Created by liujia on 2017/5/8.
 * Unify configuration interface.
 */
public class ConfigHelper {

    public static final Config instance;
    static {
        instance = ConfigFactory.parseFile(new File("conf/application.conf")).resolve();
    }
    public static Config getInstance(){
        return instance;
    }
    private ConfigHelper(){
    }

    public static Config getConfig(String path) {
        return instance.getConfig(path);
    }

    public static int getInt(String path) {
        return instance.getInt(path);
    }

    public static long getLong(String path) {
        return instance.getLong(path);
    }

    public static List<String> getStringList(String path) {
        return instance.getStringList(path);
    }
}
