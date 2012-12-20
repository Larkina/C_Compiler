
package parser;

import lexer.Token;

public class FunctionNode extends Node{
    
    Node spec, decl, stmt;

    public FunctionNode(int lvl, Token token, Node spec, Node decl, Node stmt) {
        super(lvl, token);
        this.spec = spec;
        this.decl = decl;
        this.stmt = stmt;
    }

    @Override
    public String toString() {
        String res = "";
        if (spec != null) {
            res += spec.toString();
        }
        if (decl != null) {
            res += decl.toString();
        }
        if(stmt != null) {
            res += stmt.toString();
        }
        return getSpace() + "Function\n" + res;
    }
    
    
    
}
