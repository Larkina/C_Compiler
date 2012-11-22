package lexer;

/**
 * @param <T>
 * @param <col>
 * @param <row>
 * @param <val>
 * @param <text>
 * @param <type>
 */
public class Token<T> {

    @Override
    public String toString() {
         return "value=" + value + ", text=" + text + ", type=" + type;
        //return "Token{" + "col=" + col + ", row=" + row + ", value=" + value + ", text=" + text + ", type=" + type + '}';
    }

    public Token(int col, int row, T value, String text, TokenType type) {
        this.col = col;
        this.row = row;
        this.value = value;
        this.text = text;
        this.type = type;
    }

    int col, row;
    T value;
    String text;
    TokenType type;

    public TokenType getType() {
        return type;
    }

    public int getPos() {
        return col;
    }

    public int getLine() {
        return row;
    }

    public String getText() {
        return text;
    }

}
