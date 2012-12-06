package parser;

import lexer.Token;

public class AndOpNode extends BinOpNode{

    public AndOpNode(int lvl, Token token, Node left, Node right) {
        super(lvl, token, left, right);
    }
    
}
