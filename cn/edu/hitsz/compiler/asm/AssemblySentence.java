package cn.edu.hitsz.compiler.asm;

import cn.edu.hitsz.compiler.ir.InstructionKind;

/**
 * 汇编语句类，用于构造汇编语句
 * **/
public class AssemblySentence {
    InstructionKind opKind;
    AsmOperand op1;
    AsmOperand op2;
    AsmOperand op3;

    @Override
    public String toString() {
        String opString;
        String rdString;
        String op1String;
        String op2String;
        if(opKind == InstructionKind.MOV){
            if(op2 instanceof Imm){
                opString = "addi";
            } else {
                opString = "add";
            }
            rdString = op1.toString();
            op1String = RegName.x0.toString();
            op2String = op2.toString();
        } else {
            opString = opKind.toString().toLowerCase();
            rdString = op1.toString();
            op1String = op2.toString();
            op2String = op3.toString();
        }
        return "%s %s,%s,%s".formatted(opString, rdString, op1String, op2String);
    }

    // 构造 mov 语句
    public AssemblySentence(InstructionKind opKind, AsmOperand op1,
                            AsmOperand op2) {
        this.opKind = opKind;
        this.op1 = op1;
        this.op2 = op2;
        this.op3 = null;
    }

    // 构造二元运算语句 add, sub, mul
    public AssemblySentence(InstructionKind opKind, AsmOperand op1,
                            AsmOperand op2, AsmOperand op3) {
        this.opKind = opKind;
        this.op1 = op1;
        this.op2 = op2;
        this.op3 = op3;
    }
}
