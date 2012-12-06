
package parser;

import java.util.ArrayList;
import java.util.Objects;
import lexer.Token;

public class Node {

    public Node(int lvl, Token token) {
        this.lvl = lvl;
        this.token = token;
    }

    String getSpace() {
        String res = "";
        for(int i = 0; i < lvl; ++i) {
            res += "\t";
        }
        return res;
    }
    
    String childrenToString() {
        String ch = "";
        for(Node i: children) {
            ch += i.toString();
        }    
        return ch;
    }
    
    @Override
    public String toString() {
        return getSpace() + "( " + token.getText() + " )\n";// + childrenToString();
    }

    @Override
    public int hashCode() {
        return token.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Node other = (Node) obj;
        if (!Objects.equals(this.token, other.token)) {
            return false;
        }
        if (this.lvl != other.lvl) {
            return false;
        }
        return true;
    }

    public Node addChild(Node child) {
        if (child != null) { 
            children.add(child);
        }
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
        for(Node i: children) {
            i.incLevel();
        }
        return this;
    }

    Token token;
    int lvl = 0;
    int height = 0;
    ArrayList<Node> children = new ArrayList();
}
