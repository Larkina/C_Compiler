
package parser;

import lexer.Token;

public class IncOpNode extends UnaryOpNode{

    public IncOpNode(int lvl, Token token, Node arg) {
        super(lvl, token, arg);
    }

}
