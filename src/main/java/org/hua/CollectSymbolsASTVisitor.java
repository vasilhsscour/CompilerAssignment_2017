/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
package org.hua;

import java.util.HashMap;
import java.util.Map;
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
import org.hua.symbol.LocalIndexPool;
import org.hua.types.TypeUtils;
import org.objectweb.asm.Type;

/**
 * Collect all symbols such as variables, methods, etc in symbol table.
 */
public class CollectSymbolsASTVisitor implements ASTVisitor {

    public CollectSymbolsASTVisitor() {
    }
    
    private Map <Type,SymTable<SymTableEntry>> map = new HashMap <Type,SymTable<SymTableEntry>>() ;
    private int funIndex = 0;
    private boolean inClass;
    private Type funInClass;
    
    @Override
    public void visit(CompilationUnit node) throws ASTVisitorException {
        for (Definitions s : node.getDefinitions()) {
            s.accept(this);
        }
        
        SymTable<SymTableEntry> env = ASTUtils.getSafeEnv(node);
        
        SymTableEntry main = env.lookup("main");
        
        if (main == null) {
            String message = "main does not exist!";
            ASTUtils.error(node, message);
        }
    }

    @Override
    public void visit(Definitions node) throws ASTVisitorException {
        ClassDefinition classDefinition = node.getClassDefinition();
        FunctionDefinition functionDefinition = node.getFunctionDefinition();
        
        if ( classDefinition != null) {
            classDefinition.accept(this);
        }
        
        if ( functionDefinition != null ) {
            functionDefinition.accept(this);
        }
    }
    
    @Override
    public void visit(ClassDefinition node) throws ASTVisitorException {
        SymTable<SymTableEntry> env = ASTUtils.getSafeEnv(node);
        String id = node.getId();
        
        if (env.lookupOnlyInTop(id) != null ) {
            String message = "The class "+id+" exists!";
            ASTUtils.error(node, message);
        }
        else {
            String makeType = TypeUtils.MAKE_TYPE+id+";";
            Type type = Type.getType(makeType);
            
            this.inClass = true;
            this.funInClass = type;
            
            for (FieldOrFunctionDefinition fd : node.getDefinitions()) {
                fd.accept(this);
            }
            
            this.inClass = false;
            
            SymTableEntry symTable = new SymTableEntry(id, type);
            symTable.setDefinitions(node.getDefinitions());
            
            env.put(id, symTable);
            this.map.put(type, env);
            Registry.getInstance().setMap(this.map);
        }
        
        if (!ASTUtils.getConstructorExist(node))
            ASTUtils.error(node, "Constuctor in class " + id + " don't exist!");
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
        SymTable<SymTableEntry> env = ASTUtils.getSafeEnv(node);

        String id = node.getId();
        Type type = node.getTypeSpecifier();
        
        if (env.lookupOnlyInTop(id) != null ) {
            String message = "The field "+id+" exists!";
            ASTUtils.error(node, message);
        }
        else {
            env.put(id, new SymTableEntry(id, type));
        }
    }

    @Override
    public void visit(FunctionDefinition node) throws ASTVisitorException {
        SymTable<SymTableEntry> env = ASTUtils.getSafeEnv(node);
        
        String id = node.getId();
        Type type;
        type = node.getTypeSpecifier();
        LocalIndexPool pool = ASTUtils.getSafeLocalIndexPool(node);
        this.funIndex = 0;
        
        
        if (env.lookupOnlyInTop(id) != null ) {
            String message = "The function "+id+" exists!";
            ASTUtils.error(node, message);
        }
        else {
            SymTableEntry symTableEntry = new SymTableEntry(id, type, this.funIndex);
            symTableEntry.setFunInClass(this.funInClass);
            env.put(id, symTableEntry);
            for (ParameterDeclaration pd : node.getParameters()) {
                this.funIndex++;
                pool.freeLocalIndex(funIndex);
                pd.accept(this);
            }
            node.getStmt().accept(this);
            
            symTableEntry.setParameters(node.getParameters());
            env.put(id, symTableEntry);
        }
        this.funIndex = 0;
        
    }

