
package parser;

import lexer.Token;

public class IndexOpNode extends BinOpNode {

    public IndexOpNode(int lvl, Token token, Node left, Node right) {
        super(lvl, token, left, right);
    }

    @Override
    public String toString() {
        return getSpace() + "( [] )\n" + left.toString() + right.toString();
    }

}
