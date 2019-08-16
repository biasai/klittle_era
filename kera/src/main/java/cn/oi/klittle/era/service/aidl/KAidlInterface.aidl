package cn.oi.klittle.era.service.aidl;
import cn.oi.klittle.era.service.aidl.KAidlCallback;
//aidl不会有提示。import需要自己手动导入(在同一个包名下，也要手动导入。)
//导入的对象不是java对象而是aidl对象。java对象是找不到的。只能找到aidl对象。
//aidl第二类文件，interface接口文件

//aidl不能使用private,public等修饰符修饰。这些是java的不是aidl的
//除了java基本类型(byte,short,int,long,float,double,boolean,char)，String,CharSequence不用修饰(默认且只能为in),其他类型都必须指明具体的tag【in,out,inout】
//in 可以获取参数传人之前的数据。但是修改之后(服务端)，客户端无法同步。
//out 无法接受参数传人之前的数据，即所传人参数的属性值都为null，服务端修改之后，客户端也会修改(同步)，已服务端为主。
//inout in和out都具备。但是很耗性能，建议不要使用



interface KAidlInterface {
void onMessage(KAidlCallback callback);
void setMessage(String msg);
}
