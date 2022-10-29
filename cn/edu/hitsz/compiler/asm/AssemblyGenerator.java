package cn.edu.hitsz.compiler.asm;

import cn.edu.hitsz.compiler.NotImplementedException;
import cn.edu.hitsz.compiler.ir.IRValue;
import cn.edu.hitsz.compiler.ir.IRVariable;
import cn.edu.hitsz.compiler.ir.Instruction;
import cn.edu.hitsz.compiler.ir.InstructionKind;
import cn.edu.hitsz.compiler.utils.FileUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * TODO: 实验四: 实现汇编生成
 * <br>
 * 在编译器的整体框架中, 代码生成可以称作后端, 而前面的所有工作都可称为前端.
 * <br>
 * 在前端完成的所有工作中, 都是与目标平台无关的, 而后端的工作为将前端生成的目标平台无关信息
 * 根据目标平台生成汇编代码. 前后端的分离有利于实现编译器面向不同平台生成汇编代码. 由于前后
 * 端分离的原因, 有可能前端生成的中间代码并不符合目标平台的汇编代码特点. 具体到本项目你可以
 * 尝试加入一个方法将中间代码调整为更接近 risc-v 汇编的形式, 这样会有利于汇编代码的生成.
 * <br>
 * 为保证实现上的自由, 框架中并未对后端提供基建, 在具体实现时可自行设计相关数据结构.
 *
 * @see AssemblyGenerator#run() 代码生成与寄存器分配
 */
public class AssemblyGenerator {
    List<Instruction> instList = new ArrayList<>();
    List<AssemblySentence> assemblyList = new ArrayList<>();
    BMap<IRValue, RegName> varRegMap= new BMap<>();

    /**
     * 加载前端提供的中间代码
     * <br>
     * 视具体实现而定, 在加载中或加载后会生成一些在代码生成中会用到的信息. 如变量的引用
     * 信息. 这些信息可以通过简单的映射维护, 或者自行增加记录信息的数据结构.
     *
     * @param originInstructions 前端提供的中间代码
     */
    public void loadIR(List<Instruction> originInstructions) {
        // TODO: 读入前端提供的中间代码并生成所需要的信息
//        throw new NotImplementedException();
        var temp = originInstructions;
        Instruction inst;
        IRValue leftTemp = null;
        IRValue rightTemp = null;
        // 预处理，三元运算立即数单独生成一条MOV语句插到前面
        for(int i=0; i<temp.size(); i++){
            inst = temp.get(i);
            leftTemp = null;
            rightTemp = null;
            if(inst.getKind().isBinary()){
                if(inst.getLHS().isImmediate()){
                    leftTemp = IRVariable.temp();
                    temp.add(i, Instruction.createMov((IRVariable) leftTemp, inst.getLHS()));
                    i++;
                }
                if(inst.getRHS().isImmediate()){
                    rightTemp = IRVariable.temp();
                    temp.add(i, Instruction.createMov((IRVariable) rightTemp, inst.getRHS()));
                    i++;
                }
                temp.add(i, modifyInstImm(inst, leftTemp, rightTemp));
                temp.remove(inst);
            }
        }
        // 存入预处理结果
        for(Instruction instruction : temp){
            instList.add(instruction);
        }
    }


    /**
     * 执行代码生成.
     * <br>
     * 根据理论课的做法, 在代码生成时同时完成寄存器分配的工作. 若你觉得这样的做法不好,
     * 也可以将寄存器分配和代码生成分开进行.
     * <br>
     * 提示: 寄存器分配中需要的信息较多, 关于全局的与代码生成过程无关的信息建议在代码生
     * 成前完成建立, 与代码生成的过程相关的信息可自行设计数据结构进行记录并动态维护.
     */
    public void run() {
        // TODO: 执行寄存器分配与代码生成
//        throw new NotImplementedException();
        Instruction inst;
        for(int i=0; i<instList.size(); i++){
            inst = instList.get(i);
            AssemblySentence asms;
            if(inst.getKind().isBinary()){
//                if(inst.getKind() == InstructionKind.ADD){
//                    System.out.println("here!");
//                }
                IRValue rd = inst.getResult();
                IRValue op1 = inst.getLHS();
                IRValue op2 = inst.getRHS();
                addVarToMapIfNeed(i, rd);
                Reg rdReg = new Reg(varRegMap.getByKey(rd));
                addVarToMapIfNeed(i, op1);
                Reg op1Reg = new Reg(varRegMap.getByKey(op1));
                addVarToMapIfNeed(i, op2);
                Reg op2Reg = new Reg(varRegMap.getByKey(op2));
                if(op1.isImmediate()){
                    asms = new AssemblySentence(inst.getKind(),
                                                rdReg,
                                                op2Reg,
                                                new Imm(op1.toString()));
                } else if(op2.isImmediate()){
                    asms = new AssemblySentence(inst.getKind(),
                                                rdReg,
                                                op1Reg,
                                                new Imm(op2.toString()));
                } else {
                    asms = new AssemblySentence(inst.getKind(),
                                                rdReg,
                                                op1Reg,
                                                op2Reg);
                }
                assemblyList.add(asms);
                continue;
            }
            if(inst.getKind().isUnary()){
                IRValue op1 = inst.getResult();
                IRValue op2 = inst.getFrom();
                addVarToMapIfNeed(i, op1);
                addVarToMapIfNeed(i, op2);
                if(op1.isImmediate()){
                    asms = new AssemblySentence(inst.getKind(),
                                                new Reg(varRegMap.getByKey(op2)),
                                                new Imm(op1.toString()));
                } else if(op2.isImmediate()){
                    asms = new AssemblySentence(inst.getKind(),
                                                new Reg(varRegMap.getByKey(op1)),
                                                new Imm(op2.toString()));
                } else {
                    asms = new AssemblySentence(inst.getKind(),
                            new Reg(varRegMap.getByKey(op1)),
                            new Reg(varRegMap.getByKey(op2)));
                }
                assemblyList.add(asms);
                continue;
            }
            if(inst.getKind().isReturn()){
                asms = new AssemblySentence(InstructionKind.MOV,
                                            new Reg(RegName.a0),
                                            new Reg(varRegMap.getByKey(inst.getReturnValue())));
                assemblyList.add(asms);
                break;
            }
        }
    }


