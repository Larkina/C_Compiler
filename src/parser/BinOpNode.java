
package parser;

import lexer.Token;

public class BinOpNode extends Node{
    
    Node left, right; 
    
    public BinOpNode(int lvl, Token token, Node left, Node right) {
        super(lvl, token);
        this.left = left;
        this.right = right;
    }
    
    @Override
    public Node incLevel() {
        super.incLevel();
        left.incLevel();
        right.incLevel();
        return this;
    }
    
    @Override
    public String toString() {
        return super.toString() + left.toString() + right.toString();
    }
}
