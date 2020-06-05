package cn.oi.klittle.era.entity.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

import cn.oi.klittle.era.utils.KLoggerUtils;

//    companion object{
//          private val serialVersionUID:Long=110L//fixme 实体类里面，这样可以固定serialVersionUID。一定要是 val类型，不可再变。才不会发生改变。
//        }

/**
 * fixme Java序列化--忽略serialVersionUID验证
 */
public class KFakeObjectInputStream extends ObjectInputStream {

    public KFakeObjectInputStream(InputStream in) throws IOException {
        super(in);
    }

    public KFakeObjectInputStream() throws SecurityException, IOException {
        super();
    }

    @Override
    protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
        ObjectStreamClass objInputStream = super.readClassDescriptor();
        Class<?> localClass = Class.forName(objInputStream.getName());
        ObjectStreamClass localInputStream = ObjectStreamClass.lookup(localClass);

        if (localInputStream != null) {
            final long localUID = localInputStream.getSerialVersionUID();
            final long objUID = objInputStream.getSerialVersionUID();
            if (localUID != objUID) {
                return localInputStream;
            }
        }
        return objInputStream;
    }

}
