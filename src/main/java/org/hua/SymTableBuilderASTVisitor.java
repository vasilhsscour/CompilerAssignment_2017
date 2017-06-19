package org.hua;

/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
import java.util.ArrayDeque;
import java.util.Deque;

import org.hua.symbol.HashSymTable;
import org.hua.symbol.SymTable;
import org.hua.symbol.SymTableEntry;
import org.hua.ast.ASTUtils;
import org.hua.ast.ASTVisitor;
import org.hua.ast.ASTVisitorException;
import org.hua.ast.AssignmentStatement;
import org.hua.ast.BinaryExpression;
import org.hua.ast.BreakStatement;
import org.hua.ast.ClassDefinition;
import org.hua.ast.CompilationUnit;
import org.hua.ast.CompoundStatement;
import org.hua.ast.ContinueStatement;
import org.hua.ast.ConstructorExpression;
import org.hua.ast.Definitions;
import org.hua.ast.DoWhileStatement;
import org.hua.ast.Expression;
import org.hua.ast.ExpressionStatement;
import org.hua.ast.FieldDefinition;
import org.hua.ast.FieldOrFunctionDefinition;
import org.hua.ast.FloatLiteralExpression;
import org.hua.ast.FunctionDefinition;
import org.hua.ast.FunctionExpression;
import org.hua.ast.IdentifierExpression;
import org.hua.ast.IfElseStatement;
import org.hua.ast.IfStatement;
import org.hua.ast.IntegerLiteralExpression;
import org.hua.ast.NullExpression;
import org.hua.ast.ParameterDeclaration;
import org.hua.ast.ParenthesisExpression;
import org.hua.ast.ReferenceClassMethodExpression;
import org.hua.ast.ReferenceClassVariableExpression;
import org.hua.ast.ReturnStatement;
import org.hua.ast.Statement;
import org.hua.ast.StringLiteralExpression;
import org.hua.ast.ThisExpression;
import org.hua.ast.UnaryExpression;
import org.hua.ast.VariableDeclarationStatement;
import org.hua.ast.WhileStatement;
import org.hua.ast.WriteStatement;
import org.hua.types.TypeUtils;
import org.objectweb.asm.Type;

/**
 * Build symbol tables for each node of the AST.
 */
public class SymTableBuilderASTVisitor implements ASTVisitor {

    private final Deque<SymTable<SymTableEntry>> env;
    private boolean inClass = false;
    private boolean inFunction = false;
    private boolean inLoop = false;
    private boolean constuctorExist = false;
    private boolean haveFields = false;
    private String className = null;
   
    public SymTableBuilderASTVisitor() {
        env = new ArrayDeque<SymTable<SymTableEntry>>();
    }

    @Override
    public void visit(CompilationUnit node) throws ASTVisitorException {
        pushEnvironment();
        ASTUtils.setEnv(node, env.element());
        for (Definitions s : node.getDefinitions()) {
            s.accept(this);
        }
        popEnvironment();
    }
    
    @Override
    public void visit(Definitions node) throws ASTVisitorException {
        ASTUtils.setEnv(node, env.element());
        
        ClassDefinition classDefinition = node.getClassDefinition();
        FunctionDefinition functionDefinition = node.getFunctionDefinition();
        
        if ( classDefinition == null) {
            functionDefinition.accept(this);
        }
        
        if ( functionDefinition == null ) {
            classDefinition.accept(this);
        }
    }
    
    @Override
    public void visit(ClassDefinition node) throws ASTVisitorException {
        ASTUtils.setEnv(node, env.element());
        this.inClass = true;
        this.className = node.getId();
        for(FieldOrFunctionDefinition st: node.getDefinitions()) { 
            st.accept(this);
        }
        
        if (this.constuctorExist) {
            ASTUtils.setConstructorExist(node, true);
        }
        else {
            ASTUtils.setConstructorExist(node, !this.haveFields);

        }
        
        this.className = null;
        this.constuctorExist = false;
        this.haveFields = false;
        this.inClass = false;
    }

    @Override
    public void visit(FieldOrFunctionDefinition node) throws ASTVisitorException {
        ASTUtils.setEnv(node, env.element());
        if ( node.getFieldDefinition().getColumn() != 0) {
            node.getFieldDefinition().accept(this);
        }
        else{
            node.getFunctionDefinition().accept(this);
        }
    }

    @Override
    public void visit(FieldDefinition node) throws ASTVisitorException {
        ASTUtils.setEnv(node, env.element());
        if (this.inClass) {
            this.haveFields = true;
        }
    }

    @Override
    public void visit(FunctionDefinition node) throws ASTVisitorException {
        ASTUtils.setEnv(node, env.element());
        this.inFunction = true;
        
        if (this.inClass) {
            if (this.className.equals(node.getId())) {
                if (node.getTypeSpecifier() == null) {
                    this.constuctorExist = true;
                } else {
                    System.out.println("Wrong Function Name");
                }
            }
        }
        
        for(ParameterDeclaration pd: node.getParameters()) { 
            pd.accept(this);
        }
        node.getStmt().accept(this);
        this.inFunction = false;

    }

    @Override
    public void visit(ParameterDeclaration node) throws ASTVisitorException {
        ASTUtils.setEnv(node, env.element());
    }

    @Override
    public void visit(WriteStatement node) throws ASTVisitorException {
         ASTUtils.setEnv(node, env.element());
        node.getExpression().accept(this);
    }
    
