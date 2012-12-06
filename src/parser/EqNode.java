
package parser;

import lexer.Token;

public class EqNode extends BinOpNode{

    public EqNode(int lvl, Token token, Node left, Node right) {
        super(lvl, token, left, right);
    }

}
