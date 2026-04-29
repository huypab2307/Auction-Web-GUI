package JavaGui;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomHelper {
    private static String[] colors = {"(to right, #155799, #159957);",
            "(to right, #1d2671, #c33764);",
            "(to left, #26d0ce, #1a2980);",
            "(to right, #514a9d, #24c6dc);",
            "(to left, #859398, #283048);",
            "(to left, #348ac7, #7474bf);",
            "(to left, #ef9393, #e17dc2, #998ee0, #43add0);",
            "(to left, #00bf72, #008793, #004d7a, #051937);",
            "(to left, #4286f4, #373b44);",
            "(to right, #00c996, #003d4d);",
            "(to right, #24243e, #302b63, #0f0c29);",
            "(to right, #f8ffae, #43c6ac);",
            "(to right, #004e92, #000428);",
            //deep space
            "(to right, #434343, #000000);"};
    private static Random getRand() {
        return ThreadLocalRandom.current();
    }

    public static <T> T randomChoice(T[] array) {
        if (array == null || array.length == 0) return null;
        return array[getRand().nextInt(array.length)];
    }

    public static <T> T randomChoice(List<T> list) {
        if (list == null || list.isEmpty()) return null;
        return list.get(getRand().nextInt(list.size()));
    }

    public static int nextInt(int min, int max) {
        return getRand().nextInt((max - min) + 1) + min;
    }
    public static String randomColorPicker(){
        Random rand = new Random();
        return "-fx-background-color: linear-gradient" + RandomHelper.randomChoice(colors);
    }
}