    /**
     * 输出汇编代码到文件
     *
     * @param path 输出文件路径
     */
    public void dump(String path) {
        // TODO: 输出汇编代码到文件
//        throw new NotImplementedException();
        FileUtils.writeLines(path, assemblyList.stream().map(AssemblySentence::toString).toList());
    }

    /***
     * 预处理：修改三元运算语句中的立即数为临时变量，返回修改后的变量
     * **/
    public Instruction modifyInstImm(Instruction instruction,
                                     IRValue leftTempVar,
                                     IRValue rightTempVar){
        IRValue result = instruction.getResult();
        IRValue leftIRV = instruction.getLHS();
        IRValue rightIRV = instruction.getRHS();
        switch (instruction.getKind()){
            case ADD -> {
                if(instruction.getLHS().isImmediate() && instruction.getRHS().isImmediate()){
                    return Instruction.createAdd((IRVariable) result, leftTempVar, rightTempVar);
                } else if(instruction.getLHS().isImmediate() && !instruction.getRHS().isImmediate()){
                    return Instruction.createAdd((IRVariable) result, leftTempVar, rightIRV);
                } else if(!instruction.getLHS().isImmediate() && instruction.getRHS().isImmediate()){
                    return Instruction.createAdd((IRVariable) result, leftIRV, rightTempVar);
                } else {
                    return instruction;
                }
            }
            case SUB -> {
                if(instruction.getLHS().isImmediate() && instruction.getRHS().isImmediate()){
                    return Instruction.createSub((IRVariable) result, leftTempVar, rightTempVar);
                } else if(instruction.getLHS().isImmediate() && !instruction.getRHS().isImmediate()){
                    return Instruction.createSub((IRVariable) result, leftTempVar, rightIRV);
                } else if(!instruction.getLHS().isImmediate() && instruction.getRHS().isImmediate()){
                    return Instruction.createSub((IRVariable) result, leftIRV, rightTempVar);
                } else {
                    return instruction;
                }
            }
            case MUL -> {
                if(instruction.getLHS().isImmediate() && instruction.getRHS().isImmediate()){
                    return Instruction.createMul((IRVariable) result, leftTempVar, rightTempVar);
                } else if(instruction.getLHS().isImmediate() && !instruction.getRHS().isImmediate()){
                    return Instruction.createMul((IRVariable) result, leftTempVar, rightIRV);
                } else if(!instruction.getLHS().isImmediate() && instruction.getRHS().isImmediate()){
                    return Instruction.createMul((IRVariable) result, leftIRV, rightTempVar);
                } else {
                    return instruction;
                }
            }
            default -> {
                return instruction;
            }
        }
    }

    /***
     * 为变量设定寄存器
     * */
    public void addVarToMapIfNeed(int instIndex, IRValue op){
        if(op.isImmediate()){
            return;
        }
        if(op.isIRVariable()){
            // 变量已存入临时寄存器
            if(varRegMap.containsKey(op)){
                return;
            }
            // 变量未存入寄存器，试图找到空闲的临时寄存器
            for(RegName regName : RegName.values()){
                if(varRegMap.containsValue(regName)){
                    continue;
                } else if(regName == RegName.x0){
                    break;
                } else {
                    varRegMap.replace(op, regName);
                    return;
                }
            }
            // 变量未存入寄存器且没有空闲的临时寄存器，寻找后面指令不用的空闲寄存器进行寄存器分配
            Instruction inst;
            List<RegName> regNames = new LinkedList<>();
            for(RegName regName : RegName.values()){
                if(regName != RegName.x0 && regName != RegName.a0){
                    regNames.add(regName);
                }
            }
            for(int i=instIndex; i<instList.size(); i++){
                inst = instList.get(i);
                for(IRValue ir : inst.getOperands()){
                    RegName regName = varRegMap.getByKey(ir);
                    if(regNames.contains(regName)){
                        regNames.remove(regName);
                    }
                }
            }
            if(regNames.size() != 0) {
                varRegMap.replace(op, regNames.get(0));
            } else {
                return;
            }
        }
    }
}

