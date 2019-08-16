package cn.oi.klittle.era.gson.type;

import android.util.Log;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

//泛型传入格式：Model<Mode2>
//fixme 传入的必须是具体的类型，如果是泛型，也必须是reified 具体的泛型。[泛型类型解析，无限。]
public class KTypeReference<T> {

    private final Type type;

    public Type getType() {
        return type;
    }

    //第一层[最外层类型]，Model<Mode2>或者Model<ArrayList<Mode2>> 获取的是Mode1
    public String GenericClassName = null;
    public Class GenericClass = null;

    //第二层类型，Model<Mode2> 获取的是Mode2， 或者Model<ArrayList<Mode2>> 获取的是ArrayList
    public Class GenericClass2 = null;

    //第三层类型，Model<ArrayList<Mode3>> 获取的是Mode3
    public Class GenericClass3 = null;

    //第四层类型
    public Class GenericClass4 = null;

    //Class泛型集合
    public List<Class> classes = new ArrayList<Class>();

    protected KTypeReference() {
        Type superClass = getClass().getGenericSuperclass();
        //格式为：com.example.myapplication3.Model<com.example.myapplication3.Mode2>
        //com.example.myapplication3.Model<java.util.ArrayList<com.example.myapplication3.Mode3>>
        type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        //Log.e("test","类型:\t"+type);
        String className = type.toString().trim();
        if (className.contains("<") && className.contains(">")) {
            //多层[无限层]
            try {
                className = className.replace(">", "").trim();
                String[] classNames = className.split("<");
                for (int i = 0; i < classNames.length; i++) {
                    classes.add(Class.forName(classNames[i]));
                }
                GenericClass = classes.get(0);//第一层
                if (classes.size() >=2) {
                    GenericClass2 = classes.get(1);//第二层
                }
                if (classes.size() >= 3) {
                    GenericClass3 = classes.get(2);//第三层
                }
                if (classes.size() >= 4) {
                    GenericClass4 = classes.get(3);//第四层
                }
            } catch (Exception e) {
                Log.e("test", "class类型找不到异常无限:\t" + e.getMessage());
            }

        } else {
            //单层
            try {
                GenericClassName = className.substring(5).trim();
                GenericClass = Class.forName(GenericClassName);
                classes.add(GenericClass);//第一层
            } catch (Exception e) {
                Log.e("test", "class类型找不到异常0:\t" + e.getMessage());
            }
        }
        //Log.e("test", "总类型:\t" + className);
        //Log.e("test", "类型1：\t" + GenericClass + "\t类型2:\t" + GenericClass2 + "\t类型3：\t" + GenericClass3+"\t类型四:\t"+GenericClass3);
    }

    public final static Type LIST_STRING = new KTypeReference<List<String>>() {
    }.getType();
}
