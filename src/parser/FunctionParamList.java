
package parser;

import lexer.Token;

public class FunctionParamList extends Node {

    public FunctionParamList(int lvl, Token token) {
        super(lvl, token);
    }

    @Override
    public String toString() {
        return getSpace() + "FunctionParamList" + "\n" + childrenToString();
    }
    
    
}
