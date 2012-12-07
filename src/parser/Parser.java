package parser;

import Utils.Util;
import java.util.LinkedList;
import java.util.Queue;
import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;

public class Parser {

    Lexer lex;

    public Parser(Lexer l){
        lex = l;
    }

    boolean buf = false;
    LinkedList<Token> q = new LinkedList<>();

    void next() throws Exception {
        lex.next();
        q.add(lex.getToken());
    }

    Token getToken() {
        return q.getFirst();
    }

    void popToken() {
        q.poll();
    }

    Token next(int idx) {
        return q.get(idx);
    }
    
    TokenType getType() {
        return getToken().getType();
    }
    
    void eatToken(TokenType type, String error_msg) throws Exception{
        next();
        if (getType() == type) {
            popToken();
        }
        else {
            throw new ParserException(getToken(), error_msg);
        }
    }
    
    void eatToken(String s, String error_msg) throws Exception {
        next();
        if (getToken().getText() == s) {
            popToken();
        }
        else {
            throw new ParserException(getToken(), error_msg);
        }        
    }

    boolean isUnaryOp(TokenType t) {
        return Util.isIn(t, TokenType.AND_B, TokenType.MUL, TokenType.PLUS,
            TokenType.MINUS, TokenType.NOT_B, TokenType.NOT);
    }

    boolean isAssigmentOp(TokenType t) {
        return Util.isIn(t, TokenType.ASSIGN, TokenType.MUL_ASSIGN, TokenType.DIV_ASSIGN,
                TokenType.MOD_ASSIGN, TokenType.PLUS_ASSIGN, TokenType.MINUS_ASSIGN,
                TokenType.SHL_ASSIGN, TokenType.SHR_ASSIGN, TokenType.OR_ASSIGN,
                TokenType.AND_ASSIGN, TokenType.XOR_ASSIGN);
    }

    boolean isTypeToken(String t) {
        return Util.isIn(t, "int", "void", "double", "struct");
    }
    
    Node parsePrimaryExpr(int lvl) throws Exception {
         next();
         TokenType type = getType();
        switch (type) {
            case L_PARENTHESIS: {
                popToken();
                Node l_node = parseExpr(lvl);
                eatToken(TokenType.R_PARENTHESIS, "Unclosed parenthesis");
                return l_node;
            }
            case VAR: case CHAR_CONST: case STRING: case INT: case FLOAT: {
                Token t = getToken();
                popToken();
                return new Node(lvl, t);
            }
            default: {
                throw new ParserException(getToken(), "Excpected primary expr");
            }
        }

    }

    Node parseArgumentExprtList(int lvl) throws Exception {
        Node l_node = parseAssignExpr(lvl);
        next();
        if (getType() == TokenType.COMMA) {
            Node r_node = parseArgumentExprtList(lvl + 1);
            popToken();
            return new Node(lvl, getToken()).addChild(l_node.incLevel()).addChild(r_node);
        }
        return l_node;
    }

    Node parsePostfixFactorized(int lvl, Node left) throws Exception {
        TokenType type = getType();
        switch (type) {
            case L_BRAKET: {
                popToken();
                Token t = getToken();
                Node l_node = parseExpr(lvl + 1);
                Node res = new IndexOpNode(lvl, t, left, l_node);
                eatToken(TokenType.R_BRAKET, "Unclosed braket");
                Node r_node = parsePostfixFactorized(lvl, res);
                if (r_node != null) {
                    res.incLevel();
                    return r_node;
                }
                return res;
            }
            case L_PARENTHESIS: {
                popToken();
                next();
                Node l_node, r_node;
                if (getType() != TokenType.R_PARENTHESIS) {
                    l_node = parseArgumentExprtList(lvl + 1);
                    eatToken(TokenType.R_PARENTHESIS, "Unclosed parenthesis");
                    next();
                    Token t = getToken();
                    Node res = new ArgExprListNode(lvl, t, left, l_node);
                    r_node = parsePostfixFactorized(lvl, res);
                    if (r_node != null) {
                        res.incLevel();
                        return r_node;
                    }
                    return res;
                }
                else {
                    Token t = getToken();
                    popToken();
                    Node res = new ArgExprListNode(lvl, t, left, null);
                    r_node =  parsePostfixFactorized(lvl, res);
                    if (r_node != null) {
                        res.incLevel();
                        return r_node;
                    }
                    return res;
                }
            }
            case POINT: case ARROW: {
                Token t = getToken();
                popToken();
                Token arg = getToken();
                eatToken(TokenType.VAR, "Expected identifier");
                Node l_node = new Node(lvl + 1, arg);
                Node res = new MemberOpNode(lvl, t, left, l_node);
                Node r_node = parsePostfixFactorized(lvl, res); 
                if (r_node != null) {
                    res.incLevel();
                    return r_node;
                }
                return res;
            }
            case INC: case DEC: {
                Token t = getToken();
                Node res = new PostfixOpNode(lvl, t, left);
                popToken();
                Node r_node = parsePostfixFactorized(lvl, res);
                if (r_node != null ){
                    res.incLevel();
                    return r_node;
                }
                return res;
            }
        }
        return null;
    }

