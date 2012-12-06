
package parser;

import lexer.Token;

public class RelOpNode extends BinOpNode {

    public RelOpNode(int lvl, Token token, Node left, Node right) {
        super(lvl, token, left, right);
    }

}
