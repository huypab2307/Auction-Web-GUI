public class FactoryItem {

    public static <T> T createBuilder(Class<T> clazz, String name, String desc, double price, String sellerId) {
        if (clazz == Arts.Builder.class) {
            return clazz.cast(new Arts.Builder(name, desc, price, sellerId));
        } else if (clazz == Electronics.Builder.class) {
            return clazz.cast(new Electronics.Builder(name, desc, price, sellerId));
        } else if (clazz == Vehicle.Builder.class){
            return clazz.cast(new Vehicle.Builder(name, desc, price, sellerId));
        }
        throw new IllegalArgumentException("Class không được hỗ trợ: " + clazz.getName());
    }
}
