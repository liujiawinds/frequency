package com.maxent.frequency.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Reader;
import java.lang.reflect.Type;

/**
 * Gson utilities.
 * <p/>
 * Created by kevin on 12/29/15.
 */
public abstract class GsonUtils {

    private static final Gson GSON = createGson();

    /**
     * Create the standard {@link Gson} configuration (currently use the defaults)
     *
     * @return created gson, never null
     */
    public static final Gson createGson() {
        return new GsonBuilder().create();
    }

    /**
     * Get reusable pre-configured {@link Gson} instance
     *
     * @return Gson instance
     */
    public static final Gson getGson() {
        return GSON;
    }

    /**
     * Convert object to json
     *
     * @param object
     * @return json string
     */
    public static final String toJson(Object object) {
        return GSON.toJson(object);
    }

    /**
     * Convert string to given type
     *
     * @param json
     * @param type
     * @return instance of type
     */
    public static final <V> V fromJson(String json, Class<V> type) {
        return GSON.fromJson(json, type);
    }

    /**
     * Convert string to given type
     *
     * @param json
     * @param type
     * @return instance of type
     */
    public static final <V> V fromJson(String json, Type type) {
        return GSON.fromJson(json, type);
    }

    /**
     * Convert content of reader to given type
     *
     * @param reader
     * @param type
     * @return instance of type
     */
    public static final <V> V fromJson(Reader reader, Class<V> type) {
        return GSON.fromJson(reader, type);
    }

    /**
     * Convert content of reader to given type
     *
     * @param reader
     * @param type
     * @return instance of type
     */
    public static final <V> V fromJson(Reader reader, Type type) {
        return GSON.fromJson(reader, type);
    }
}