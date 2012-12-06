
package parser;

import lexer.Token;

public class PostfixOpNode extends BinOpNode {

    public PostfixOpNode(int lvl, Token token, Node left, Node right) {
        super(lvl, token, left, right);
    }

}
