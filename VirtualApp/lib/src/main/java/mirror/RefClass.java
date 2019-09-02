package mirror;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

public final class RefClass {

    private static HashMap<Class<?>,Constructor<?>> REF_TYPES = new HashMap<Class<?>, Constructor<?>>();
    static {
        try {
            REF_TYPES.put(RefObject.class, RefObject.class.getConstructor(Class.class, Field.class));
            REF_TYPES.put(RefMethod.class, RefMethod.class.getConstructor(Class.class, Field.class));
            REF_TYPES.put(RefInt.class, RefInt.class.getConstructor(Class.class, Field.class));
            REF_TYPES.put(RefLong.class, RefLong.class.getConstructor(Class.class, Field.class));
            REF_TYPES.put(RefFloat.class, RefFloat.class.getConstructor(Class.class, Field.class));
            REF_TYPES.put(RefDouble.class, RefDouble.class.getConstructor(Class.class, Field.class));
            REF_TYPES.put(RefBoolean.class, RefBoolean.class.getConstructor(Class.class, Field.class));
            REF_TYPES.put(RefStaticObject.class, RefStaticObject.class.getConstructor(Class.class, Field.class));
            REF_TYPES.put(RefStaticInt.class, RefStaticInt.class.getConstructor(Class.class, Field.class));
            REF_TYPES.put(RefStaticMethod.class, RefStaticMethod.class.getConstructor(Class.class, Field.class));
            REF_TYPES.put(RefConstructor.class, RefConstructor.class.getConstructor(Class.class, Field.class));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    //通过load调用，mappingClass中的Ref开头类型的字段都被赋值为，和系统中同名字的value，
    public static Class<?> load(Class<?> mappingClass, String className) {
        try {
            return load(mappingClass, Class.forName(className));
        } catch (Exception e) {
            return null;
        }
    }


    public static Class load(Class mappingClass, Class<?> realClass) {
        //这里获取这个类中的所有字段
        Field[] fields = mappingClass.getDeclaredFields();

        for (Field field : fields) {
            try {
                    //只赋值静态字段
                if (Modifier.isStatic(field.getModifiers())) {
                    Constructor<?> constructor = REF_TYPES.get(field.getType());
                    //获取类型为Ref类开头的字段
                    if (constructor != null) {
                        // 设置值（）
                        // constructor.newInstance(realClass, field)，
                        // realClass是系统类，field是mappingClass的自定义字段（和系统名字相同的同名字段），
                        // 例如mirror.android.app.LoadedApk中的
                        // mApplicationInfo(在android.app.LoadedApk中也有mApplicationInfo)

                        // public static Class Class = RefClass.load(LoadedApk.class, "android.app.LoadedApk");
                        // public static RefObject<ApplicationInfo> mApplicationInfo;

                        // 通过构造函数，newInstance返回一个相同名字的系统字段
                        // 详情看Ref开头的类的构造函数，有注释的是RefObject
                        field.set(null, constructor.newInstance(realClass, field));
                    }
                }
            }
            catch (Exception e) {
                // Ignore
            }
        }
        return realClass;
    }

}