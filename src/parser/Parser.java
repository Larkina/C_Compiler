package parser;

import Utils.Util;
import java.util.LinkedList;
import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;

public class Parser {

    Lexer lex;

    public Parser(Lexer l){
        lex = l;
    }

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
        if (getType() != type) {
            throw new ParserException(getToken(), error_msg);
        }
        popToken();
    }
    
    void eatToken(String s, String error_msg) throws Exception {
        next();
        if (!getToken().getText().equals(s)) {
            throw new ParserException(getToken(), error_msg);
        }        
        popToken();
    }

    boolean isUnaryOp(TokenType t) {
        return Util.isIn(t, TokenType.AND_B, TokenType.STAR, TokenType.PLUS,
            TokenType.MINUS, TokenType.NOT_B, TokenType.NOT);
    }

    boolean isAssigmentOp(TokenType t) {
        return Util.isIn(t, TokenType.ASSIGN, TokenType.MUL_ASSIGN, TokenType.DIV_ASSIGN,
                TokenType.MOD_ASSIGN, TokenType.PLUS_ASSIGN, TokenType.MINUS_ASSIGN,
                TokenType.SHL_ASSIGN, TokenType.SHR_ASSIGN, TokenType.OR_ASSIGN,
                TokenType.AND_ASSIGN, TokenType.XOR_ASSIGN);
    }

    boolean isTypeToken() {
        return isTypeToken(getToken().getText());
    }

    boolean isTypeToken(String s) {
        return Util.isIn(s, "int", "void", "double", "struct", "char", "float");
    }
    
    boolean isDeclarator() {
        return Util.isIn(getType(), TokenType.L_PARENTHESIS, TokenType.VAR) ||
                ((getType() == TokenType.STAR) && 
                (Util.isIn(next(1).getType(), TokenType.L_PARENTHESIS, TokenType.VAR)));
    }
    
    boolean isAbstractDeclarator() {
        return Util.isIn(getType(), TokenType.STAR, TokenType.R_BRAKET, TokenType.R_PARENTHESIS);
    }
    
    boolean isNextTokenDeclarator() {
        return Util.isIn(next(1).getType(), TokenType.L_PARENTHESIS, TokenType.VAR, TokenType.STAR);
    }
    
    boolean isFunctionDefinition() {
        return (isTypeToken() && isNextTokenDeclarator()) || isNextTokenDeclarator();
    }
    
    boolean isAdditiveOperation() {
        return Util.isIn(getType(), TokenType.PLUS, TokenType.MINUS);
    }
    
    boolean isMultiplicativeOperation() {
        return Util.isIn(getType(), TokenType.STAR, TokenType.DIV, TokenType.MOD);
    }
    
    Node parsePrimaryExpr(int lvl, boolean can_be_null) throws Exception {
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
                if (!can_be_null) {
                    throw new ParserException(getToken(), "Excpected primary expr");
                }
                return null;
            }
        }

    }

    Node parseArgumentExprtList(int lvl, boolean can_be_null) throws Exception {
        Node l_node = parseAssignExpr(lvl);
        next();
        if (getType() == TokenType.COMMA) {
            Node r_node = parseArgumentExprtList(lvl + 1, can_be_null);
            popToken();
            return new Node(lvl, getToken()).addChild(l_node.incLevel()).addChild(r_node);
        }
        return l_node;
    }

    Node parsePostfixFactorized(int lvl, Node left, boolean can_be_null) throws Exception {
        TokenType type = getType();
        switch (type) {
            case L_BRAKET: {
                popToken();
                Token t = getToken();
                Node l_node = parseExpr(lvl + 1);
                Node res = new IndexOpNode(lvl, t, left, l_node);
                eatToken(TokenType.R_BRAKET, "Unclosed braket");
                Node r_node = parsePostfixFactorized(lvl, res, can_be_null);
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
                    l_node = parseArgumentExprtList(lvl + 1, can_be_null);
                    eatToken(TokenType.R_PARENTHESIS, "Unclosed parenthesis");
                    next();
                    Token t = getToken();
                    Node res = new ArgExprListNode(lvl, t, left, l_node);
                    r_node = parsePostfixFactorized(lvl, res, can_be_null);
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
                    r_node =  parsePostfixFactorized(lvl, res, can_be_null);
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
                Node r_node = parsePostfixFactorized(lvl, res, can_be_null); 
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
                Node r_node = parsePostfixFactorized(lvl, res, can_be_null);
                if (r_node != null ){
                    res.incLevel();
                    return r_node;
                }
                return res;
            }
        }
        return null;
    }

    Node parsePostfixExpr(int lvl, boolean can_be_null) throws Exception {
        Node l_node = parsePrimaryExpr(lvl, can_be_null);
        next();
        if (Util.isIn(getType(), TokenType.L_BRAKET, TokenType.L_PARENTHESIS,
                TokenType.POINT, TokenType.ARROW, TokenType.INC, TokenType.DEC)) {
            Token t = getToken();
            Node r_node = parsePostfixFactorized(lvl, l_node.incLevel(), can_be_null);
            return r_node;
        }
        return l_node;
    }
    
    Node parseCastExpr(int lvl, boolean can_be_null) throws Exception{
        next();
        if ((getType() == TokenType.L_PARENTHESIS) && isTypeToken(next(1).getText())) {
            popToken();
            parseTypeName(lvl + 1);
            eatToken(TokenType.R_PARENTHESIS, "Unclosed parenthesis");
            parseCastExpr(lvl + 1, can_be_null);
            return null;
        } 
        else {
            return parseUnaryExpr(lvl, can_be_null);    
        }
    }

    Node parseUnaryExpr(int lvl, boolean can_be_null) throws Exception {
        next();
        if (isUnaryOp(getType())) {
            Token t = getToken();
            popToken();
            return new UnaryOpNode(lvl, t, parseCastExpr(lvl + 1, can_be_null));
        } else
            if (Util.isIn(getType(), TokenType.DEC, TokenType.INC)) {
                Token t = getToken();
                popToken();
                return new IncOpNode(lvl, t, parseUnaryExpr(lvl + 1, can_be_null));
            }
            else {
                return parsePostfixExpr(lvl, can_be_null);
            }
    }

    Node parseMultiplicativeExpr(int lvl, boolean can_be_null) throws Exception {
        Node l_node = parseCastExpr(lvl, can_be_null);
        next();
        while (isMultiplicativeOperation()) {
            Token t = getToken();
            popToken();
            l_node = new MulOpNode(lvl, t, l_node.incLevel(), parseCastExpr(lvl + 1, can_be_null));
        }
        return l_node;
    }

    Node parseAdditiveExpr(int lvl, boolean can_be_null) throws Exception{
        Node l_node = parseMultiplicativeExpr(lvl, can_be_null);
        next();
        while (isAdditiveOperation()) {
            Token t = getToken();
            popToken();
            l_node = new AddOpNode(lvl, t, l_node.incLevel(), parseMultiplicativeExpr(lvl + 1, can_be_null));
            next();
        }
        return l_node;
    }

    Node parseShiftExpr(int lvl, boolean can_be_null) throws Exception {
        Node l_node = parseAdditiveExpr(lvl, can_be_null);
        next();
        if (Util.isIn(getType(), TokenType.SHL, TokenType.SHR)) {
            Token t = getToken();
            popToken();
            Node r_node = parseShiftExpr(lvl + 1, can_be_null);
            return new ShiftOpNode(lvl, t, l_node.incLevel(), r_node);
        }
        return l_node;
    }

    Node parseRelExpr(int lvl, boolean can_be_null) throws Exception {
        Node l_node = parseShiftExpr(lvl, can_be_null);
        next();
        if (Util.isIn(getType(), TokenType.LE, TokenType.LT, TokenType.GE, TokenType.GT)) {
            Token t = getToken();
            popToken();
            Node r_node = parseRelExpr(lvl + 1, can_be_null);
            return new RelOpNode(lvl, t, l_node.incLevel(), r_node);
        }
        return l_node;
    }

    Node parseEqExpr(int lvl, boolean can_be_null) throws Exception {
        Node l_node = parseRelExpr(lvl, can_be_null);
        next();
        if (Util.isIn(getType(), TokenType.EQ, TokenType.NE)) {
            Token t = getToken();
            popToken();
            Node r_node = parseEqExpr(lvl + 1, can_be_null);
            return new EqNode(lvl, t, l_node.incLevel(), r_node);
        }
        return l_node;
    }

    Node parseAndExpr(int lvl, boolean can_be_null) throws Exception {
        Node l_node = parseEqExpr(lvl, can_be_null);
        next();
        if (getType() == TokenType.AND_B) {
            Token t = getToken();
            popToken();
            Node r_node = parseAndExpr(lvl + 1, can_be_null);
            return new BiAndOpNode(lvl, t, l_node, r_node);
        }
        return l_node;
    }

    Node parseExclusiveOrExpr(int lvl, boolean can_be_null) throws Exception {
        Node l_node = parseAndExpr(lvl, can_be_null);
        next();
        if ((getType() == TokenType.XOR)) {
            Token t = getToken();
            popToken();
            Node r_node = parseExclusiveOrExpr(lvl + 1, can_be_null);
            return new XorOpNode(lvl, t, l_node.incLevel(), r_node);
        }
        return l_node;
    }

    Node parseInclusiveOrExpr(int lvl, boolean can_be_null) throws Exception{
        Node l_node = parseExclusiveOrExpr(lvl, can_be_null);
        next();
        if (getType() == TokenType.OR_B) {
            Token t = getToken();
            popToken();
            Node r_node = parseInclusiveOrExpr(lvl + 1, can_be_null);
            return new IncOrOpNode(lvl, t, l_node.incLevel(), r_node);
        }
        return l_node;
    }

    Node parseLogicalAndExpr(int lvl, boolean can_be_null)throws Exception {
        Node l_node = parseInclusiveOrExpr(lvl, can_be_null);
        next();
        if (getType() == TokenType.AND) {
            Token t = getToken();
            popToken();
            Node r_node = parseLogicalAndExpr(lvl + 1, can_be_null);
            return new AndOpNode(lvl, t, l_node.incLevel(), r_node);
        }
        return l_node;
    }

    Node parseLogicalOrExpr(int lvl, boolean can_be_null) throws Exception {
        Node l_node = parseLogicalAndExpr(lvl, can_be_null);
        next();
        if (getType() == TokenType.OR) {
            Token t = getToken();
            popToken();
            Node r_node = parseLogicalOrExpr(lvl + 1, can_be_null);
            return new OrOpNode(lvl, t, l_node.incLevel(), r_node);
        }
        return l_node;
    }

    Node parseConditionalExpr(int lvl, boolean can_be_null) throws Exception {
        Node l_node = parseLogicalOrExpr(lvl, can_be_null);
        next();
        if (getType() == TokenType.QUESTION) {
            Node middle, end;
            Token t = getToken();
            popToken();
            middle = parseExpr(lvl + 1);
            eatToken(TokenType.COLON, "Expected colon");
            end = parseConditionalExpr(lvl + 1, can_be_null);
            return new TernaryOpNode(lvl, t, l_node.incLevel(), middle, end);
        }
        return l_node;
    }
    
    Node parseAssignExpr(int lvl) throws Exception {
        Node l_node = parseConditionalExpr(lvl, false);       
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
        
    String parseDeclarator(int lvl) throws Exception {
        next();
        String res = "";
        while (getType() == TokenType.STAR) {
            res += " pointer to";
            popToken();
            next(); 
        }
        String arg = "";
        if (getType() == TokenType.L_PARENTHESIS) {
            eatToken(TokenType.L_PARENTHESIS, "");
            arg = " " + parseInitDeclarator(lvl + 1, "");
            eatToken(TokenType.R_PARENTHESIS, "Expected )");
        }
        String var = "";
        if (getType() == TokenType.VAR) {
            var = getToken().getText() + " is";
            popToken();
        }
        String arr = "";
        while (getType() == TokenType.L_BRAKET) {
            popToken();
            next();
            String add = "";
            if (getType() != TokenType.R_BRAKET) {
                add = parseConditionalExpr(lvl, false).toString();
            }
            popToken();
            arr = "\n" + add + "array of" + arr;
        }
        return var + arg + arr + res;
    }
    
    String parseStructSpecifier(int lvl) throws Exception {
        eatToken("struct", ":P");
        String res = "struct ";
        if(getType() == TokenType.VAR) {
            res = getToken().getText() + " " + res;
            popToken();
        }
        else {
            res = "anonymous " + res;
        }
        eatToken(TokenType.L_BRACE, "Expected {");
        res += "with members { \n" + parseDeclarationList(lvl) + "\n}";
        eatToken(TokenType.R_BRACE, "Expected }");
        return res;
    }
    
    String parseDeclarationSpecifiers(int lvl) throws Exception {
        next();
        if ("struct".equals(getToken().getText())) {
            return parseStructSpecifier(lvl);
        }
        String res = getToken().getText();
        popToken();
        return res;
    }
    
    Node parseInitializerList(int lvl) throws Exception {
        Node l_node = parseInitializer(lvl);
        next();
        if (getType() == TokenType.COMMA) {
            Token t = getToken();
            popToken();
            Node r_node = parseInitializerList(lvl + 1);
            return new Node(lvl, t).addChild(l_node.incLevel()).addChild(r_node);
        }
        return l_node;
    }
    
    Node parseInitializer(int lvl) throws Exception {
        next();
        if (getType() == TokenType.L_BRACE) {
            popToken();
            Node l_node = parseInitializerList(lvl);
            next();
            if (getType() == TokenType.COMMA) {
                popToken();
            }
            return l_node;
        }
        return parseAssignExpr(lvl);
    }
    
    String parseInitDeclarator(int lvl, String type) throws Exception {
        String res = "";
        while (getType() == TokenType.STAR) {
            res += "pointer to ";
            popToken();
            next();
        }       
        String l_node = parseDeclarator(lvl);
        next();
        String arg = "";
        if (getType() == TokenType.L_PARENTHESIS) {
            eatToken(TokenType.L_PARENTHESIS, "e");
            int count = 0;
            next();
            while(isTypeToken()) {
                ++count;
                String t = getToken().getText();
                popToken();
                arg += (count > 1 ? ", " : "") + parseInitDeclarator(lvl, t);
                next();
                if (getType() == TokenType.COMMA) {
                    popToken();
                    next();
                }
            }
            if (count > 0) {
                arg = "passing " + arg;
            }
            arg = "function " + arg + "returning ";
            eatToken(TokenType.R_PARENTHESIS, "expected )");
        }
        
        if (getType() == TokenType.ASSIGN) {
            popToken();
            Node r_node = parseInitializer(0);
            return l_node + " " + arg + res + type + " and init with\n" +  r_node.toString();
        }
        return l_node + " " + arg + res + type + " ";
    }
    
    String parseInitDeclaratorlList(int lvl, String type) throws Exception {
        String l_node = parseInitDeclarator(lvl, type);
        next();
        if (getType() == TokenType.COMMA) {
            Token t = getToken();
            popToken();
            String r_node = parseInitDeclaratorlList(lvl + 1, type);
            return l_node + "\n" + r_node;
        }
        return l_node;
    }
    
    String parseDeclaration(int lvl, boolean should_be_semicolon) throws Exception {
        String l_node = parseDeclarationSpecifiers(lvl);
        next();
        if (getType() == TokenType.SEMICOLON) {
            popToken();
            return l_node.toString();
        }
        String r_node = parseInitDeclaratorlList(0, l_node);
        next();
        if (should_be_semicolon) {
            if (getType() != TokenType.SEMICOLON) {
                throw new ParserException(getToken(), "Forgot ;");
            } else {
                popToken();
            }
        }
        return r_node;
    }
    
    Node parseTypeName(int lvl) throws Exception {
        next();
        Node type = new Node(lvl, getToken());
        popToken();
        next();
        while(isTypeToken()) {
            type.addChild(new Node(lvl, getToken()));
            popToken();
            next();
        }
        return type;
    }
    
    Node parseStatement(int lvl) throws Exception{
        switch (getToken().getText()) {
            case "if": {
                return parseSelectionSatement(lvl);
            } 
            case "while": case "do": case "for": {
                return parseIterationStatement(lvl);
            }
            case "continue": case "break": case "return": {
                return parseJumpStatement(lvl);
            }
            case "{": {
                return parseCompoundStatement(lvl);
            }    
            default: {
                return parseExprStatement(lvl);
            }
        }
    }
    
    Node parseJumpStatement(int lvl) throws Exception{
        next();
        if (Util.isIn(getToken().getText(), "continue", "break")) {
            Token tok = getToken();
            popToken();
            eatToken(TokenType.SEMICOLON, "Forgot ;");
            return new Node(lvl, tok);
        }
        if ("return".equals(getToken().getText())) {
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
    
    Node parseCompoundStatement(int lvl) throws Exception {
        eatToken(TokenType.L_BRACE, "Expected {");
        Token curr = getToken();
        if (getType() == TokenType.R_BRACE) {
            return null;
        }
        String decl = "";
        if (isTypeToken(curr.getText())) {
            decl = parseDeclaration(lvl, true);
        }
        if (getType() == TokenType.R_BRACE) {
            return null;
        }
        Node stmt = new Node(lvl, curr);
        while (getType() != TokenType.R_BRACE) {
            stmt.addChild(parseStatement(lvl));
            next();
        }
        eatToken(TokenType.R_BRACE, "Expected }");
        return new CompoundStmtNode(lvl, curr, null, stmt);
    }
    
    Node parseExprStatement(int lvl) throws Exception {
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
    
    Node parseSelectionSatement(int lvl) throws Exception {
        Token tok = getToken();
        eatToken("if", "Expected if");
        eatToken(TokenType.L_PARENTHESIS, "Expected (");
        Node cond = parseExpr(lvl + 1);
        eatToken(TokenType.R_PARENTHESIS, "Expected )");
        Node stmt = parseStatement(lvl + 1);
        next();
        Node el = null;
        if ("else".equals(getToken().getText())) {
            popToken();
            el = parseStatement(lvl + 1);
        }
        return new IfStmtNode(lvl, tok, cond, stmt, el);
    }
    
    Node parseIterationStatement(int lvl) throws Exception {
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
    
    String parseDeclarationList(int lvl) throws Exception {
        String decl = parseDeclaration(lvl, true);
        next();
        while (isTypeToken()) {
            decl += "\n" + parseDeclaration(lvl, true);
        }
        return decl;        
    }
    
    Node parseExternalDeclaration(int lvl) throws Exception{
        next();
        String res = parseDeclaration(lvl, false);
        next();
        Node body = null; 
        if (getType() == TokenType.L_BRACE) {
            body = parseCompoundStatement(lvl);
            if (body != null){
                res += "with body\n" + body.toString();
            }
        }
        else
            if (getType() != TokenType.SEMICOLON) {
                throw new ParserException(getToken(), "Expected ;");
            } else {
                eatToken(TokenType.SEMICOLON, "");
            }
        System.out.println(res);
        return body;
    }
     
    Node parseTraslationUnit(int lvl) throws Exception {
        Node decl = parseExternalDeclaration(lvl);
        while (isTypeToken()) {
            decl.addChild(parseExternalDeclaration(lvl));
        }
        return decl;
    }
    
    public Node parse() throws Exception {
       // return parseExpr(0);
        return parseTraslationUnit(0);
    }
}
