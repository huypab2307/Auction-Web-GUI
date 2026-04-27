package Items;
public class FactoryItem {

    public static <T> T createBuilder(Class<T> clazz, String name, String desc, int sellerId, String imagePath) {
        if (clazz == Arts.Builder.class) {
            return clazz.cast(new Arts.Builder(name, desc, sellerId,imagePath));
        } else if (clazz == Electronics.Builder.class) {
            return clazz.cast(new Electronics.Builder(name, desc, sellerId,imagePath));
        } else if (clazz == Vehicle.Builder.class){
            return clazz.cast(new Vehicle.Builder(name, desc, sellerId,imagePath));
        }
        throw new IllegalArgumentException("Class không được hỗ trợ: " + clazz.getName());
    }
}
