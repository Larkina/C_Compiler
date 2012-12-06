
package parser;

import lexer.Token;

public class BiAndOpNode extends BinOpNode{

    public BiAndOpNode(int lvl, Token token, Node left, Node right) {
        super(lvl, token, left, right);
    }
    
}
