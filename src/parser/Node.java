
package parser;

import java.util.ArrayList;
import lexer.Token;

public class Node {

    public Node(int lvl, Token token) {
        this.lvl = lvl;
        this.token = token;
    }

    @Override
    public String toString() {
        return "( " + token.getText() + " )";
    }

    public Node addChild(Node child) {
        children.add(child);
        return this;
    }

    Token token;
    int lvl = 0;
    int height = 0;
    ArrayList children = new ArrayList();
}