    Node parsePostfixExpr(int lvl) throws Exception {
        Node l_node = parsePrimaryExpr(lvl);
        next();
        if (Util.isIn(getType(), TokenType.L_BRAKET, TokenType.L_PARENTHESIS,
                TokenType.POINT, TokenType.ARROW, TokenType.INC, TokenType.DEC)) {
            Token t = getToken();
            Node r_node = parsePostfixFactorized(lvl, l_node.incLevel());
            return r_node;
        }
        return l_node;
    }
    
    Node parseCastExpr(int lvl) throws Exception{
        next();
        if ((getType() == TokenType.L_PARENTHESIS) && isTypeToken(next(1).getText())) {
            popToken();
            parseTypeName(lvl + 1);
            eatToken(TokenType.R_PARENTHESIS, "Unclosed parenthesis");
            parseCastExpr(lvl + 1);
            return null;
        } 
        else {
            return parseUnaryExpr(lvl);    
        }
    }

    Node parseUnaryExpr(int lvl) throws Exception {
        next();
        if (isUnaryOp(getType())) {
            Token t = getToken();
            popToken();
            return new UnaryOpNode(lvl, t, parseCastExpr(lvl + 1));
        } else
            if (Util.isIn(getType(), TokenType.DEC, TokenType.INC)) {
                Token t = getToken();
                popToken();
                return new IncOpNode(lvl, t, parseUnaryExpr(lvl + 1));
            }
            else {
                return parsePostfixExpr(lvl);
            }
    }

    Node parseMultiplicativeExpr(int lvl) throws Exception {
        Node l_node = parseCastExpr(lvl);
        next();
        if (Util.isIn(getType(), TokenType.MUL, TokenType.DIV, TokenType.MOD)) {
            Token t = getToken();
            popToken();
            Node r_node = parseMultiplicativeExpr(lvl + 1);
            return new MulOpNode(lvl, t, l_node.incLevel(), r_node);
        }
        return l_node;
    }

    Node parseAdditiveExpr(int lvl) throws Exception{
        Node l_node = parseMultiplicativeExpr(lvl);
        next();
        if (Util.isIn(getType(), TokenType.PLUS, TokenType.MINUS)) {
            Token t = getToken();
            popToken();
            Node r_node = parseAdditiveExpr(lvl + 1);
            return new AddOpNode(lvl, t, l_node.incLevel(), r_node);
        }
        return l_node;
    }

    Node parseShiftExpr(int lvl) throws Exception {
        Node l_node = parseAdditiveExpr(lvl);
        next();
        if (Util.isIn(getType(), TokenType.SHL, TokenType.SHR)) {
            Token t = getToken();
            popToken();
            Node r_node = parseShiftExpr(lvl + 1);
            return new ShiftOpNode(lvl, t, l_node.incLevel(), r_node);
        }
        return l_node;
    }

    Node parseRelExpr(int lvl) throws Exception {
        Node l_node = parseShiftExpr(lvl);
        next();
        if (Util.isIn(getType(), TokenType.LE, TokenType.LT, TokenType.GE, TokenType.GT)) {
            Token t = getToken();
            popToken();
            Node r_node = parseRelExpr(lvl + 1);
            return new RelOpNode(lvl, t, l_node.incLevel(), r_node);
        }
        return l_node;
    }

