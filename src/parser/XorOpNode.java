
package parser;

import lexer.Token;

public class XorOpNode extends BinOpNode{

    public XorOpNode(int lvl, Token token, Node left, Node right) {
        super(lvl, token, left, right);
    }
    
}
