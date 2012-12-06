
package parser;

import lexer.Token;

public class UnaryOpNode extends Node{
    
    Node arg;

    public UnaryOpNode(int lvl, Token token, Node arg) {
        super(lvl, token);
        this.arg = arg;
    }
    
}
