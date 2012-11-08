package lexer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import lexer.LexerException;


public class Lexer {

    public Lexer(String file_name) throws FileNotFoundException, IOException {
        this.file_name = file_name;
        BufferedReader fi = new BufferedReader(new FileReader(file_name));
        int i = 0;
        String t;
        while ((t = fi.readLine()) != null)
            if (buff.length() == 0)
                buff = t;
            else
                buff += '\n' + t;
        fi.close();

        // Инициализировать меп
        str_to_type.put(";", TokenType.SEMICOLON);
        str_to_type.put(".", TokenType.POINT);
        str_to_type.put(",", TokenType.COMMA);
        str_to_type.put("(", TokenType.L_PARENTHESIS);
        str_to_type.put(")", TokenType.R_PARENTHESIS);
        str_to_type.put("[", TokenType.L_BRAKET);
        str_to_type.put("]", TokenType.R_BRAKET);
        str_to_type.put("{", TokenType.L_BRACE);
        str_to_type.put("}", TokenType.R_BRACE);
        str_to_type.put("/", TokenType.DIV);
        str_to_type.put("/=", TokenType.DIV_ASSIGN);
        str_to_type.put("+", TokenType.PLUS);
        str_to_type.put("-", TokenType.MINUS);
        str_to_type.put("*", TokenType.MUL);
        str_to_type.put("%", TokenType.MOD);
        str_to_type.put("+=", TokenType.PLUS_ASSIGN);
        str_to_type.put("-=", TokenType.MINUS_ASSIGN);
        str_to_type.put("*=", TokenType.MUL_ASSIGN);
        str_to_type.put("%=", TokenType.MOD_ASSIGN);
        str_to_type.put("!", TokenType.NOT);
        str_to_type.put("~", TokenType.NOT_B);
        str_to_type.put("|", TokenType.OR_B);
        str_to_type.put("&", TokenType.AND_B);
        str_to_type.put("^", TokenType.XOR);
        str_to_type.put("!=", TokenType.NE);
        str_to_type.put("&=", TokenType.AND_ASSIGN);
        str_to_type.put("|=", TokenType.OR_ASSIGN);
        str_to_type.put("==", TokenType.EQ);
        str_to_type.put("||", TokenType.OR);
        str_to_type.put("&&", TokenType.AND);
        str_to_type.put("=", TokenType.ASSIGN);
        str_to_type.put("++", TokenType.INC);
        str_to_type.put("--", TokenType.DEC);
        str_to_type.put(">>", TokenType.SHR);
        str_to_type.put("<<", TokenType.SHL);
        str_to_type.put(">>=", TokenType.SHR_ASSIGN);
        str_to_type.put("<<=", TokenType.SHL_ASSIGN);
        str_to_type.put(">=", TokenType.GE);
        str_to_type.put("<=", TokenType.LE);

        key_words.add("break");
        key_words.add("case");
        key_words.add("char");
        key_words.add("continue");
        key_words.add("do");
        key_words.add("double");
        key_words.add("else");
        key_words.add("float");
        key_words.add("for");
        key_words.add("goto");
        key_words.add("if");
        key_words.add("int");
        key_words.add("long");
        key_words.add("return");
        key_words.add("short");
        key_words.add("signed");
        key_words.add("sizeof");
        key_words.add("struct");
        key_words.add("switch");
        key_words.add("unsigned");
        key_words.add("void");
        key_words.add("while");

    }

    String file_name;
    private int line = 0;
    private int pos = 0;
    private int curr_pos = 0;
    private int end_pos;
    private int p = 0;
    private int l = 0;
    String buff = "";
    char curr = 0;
    Token currentToken;
    HashMap<String, TokenType> str_to_type = new HashMap<>();
    HashSet<String> key_words = new HashSet<>();

    private void eatSpace(){
        int tt = curr_pos;
        for(int i = tt; i < buff.length(); ++i){
            curr = buff.charAt(i);
            if (curr == ' ' || curr == '\t' || curr == 0){
                ++p;
                ++curr_pos;
                continue;
            }
           if (curr == '\n'){
                ++l;
                p = 0;
                ++curr_pos;
            }
            else
                break;
        }
    }

    String eatComments() throws LexerException{
        while (curr == '/'){
            // Однострочный комментарий
            if (buff.charAt(curr_pos + 1) == '/'){
                //Захавать всю строку
                ++l;
                p = 0;
                while((curr = buff.charAt(curr_pos++)) != '\n'){}
            }
            else
                //многостночный
                if (buff.charAt(curr_pos + 1) == '*'){
                    // Хаваем до */, вложенные побоку
                    curr_pos += 2;
                    p+= 2;
                    while((curr_pos + 1 < buff.length()) &&
                          (buff.charAt(curr_pos + 1) != '/') && (curr = buff.charAt(curr_pos++)) != '*'){
                        if (curr == '\n'){
                            l++;
                            p = 0;
                        }
                        p++;
                    }
                    curr_pos += 2;
                    p += 2;
                    curr = buff.charAt(curr_pos);
                    if (curr_pos + 1 == buff.length()){
                    // ВСе плохо ничего не нашли скобку не закрыли конец файла
                        throw new LexerException();
                    }
                }
                else
                {
                    if (buff.charAt(curr_pos + 1) == '=')
                        return "/=";
                    else
                        return "/";
                }
        }
        return "";
    }

