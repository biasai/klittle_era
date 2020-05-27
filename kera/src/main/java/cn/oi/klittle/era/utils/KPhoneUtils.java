package cn.oi.klittle.era.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import cn.oi.klittle.era.exception.KCatchException;

//fixme 手机通讯录，获取名称和号码
public class KPhoneUtils {
    //                //处理返回的data,获取选择的联系人信息
//                var uri: Uri = data.getData()
//                var contacts: Array<String>? = KPhoneUtils.getPhoneContacts(uri, this)
//                contacts?.let {
//                    if (it.size >= 2) {
//                        it[0]//名称
//                        it[1]//手机号；fixme 默认格式是 153 1234 5678；中间有空格。KStringUtils.removeBlank(tel)可以去除中间的空格。
//                    }
//                }
    public static String[] getPhoneContacts(Uri uri, Activity activity) {
        if (uri == null || activity == null) {
            return null;
        }
        try {
            String[] contact = new String[2];
            //得到ContentResolver对象
            ContentResolver cr = activity.getContentResolver();
            //取得电话本中开始一项的光标
            Cursor cursor = cr.query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                //取得联系人姓名
                int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                contact[0] = cursor.getString(nameFieldColumnIndex);
                //取得电话号码
                String ContactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                Cursor phone = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + ContactId, null, null);
                if (phone != null) {
                    phone.moveToFirst();
                    contact[1] = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                }
                phone.close();
                cursor.close();
            } else {
                return null;
            }
            return contact;
        } catch (Exception e) {
            KLoggerUtils.INSTANCE.e("KPhoneUtils->手机通讯录，获取名称和号码异常：\t" + KCatchException.getExceptionMsg(e));
        }
        return null;
    }
}
