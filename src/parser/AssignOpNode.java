
package parser;

import lexer.Token;

public class AssignOpNode extends BinOpNode {

    public AssignOpNode(int lvl, Token token, Node left, Node right) {
        super(lvl, token, left, right);
    }

}
