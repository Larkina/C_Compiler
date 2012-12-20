
package c_compiler;

import Utils.*;
import lexer.Lexer;
import parser.*;

public class C_Compiler {

    public static void main(String[] args) throws Exception {

        String tests = System.getProperty("user.dir") + "\\tests\\parser\\stmt\\";
        String t_n = "07.in";
        try {
        Lexer ll = new Lexer(tests + t_n);
        Parser pp = new Parser(ll);
        pp.parse();
        if(true) {
            return;
        }
       if (args.length == 0){
             System.out.println(" Simple C Compiler S8303a Larkina O.S. 2012.");
             System.out.println("Usage: java -jar C_compiler.java filename");
             return;
           }
       
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
                    Util.drawSemanticTree(n);
                }
            }
       }
       catch (Exception e) {
             System.out.println(e.getMessage());
       }
    }
}
