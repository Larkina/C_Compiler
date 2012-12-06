
package c_compiler;

import java.io.FileNotFoundException;
import java.io.IOException;
import lexer.Lexer;
import lexer.LexerException;
import parser.*;
import Utils.*;

public class C_Compiler {

    public static void main(String[] args) throws Exception {

        String tests = System.getProperty("user.dir") + "\\tests\\parser\\expr\\";
        String t_n = "10.in";
        Lexer ll = new Lexer(tests + t_n);
        Parser pp = new Parser(ll);
         Node nn = pp.parse();
        Util.drawSymantecTree(nn);
        if (true)
            return;
       if (args.length == 0){
             System.out.println(" Simple C Compiler S8303a Larkina O.S. 2012.");
             System.out.println("Usage: java -jar C_compiler.java filename");
             return;
           }
       try {
            if (args.length == 2) {
                if (args[0] == "-l") {
                        Lexer l = new Lexer(args[1]);
                        while (l.next()){
                            System.out.println(l.getToken().toString());
                        }
                }
                else {
                    Lexer l = new Lexer(args[1]);
                    Parser p = new Parser(l);
                    Node n = p.parse();
                }
            }
       }
       catch (Exception e) {
            System.out.println(e.getMessage());
       }
    }
}
