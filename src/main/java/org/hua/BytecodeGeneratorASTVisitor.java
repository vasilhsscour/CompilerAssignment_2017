package org.hua;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.hua.ast.ASTNode;
import org.hua.ast.ASTUtils;
import org.hua.ast.ASTVisitor;
import org.hua.ast.ASTVisitorException;
import org.hua.ast.AssignmentStatement;
import org.hua.ast.BinaryExpression;
import org.hua.ast.BreakStatement;
import org.hua.ast.ClassDefinition;
import org.hua.ast.CompilationUnit;
import org.hua.ast.CompoundStatement;
import org.hua.ast.ConstructorExpression;
import org.hua.ast.ContinueStatement;
import org.hua.ast.Definition;
import org.hua.ast.Definitions;
import org.hua.ast.DoWhileStatement;
import org.hua.ast.FloatLiteralExpression;
import org.hua.ast.Expression;
import org.hua.ast.ExpressionStatement;
import org.hua.ast.FieldDefinition;
import org.hua.ast.FieldOrFunctionDefinition;
import org.hua.ast.FunctionDefinition;
import org.hua.ast.FunctionExpression;
import org.hua.ast.IdentifierExpression;
import org.hua.ast.IfElseStatement;
import org.hua.ast.IfStatement;
import org.hua.ast.IntegerLiteralExpression;
import org.hua.ast.NullExpression;
import org.hua.ast.Operator;
import org.hua.ast.ParameterDeclaration;
import org.hua.ast.ParenthesisExpression;
import org.hua.ast.ReferenceClassMethodExpression;
import org.hua.ast.ReferenceClassVariableExpression;
import org.hua.ast.ReturnStatement;
import org.hua.ast.WriteStatement;
import org.hua.ast.Statement;
import org.hua.ast.StringLiteralExpression;
import org.hua.ast.ThisExpression;
import org.hua.ast.UnaryExpression;
import org.hua.ast.VariableDeclarationStatement;
import org.hua.ast.WhileStatement;
import org.hua.symbol.LocalIndexPool;
import org.hua.symbol.SymTable;
import org.hua.symbol.SymTableEntry;
import org.hua.types.TypeUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class BytecodeGeneratorASTVisitor implements ASTVisitor {

    private ClassNode cn;
    private MethodNode mn;
    private FieldNode fn;
    private ClassNode innerClass;
    private boolean inClass = false;

    public BytecodeGeneratorASTVisitor() {
        // create class
        cn = new ClassNode();
        cn.access = Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER;
        cn.version = Opcodes.V1_5;
        cn.name = "Calculator";
        cn.sourceFile = "Calculator.in";
        cn.superName = "java/lang/Object";

        // create constructor
        mn = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V"));
        mn.instructions.add(new InsnNode(Opcodes.RETURN));
        mn.maxLocals = 1;
        mn.maxStack = 1;
        cn.methods.add(mn);
    }

    public ClassNode getClassNode() {
        return cn;
    }

    @Override
    public void visit(CompilationUnit node) throws ASTVisitorException {
        for ( Definition def : node.getDefinitions()) {
            def.accept(this);
        }
    }
    
    @Override
    public void visit(Definitions node) throws ASTVisitorException {
        ClassDefinition classDefinition = node.getClassDefinition();
        FunctionDefinition functionDefinition = node.getFunctionDefinition();
        
        if (functionDefinition != null && !ASTUtils.getNextList(functionDefinition).isEmpty()) {
            functionDefinition.accept(this);
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V"));
            mn.instructions.add(new InsnNode(Opcodes.RETURN));
            mn.maxLocals = 10;
            mn.maxStack = 10;
            cn.methods.add(mn);
        }
        
        if (classDefinition != null && !ASTUtils.getNextList(classDefinition).isEmpty() ) {
            inClass = true;
            classDefinition.accept(this);
            inClass = false;
            cn.innerClasses.add(innerClass);
        }
    }

    @Override
    public void visit(ClassDefinition node) throws ASTVisitorException {
        innerClass = new ClassNode();
        innerClass.access = Opcodes.ACC_PUBLIC;
        innerClass.version = Opcodes.V1_5;
        innerClass.name = node.getId();
        innerClass.sourceFile = node.getId()+".in";
        innerClass.superName = "java/lang/Object";

        // create constructor
        mn = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V"));
        mn.instructions.add(new InsnNode(Opcodes.RETURN));
        mn.maxLocals = 1;
        mn.maxStack = 1;
        innerClass.methods.add(mn);
        
        for (FieldOrFunctionDefinition ffd : node.getDefinitions()) {
            ffd.accept(this);
        }
    }

    @Override
    public void visit(FieldOrFunctionDefinition node) throws ASTVisitorException {
        if ( node.getFieldDefinition().getColumn() != 0) {
            node.getFieldDefinition().accept(this);
        }
        else{
            node.getFunctionDefinition().accept(this);
        }
    }

    @Override
    public void visit(FieldDefinition node) throws ASTVisitorException {
        String id = node.getId();
        String type = node.getTypeSpecifier().toString();
        int opcode = Opcodes.ACC_PUBLIC;
        
        fn = new FieldNode(opcode, id, type, null, null);
        innerClass.fields.add(fn);
    }

    @Override
    public void visit(FunctionDefinition node) throws ASTVisitorException {
        Type type = node.getTypeSpecifier();
        String funName = node.getId();
        String funType = ASTUtils.getFunctionType(node);
        int opcode = Opcodes.ACC_PUBLIC;
        
        if (type == null) {
            mn = new MethodNode(opcode, "<init>", funType, null, null);
        }
        else {
            if (funName.equals("main")) {
                funType = "([Ljava/lang/String;)V";
                opcode += Opcodes.ACC_STATIC;
            }
            mn = new MethodNode(opcode, funName, funType, null, null);
        }
        for (ParameterDeclaration param : node.getParameters()) {
            param.accept(this);
            LabelNode labelNode = new LabelNode();
            mn.instructions.add(labelNode);
            backpatch(ASTUtils.getNextList(param), labelNode);
        }
        Statement stmt = node.getStmt();
        stmt.accept(this);
        LabelNode labelNode = new LabelNode();
        mn.instructions.add(labelNode);
        backpatch(ASTUtils.getNextList(stmt), labelNode);

        if (!inClass) {
            cn.methods.add(mn);
        }
        else {
            innerClass.methods.add(mn);
        }
    }

    @Override
    public void visit(ParameterDeclaration node) throws ASTVisitorException {
        String id = node.getId();
        SymTable<SymTableEntry> symTable = ASTUtils.getSafeEnv(node);
        SymTableEntry symTableEntry = symTable.lookup(id);
        Type type = symTableEntry.getType();
        
        int opcode = type.getOpcode(Opcodes.ILOAD);
        int index = symTableEntry.getIndex();
        mn.instructions.add(new VarInsnNode(opcode, index));
    }
    
    @Override
    public void visit(WriteStatement node) throws ASTVisitorException {
        mn.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
        node.getExpression().accept(this);
        Type type = ASTUtils.getSafeType(node.getExpression());
        mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(" + type + ")V"));
    }
    
    @Override
    public void visit(AssignmentStatement node) throws ASTVisitorException {
        LocalIndexPool pool = ASTUtils.getSafeLocalIndexPool(node);
        node.getExpression2().accept(this);
        Type typeExpr2 = ASTUtils.getSafeType(node.getExpression2());
        int indexExpr2 = pool.getLocalIndex(typeExpr2);
        int opcode2 = typeExpr2.getOpcode(Opcodes.ILOAD);
        mn.instructions.add(new VarInsnNode(opcode2, indexExpr2));
        
        node.getExpression1().accept(this);
        Type typeExpr1 = ASTUtils.getSafeType(node.getExpression1());
        int indexExpr1 = pool.getLocalIndex(typeExpr1);
        int opcode1 = typeExpr1.getOpcode(Opcodes.ISTORE);
        mn.instructions.add(new VarInsnNode(opcode1, indexExpr1));
    }

    @Override
    public void visit(CompoundStatement node) throws ASTVisitorException {
        List<JumpInsnNode> breakList = new ArrayList<JumpInsnNode>();
        List<JumpInsnNode> continueList = new ArrayList<JumpInsnNode>();
        Statement s = null, ps;
        Iterator<Statement> it = node.getStatements().iterator();
        while (it.hasNext()) {
            ps = s;
            s = it.next();
            if (ps != null && !ASTUtils.getNextList(ps).isEmpty()) {
                LabelNode labelNode = new LabelNode();
                mn.instructions.add(labelNode);
                backpatch(ASTUtils.getNextList(ps), labelNode);
            }
            s.accept(this);
            breakList.addAll(ASTUtils.getBreakList(s));
            continueList.addAll(ASTUtils.getContinueList(s));
        }
        if (s != null) {
            ASTUtils.setNextList(node, ASTUtils.getNextList(s));
        }
        ASTUtils.setBreakList(node, breakList);
        ASTUtils.setContinueList(node, continueList);
    }
    
    @Override
    public void visit(WhileStatement node) throws ASTVisitorException {
        ASTUtils.setBooleanExpression(node.getExpression(), true);

        LabelNode beginLabelNode = new LabelNode();
        mn.instructions.add(beginLabelNode);

        node.getExpression().accept(this);

        LabelNode trueLabelNode = new LabelNode();
        mn.instructions.add(trueLabelNode);
        backpatch(ASTUtils.getTrueList(node.getExpression()), trueLabelNode);

        node.getStmt().accept(this);

        backpatch(ASTUtils.getNextList(node.getStmt()), beginLabelNode);
        backpatch(ASTUtils.getContinueList(node.getStmt()), beginLabelNode);

        mn.instructions.add(new JumpInsnNode(Opcodes.GOTO, beginLabelNode));

        ASTUtils.getNextList(node).addAll(ASTUtils.getFalseList(node.getExpression()));
        ASTUtils.getNextList(node).addAll(ASTUtils.getBreakList(node.getStmt()));
    }

    @Override
    public void visit(DoWhileStatement node) throws ASTVisitorException {
        ASTUtils.setBooleanExpression(node.getExpression(), true);

        LabelNode beginLabelNode = new LabelNode();
        mn.instructions.add(beginLabelNode);

        node.getStmt().accept(this);
        ASTUtils.getNextList(node).addAll(ASTUtils.getBreakList(node.getStmt()));

        LabelNode beginExprLabelNode = new LabelNode();
        mn.instructions.add(beginExprLabelNode);
        backpatch(ASTUtils.getNextList(node.getStmt()), beginExprLabelNode);
        backpatch(ASTUtils.getContinueList(node.getStmt()), beginExprLabelNode);

        node.getExpression().accept(this);
        ASTUtils.getNextList(node).addAll(ASTUtils.getFalseList(node.getExpression()));
        backpatch(ASTUtils.getTrueList(node.getExpression()), beginLabelNode);
    }
    
    @Override
    public void visit(IfStatement node) throws ASTVisitorException {
        ASTUtils.setBooleanExpression(node.getExpression(), true);

        node.getExpression().accept(this);

        LabelNode labelNode = new LabelNode();
        mn.instructions.add(labelNode);
        backpatch(ASTUtils.getTrueList(node.getExpression()), labelNode);

        node.getStatement().accept(this);

        ASTUtils.getBreakList(node).addAll(ASTUtils.getBreakList(node.getStatement()));
        ASTUtils.getContinueList(node).addAll(ASTUtils.getContinueList(node.getStatement()));

        ASTUtils.getNextList(node).addAll(ASTUtils.getFalseList(node.getExpression()));
        ASTUtils.getNextList(node).addAll(ASTUtils.getNextList(node.getStatement()));
    }

    @Override
    public void visit(IfElseStatement node) throws ASTVisitorException {
        ASTUtils.setBooleanExpression(node.getExpression(), true);

        node.getExpression().accept(this);

        LabelNode stmt1StartLabelNode = new LabelNode();
        mn.instructions.add(stmt1StartLabelNode);
        node.getStatement1().accept(this);

        JumpInsnNode skipGoto = new JumpInsnNode(Opcodes.GOTO, null);
        mn.instructions.add(skipGoto);

        LabelNode stmt2StartLabelNode = new LabelNode();
        mn.instructions.add(stmt2StartLabelNode);
        node.getStatement2().accept(this);

        backpatch(ASTUtils.getTrueList(node.getExpression()), stmt1StartLabelNode);
        backpatch(ASTUtils.getFalseList(node.getExpression()), stmt2StartLabelNode);

        ASTUtils.getNextList(node).addAll(ASTUtils.getNextList(node.getStatement1()));
        ASTUtils.getNextList(node).addAll(ASTUtils.getNextList(node.getStatement2()));
        ASTUtils.getNextList(node).add(skipGoto);

        ASTUtils.getBreakList(node).addAll(ASTUtils.getBreakList(node.getStatement1()));
        ASTUtils.getBreakList(node).addAll(ASTUtils.getBreakList(node.getStatement2()));

        ASTUtils.getContinueList(node).addAll(ASTUtils.getContinueList(node.getStatement1()));
        ASTUtils.getContinueList(node).addAll(ASTUtils.getContinueList(node.getStatement2()));
    }

    @Override
    public void visit(ExpressionStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
    }

    @Override
    public void visit(VariableDeclarationStatement varDeclaration) throws ASTVisitorException {
        // nothing here
    }
    
     @Override
    public void visit(ReturnStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        Type type = ASTUtils.getSafeType(node.getExpression());
        int opcode = type.getOpcode(Opcodes.IRETURN);
        mn.instructions.add(new InsnNode(opcode));
    }
    
    @Override
    public void visit(BreakStatement node) throws ASTVisitorException {
        JumpInsnNode jmp = new JumpInsnNode(Opcodes.GOTO, null);
        mn.instructions.add(jmp);
        ASTUtils.getBreakList(node).add(jmp);
    }

    @Override
    public void visit(ContinueStatement node) throws ASTVisitorException {
        JumpInsnNode jmp = new JumpInsnNode(Opcodes.GOTO, null);
        mn.instructions.add(jmp);
        ASTUtils.getContinueList(node).add(jmp);
    }
    
    @Override
    public void visit(BinaryExpression node) throws ASTVisitorException {
        node.getExpression1().accept(this);
        Type expr1Type = ASTUtils.getSafeType(node.getExpression1());

        node.getExpression2().accept(this);
        Type expr2Type = ASTUtils.getSafeType(node.getExpression2());

        Type maxType = TypeUtils.maxType(expr1Type, expr2Type);

        // cast top of stack to max
        if (!maxType.equals(expr2Type)) {
            widen(maxType, expr2Type);
        }

        // cast second from top to max
        if (!maxType.equals(expr1Type)) {
            LocalIndexPool lip = ASTUtils.getSafeLocalIndexPool(node);
            int localIndex = -1;
            if (expr2Type.equals(Type.DOUBLE_TYPE) || expr1Type.equals(Type.DOUBLE_TYPE)) {
                localIndex = lip.getLocalIndex(expr2Type);
                mn.instructions.add(new VarInsnNode(expr2Type.getOpcode(Opcodes.ISTORE), localIndex));
            } else {
                mn.instructions.add(new InsnNode(Opcodes.SWAP));
            }
            widen(maxType, expr1Type);
            if (expr2Type.equals(Type.DOUBLE_TYPE) || expr1Type.equals(Type.DOUBLE_TYPE)) {
                mn.instructions.add(new VarInsnNode(expr2Type.getOpcode(Opcodes.ILOAD), localIndex));
                lip.freeLocalIndex(localIndex, expr2Type);
            } else {
                mn.instructions.add(new InsnNode(Opcodes.SWAP));
            }
        }

        if (ASTUtils.isBooleanExpression(node)) {
            handleBooleanOperator(node, node.getOperator(), maxType);
        } else if (maxType.equals(TypeUtils.STRING_TYPE)) {
            mn.instructions.add(new InsnNode(Opcodes.SWAP));
            handleStringOperator(node, node.getOperator());
        } else {
            handleNumberOperator(node, node.getOperator(), maxType);
        }
    }
    
    @Override
    public void visit(UnaryExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);

        Type type = ASTUtils.getSafeType(node.getExpression());

        if (node.getOperator().equals(Operator.MINUS)) {
            mn.instructions.add(new InsnNode(type.getOpcode(Opcodes.INEG)));
        } else {
            ASTUtils.error(node, "Operator not recognized.");
        }
    }
    
    @Override
    public void visit(FunctionExpression node) throws ASTVisitorException {
        SymTable<SymTableEntry> env = ASTUtils.getSafeEnv(node);
        String id = node.getId();
        SymTableEntry symTableEntry = env.lookup(id);
        Type functionType = symTableEntry.getType();
        Type funInClass = symTableEntry.getFunInClass();
        
        String type = "(";
        for (Expression ex : node.getExpressions()) {
            ex.accept(this);
            type += ASTUtils.getSafeType(ex);
        }
        type += ")"+functionType;
        
        mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, funInClass.toString(), id, type));
    }

    @Override
    public void visit(ConstructorExpression node) throws ASTVisitorException {
        String id = node.getId();
        String type = "(";
        
        mn.instructions.add(new TypeInsnNode(Opcodes.NEW, id));
        mn.instructions.add(new InsnNode(Opcodes.DUP));
        
        for (Expression ex : node.getExpressions()){
            ex.accept(this);
            type += ASTUtils.getType(ex);
        }
        type += ")V";
        
        mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, id, "<init>", type));
    }

    @Override
    public void visit(ReferenceClassVariableExpression node) throws ASTVisitorException {
        String className = ASTUtils.getClassName(node);
        
        SymTable<SymTableEntry> env = ASTUtils.getSafeEnv(node);
        String id = node.getId();
        SymTableEntry symTableEntry = env.lookup(id);
        String type = symTableEntry.getType().toString();
        
        mn.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, className, id, type));
        
    }

    @Override
    public void visit(ReferenceClassMethodExpression node) throws ASTVisitorException {
        String className = ASTUtils.getClassName(node);
        
        SymTable<SymTableEntry> env = ASTUtils.getSafeEnv(node);
        String id = node.getId();
        SymTableEntry symTable = env.lookup(id);
        String type = "(";
        
        for (Expression ex : node.getExpressions()) {
            ex.accept(this);
            type += ASTUtils.getSafeType(ex);
        }
        type += symTable.getType();
        
        mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, className, id, type));
    }
    
     @Override
    public void visit(IdentifierExpression node) throws ASTVisitorException {
        SymTable<SymTableEntry> env = ASTUtils.getSafeEnv(node);
        
        SymTableEntry symTableEntry = env.lookup(node.getIdentifier());
        Type type = symTableEntry.getType();
        int index = symTableEntry.getIndex();
        int opcode = type.getOpcode(Opcodes.ILOAD);

        mn.instructions.add(new VarInsnNode(opcode,index));
    }

    @Override
    public void visit(FloatLiteralExpression node) throws ASTVisitorException {
        if (ASTUtils.isBooleanExpression(node)) {
            JumpInsnNode i = new JumpInsnNode(Opcodes.GOTO, null);
            mn.instructions.add(i);
            if (node.getLiteral() != 0) {
                ASTUtils.getTrueList(node).add(i);
            } else {
                ASTUtils.getFalseList(node).add(i);
            }
        } else {
            Double d = Double.valueOf(node.getLiteral());
            mn.instructions.add(new LdcInsnNode(d));
        }
    }
    
    @Override
    public void visit(IntegerLiteralExpression node) throws ASTVisitorException {
        if (ASTUtils.isBooleanExpression(node)) {
            JumpInsnNode i = new JumpInsnNode(Opcodes.GOTO, null);
            mn.instructions.add(i);
            if (node.getLiteral() != 0) {
                ASTUtils.getTrueList(node).add(i);
            } else {
                ASTUtils.getFalseList(node).add(i);
            }
        } else {
            Integer i = Integer.valueOf(node.getLiteral());
            mn.instructions.add(new LdcInsnNode(i));
        }
    }

    @Override
    public void visit(StringLiteralExpression node) throws ASTVisitorException {
        if (ASTUtils.isBooleanExpression(node)) {
            ASTUtils.error(node, "String is boolean expression!");
        } else {
            String s = String.valueOf(node.getLiteral());
            mn.instructions.add(new LdcInsnNode(s));
        }
    }

    @Override
    public void visit(ParenthesisExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
    }
   
    @Override
    public void visit(ThisExpression node) throws ASTVisitorException {
        mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    }

    @Override
    public void visit(NullExpression node) throws ASTVisitorException {
        mn.instructions.add(new LdcInsnNode("null"));
    }
   
    
    
    
    
    

    private void backpatch(List<JumpInsnNode> list, LabelNode labelNode) {
        if (list == null) {
            return;
        }
        for (JumpInsnNode instr : list) {
            instr.label = labelNode;
        }
    }

    /**
     * Cast the top of the stack to a particular type
     * Must add logical types probable
     */
    private void widen(Type target, Type source) {
        if (source.equals(target)) {
            return;
        }

        if (source.equals(Type.BOOLEAN_TYPE)) {
            if (target.equals(Type.INT_TYPE)) {
                // nothing
            } else if (target.equals(Type.DOUBLE_TYPE)) {
                mn.instructions.add(new InsnNode(Opcodes.I2D));
            } else if (target.equals(TypeUtils.STRING_TYPE)) {
                mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Boolean", "toString", "(Z)Ljava/lang/String;"));
            }
        } else if (source.equals(Type.INT_TYPE)) {
            if (target.equals(Type.DOUBLE_TYPE)) {
                mn.instructions.add(new InsnNode(Opcodes.I2D));
            } else if (target.equals(TypeUtils.STRING_TYPE)) {
                mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "toString", "(I)Ljava/lang/String;"));
            }
        } else if (source.equals(Type.DOUBLE_TYPE)) {
            if (target.equals(TypeUtils.STRING_TYPE)) {
                mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Double", "toString", "(D)Ljava/lang/String;"));
            }
        }
    }

    private void handleBooleanOperator(Expression node, Operator op, Type type) throws ASTVisitorException {
        List<JumpInsnNode> trueList = new ArrayList<JumpInsnNode>();

        if (type.equals(TypeUtils.STRING_TYPE)) {
            mn.instructions.add(new InsnNode(Opcodes.SWAP));
            JumpInsnNode jmp = null;
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z"));
            switch (op) {
                case EQUAL:
                    jmp = new JumpInsnNode(Opcodes.IFNE, null);
                    break;
                case NOTEQUAL:
                    jmp = new JumpInsnNode(Opcodes.IFEQ, null);
                    break;
                default:
                    ASTUtils.error(node, "Operator not supported on strings");
                    break;
            }
            mn.instructions.add(jmp);
            trueList.add(jmp);
        } else if (type.equals(Type.DOUBLE_TYPE)) {
            mn.instructions.add(new InsnNode(Opcodes.DCMPG));
            JumpInsnNode jmp = null;
            
            switch (op) {
                case EQUAL:
                    jmp = new JumpInsnNode(Opcodes.IFEQ, null);
                    break;
                case NOTEQUAL:
                    jmp = new JumpInsnNode(Opcodes.IFNE, null);
                    break;
                case GREATERTHAN:
                    jmp = new JumpInsnNode(Opcodes.IFGT, null);
                    break;
                case GREATEREQUALTHAN:
                    jmp = new JumpInsnNode(Opcodes.IFGE, null);
                    break;
                case LESSTHAN:
                    jmp = new JumpInsnNode(Opcodes.IFLT, null);
                    break;
                case LESSEQUALTHAN:
                    jmp = new JumpInsnNode(Opcodes.IFLE, null);
                    break;
                default:
                    ASTUtils.error(node, "No such operator!");
                    break;
            }
            mn.instructions.add(jmp);
            trueList.add(jmp);
        } else {
            JumpInsnNode jmp = null;
            switch (op) {
                case EQUAL:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPEQ, null);
                    mn.instructions.add(jmp);
                    break;
                case NOTEQUAL:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPNE, null);
                    mn.instructions.add(jmp);
                    break;
                case GREATERTHAN:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPGT, null);
                    mn.instructions.add(jmp);
                    break;
                case GREATEREQUALTHAN:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPGE, null);
                    mn.instructions.add(jmp);
                    break;
                case LESSTHAN:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPLT, null);
                    mn.instructions.add(jmp);
                    break;
                case LESSEQUALTHAN:
                    jmp = new JumpInsnNode(Opcodes.IF_ICMPLE, null);
                    mn.instructions.add(jmp);
                    break;
                default:
                    ASTUtils.error(node, "Operator not supported");
                    break;
            }
            trueList.add(jmp);
        }
        ASTUtils.setTrueList(node, trueList);
        List<JumpInsnNode> falseList = new ArrayList<JumpInsnNode>();
        JumpInsnNode jmp = new JumpInsnNode(Opcodes.GOTO, null);
        mn.instructions.add(jmp);
        falseList.add(jmp);
        ASTUtils.setFalseList(node, falseList);
    }

    /**
     * Assumes top of stack contains two strings
     */
    private void handleStringOperator(ASTNode node, Operator op) throws ASTVisitorException {
        if (op.equals(Operator.PLUS)) {
            mn.instructions.add(new TypeInsnNode(Opcodes.NEW, "java/lang/StringBuilder"));
            mn.instructions.add(new InsnNode(Opcodes.DUP));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V"));
            mn.instructions.add(new InsnNode(Opcodes.SWAP));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"));
            mn.instructions.add(new InsnNode(Opcodes.SWAP));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;"));
        } else if (op.isRelational()) {
            LabelNode trueLabelNode = new LabelNode();
            switch (op) {
                case EQUAL:
                    mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z"));
                    mn.instructions.add(new JumpInsnNode(Opcodes.IFNE, trueLabelNode));
                    break;
                case NOTEQUAL:
                    mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z"));
                    mn.instructions.add(new JumpInsnNode(Opcodes.IFEQ, trueLabelNode));
                    break;
                default:
                    ASTUtils.error(node, "Operator not supported on strings");
                    break;
            }
            mn.instructions.add(new InsnNode(Opcodes.ICONST_0));
            LabelNode endLabelNode = new LabelNode();
            mn.instructions.add(new JumpInsnNode(Opcodes.GOTO, endLabelNode));
            mn.instructions.add(trueLabelNode);
            mn.instructions.add(new InsnNode(Opcodes.ICONST_1));
            mn.instructions.add(endLabelNode);
        } else {
            ASTUtils.error(node, "Operator not recognized");
        }
    }

    private void handleNumberOperator(ASTNode node, Operator op, Type type) throws ASTVisitorException {
        if (op.equals(Operator.PLUS)) {
            int operator = type.getOpcode(Opcodes.IADD);
            mn.instructions.add(new InsnNode(operator));
            
        } else if (op.equals(Operator.MINUS)) {
            int operator = type.getOpcode(Opcodes.ISUB);
            mn.instructions.add(new InsnNode(operator));
            
        } else if (op.equals(Operator.TIMES)) {
            int operator = type.getOpcode(Opcodes.IMUL);
            mn.instructions.add(new InsnNode(operator));
            
        } else if (op.equals(Operator.DIVISION)) {
            int operator = type.getOpcode(Opcodes.IDIV);
            mn.instructions.add(new InsnNode(operator));
            
        } else if (op.isRelational()) {
            if (type.equals(Type.DOUBLE_TYPE)) {
                mn.instructions.add(new InsnNode(Opcodes.DCMPG));
                JumpInsnNode jmp = null;
                switch (op) {
                    case EQUAL:
                        jmp = new JumpInsnNode(Opcodes.IFEQ, null);
                        mn.instructions.add(jmp);
                        break;
                    case NOTEQUAL:
                        jmp = new JumpInsnNode(Opcodes.IFNE, null);
                        mn.instructions.add(jmp);
                        break;
                    case GREATERTHAN:
                        jmp = new JumpInsnNode(Opcodes.IFGT, null);
                        mn.instructions.add(jmp);
                        break;
                    case GREATEREQUALTHAN:
                        jmp = new JumpInsnNode(Opcodes.IFGE, null);
                        mn.instructions.add(jmp);
                        break;
                    case LESSTHAN:
                        jmp = new JumpInsnNode(Opcodes.IFLT, null);
                        mn.instructions.add(jmp);
                        break;
                    case LESSEQUALTHAN:
                        jmp = new JumpInsnNode(Opcodes.IFLE, null);
                        mn.instructions.add(jmp);
                        break;
                    default:
                        ASTUtils.error(node, "Operator not supported");
                        break;
                }
                mn.instructions.add(new InsnNode(Opcodes.ICONST_0));
                LabelNode endLabelNode = new LabelNode();
                mn.instructions.add(new JumpInsnNode(Opcodes.GOTO, endLabelNode));
                LabelNode trueLabelNode = new LabelNode();
                jmp.label = trueLabelNode;
                mn.instructions.add(trueLabelNode);
                mn.instructions.add(new InsnNode(Opcodes.ICONST_1));
                mn.instructions.add(endLabelNode);
            } else if (type.equals(Type.INT_TYPE)) {
                LabelNode trueLabelNode = new LabelNode();
                switch (op) {
                    case EQUAL:
                        mn.instructions.add(new JumpInsnNode(Opcodes.IF_ICMPEQ, trueLabelNode));
                        break;
                    case NOTEQUAL:
                        mn.instructions.add(new JumpInsnNode(Opcodes.IF_ICMPNE, trueLabelNode));
                        break;
                    case GREATERTHAN:
                        mn.instructions.add(new JumpInsnNode(Opcodes.IF_ICMPGT, trueLabelNode));
                        break;
                    case GREATEREQUALTHAN:
                        mn.instructions.add(new JumpInsnNode(Opcodes.IF_ICMPGE, trueLabelNode));
                        break;
                    case LESSTHAN:
                        mn.instructions.add(new JumpInsnNode(Opcodes.IF_ICMPLT, trueLabelNode));
                        break;
                    case LESSEQUALTHAN:
                        mn.instructions.add(new JumpInsnNode(Opcodes.IF_ICMPLE, trueLabelNode));
                        break;
                    default:
                        break;
                }
                mn.instructions.add(new InsnNode(Opcodes.ICONST_0));
                LabelNode endLabelNode = new LabelNode();
                mn.instructions.add(new JumpInsnNode(Opcodes.GOTO, endLabelNode));
                mn.instructions.add(trueLabelNode);
                mn.instructions.add(new InsnNode(Opcodes.ICONST_1));
                mn.instructions.add(endLabelNode);
            } else {
                ASTUtils.error(node, "Cannot compare such types.");
            }
        } else {
            ASTUtils.error(node, "Operator not recognized.");
        }
    }
}
