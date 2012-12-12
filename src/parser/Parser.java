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
        if (getType() != type) {
            throw new ParserException(getToken(), error_msg);
        }
        popToken();
    }
    
    void eatToken(String s, String error_msg) throws Exception {
        next();
        if (getToken().getText() != s) {
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
        return Util.isIn(s, "int", "void", "double", "struct");
    }
    
    boolean isDeclarator() {
        return Util.isIn(getType(), TokenType.STAR, TokenType.L_PARENTHESIS, TokenType.VAR);
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
        if (Util.isIn(getType(), TokenType.STAR, TokenType.DIV, TokenType.MOD)) {
            Token t = getToken();
            popToken();
            Node r_node = parseMultiplicativeExpr(lvl + 1, can_be_null);
            return new MulOpNode(lvl, t, l_node.incLevel(), r_node);
        }
        return l_node;
    }

    Node parseAdditiveExpr(int lvl, boolean can_be_null) throws Exception{
        Node l_node = parseMultiplicativeExpr(lvl, can_be_null);
        next();
        if (Util.isIn(getType(), TokenType.PLUS, TokenType.MINUS)) {
            Token t = getToken();
            popToken();
            Node r_node = parseAdditiveExpr(lvl + 1, can_be_null);
            return new AddOpNode(lvl, t, l_node.incLevel(), r_node);
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
    
    Node parsePoint(int lvl) throws Exception {
        Node p = new Node(lvl, getToken());
        popToken();
        next();
        while(getType() == TokenType.STAR) {
            p.addChild(new Node(lvl + 1, getToken()));
            popToken();
            next();
        }
        return p;
    }
    
    Node parseParameterList(int lvl) {
        
    }
    
    Node parseDirecDeclaratorFactorized(int lvl) throws Exception {
        TokenType type = getType();
        Token tok = getToken();
        Node inner = null;
        switch (type) {
            case L_BRAKET: {
               eatToken(TokenType.L_BRAKET, "Expected (");
               inner = parseConditionalExpr(lvl + 1, true);
               eatToken(TokenType.R_BRAKET, "Expected )");

            }
            case L_PARENTHESIS: {
                eatToken(TokenType.L_PARENTHESIS, "Expected (");
                if (getType() == TokenType.VAR) {
                    inner = parseIdentiferList(lvl + 1);
                }
                if (getType() != TokenType.R_PARENTHESIS) {
                    inner = parseParameterList(lvl + 1);
                }
                eatToken(TokenType.R_PARENTHESIS, "Expected )");
            }
        }
        return new Node(lvl, tok).addChild(inner).addChild(parseDirecDeclaratorFactorized(lvl + 1));
    }
    
    Node parseDirectDeclarator(int lvl) throws Exception {
        Node node = null;
        Token t = getToken();;
        if (getType() == TokenType.VAR) {
            node = new Node(lvl + 1, t);
        } else {
            eatToken(TokenType.L_PARENTHESIS, "Expected (");
            node = parseDeclarator(lvl + 1);
            eatToken(TokenType.R_PARENTHESIS, "Expected )");
        }
        Node fact = parseDirecDeclaratorFactorized(lvl + 1);
        return new Node(lvl, t).addChild(node).addChild(fact);
    }
    
    Node parseDeclarator(int lvl) throws Exception {
        next();
        Node point = null;
        if (getType() == TokenType.STAR) {
            point = parsePoint(lvl + 1);
        }
        return parseDirectDeclarator(lvl + 1).addChild(point);
    }
    
    Node parseStructDeclarator(int lvl) throws Exception {
        next();
        if(isDeclarator()) {
            Token t = getToken();
            Node decl = parseDeclarator(lvl + 1);
            Node expr = parseConditionalExpr(lvl + 1, true);
            return new Node(lvl, t).addChild(decl).addChild(expr);
        }
        return parseConditionalExpr(lvl, false);
    }
    
    Node parseStructDeclaratorList(int lvl) throws Exception {
        Node l_node = parseStructDeclarator(lvl);
        next();
        if (getType() == TokenType.COMMA) {
            Token t = getToken();
            popToken();
            Node r_node = parseStructDeclaratorList(lvl + 1);
            return new Node(lvl, t).addChild(l_node.incLevel()).addChild(r_node);
        }
        return l_node;
    }
    
    Node parseStructDeclaration(int lvl) throws Exception {
       Token t = getToken();
       Node type = new Node(lvl + 1, t); 
       next();
       while(isTypeToken()) {
           popToken();
           type.addChild(parseTypeSpecifier(lvl + 1));
           next();
       }
       Node decl_list = parseStructDeclaratorList(lvl + 1);
       return new Node(lvl, t).addChild(type).addChild(decl_list);
    }
    
    Node parseStructDeclList(int lvl) throws Exception {
        Node str_decl = new Node(lvl, getToken());
        next();
        while (isTypeToken()) {
            popToken();
            str_decl.addChild(parseStructDeclaration(lvl + 1));
            next();
        }
        return str_decl;
    }
    
    Node parseStructSpecifier(int lvl) throws Exception{
        eatToken("struct", "Expected struct key word");
        if (getToken().getType() == TokenType.VAR) {
            // add to symbol table, check existense;
            Node var = new Node(lvl, getToken());
            popToken();
            next();
            if (getType() == TokenType.L_BRACE) {
                var.addChild(parseStructDeclList(lvl + 1));
                eatToken(TokenType.R_BRACE, "Expected }");
            }
            return var;
        } 
        eatToken(TokenType.L_BRACE, "Expected {");
        Node st = parseStructDeclList(lvl + 1);
        eatToken(TokenType.R_BRACE, "Expected }");
        return st;
    }
    
    Node parseTypeSpecifier(int lvl) throws Exception {
        if (Util.isIn(getToken().getText(), "void", "int", "double")) {
            Token t = getToken();
            return new Node(lvl, t);
        }
        return parseStructSpecifier(lvl);
    }
    
    Node declarationSpec(int lvl) throws Exception {
        Node type = new Node(lvl, getToken());
        next();
        while(isTypeToken(getToken().getText())) {
            type.addChild(parseTypeSpecifier(lvl + 1));
            popToken();
            next();
        }
        return type;
    }
    
    Node initDeclList(int lvl) {
        return null;
    }
    
    Node parseDelaration(int lvl) throws Exception {
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
        return null;
    }
    
    Node parseTypeName(int lvl) {
        //Node l_node = parseSpecifierQualiferList(lvl + 1);
        
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
            if (getType() == TokenType.R_BRACE) {
                return null;
            }
            Node decl = null;
            if (isTypeToken(curr.getText())) {
                decl = parseDelaration(lvl + 1);
            }
            if (getType() == TokenType.R_BRACE) {
                return decl;
            }
            Node stmt = parseStatement(lvl + 1);
            eatToken(TokenType.R_BRACE, "Expected }");
            return new CompoundStmtNode(lvl, curr, decl, stmt);
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
