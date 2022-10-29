package cn.edu.hitsz.compiler.asm;

/**
 * 汇编代码操作数中的立即数类
 * **/
public class Imm extends AsmOperand{
    private String value;

    public Imm(String value) {
        this.value = value;
    }

    @Override
    public String toString(){
        return value;
    }
}
