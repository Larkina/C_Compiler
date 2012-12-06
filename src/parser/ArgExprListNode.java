
package parser;

import lexer.Token;

public class ArgExprListNode extends Node{

    Node args, left;
    
    public ArgExprListNode(int lvl, Token token, Node left, Node args) {
        super(lvl, token);
        this.args = args;
        this.left = left;
    }

    @Override
    public String toString() {
        String a = "";
        if (args != null) {
            a = args.toString();
        }
        if (left != null) {
            a = left.toString() + a;
        }
        return getSpace() + "( args )\n" + a;
    }
    
    @Override
    public Node incLevel() {
        super.incLevel();
        left.incLevel();
        args.incLevel();
        return this;
    }
    
}
