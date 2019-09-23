package cn.oi.klittle.era.base;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.Stack;

import cn.oi.klittle.era.utils.KAssetsUtils;
import cn.oi.klittle.era.utils.KCache;
import cn.oi.klittle.era.utils.KLoggerUtils;

/**
 * Created by  彭治铭 on 2017/9/10.
 */

public class KBaseActivityManager {
    private static KBaseActivityManager sInstance;
    private Stack<Activity> mActivityStack;

    private KBaseActivityManager() {
    }

    //初始化
    public static KBaseActivityManager getInstance() {
        if (null == sInstance) {
            sInstance = new KBaseActivityManager();
        }
        return sInstance;
    }

    //启动Activity
    public static void startActivity(Context cxt, Class<?> clazz) {
        Intent intent = new Intent(cxt, clazz);
        cxt.startActivity(intent);
    }


    //入栈
    public void pushActivity(Activity activity) {
        if (null == mActivityStack) {
            mActivityStack = new Stack<Activity>();
        }
        mActivityStack.push(activity);
    }

    /**
     * fixme 当前Activity（栈中最后一个压入的）
     *
     * @return
     */
    public Activity getStackTopActivity() {
        if (mActivityStack != null && mActivityStack.size() > 0) {
            Activity stack = mActivityStack.lastElement();
            if (stack != null && !stack.isFinishing()) {
                return stack;
            }
        }
//        try {
//            ActivityManager am = (ActivityManager)KBaseApplication.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
//            //String activityName = am.getRunningTasks(1).get(0).topActivity.getClassName();
//            //Class clazz = Class.forName(activityName);
//        }catch (Exception e){
//            e.printStackTrace();
//            Log.e("test","栈顶获取异常：\t"+e.getMessage());
//        }
        return null;
    }

    /**
     * 获取当前Activity的上一个Activity
     *
     * @param activity
     * @return
     */
    public Activity getStackPreviousActivity(Activity activity) {
        removeNullActivity();
        if (mActivityStack.size() >= 2) {
            int index = mActivityStack.size() - 2;
            for (int i = mActivityStack.size() - 1; i >= 0; i--) {
                if (activity.getLocalClassName().equals(mActivityStack.get(i).getLocalClassName()) && activity == mActivityStack.get(i)) {
                    if (activity != null && !activity.isFinishing()) {
                        index = i - 1;
                    }
                }
            }
            return mActivityStack.get(index);
        }
        return null;
    }


    private ArrayList<Activity> allActivity = new ArrayList();

    /**
     * 获取所有Activity
     *
     * @return
     */
    public ArrayList<Activity> getStackAllActivity() {
        if (allActivity == null) {
            allActivity = new ArrayList();
        }
        allActivity.clear();
        if (mActivityStack != null && mActivityStack.size() > 0) {
            for (int i = 0; i < mActivityStack.size(); i++) {
                Activity stack = mActivityStack.get(i);
                if (stack != null && !stack.isFinishing()) {
                    allActivity.add(stack);
                }
            }
        }
        return allActivity;
    }

    /**
     * 判断是否包含该Activity
     *
     * @param activity
     * @return
     */
    public boolean containsActivity(Activity activity) {
        if (activity == null || activity.isFinishing()) {
            return false;
        }
        ArrayList<Activity> allActivity = getStackAllActivity();
        if (allActivity.size() > 0) {
            for (int i = 0; i < allActivity.size(); i++) {
                if (activity == allActivity.get(i)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 移除所有为空，或已经销毁的Activity
     */
    public void removeNullActivity() {
        for (int i = 0; i < mActivityStack.size(); i++) {
            if (mActivityStack.get(i) == null || mActivityStack.get(i).isFinishing()) {
                mActivityStack.remove(i);
                removeNullActivity();
                break;
            }
        }
    }

    //结束当前的Activity
    public void finishActivity() {
        finishActivity(getStackTopActivity());
    }

    //结束指定的Activity
    public void finishActivity(Activity activity) {
        if (null != activity) {
            mActivityStack.remove(activity);
            if (!activity.isFinishing()) {
                activity.finish();
            }
            activity = null;
        }
    }

    //结束指定类名的Activity
    public void finishActivity(Class<?> clazz) {
        for (Activity activity : mActivityStack) {
            if (activity.getClass().equals(clazz)) {
                finishActivity(activity);
            }
        }
    }

    //销毁所有Activity
    public void finishAllActivity() {
        KBaseApplication.getInstance().recyclePreviousBitmap();//釋放上一個Actvity的位图
        for (Activity activity : mActivityStack) {
            if (null != activity && !activity.isFinishing()) {
                activity.finish();
            }
        }
        mActivityStack.clear();
        //在Activity之后销毁，防止异常。
        if (KCache.mInstanceMap != null) {
            KCache.mInstanceMap.clear();
        }
        KCache.cache = null;
        KCache.mInstanceMap = null;
        //fixme 销毁所有的位图
        KAssetsUtils.getInstance().recycleAll();
    }
}
