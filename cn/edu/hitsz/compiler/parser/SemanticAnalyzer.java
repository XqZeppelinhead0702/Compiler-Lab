package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.symtab.SourceCodeType;
import cn.edu.hitsz.compiler.symtab.SymbolTable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

// TODO: 实验三: 实现语义分析
public class SemanticAnalyzer implements ActionObserver {
    // lab3 add
    private final Stack<Symbol> symstack = new Stack<>();
    private SymbolTable symtbl;
    @Override
    public void whenAccept(Status currentStatus) {
        // TODO: 该过程在遇到 Accept 时要采取的代码动作
//        throw new NotImplementedException();
        return;
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // TODO: 该过程在遇到 reduce production 时要采取的代码动作
//        throw new NotImplementedException();
        List<Symbol> temp = new LinkedList<>();
        for(int i=0; i<production.body().size(); i++){
            temp.add(symstack.peek());
            symstack.pop();
        }
        symstack.push(new Symbol(production.head()));
        switch (production.index()){
            case 4 -> { // S -> D id;
                Symbol id = temp.get(0);
                Symbol D = temp.get(1);
                if(!symtbl.has(id.token.getText())){
                    throw new RuntimeException("Unknown id name");
                }
                symtbl.get(id.token.getText()).setType(D.type);
            }
            case 5 -> { // D -> int;
                symstack.peek().setType(temp.get(0).type);
            }
            default -> {
                if(production.index() < 1 || production.index() > 15) {
                    throw new RuntimeException("Unknown production index");
                }
            }
        }
    }

    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO: 该过程在遇到 shift 时要采取的代码动作
//        throw new NotImplementedException();
        Symbol sym = new Symbol(currentToken);
        if(currentToken.getKind().getTermName().equals("int")){
            sym.setType(SourceCodeType.Int);
        }
        symstack.push(sym);
    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO: 设计你可能需要的符号表存储结构
        // 如果需要使用符号表的话, 可以将它或者它的一部分信息存起来, 比如使用一个成员变量存储
//        throw new NotImplementedException();
        symtbl = table;
    }
}

