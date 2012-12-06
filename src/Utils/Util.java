
package Utils;

import parser.Node;

public class Util {

    public static <T> boolean isIn(T t, T... ts) {
        for(T i: ts) {
            if (t.equals(i)) {
                return true;
            }
        }
        return false;
    }

    public static void drawSymantecTree(Node node) {
 //       for(int i = 0; i < node.getLevel(); ++i)
  //          System.out.print("\t");
        System.out.print(node.toString());
        for(Node i: node.getChildren()) {
            drawSymantecTree(i);
        }
    }

}
