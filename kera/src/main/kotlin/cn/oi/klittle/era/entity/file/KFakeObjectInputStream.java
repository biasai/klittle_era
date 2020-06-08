package cn.oi.klittle.era.entity.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

import cn.oi.klittle.era.exception.KCatchException;
import cn.oi.klittle.era.utils.KLoggerUtils;

//    companion object{
//          private val serialVersionUID:Long=110L//fixme 实体类里面，这样可以固定serialVersionUID。一定要是 val类型，不可再变。才不会发生改变。
//        }

/**
 * fixme Java序列化--忽略serialVersionUID验证;（数据结构不能变哦。）
 * fixme 虽然忽略了uid验证，但是如果属性个数对不上，即数据结构不一致时，一样会报转换错误。
 */
public class KFakeObjectInputStream extends ObjectInputStream {

    public KFakeObjectInputStream(InputStream in) throws IOException {
        super(in);
    }

    public KFakeObjectInputStream() throws SecurityException, IOException {
        super();
    }

//    @Override
//    protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
//        ObjectStreamClass objInputStream = super.readClassDescriptor();
//        Class<?> localClass = Class.forName(objInputStream.getName());
//        ObjectStreamClass localInputStream = ObjectStreamClass.lookup(localClass);
//
//        if (localInputStream != null) {
//            final long localUID = localInputStream.getSerialVersionUID();
//            final long objUID = objInputStream.getSerialVersionUID();
//            if (localUID != objUID) {
//                return localInputStream;
//            }
//        }
//        return objInputStream;
//    }

    @Override
    protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
        ObjectStreamClass resultClassDescriptor = super.readClassDescriptor(); // initially streams descriptor
        try {
            Class localClass = Class.forName(resultClassDescriptor.getName()); // the class in the local JVM that this descriptor represents.
            if (localClass == null) {
                //System.out.println("No local class for " + resultClassDescriptor.getName());
                KLoggerUtils.INSTANCE.e("No local class for " + resultClassDescriptor.getName(), true);
                return resultClassDescriptor;
            }
            ObjectStreamClass localClassDescriptor = ObjectStreamClass.lookup(localClass);
            if (localClassDescriptor != null) { // only if class implements serializable
                final long localSUID = localClassDescriptor.getSerialVersionUID();
                final long streamSUID = resultClassDescriptor.getSerialVersionUID();
                if (streamSUID != localSUID) { // check for serialVersionUID mismatch.
                    final StringBuffer s = new StringBuffer("Overriding serialized class version mismatch: ");
                    s.append("local serialVersionUID = ").append(localSUID);
                    s.append(" stream serialVersionUID = ").append(streamSUID);
                    //Exception e = new InvalidClassException(s.toString());
                    //System.out.println("Potentially Fatal Deserialization Operation. " + e);
                    //KLoggerUtils.INSTANCE.e("serialVersionUID不一致：\t " + e.getMessage(),true);
                    resultClassDescriptor = localClassDescriptor; // Use local class descriptor for deserialization
                }
            }
        } catch (Exception e) {
            KLoggerUtils.INSTANCE.e("readClassDescriptor异常：\t " + KCatchException.getExceptionMsg(e), true);
        }
        return resultClassDescriptor;
    }


}
