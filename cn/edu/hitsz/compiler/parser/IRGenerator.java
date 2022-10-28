package cn.edu.hitsz.compiler.parser;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.ir.IRImmediate;
import cn.edu.hitsz.compiler.ir.IRVariable;
import cn.edu.hitsz.compiler.ir.Instruction;
import cn.edu.hitsz.compiler.lexer.Token;
import cn.edu.hitsz.compiler.lexer.TokenKind;
import cn.edu.hitsz.compiler.parser.table.Production;
import cn.edu.hitsz.compiler.parser.table.Status;
import cn.edu.hitsz.compiler.symtab.SymbolTable;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

// TODO: 实验三: 实现 IR 生成

/**
 *
 */
public class IRGenerator implements ActionObserver {
    Stack<Symbol> symstack = new Stack<>();
    SymbolTable symtbl;
    List<Instruction> irlist = new ArrayList<>();
    @Override
    public void whenShift(Status currentStatus, Token currentToken) {
        // TODO
//        throw new NotImplementedException();
        Symbol sym = new Symbol(currentToken);
        int typeCode = currentToken.getKind().getCode();
        int idCode = TokenKind.allAllowedTokenKinds().get("id").getCode();
        int intConstCode = TokenKind.allAllowedTokenKinds().get("IntConst").getCode();
        if(typeCode == idCode || typeCode == intConstCode) {
            IRVariable irv = IRVariable.named(currentToken.getText());
            sym.setVal(irv);
        }
        symstack.push(sym);
    }

    @Override
    public void whenReduce(Status currentStatus, Production production) {
        // TODO
//        throw new NotImplementedException();
        List<Symbol> symlist = new LinkedList<>();
        for(int i=0; i<production.body().size(); i++){
            symlist.add(symstack.peek());
            symstack.pop();
        }
        symstack.push(new Symbol(production.head()));
        switch (production.index()){
            case 6 -> { // S -> id = E;
                Symbol id = symlist.get(2);
                Symbol E = symlist.get(0);
                Instruction inst = Instruction.createMov((IRVariable) id.val, E.val);
                irlist.add(inst);
            }
            case 7 -> { // return E;
                Symbol E = symlist.get(0);
                Instruction inst = Instruction.createRet(E.val);
                irlist.add(inst);
            }
            case 8 -> { // E -> E + A;
                Symbol A = symlist.get(0);
                Symbol E1 = symlist.get(2);
                symstack.peek().setVal(IRVariable.temp());
                Symbol E = symstack.peek();
                Instruction inst = Instruction.createAdd((IRVariable)E.val, E1.val, A.val);
                irlist.add(inst);
            }
            case 9 -> { // E -> E - A;
                Symbol A = symlist.get(0);
                Symbol E1 = symlist.get(2);
                symstack.peek().setVal(IRVariable.temp());
                Symbol E = symstack.peek();
                Instruction inst = Instruction.createSub((IRVariable)E.val, E1.val, A.val);
                irlist.add(inst);
            }
            case 10 -> { // E -> A;
                Symbol A = symlist.get(0);
                Symbol E = symstack.peek();
                E.setVal(A.val);
            }
            case 11 -> { // A -> A * B;
                Symbol B = symlist.get(0);
                Symbol A1 = symlist.get(2);
                symstack.peek().setVal(IRVariable.temp());
                Symbol A = symstack.peek();
                Instruction inst = Instruction.createMul((IRVariable)A.val, A1.val, B.val);
                irlist.add(inst);
            }
            case 12 -> { // A -> B;
                Symbol B = symlist.get(0);
                Symbol A = symstack.peek();
                A.setVal(B.val);
            }
            case 13 -> { // B -> ( E );
                Symbol E = symlist.get(1);
                Symbol B = symstack.peek();
                B.setVal(E.val);
            }
            case 14 -> { // B -> id;
                Symbol id = symlist.get(0);
                Symbol B = symstack.peek();
                if(!symtbl.has(id.token.getText())){
                    throw new RuntimeException("Unknown id name");
                }
                IRVariable Bval = IRVariable.named(id.token.getText());
                B.setVal(Bval);
            }
            case 15 -> { // B -> IntConst
                Symbol intConst = symlist.get(0);
                Symbol B = symstack.peek();
                IRImmediate Bval = IRImmediate.of(Integer.parseInt(intConst.token.getText()));
                B.setVal(Bval);
            }
            default -> {
                if(production.index() < 1 || production.index() > 15) {
                    throw new RuntimeException("Unknown production index");
                }
            }
        }
    }


    @Override
    public void whenAccept(Status currentStatus) {
        // TODO
//        throw new NotImplementedException();
        return;
    }

    @Override
    public void setSymbolTable(SymbolTable table) {
        // TODO
//        throw new NotImplementedException();
        symtbl = table;
    }

    public List<Instruction> getIR() {
        // TODO
//        throw new NotImplementedException();
        return irlist;
    }

    public void dumpIR(String path) {
        FileUtils.writeLines(path, getIR().stream().map(Instruction::toString).toList());
    }
}

