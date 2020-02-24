package cn.oi.klittle.era.exception

interface KExceptionCallback {
    fun catchException(msg:String?)//msg是异常信息
}