
package parser;

import lexer.Token;

public class OrOpNode extends BinOpNode {

    public OrOpNode(int lvl, Token token, Node left, Node right) {
        super(lvl, token, left, right);
    }
    
}
