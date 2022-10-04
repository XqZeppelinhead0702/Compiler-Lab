package cn.edu.hitsz.compiler.lexer;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.stream.StreamSupport;

/**
 * TODO: 实验一: 实现词法分析
 * <br>
 * 你可能需要参考的框架代码如下:
 *
 * @see Token 词法单元的实现
 * @see TokenKind 词法单元类型的实现
 */
public class LexicalAnalyzer {
    private final SymbolTable symbolTable;
    //字符缓冲区
    private ArrayList<String> strbuf = new ArrayList<>();
    //分析出来的Token列表
    private ArrayList<Token> tokenlist = new ArrayList<>();

    public LexicalAnalyzer(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }


    /**
     * 从给予的路径中读取并加载文件内容
     *
     * @param path 路径
     */
    public void loadFile(String path) {
        // TODO: 词法分析前的缓冲区实现
        // 可自由实现各类缓冲区
        // 或直接采用完整读入方法
//        throw new NotImplementedException();
        File file = new File(path);
        //逐行将源程序字符串读入字符缓冲区strbuf中
        try(BufferedReader br = new BufferedReader(new FileReader(file))){
            String line;
            while((line = br.readLine()) != null){
                strbuf.add(line);
            }
        } catch(Exception e){
            e.printStackTrace();
            return;
        }

    }

    /**
     * 执行词法分析, 准备好用于返回的 token 列表 <br>
     * 需要维护实验一所需的符号表条目, 而得在语法分析中才能确定的符号表条目的成员可以先设置为 null
     */
    public void run() {
        // TODO: 自动机实现的词法分析过程
//        throw new NotImplementedException();
        int k;
        String word;
        for(String line : strbuf){
            k = 0;
            for(int i=0; i<line.length();i++){
                if(!Character.isWhitespace(line.charAt(i)) && line.charAt(i) != ';'){
                    continue;
                } else {
                    word = line.substring(k, i);
//                    System.out.println(word);
                    if(TokenKind.isAllowed(word)){
                        //能在coding_map里直接找到的，无具体文本信息
                        tokenlist.add(Token.simple(TokenKind.fromString(word)));
                    } else {
                        boolean flag = true;
                        for(int j=0; j<word.length(); j++){
                            // 利用DFA的思想判断该字符串是数字还是变量名
                            if(Character.isDigit(word.charAt(j))){
                                continue;
                            } else {
                                flag = false;
                                break;
                            }
                        }
                        if(flag){
                            tokenlist.add(Token.normal("IntConst", word));
                        } else {
                            tokenlist.add(Token.normal("id", word));
                            symbolTable.add(word);
                        }
                    }
                    k = i;
                    while(k<line.length() && Character.isWhitespace(line.charAt(k))){
                        k++;
                    }
                    if(k==line.length()-1){
                        if(line.charAt(k) == ';'){
                            //分号特殊处理
                            tokenlist.add(Token.simple("Semicolon"));
                        }
                    }
                }
            }
        }
        tokenlist.add(Token.simple(TokenKind.eof()));
    }

    /**
     * 获得词法分析的结果, 保证在调用了 run 方法之后调用
     *
     * @return Token 列表
     */
    public Iterable<Token> getTokens() {
        // TODO: 从词法分析过程中获取 Token 列表
        // 词法分析过程可以使用 Stream 或 Iterator 实现按需分析
        // 亦可以直接分析完整个文件
        // 总之实现过程能转化为一列表即可
//        throw new NotImplementedException();\
        return tokenlist;
    }

    public void dumpTokens(String path) {
        FileUtils.writeLines(
            path,
            StreamSupport.stream(getTokens().spliterator(), false).map(Token::toString).toList()
        );
    }


}
