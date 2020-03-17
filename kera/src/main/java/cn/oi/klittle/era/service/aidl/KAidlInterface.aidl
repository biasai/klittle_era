package cn.oi.klittle.era.service.aidl;
import cn.oi.klittle.era.service.aidl.KAidlCallback;
interface KAidlInterface {
void onMessage(KAidlCallback callback);
void setMessage(String msg);
}