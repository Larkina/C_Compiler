
package parser;

import lexer.Token;

public class AddOpNode extends BinOpNode {

    public AddOpNode(int lvl, Token token, Node left, Node right) {
        super(lvl, token, left, right);
    }
    
}
