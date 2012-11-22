
package Utils;

public class Util {

    public static <T> boolean isIn(T t, T... ts) {
        for(T i: ts) {
            if (t.equals(i)) {
                return true;
            }
        }
        return false;
    }

}
