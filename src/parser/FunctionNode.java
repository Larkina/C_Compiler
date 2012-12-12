
package parser;

import lexer.Token;

public class FunctionNode extends Node{
    
    Node spec, decl, decl_list, stmt;

    public FunctionNode(int lvl, Token token, Node spec, Node decl, Node decl_list, Node stmt) {
        super(lvl, token);
        this.spec = spec;
        this.decl = decl;
        this.decl_list = decl_list;
        this.stmt = stmt;
    }
    
}
