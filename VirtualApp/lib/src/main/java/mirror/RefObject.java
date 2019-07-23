package mirror;


import com.lody.virtual.VALog;

import java.lang.reflect.Field;

@SuppressWarnings("unchecked")
public class RefObject<T> {
    private Field field;

    public RefObject(Class<?> cls, Field field) throws NoSuchFieldException {
        //这里是load里面调用的构造函数，此时传过来的cls是系统类，然后filed是自定义的和系统名字相同的同名字段
        //这里很机智，因为同名，所以this.field变为了系统字段，机智的一匹啊，写的真好
        this.field = cls.getDeclaredField(field.getName());
        this.field.setAccessible(true);
    }

    public T get(Object object) {
        try {
            return (T) this.field.get(object);
        } catch (Exception e) {
            return null;
        }
    }

    public void set(Object obj, T value) {
        try {
            this.field.set(obj, value);
        } catch (Exception e) {
            //Ignore
        }
    }
}