    @Override
    public void visit(ParameterDeclaration node) throws ASTVisitorException {
        SymTable<SymTableEntry> env = ASTUtils.getSafeEnv(node);

        String id = node.getId();
        Type type = node.getTypeSpecifier();
        
        if (env.lookupOnlyInTop(id) != null ) {
            String message = "The function "+id+" exists!";
            ASTUtils.error(node, message);
        }
        else {
            env.put(id, new SymTableEntry(id, type, this.funIndex));
        }
    }

     @Override
    public void visit(WriteStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
    }
    
    @Override
    public void visit(AssignmentStatement node) throws ASTVisitorException {
        node.getExpression1().accept(this);
        node.getExpression2().accept(this);
    }
    
    @Override
    public void visit(CompoundStatement node) throws ASTVisitorException {
        for (Statement st : node.getStatements()) {
            st.accept(this);
        }
    }
    
    @Override
    public void visit(WhileStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        node.getStmt().accept(this);
    }
    
    @Override
    public void visit(DoWhileStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        node.getStmt().accept(this);
    }

    @Override
    public void visit(IfStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        node.getStatement().accept(this);
    }

    @Override
    public void visit(IfElseStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        node.getStatement1().accept(this);
        node.getStatement2().accept(this);
    }
    
    @Override
    public void visit(ExpressionStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
    }
    
    @Override
    public void visit(VariableDeclarationStatement node) throws ASTVisitorException {
        SymTable<SymTableEntry> env = ASTUtils.getSafeEnv(node);

        String id = node.getIdentifier();
        Type type = node.getTypeSpecifier();
        LocalIndexPool pool = ASTUtils.getSafeLocalIndexPool(node);
        int index = pool.getLocalIndex(type);
        
        if (env.lookupOnlyInTop(id) != null ) {
            String message = "The variable "+id+" exists!";
            ASTUtils.error(node, message);
        }
        else {
            env.put(id, new SymTableEntry(id, type, index));
        }
    }

    @Override
    public void visit(ReturnStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
    }
    
    @Override
    public void visit(BreakStatement node) throws ASTVisitorException {
        // nothing
    }

    @Override
    public void visit(ContinueStatement node) throws ASTVisitorException {
        // nothing
    }

     @Override
    public void visit(BinaryExpression node) throws ASTVisitorException {
        node.getExpression1().accept(this);
        node.getExpression2().accept(this);
    }

    @Override
    public void visit(UnaryExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
    }

    @Override
    public void visit(FunctionExpression node) throws ASTVisitorException {
        for (Expression ex : node.getExpressions()) {
            ex.accept(this);
        }
    }

    @Override
    public void visit(ConstructorExpression node) throws ASTVisitorException {
        for(Expression ex : node.getExpressions()) {
            ex.accept(this);
        }
        
     }

    @Override
    public void visit(ReferenceClassVariableExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
       
    }

    @Override
    public void visit(ReferenceClassMethodExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
        for (Expression ex : node.getExpressions()) {
            ex.accept(this);
        }
    }
    
    @Override
    public void visit(IdentifierExpression node) throws ASTVisitorException {
        // nothing
    }
    
    @Override
    public void visit(IntegerLiteralExpression node) throws ASTVisitorException {
        // nothing        
    }
    
    @Override
    public void visit(FloatLiteralExpression node) throws ASTVisitorException {
        // nothing
    }

    @Override
    public void visit(StringLiteralExpression node) throws ASTVisitorException {
        // nothing
    }
    
    @Override
    public void visit(ParenthesisExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
    }

    
    @Override
    public void visit(ThisExpression node) throws ASTVisitorException {
        // nothing
    }

    @Override
    public void visit(NullExpression node) throws ASTVisitorException {
        // nothing
    }

}
