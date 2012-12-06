
package parser;

import lexer.Token;

public class UnaryOpNode extends Node{
    
    Node arg;

    public UnaryOpNode(int lvl, Token token, Node arg) {
        super(lvl, token);
        this.arg = arg;
    }

    @Override
    public String toString() {
        return super.toString() + arg.toString();
    }
    
    @Override
    public Node incLevel(){
        super.incLevel();
        arg.incLevel();
        return this;
    }
}