    @Override
    public void visit(AssignmentStatement node) throws ASTVisitorException {
        ASTUtils.setEnv(node, env.element());
        node.getExpression1().accept(this);
        node.getExpression2().accept(this);
    }
    
    @Override
    public void visit(CompoundStatement node) throws ASTVisitorException {
        pushEnvironment();
        ASTUtils.setEnv(node, env.element());
        for (Statement s : node.getStatements()) {
            s.accept(this);
        }
        popEnvironment();
    }
    
    @Override
    public void visit(WhileStatement node) throws ASTVisitorException {
        ASTUtils.setEnv(node, env.element());
        if (this.inLoop) {
            node.getExpression().accept(this);
            node.getStmt().accept(this);
        }
        else {
            this.inLoop = true;
            node.getExpression().accept(this);
            node.getStmt().accept(this);
            this.inLoop = false;
        }
    }

    @Override
    public void visit(DoWhileStatement node) throws ASTVisitorException {
        ASTUtils.setEnv(node, env.element());
        if (this.inLoop) {
            node.getExpression().accept(this);
            node.getStmt().accept(this);
        }
        else {
            this.inLoop = true;
            node.getExpression().accept(this);
            node.getStmt().accept(this);
            this.inLoop = false;
        }
    }
    
    @Override
    public void visit(IfStatement node) throws ASTVisitorException {
        ASTUtils.setEnv(node, env.element());
        node.getExpression().accept(this);
        node.getStatement().accept(this);
    }
    
    @Override
    public void visit(IfElseStatement node) throws ASTVisitorException {
        ASTUtils.setEnv(node, env.element());
        node.getExpression().accept(this);
        node.getStatement1().accept(this);
        node.getStatement2().accept(this);
    }
    
    @Override
    public void visit(ExpressionStatement node) throws ASTVisitorException {
        ASTUtils.setEnv(node, env.element());
        node.getExpression().accept(this);
    }
    
    @Override
    public void visit(VariableDeclarationStatement node) throws ASTVisitorException {
        ASTUtils.setEnv(node, env.element());
    }

    @Override
    public void visit(ReturnStatement node) throws ASTVisitorException {
        ASTUtils.setEnv(node, env.element());
        node.getExpression().accept(this);
    }
    
    @Override
    public void visit(BreakStatement node) throws ASTVisitorException {
        ASTUtils.setEnv(node, env.element());
        ASTUtils.setBreakInLoop(node, this.inLoop);
    }

    @Override
    public void visit(ContinueStatement node) throws ASTVisitorException {
        ASTUtils.setEnv(node, env.element());
        ASTUtils.setContinueInLoop(node, this.inLoop);
    }
    
    @Override
    public void visit(BinaryExpression node) throws ASTVisitorException {
        ASTUtils.setEnv(node, env.element());
        node.getExpression1().accept(this);
        node.getExpression2().accept(this);
    }

    @Override
    public void visit(UnaryExpression node) throws ASTVisitorException {
        ASTUtils.setEnv(node, env.element());
        node.getExpression().accept(this);
    }
    
    @Override
    public void visit(FunctionExpression node) throws ASTVisitorException {
        ASTUtils.setEnv(node, env.element());
        for (Expression e : node.getExpressions()) {
            e.accept(this);
        }
    }

    @Override
    public void visit(ConstructorExpression node) throws ASTVisitorException {
        ASTUtils.setEnv(node, env.element());
        for (Expression e : node.getExpressions()) {
            e.accept(this);
        }

    }

    @Override
    public void visit(ReferenceClassVariableExpression node) throws ASTVisitorException {
        ASTUtils.setEnv(node, env.element());
        node.getExpression().accept(this);
    }

    @Override
    public void visit(ReferenceClassMethodExpression node) throws ASTVisitorException {
        ASTUtils.setEnv(node, env.element());
        node.getExpression().accept(this);
        for (Expression e : node.getExpressions()) {
            e.accept(this);
        }
    }

    @Override
    public void visit(IdentifierExpression node) throws ASTVisitorException {
        ASTUtils.setEnv(node, env.element());
    }
    
    @Override
    public void visit(IntegerLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setEnv(node, env.element());
    }
   
    @Override
    public void visit(FloatLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setEnv(node, env.element());
    }
    
    @Override
    public void visit(StringLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setEnv(node, env.element());
    }

    @Override
    public void visit(ParenthesisExpression node) throws ASTVisitorException {
        ASTUtils.setEnv(node, env.element());
        node.getExpression().accept(this);
    }

    @Override
    public void visit(ThisExpression node) throws ASTVisitorException {
        ASTUtils.setEnv(node, env.element());
        Boolean exist;
        
        exist = this.inClass && this.inFunction;
        ASTUtils.setThisInClassFun(node, exist);
        ASTUtils.setThisType(node, Type.getType(TypeUtils.MAKE_TYPE+className+";"));
        
    }

    @Override
    public void visit(NullExpression node) throws ASTVisitorException {
        ASTUtils.setEnv(node, env.element());
    }

    private void pushEnvironment() {
        SymTable<SymTableEntry> oldSymTable = env.peek();
        SymTable<SymTableEntry> symTable = new HashSymTable<SymTableEntry>(
                oldSymTable);
        env.push(symTable);
    }

    private void popEnvironment() {
        env.pop();
    }

}
