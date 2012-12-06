
package parser;

import lexer.Token;

public class ShiftOpNode extends BinOpNode {

    public ShiftOpNode(int lvl, Token token, Node left, Node right) {
        super(lvl, token, left, right);
    }

}
