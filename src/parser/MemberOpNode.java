
package parser;

import lexer.Token;

public class MemberOpNode extends BinOpNode {

    public MemberOpNode(int lvl, Token token, Node left, Node right) {
        super(lvl, token, left, right);
    }

}
