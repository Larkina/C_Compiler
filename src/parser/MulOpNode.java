
package parser;

import lexer.Token;

public class MulOpNode extends BinOpNode {

    public MulOpNode(int lvl, Token token, Node left, Node right) {
        super(lvl, token, left, right);
    }
    
}
