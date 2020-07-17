package cn.oi.klittle.era.https.ko

//fixme 网络请求调用案例；
//                                var url=""
//                                KOhttp.url(url).apply {
//                                    //fixme .net asp 无法交互时；建议设置为空（为空时，http就不会设置contentType,就会使用默认的。）；
//                                    //fixme 如果是java;就不要设置为空。默认就行（不要设置）。
//                                    contentType(null)
//                                    var fields = java.util.HashMap<String, String>()
//                                    var subData = SubData()
//                                    subData.commondata = Https.getCommonData("")
//                                    fields.put("data", subData.toJson())
//                                    isShowParams(true)//是否打印参数
//                                    isShowLoad(true)//显示网络进度条
//                                    isCacle(false)//不缓存
//                                    isSharingDialog(true)//fixme 共享弹窗
//                                    isUrlUniqueParams(true)//fixme 唯一标志带上参数。
//                                    onSuccess {
//                                        //成功回调，返回服务器原始数据
//                                        KLoggerUtils.e("提交成功：\t" + it)
//                                    }
//                                    onFailure {
//                                        //网络访问失败回调
//                                        KLoggerUtils.e("提交失败：\t" + it)
//                                    }
//                                    addParam(fields)
//                                    isShowLoad(false)
//                                    //开始进度回调
//                                    onStart {
//
//                                    }
//                                    //结束进度回调（最后执行，并且一定会回调）
//                                    onFinish {
//
//                                    }
//                                    //post请求（通过泛型，直接返回对应数据）
//                                    post<AppHttpResult<String>>() {
//
//                                    }
//                                }

object KOhttp {
    //普通网络请求
    fun url(url: String): KHttps {
        return KHttps().url(url)
    }

    //轮询网络请求
    fun polling(url: String): KPolling {
        return KPolling(url = url)
    }

}