package cn.oi.klittle.era.activity.photo.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.luck.picture.lib.tools.DateUtils;
import com.luck.picture.lib.tools.PictureFileUtils;

public class KDCIMUtils {
    /**
     * 获取DCIM文件下最新一条拍照记录
     *
     * @param eqVideo 是否未视频，true 是视频，false不是。
     * @return
     */
    private static int getLastImageId(boolean eqVideo, Activity activity) {
        try {
            //selection: 指定查询条件
            String absolutePath = PictureFileUtils.getDCIMCameraPath();
            String ORDER_BY = MediaStore.Files.FileColumns._ID + " DESC";
            String selection = eqVideo ? MediaStore.Video.Media.DATA + " like ?" :
                    MediaStore.Images.Media.DATA + " like ?";
            //定义selectionArgs：
            String[] selectionArgs = {absolutePath + "%"};
            Cursor imageCursor = activity.getContentResolver().query(eqVideo ?
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                            : MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
                    selection, selectionArgs, ORDER_BY);
            if (imageCursor.moveToFirst()) {
                int id = imageCursor.getInt(eqVideo ?
                        imageCursor.getColumnIndex(MediaStore.Video.Media._ID)
                        : imageCursor.getColumnIndex(MediaStore.Images.Media._ID));
                long date = imageCursor.getLong(eqVideo ?
                        imageCursor.getColumnIndex(MediaStore.Video.Media.DURATION)
                        : imageCursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
                int duration = DateUtils.dateDiffer(date);
                imageCursor.close();
                // DCIM文件下最近时间30s以内的图片，可以判定是最新生成的重复照片
                //return duration <= 30 ? id : -1;//时间单位是秒
                //Log.e("test","duration:\t"+duration);//基本上都是0；
                return duration <= 4 ? id : -1;//时间改成5秒左右比较好（亲测有效，测试发现<=1秒都有效。）。时间太长防止误删（fixme 其他应用在DCIM生成的图片也会删除）
            } else {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 删除部分手机 拍照在DCIM也生成一张的问题
     *
     * @param id
     * @param eqVideo
     */
    private static void removeImage(int id, boolean eqVideo, Activity activity) {
        try {
            ContentResolver cr = activity.getContentResolver();
            Uri uri = eqVideo ? MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String selection = eqVideo ? MediaStore.Video.Media._ID + "=?"
                    : MediaStore.Images.Media._ID + "=?";
            cr.delete(uri,
                    selection,
                    new String[]{Long.toString(id)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * fixme 删除部分手机 拍照在DCIM也生成一张的问题
     *
     * @param activity
     */
    public static void remove(Activity activity) {
        try {
            if (activity != null && !activity.isFinishing()) {
                boolean eqVideo = false;
                int lastImageId = getLastImageId(eqVideo, activity);
                if (lastImageId != -1) {
                    removeImage(lastImageId, eqVideo, activity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
