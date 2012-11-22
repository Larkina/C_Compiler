
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

    @Override
    public int hashCode() {
        return token.hashCode();
    }

    public Node addChild(Node child) {
        if (child != null)
            children.add(child);
        return this;
    }

    public int getLevel() {
        return lvl;
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

    public Node incLevel() {
        lvl++;
        for(Node i: children)
            i.incLevel();
        return this;
    }

    Token token;
    int lvl = 0;
    int height = 0;
    ArrayList<Node> children = new ArrayList();
}
