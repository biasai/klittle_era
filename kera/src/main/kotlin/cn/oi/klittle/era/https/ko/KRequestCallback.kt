package cn.oi.klittle.era.https.ko


/**
 * fixme 回调请求参数;与http本身的回调方法；不冲突；二者并列。
 * fixme 这样回调方法；就能以参数的形式传递进去了。
 * Created by 彭治铭 on 2019/4/7.
 */
open class KRequestCallback {
    //fixme 开始回调
    open var start: (() -> Unit)? = null

    open fun onStart(start: (() -> Unit)? = null): KRequestCallback {
        this.start = start
        return this
    }

    //fixme 成功回调(返回服务器原始数据)
    open var success: ((result: String) -> Unit)? = null

    open fun onSuccess(success: ((result: String) -> Unit)? = null): KRequestCallback {
        this.success = success
        return this
    }

    //fixme 失败回调
    open var failure: ((errStr: String?) -> Unit)? = null

    open fun onFailure(failure: ((errStr: String?) -> Unit)? = null): KRequestCallback {
        this.failure = failure
        return this
    }

    //fixme 结束回调，无论是成功还是失败都会调用(最后执行)
    open var finish: (() -> Unit)? = null

    open fun onFinish(finish: (() -> Unit)? = null): KRequestCallback {
        this.finish = finish
        return this
    }
}