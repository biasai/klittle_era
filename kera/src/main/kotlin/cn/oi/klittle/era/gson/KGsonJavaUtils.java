package cn.oi.klittle.era.gson;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 彭治铭 on 2019/3/14.
 * json库(第三方)
 * api 'com.google.code.gson:gson:2.8.5'
 * Gson utilities.
 */
public abstract class KGsonJavaUtils {

    private static final Gson GSON = createGson(true);

    private static final Gson GSON_NO_NULLS = createGson(false);

    /**
     * Create the standard {@link Gson} configuration
     *
     * @return created gson, never null
     */
    public static final Gson createGson() {
        return createGson(true);
    }

    /**
     * Create the standard {@link Gson} configuration
     *
     * @param serializeNulls whether nulls should be serialized
     * @return created gson, never null
     */
    public static final Gson createGson(final boolean serializeNulls) {
        final GsonBuilder builder = new GsonBuilder();
        if (serializeNulls)
            builder.serializeNulls();
        builder.setDateFormat("yyyy-MM-dd HH:mm:ss");
        return builder.create();
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
     * Get reusable pre-configured {@link Gson} instance
     *
     * @param serializeNulls
     * @return Gson instance
     */
    public static final Gson getGson(final boolean serializeNulls) {
        return serializeNulls ? GSON : GSON_NO_NULLS;
    }

    /**
     * Convert object to json
     *
     * @param object
     * @return json string fixme 这个第三方库，(斜杠前面不会加上反斜杠) 。 /Date(1557278096000)/不会变，不会加上反斜杠。
     */
    public static final String toJson(final Object object) {
        return toJson(object, true);
    }

    /**
     * fixme 对象转JSON
     * Convert object to json
     *
     * @param object
     * @param includeNulls
     * @return json string
     */
    public static final String toJson(final Object object,
                                      final boolean includeNulls) {
        return includeNulls ? GSON.toJson(object) : GSON_NO_NULLS.toJson(object);
    }

//    fixme json转 Map
//    val type = object : TypeToken<HashMap<String?, TestEntity?>?>() {}.type
//    var map:HashMap<String?, TestEntity?>?=KGsonJavaUtils.fromJson(json!!,type)

    /**
     * fixme json解析成对象，如：KGsonJavaUtils.fromJson(json!!,TestEntity::class.java)
     * fixme Gson工具类也很强大，是谷歌的。实体类多个一个字段少一个字段，无所谓，不会报错的。兼容性还是很好的。甚至类名变了也无所谓，只有字段对的上就行。
     * Convert string to given type
     *
     * @param json
     * @param type
     * @return instance of type
     */
    public static final <V> V fromJson(String json, Class<V> type) {
        V v = null;
        if (!TextUtils.isEmpty(json)) {
            try {
                v = GSON.fromJson(json, type);
            } catch (Exception e) {
            }
        }
        return v;
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


    /**
     * see {http://stackoverflow.com/questions/20773850/gson-typetoken-with-dynamic-arraylist-item-type}
     *
     * @param json
     * @param type
     * @param <V>
     * @return
     */
    public static final <V> List<V> fromJsonToList(String json, Class<V> type) {
        List<V> list = new ArrayList<>();
        if (!TextUtils.isEmpty(json)) {
            try {
                JsonArray array = new JsonParser().parse(json).getAsJsonArray();
                for (final JsonElement elem : array) {
                    list.add(GSON.fromJson(elem, type));
                }
            } catch (Exception e) {
            }
        }
        return list;
    }


}
