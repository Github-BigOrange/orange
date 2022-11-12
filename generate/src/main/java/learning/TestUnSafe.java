package learning;

import sun.misc.Unsafe;

/**
 * @author 王志成
 * @date 2022年10月28日 9:37
 */
public class TestUnSafe {

    //获取UnSafe的实例(2.2.1)
    static final Unsafe unsafe = Unsafe.getUnsafe();

    //记录变量state在类TestUnSafe中的偏移量(2.2.2)
    static final long stateOffset;

    //变量（2.2.3）
    private volatile long state = 0;

    static {
        try {
            //获取state变量在类TestUnSafe中的偏移量（2.2.4）
            stateOffset = unsafe.objectFieldOffset(TestUnSafe.class.getDeclaredField("state"));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        //创建实例,并且设置state值为1（2.2.5）
        TestUnSafe test = new TestUnSafe();

        //2.2.6
        Boolean success = unsafe.compareAndSwapInt(test,stateOffset,0,1);
        System.out.println(success);
    }
}