    Node parseEqExpr(int lvl) throws Exception {
        Node l_node = parseRelExpr(lvl);
        next();
        if (Util.isIn(getType(), TokenType.EQ, TokenType.NE)) {
            Token t = getToken();
            popToken();
            Node r_node = parseEqExpr(lvl + 1);
            return new EqNode(lvl, t, l_node.incLevel(), r_node);
        }
        return l_node;
    }

    Node parseAndExpr(int lvl) throws Exception {
        Node l_node = parseEqExpr(lvl);
        next();
        if (getType() == TokenType.AND_B) {
            Token t = getToken();
            popToken();
            Node r_node = parseAndExpr(lvl + 1);
            return new BiAndOpNode(lvl, t, l_node, r_node);
        }
        return l_node;
    }

    Node parseExclusiveOrExpr(int lvl) throws Exception {
        Node l_node = parseAndExpr(lvl);
        next();
        if (getType() == TokenType.XOR) {
            Token t = getToken();
            popToken();
            Node r_node = parseExclusiveOrExpr(lvl + 1);
            return new XorOpNode(lvl, t, l_node.incLevel(), r_node);
        }
        return l_node;
    }

    Node parseInclusiveOrExpr(int lvl) throws Exception{
        Node l_node = parseExclusiveOrExpr(lvl);
        next();
        if (getType() == TokenType.OR_B) {
            Token t = getToken();
            popToken();
            Node r_node = parseInclusiveOrExpr(lvl + 1);
            return new IncOrOpNode(lvl, t, l_node.incLevel(), r_node);
        }
        return l_node;
    }

    Node parseLogicalAndExpr(int lvl)throws Exception {
        Node l_node = parseInclusiveOrExpr(lvl);
        next();
        if (getType() == TokenType.AND) {
            Token t = getToken();
            popToken();
            Node r_node = parseLogicalAndExpr(lvl + 1);
            return new AndOpNode(lvl, t, l_node.incLevel(), r_node);
        }
        return l_node;
    }

    Node parseLogicalOrExpr(int lvl) throws Exception {
        Node l_node = parseLogicalAndExpr(lvl);
        next();
        if (getType() == TokenType.OR) {
            Token t = getToken();
            popToken();
            Node r_node = parseLogicalOrExpr(lvl + 1);
            return new OrOpNode(lvl, t, l_node.incLevel(), r_node);
        }
        return l_node;
    }

    Node parseConditionalExpr(int lvl) throws Exception {
        Node l_node = parseLogicalOrExpr(lvl);
        next();
        if (getType() == TokenType.QUESTION) {
            Node middle, end;
            Token t = getToken();
            popToken();
            middle = parseExpr(lvl + 1);
            eatToken(TokenType.COLON, "Expected colon");
            end = parseConditionalExpr(lvl + 1);
            return new TernaryOpNode(lvl, t, l_node.incLevel(), middle, end);
        }
        return l_node;
    }
    
    Node parseAssignExpr(int lvl) throws Exception {
        Node l_node = parseConditionalExpr(lvl);       
        next();
        if (isAssigmentOp(getType())) {
            Token curr_tok = getToken();
            popToken();
            Node r_node = parseAssignExpr(lvl + 1);
            return new AssignOpNode(lvl, curr_tok, l_node.incLevel(), r_node);
        }
        return l_node;
    }

    Node parseExpr(int lvl) throws Exception {
        Node l_node = parseAssignExpr(lvl);
        next();
        if (getType() == TokenType.COMMA) {
            Token curr = getToken();
            popToken();
            Node r_node = parseExpr(lvl + 1);
            return new Node(lvl, curr).addChild(l_node.incLevel()).addChild(r_node);
        }
        return l_node;
    }

    /*void delaration(int lvl) throws Exception {
        Node l_node = declarationSpec(lvl);
        next();
        if (getType() == TokenType.SEMICOLON) {
            popToken();
            return l_node;
        }
        Node r_node = initDeclList(lvl + 1);
        next();
        if (getType() == TokenType.SEMICOLON) {
            popToken();
        } else {
            throw new ParserException(getToken(), "Forgot ;");
        }
        //
    }*/
    
