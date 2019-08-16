package cn.oi.klittle.era.utils

import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

//    fixme 使用案例
//    fun execute(){
//      ....执行代码
//    }
/**
 * 线程池工具类
 */
object KThreadPoolUtils {
    private var threadPoolExecutor: ThreadPoolExecutor? = null
    fun getPool(): ThreadPoolExecutor {
        if (threadPoolExecutor == null) {
            val corePoolSize = Runtime.getRuntime().availableProcessors()//核心线程池数量,即并发线程数量。显示java虚拟机可用的处理器个数，一般都是2。
            val maximumPoolSize = corePoolSize * 2 + 1//最大线程池数量,一般设置成核心线程数的两倍+1。
            val keepAliveTime: Long = 10//超过最大线程池数量，空闲线程存活时间
            val unit = TimeUnit.SECONDS//存活时间单位
            //使用LinkedBlockingDeque连接队列数量无限，将会忽略maximumPoolSize最大线程数量。
            val workQueue = LinkedBlockingDeque<Runnable>()//阻塞队列，超过核心线程池，就放入阻塞队列等待。只有核心线程执行完毕，才会继续执行。
            threadPoolExecutor = ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue)
        }
        return threadPoolExecutor!!
    }
}