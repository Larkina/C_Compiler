
package parser;

import lexer.Token;

public class PostfixOpNode extends Node {

    Node left;
    
    public PostfixOpNode(int lvl, Token token, Node left) {
        super(lvl, token);
        this.left = left;
    }

    @Override
    public String toString() {
        return super.toString() + left.toString();
    }
    
    
}