    Node parseTypeName(int lvl) {
        //Todo: decl
        return null;
    }
    
    Node parseStatement(int lvl){
        return null;
    }
    
    Node jmpStatement(int lvl) throws Exception{
        next();
        if (Util.isIn(getToken().getText(), "continue", "break")) {
            Token tok = getToken();
            popToken();
            eatToken(TokenType.SEMICOLON, "Forgot ;");
            return new Node(lvl, tok);
        }
        if (getToken().getText() == "return") {
            Token tok = getToken();
            popToken();
            next();
            if (getType() == TokenType.SEMICOLON) {
                return new Node(lvl, getToken());
            }
            Node l_node = parseExpr(lvl + 1);
            eatToken(TokenType.SEMICOLON, "Forgot ;");
            return new Node(lvl, tok).addChild(l_node);
        }
        return null;
    }
    
    Node compoundStatement(int lvl) throws Exception {
        next();
        if (getType() == TokenType.L_BRACE) {
            popToken();
            next();
            Token curr = getToken();
            if (curr.getType() == TokenType.R_BRACE) {
                return null;
            }
            
        }
        return null;
    }
    
    Node exprStatement(int lvl) throws Exception {
        next();
        if (getType() == TokenType.SEMICOLON) {
            popToken();
            return null;
        }
        Node l_node = parseExpr(lvl);
        next();
        eatToken(TokenType.SEMICOLON, "Forogot ;");
        return l_node;
    }
    
    Node selectionSatement(int lvl) throws Exception {
        next();
        if (getToken().getText() == "if") {
            Token tok = getToken();
            popToken();
            eatToken(TokenType.L_PARENTHESIS, "Excpected (");
            Node cond = parseExpr(lvl + 1);
            eatToken(TokenType.R_PARENTHESIS, "Expected )");
            Node stmt = null;//= parseStatement(lvl + 1);
            next();
            Node el = null;
            if (getToken().getText() == "else") {
                popToken();
                el = parseStatement(lvl + 1);
            }
            return new IfStmtNode(lvl, tok, cond, stmt, el);
        }
        return null;
    }
    
    Node iterationStatement(int lvl) throws Exception {
        next();
        switch (getToken().getText()) {
            case "while": {
                Token t = getToken();
                popToken();
                eatToken(TokenType.L_PARENTHESIS, "Expected (");
                Node cond = parseExpr(lvl + 1);
                eatToken(TokenType.R_PARENTHESIS, "Unclosed parenthesis");
                Node body = parseStatement(lvl + 1);
                return new WhileStmtNode(lvl, t, cond, body);
            }
            case "do": {
                Token t = getToken();
                popToken();
                Node stmt = parseStatement(lvl + 1);
                eatToken("while", "Expected while");
                eatToken(TokenType.L_PARENTHESIS, "Expected (");
                Node cond = parseExpr(lvl + 1);
                eatToken(TokenType.R_PARENTHESIS, "Unclosed parenthesis");
                eatToken(TokenType.SEMICOLON, "Forgot ;");
                return new DoWhileStmtNode(lvl, t, cond, stmt);
            }
            case "for": {
                Token t = getToken();
                popToken();
                eatToken(TokenType.L_PARENTHESIS, "Expected (");
                Node var = null, cond = null, act = null;
                if (getType() == TokenType.SEMICOLON) {
                   popToken(); 
                }
                var = parseExpr(lvl + 1);
                eatToken(TokenType.SEMICOLON, "Expected ;");
                if (getType() == TokenType.SEMICOLON) {
                    popToken();
                }
                cond = parseExpr(lvl + 1);
                eatToken(TokenType.SEMICOLON, "Expected ;");
                if (getType() == TokenType.R_PARENTHESIS) {
                    popToken();
                }
                act = parseExpr(lvl + 1);
                eatToken(TokenType.R_PARENTHESIS, "Expected )");
                Node stmt = parseStatement(lvl + 1);
                return new ForStmtNode(lvl, t, var, cond, act, stmt);
            }
        }
        return null;
    }
    
    public Node parse() throws Exception {
        return parseExpr(0);
    }
}
