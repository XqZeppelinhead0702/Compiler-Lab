package cn.edu.hitsz.compiler.asm;

/**
 * 汇编代码操作数中的寄存器类
 * **/
public class Reg extends AsmOperand{
    private RegName regName;

    @Override
    public String toString(){
        return regName.toString();
    }

    public Reg(RegName regName) {
        this.regName = regName;
    }

    public RegName getRegName() {
        return regName;
    }
}
