package cn.oi.klittle.era.gson;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * fixme 属性获取工具类型
 */
public class KFieldsUtils {

    /**
     * fixme 获取当前类，及其父类的所有属性。亲测有效。
     *
     * @param object
     * @return
     */
    public static Field[] getAllFields(Object object) {
        return getAllFields(object.getClass());
    }

    public static Field[] getAllFields(Class clazz) {
        List<Field> fieldList = new ArrayList<>();
        try {
            int count = 0;//加个父类数量判断；防止父类太多。异常。
            while (clazz != null && count < 10) {
                //clazz.getDeclaredFields()只能获取当前类的属性
                fieldList.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
                clazz = clazz.getSuperclass();//获取父类
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Field[] fields = new Field[fieldList.size()];
        fieldList.toArray(fields);
        return fields;
    }

}
