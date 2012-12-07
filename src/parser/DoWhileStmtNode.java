
package parser;

import lexer.Token;

public class DoWhileStmtNode extends WhileStmtNode {

    public DoWhileStmtNode(int lvl, Token token, Node cond, Node body) {
        super(lvl, token, cond, body);
    }
    
}
