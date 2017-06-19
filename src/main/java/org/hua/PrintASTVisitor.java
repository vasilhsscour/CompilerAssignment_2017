package org.hua;

/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
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
import org.hua.ast.WriteStatement;
import org.hua.ast.Statement;
import org.hua.ast.StringLiteralExpression;
import org.hua.ast.ThisExpression;
import org.hua.ast.UnaryExpression;
import org.hua.ast.VariableDeclarationStatement;
import org.hua.ast.WhileStatement;
import org.apache.commons.lang3.StringEscapeUtils;
import org.hua.ast.ASTUtils;
import org.hua.types.TypeUtils;
import org.objectweb.asm.Type;

public class PrintASTVisitor implements ASTVisitor {

    @Override
    public void visit(CompilationUnit node) throws ASTVisitorException {
        for (Definitions d : node.getDefinitions()) {
            d.accept(this);
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
        System.out.println();
        System.out.print("class ");
        System.out.print(node.getId());
        System.out.println(" { ");
        for(FieldOrFunctionDefinition st: node.getDefinitions()) { 
            st.accept(this);
        }
        System.out.println(" } ");
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
        System.out.println();
        System.out.print(node.getTypeSpecifier());
        System.out.print(" " + node.getId());
        System.out.println(";");
    }

    @Override
    public void visit(FunctionDefinition node) throws ASTVisitorException {
        System.out.print(ASTUtils.getFunctionType(node));
        System.out.print(" "+node.getId());
        System.out.print(" ( ");
        
        int size = node.getParameters().size();
        int i=0;
        
        for(ParameterDeclaration pd: node.getParameters()) { 
            pd.accept(this);
            i++;
            if (i != size) {
                System.out.print(", ");
            }
        }
        System.out.print(" ) ");
        
        node.getStmt().accept(this);
        
    }
    
    @Override
    public void visit(ParameterDeclaration node) throws ASTVisitorException { 
        System.out.print(node.getTypeSpecifier()+" "+node.getId());
    }

    @Override
    public void visit(WriteStatement node) throws ASTVisitorException {
        System.out.print("print( ");
        node.getExpression().accept(this);
        System.out.println(" );");
    }

    @Override
    public void visit(AssignmentStatement node) throws ASTVisitorException {
        node.getExpression1().accept(this);
        System.out.print(" = ");
        node.getExpression2().accept(this);
        System.out.println(";");
    }
    
    @Override
    public void visit(CompoundStatement node) throws ASTVisitorException {
        System.out.print(" { ");
        System.out.println();
        for(Statement st: node.getStatements()) { 
            st.accept(this);
        }
        System.out.println();
        System.out.print("}");
    }
    
    @Override
    public void visit(WhileStatement node) throws ASTVisitorException {
        System.out.print("while");
        System.out.print (" ( ");
        node.getExpression().accept(this);
        System.out.print(" ) ");
        System.out.print(" { ");
        System.out.println();
        node.getStmt().accept(this);
        System.out.println();
        System.out.print(" } ");
    }
    
    @Override
    public void visit(DoWhileStatement node) throws ASTVisitorException {
        System.out.print("do");
        node.getStmt().accept(this);
        System.out.print("while");
        System.out.print (" ( ");
        node.getExpression().accept(this);
        System.out.print(" ) ");
        System.out.print("; ");
    }
    
    @Override
    public void visit(IfStatement node) throws ASTVisitorException {
        System.out.println();
        System.out.print("If");
        System.out.print(" ( ");
        node.getExpression().accept(this);
        System.out.print(" ) ");
        node.getStatement().accept(this);
    }
    
    @Override
    public void visit(IfElseStatement node) throws ASTVisitorException {
        System.out.println();
        System.out.print("If");
        System.out.print(" ( ");
        node.getExpression().accept(this);
        System.out.print(" ) ");
        node.getStatement1().accept(this);
        System.out.print("Else");
        node.getStatement2().accept(this);
    }

     @Override
    public void visit(ExpressionStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        System.out.print(";");
    }
    
     @Override
    public void visit(VariableDeclarationStatement node) throws ASTVisitorException {
        System.out.println();
        System.out.print(node.getTypeSpecifier());
        System.out.print(" ");
        System.out.print(node.getIdentifier());
        System.out.print(";");
        System.out.println();
    }
    
     @Override
    public void visit(ReturnStatement node) throws ASTVisitorException {
        System.out.print("return ");
        node.getExpression().accept(this);
        System.out.print(";");
    }

    @Override
    public void visit(BreakStatement node) throws ASTVisitorException {
        System.out.print("break ");
        System.out.print(";");
    }

    @Override
    public void visit(ContinueStatement node) throws ASTVisitorException {
        System.out.print("continue ");
        System.out.print(";");
        
    }

    @Override
    public void visit(BinaryExpression node) throws ASTVisitorException {
        node.getExpression1().accept(this);
        System.out.print(" ");
        System.out.print(node.getOperator());
        System.out.print(" ");
        node.getExpression2().accept(this);
    }

    @Override
    public void visit(UnaryExpression node) throws ASTVisitorException {
        System.out.print(node.getOperator());
        System.out.print(" ");
        node.getExpression().accept(this);
    }

    @Override
    public void visit(FunctionExpression node) throws ASTVisitorException {
        System.out.println();
        System.out.print(node.getId());
        System.out.print(" ( ");
        
        int size = node.getExpressions().size();
        int i=0;
        
        for (Expression e : node.getExpressions()) {
            e.accept(this);
            i++;
            if (i != size) {
                System.out.print(", ");
            }
        }
        
        System.out.print(" ) ");
    }
    
    @Override
    public void visit(ConstructorExpression node) throws ASTVisitorException {
        System.out.println();
        System.out.print("new ");
        System.out.print(node.getId());
        System.out.print(" ( ");
        
        int size = node.getExpressions().size();
        int i=0;
        
        for (Expression e : node.getExpressions()) {
            e.accept(this);
            i++;
            if (i != size) {
                System.out.print(", ");
            }
        }
        
        System.out.print(" ) ");
    }

    @Override
    public void visit(ReferenceClassVariableExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
        System.out.print(".");
        System.out.print(node.getId());
    }

    @Override
    public void visit(ReferenceClassMethodExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
        System.out.print(".");
        System.out.print(node.getId());
        System.out.print(" ( ");
        
        int size = node.getExpressions().size();
        int i=0;
        
        for (Expression e : node.getExpressions()) {
            e.accept(this);
            i++;
            if (i != size) {
                System.out.print(", ");
            }
        }
        
        System.out.print(" ) ");
    }
    
    @Override
    public void visit(IdentifierExpression node) throws ASTVisitorException {
        System.out.print(node.getIdentifier());
    }

    @Override
    public void visit(IntegerLiteralExpression node) throws ASTVisitorException {
        System.out.print(node.getLiteral());
    }

    @Override
    public void visit(FloatLiteralExpression node) throws ASTVisitorException {
        System.out.print(node.getLiteral());
    }
    
    @Override
    public void visit(StringLiteralExpression node) throws ASTVisitorException {
        System.out.print("\"");
        System.out.print(StringEscapeUtils.escapeJava(node.getLiteral()));
        System.out.print("\"");
    }

    @Override
    public void visit(ParenthesisExpression node) throws ASTVisitorException {
        System.out.print("( ");
        node.getExpression().accept(this);
        System.out.print(" )");
    } 
 
    @Override
    public void visit(ThisExpression node) throws ASTVisitorException {
        System.out.print("this");
    }

    @Override
    public void visit(NullExpression node) throws ASTVisitorException {
        System.out.print("null ");
    }

}