    public Token getToken(){
        return currentToken;
    }

    public boolean next() throws LexerException{
        l = line;
  //      p = pos;
        curr_pos = pos;
        // Съели пробелы в начале
        if (curr_pos == buff.length()){
            currentToken = new Token<>(p, l + 1, "EOF", "EOF", TokenType.EOF);
            return false;
        }
        eatSpace();
        // Деление или комментарий
        while (curr == '/'){
            String t = eatComments();
            if (t != ""){
               currentToken = new Token<>(p + 1, l + 1, t, t, str_to_type.get(t));
               pos = curr_pos + t.length();
               return true;
            }
            eatSpace();
        }
        /* Если первая буква, то идентификатор /**/
        if (Character.isLetter(curr)) {
            StringBuilder tmp = new StringBuilder();
            do {
                tmp.append(curr);
                if (curr_pos + 1 == buff.length()){
                    curr_pos++;
                    break;
                }
                curr = buff.charAt(++curr_pos);
            } while (Character.isLetterOrDigit(curr));
            String s = tmp.toString();
            line = l;
            pos = curr_pos;
            TokenType type = TokenType.VAR;
            if (key_words.contains(s))
                type = TokenType.KEY_WORD;
            currentToken = new Token<>(p + 1, l + 1, s, s, type);
            return true;
        }


        // Разделители Операции
        switch(curr){
            case ';': case ',': case '.':
            case '[': case ']': case '{': case '}':
            case '(': case ')':
            {
                String s = curr + "";
                TokenType t = str_to_type.get(s);
                currentToken = new Token(p + 1, l + 1, s, s, t);
                line = l;
                pos = curr_pos + 1;
                return true;
            }
            case '+': case '-': case '*': case '%':
            case '~': case '!': case '&': case '|':
            case '=': case '>': case '<': case '^': {
                String to_str = curr + "";
                if (curr_pos + 1 < buff.length()){
                    char next_ch = buff.charAt(curr_pos + 1);
                    String concat = "" + curr + next_ch;
                    // *= +* -=
                    if (next_ch == '='){
                        currentToken = new Token(p + 1, l + 1, concat, concat, str_to_type.get(concat));
                        pos = curr_pos + 2;
                        line = l;
                        return true;
                    }
                    if (next_ch == curr){
                        if (curr == '+' || curr == '-' || curr == '=' || curr == '|' || curr == '&') {
                            currentToken = new Token(p + 1, l + 1, concat, concat, str_to_type.get(concat));
                            pos = curr_pos + 2;
                            return true;
                        }
                        else
                            if (curr == '>' || curr == '<'){
                                if (curr_pos + 2 < buff.length() && buff.charAt(curr_pos + 2) == '='){
                                    currentToken = new Token(p + 1, l + 1, concat + "=", concat + "=", str_to_type.get(concat + "="));
                                    pos = curr_pos + 3;
                                    return true;
                                }
                                currentToken = new Token(p + 1, l + 1, concat, concat, str_to_type.get(concat));
                                pos = curr_pos + 2;
                                return true;
                            }
                    }
                }
                currentToken = new Token(p + 1, l + 1, to_str, to_str, str_to_type.get(to_str));
                line = l;
                pos = curr_pos + 1;
                return true;
            }
        }

        // Строка

        if (curr == '\"'){
            StringBuilder tmp = new StringBuilder();
            tmp.append('\"');
            while (curr_pos + 1  < buff.length() && buff.charAt(curr_pos + 1) != '\"'){
                curr = buff.charAt(++curr_pos);
                if (curr == '\\'){
                    if (curr_pos + 1 == buff.length())
                        break;
                    char tail = buff.charAt(++curr_pos);
                    String res = "" + curr + tail;
                    switch(tail){
                        case '\\': case '\"': case '\'':
                        case 'n': case 'r': case 'b':
                        case 't': case 'f': case 'a':
                        case 'v': case '?': {
                            tmp.append(res);
                            break;
                        }
                    }
                    if (Character.isDigit(tail)){
                        if (curr_pos + 2 == buff.length()){
                            ++curr_pos;
                            break;
                        }
                        char f = buff.charAt(curr_pos + 1);
                        char s = buff.charAt(curr_pos + 2);
                        if (Character.isDigit(f)){
                            res += f;
                            curr_pos ++;
                        }

                         if (Character.isDigit(s)){
                            res += s;
                            curr_pos ++;
                        }

                        tmp.append(res);
                    }
                    if (tail == 'x'){
                        if (curr_pos + 2 == buff.length()){
                            ++curr_pos;
                            break;
                        }
                        char f = buff.charAt(curr_pos + 1);
                        char s = buff.charAt(curr_pos + 2);
                        if (Character.isLetterOrDigit(f))
                            if (Character.toLowerCase(f) > 'f'){
                                // Плохая 16тьи ричная константа
                                throw new LexerException();
                            }
                            else {
                                res += f;
                                curr_pos++;
                            }
                       if (Character.isLetterOrDigit(s))
                            if (Character.toLowerCase(s) > 'f'){
                                // Плохая 16тьи ричная константа
                                throw new LexerException();
                            }
                            else {
                                res += s;
                                curr_pos++;
                            }
                        tmp.append(res);

                    }
                }
                else
                    tmp.append(curr);
            }
            if (curr_pos + 1  == buff.length()){
                // бросить искллючение. мы строковую константу не знакрыли
                throw new LexerException();
            }
            tmp.append("\"");
            line = l;
            pos = curr_pos + 2;
            currentToken = new Token<>(p + 1, l + 1, tmp.toString(), tmp.toString(), TokenType.STRING);
            return true;
        }

        //Числа
       if (Character.isDigit(curr)){
           // 8/16-ти ричные
           if (curr == '0'){
               if (curr_pos + 1 < buff.length() &&
                   Character.toLowerCase(buff.charAt(curr_pos + 1)) == 'x') {
                   // Пробуем получить 16-ти ричное
                        StringBuilder tmp = new StringBuilder();
                        do {
                            tmp.append(curr);
                            if (curr_pos + 1 == buff.length()){
                                curr_pos++;
                                break;
                            }
                            curr = buff.charAt(++curr_pos);
                            curr = Character.toLowerCase(curr);
                        } while (Character.isLetterOrDigit(curr));
                        String s = tmp.toString().substring(2);
                        Integer val = Integer.parseInt(s,  16);
                        line = l;
                        pos = curr_pos;
                        currentToken = new Token<>(p + 1, l + 1, val, "n16", TokenType.INT);
                        return true;
                   }
                else {
                    if (curr_pos + 1 < buff.length() &&
                        Character.isDigit(buff.charAt(curr_pos + 1))){
                        // 8-миричное
                                StringBuilder tmp = new StringBuilder();
                                do {
                                    tmp.append(curr);
                                    if (curr_pos + 1 == buff.length())
                                    {
                                        curr_pos++;
                                        break;
                                    }
                                    curr = buff.charAt(++curr_pos);
                                    curr = Character.toLowerCase(curr);
                                } while (Character.isDigit(curr));
                                String s = tmp.toString();
                                line = l;
                                pos = curr_pos;
                                Integer val = Integer.parseInt(s,  8);
                                currentToken = new Token<>(p + 1, l + 1, val, "n8", TokenType.INT);
                                return true;
                    }
                 }
            }
            TokenType type = TokenType.INT;
            boolean was_point = false;
            boolean was_exp = false;
            boolean was_sign = false;
            StringBuilder tmp = new StringBuilder();
            do {
                tmp.append(curr);
                if (curr_pos + 1 == buff.length())
                {
                   curr_pos++;
                   break;
                }
                curr = buff.charAt(++curr_pos);
                curr = Character.toLowerCase(curr);
                if (curr == '.')
                    if (was_point){
                        // Ошибка, две точки в вещественном числе
                        throw new LexerException();
                    }
                    else
                    was_point = true;
                if (curr == 'e'){
                    if (was_exp || curr_pos + 1 == buff.length()){
                        // Плохое вещественное число
                        throw new LexerException();
                    }
                    char next_ch = buff.charAt(curr_pos + 1);
                    if (next_ch != '-' && next_ch != '+' && !Character.isDigit(next_ch) || was_sign){
                        // Плохая экспонента
                        throw new LexerException();
                    }
                    was_sign = (curr == '+' || curr == '-');
                    was_exp = true;
                }

            } while (Character.isDigit(curr) || (curr == '.' || curr == 'e' || curr == '+' || curr == '-'));
            String s = tmp.toString();
             Double dval;
             Integer val;
            if (was_point || was_exp){
                dval = Double.parseDouble(s);
                currentToken = new Token<>(p + 1, l + 1, dval, "n10", type);
            }
            else {
                val = Integer.parseInt(s,  10);
                currentToken = new Token<>(p + 1, l + 1, val, "n10", type);
            }
            line = l;
            pos = curr_pos;
            return true;
       }
        pos = buff.length();
        line = l;
        currentToken = new Token<>(p + 1, l + 1, "EOF", "EOF", TokenType.EOF);
        return false;
    }

}
