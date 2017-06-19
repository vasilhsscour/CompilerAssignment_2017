/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
package org.hua;

import java.util.List;
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
import org.hua.ast.Definition;
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
import org.hua.types.TypeUtils;
import org.hua.ast.UnaryExpression;
import org.hua.ast.VariableDeclarationStatement;
import org.hua.ast.WhileStatement;
import org.hua.ast.WriteStatement;
import org.objectweb.asm.Type;
import org.hua.types.TypeException;

/**
 * Compute possible types for each node.
 */
public class CollectTypesASTVisitor implements ASTVisitor {

    public CollectTypesASTVisitor() {
    }
    
    private Type functionType = TypeUtils.NULL_TYPE;

    @Override
    public void visit(CompilationUnit node) throws ASTVisitorException {
        for (Definition s : node.getDefinitions()) {
            s.accept(this);
        }
        
        SymTable<SymTableEntry> env = ASTUtils.getSafeEnv(node);
        
        SymTableEntry main = env.lookup("main");
        
        if (main == null) {
            String message = "main does not exist!";
            ASTUtils.error(node, message);
        }
        
        ASTUtils.setType(node, Type.VOID_TYPE);
    }
    
    @Override
    public void visit(Definitions node) throws ASTVisitorException {
        ClassDefinition classDefinition = node.getClassDefinition();
        FunctionDefinition functionDefinition = node.getFunctionDefinition();
        
        if ( classDefinition != null) {
            classDefinition.accept(this);
            ASTUtils.setType(node, TypeUtils.FUNCTION_TYPE);
        }
        
        if ( functionDefinition != null ) {
            functionDefinition.accept(this);
            ASTUtils.setType(node, TypeUtils.CLASS_TYPE);
        }
    }

    @Override
    public void visit(ClassDefinition node) throws ASTVisitorException {
        for(FieldOrFunctionDefinition st: node.getDefinitions()) { 
            st.accept(this);
        }
        ASTUtils.setType(node, Type.getType(TypeUtils.MAKE_TYPE+node.getId()+";"));
    }

    @Override
    public void visit(FieldOrFunctionDefinition node) throws ASTVisitorException {
        if ( node.getFieldDefinition().getColumn() != 0) {
            node.getFieldDefinition().accept(this);
        }
        else{
            node.getFunctionDefinition().accept(this);
        }
        ASTUtils.setType(node, Type .VOID_TYPE);
    }

    @Override
    public void visit(FieldDefinition node) throws ASTVisitorException {
        Type type = node.getTypeSpecifier();
        ASTUtils.setType(node, type);
    }

    @Override
    public void visit(FunctionDefinition node) throws ASTVisitorException {
        Type type = node.getTypeSpecifier();
        if (type == null) {
            type = Type.VOID_TYPE;
        }
        this.functionType = type;
        
        String funType = "(";
        for(ParameterDeclaration pd: node.getParameters()) { 
            pd.accept(this);
            funType += pd.getTypeSpecifier();
        }
        node.getStmt().accept(this);
        funType += ")"+type;
        ASTUtils.setFunctionType(node, funType);
        ASTUtils.setType(node, type);
        
    }

    @Override
    public void visit(ParameterDeclaration node) throws ASTVisitorException {
        Type type = node.getTypeSpecifier();        
        ASTUtils.setType(node, type);
    }
 
    @Override
    public void visit(WriteStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        ASTUtils.setType(node, Type.VOID_TYPE);
    }
    
    @Override
    public void visit(AssignmentStatement node) throws ASTVisitorException {
        node.getExpression1().accept(this);
        node.getExpression2().accept(this);
        
        Type exp1Type = ASTUtils.getSafeType(node.getExpression1());        
        Type exp2Type = ASTUtils.getSafeType(node.getExpression2());        
        
        Boolean bool = TypeUtils.isAssignable(exp1Type, exp2Type);
        
        if ( !bool ) {
            ASTUtils.error(node, " No Assignable statement!");
        }
        else {
            ASTUtils.setType(node, exp1Type);
        }
    }
    
    @Override
    public void visit(CompoundStatement node) throws ASTVisitorException {
        for (Statement st : node.getStatements()) {
            st.accept(this);
        }
        ASTUtils.setType(node, Type.VOID_TYPE);
    }
    
    @Override
    public void visit(WhileStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        if (!ASTUtils.getSafeType(node.getExpression()).equals(Type.BOOLEAN_TYPE)) {
            ASTUtils.error(node.getExpression(), "Invalid expression, should be boolean");
        }
        node.getStmt().accept(this);
        
        ASTUtils.setType(node, Type.VOID_TYPE);
    }
    
    @Override
    public void visit(DoWhileStatement node) throws ASTVisitorException {
        node.getStmt().accept(this);
        node.getExpression().accept(this);
        if (!ASTUtils.getSafeType(node.getExpression()).equals(Type.BOOLEAN_TYPE)) {
            ASTUtils.error(node.getExpression(), "Invalid expression, should be boolean");
        }
        ASTUtils.setType(node, Type.VOID_TYPE);
        
    }
    @Override
    public void visit(IfStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        if (!ASTUtils.getSafeType(node.getExpression()).equals(Type.BOOLEAN_TYPE)) {
            ASTUtils.error(node.getExpression(), "Invalid expression, should be boolean");
        }
        node.getStatement().accept(this);
        ASTUtils.setType(node, Type.VOID_TYPE);
    }
    
    @Override
    public void visit(IfElseStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        if (!ASTUtils.getSafeType(node.getExpression()).equals(Type.BOOLEAN_TYPE)) {
            ASTUtils.error(node.getExpression(), "Invalid expression, should be boolean");
        }
        node.getStatement1().accept(this);
        node.getStatement2().accept(this);
        ASTUtils.setType(node, Type.VOID_TYPE);
    }
    
    @Override
    public void visit(ExpressionStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        ASTUtils.setType(node, ASTUtils.getSafeType(node.getExpression()));
    }

    @Override
    public void visit(VariableDeclarationStatement node) throws ASTVisitorException {
        Type type = node.getTypeSpecifier();
        ASTUtils.setType(node, type);
    }

    @Override
    public void visit(ReturnStatement node) throws ASTVisitorException {
        node.getExpression().accept(this);
        Type returnType = ASTUtils.getSafeType(node.getExpression());
        String message = null;
        
        
        if (this.functionType.equals(TypeUtils.NULL_TYPE)) {
            message = "Return statement out of method";
            ASTUtils.error(node, message);
        }
        else if (this.functionType.equals(Type.VOID_TYPE)) {
            if (!returnType.equals(Type.INT_TYPE)) {
                message = "Return statement in void method";
                ASTUtils.error(node, message);
            }
            else {
                ASTUtils.setType(node, returnType);
            }
        }
        else if (!TypeUtils.areComparable(this.functionType, returnType)){
            message = "Return statement have deferent type!";
            ASTUtils.error(node, message);
        }
        else {
            ASTUtils.setType(node, returnType);
        }
    }
    
    @Override
    public void visit(BreakStatement node) throws ASTVisitorException {
        Boolean inLoop = ASTUtils.getBreakInLoop(node);
        if (!inLoop) {
            String message = "Break out of loop";
            ASTUtils.error(node, message);
        }
        ASTUtils.setType(node, Type.VOID_TYPE);
    }

    @Override
    public void visit(ContinueStatement node) throws ASTVisitorException {
        ASTUtils.setType(node, Type.VOID_TYPE);
        Boolean inLoop = ASTUtils.getContinueInLoop(node);
        if (!inLoop) {
            String message = "Continue out of loop";
            ASTUtils.error(node, message);
        }
    }

    @Override
    public void visit(BinaryExpression node) throws ASTVisitorException {
        node.getExpression1().accept(this);
        node.getExpression2().accept(this);
        
        Type type1 = ASTUtils.getSafeType(node.getExpression1());
        Type type2 = ASTUtils.getSafeType(node.getExpression2());
        
        try {
            Type type = TypeUtils.applyBinary(node.getOperator(), type1, type2, node.getLine());
            ASTUtils.setType(node, type);
        } catch (TypeException ex) {
            ASTUtils.error(node, ex.getMessage());
        } 
    }

    @Override
    public void visit(UnaryExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
        try {
            ASTUtils.setType(node, TypeUtils.applyUnary(node.getOperator(), ASTUtils.getSafeType(node.getExpression())));
        } catch (TypeException e) {
            ASTUtils.error(node, e.getMessage());
        }
    }

    @Override
    public void visit(FunctionExpression node) throws ASTVisitorException {
        SymTable<SymTableEntry> env = ASTUtils.getSafeEnv(node);
        String id = node.getId();
        SymTableEntry symTableEntry = env.lookup(id);
        
        if (symTableEntry == null) {
            String message = "The method " + id + "don't exist!";
            ASTUtils.error(node, message);
        }
        List<Expression> expressions = node.getExpressions();
        List<ParameterDeclaration> parameters = symTableEntry.getParameters();
        Type type = symTableEntry.getType();
            
        for (Expression e : expressions) {
           e.accept(this);
        }
        
        int numOfExpr = expressions.size();
        int numOfParams = parameters.size();
        
        if (numOfExpr != numOfParams) {
            String message = "Wrong value of parameter in function " +id;
            ASTUtils.error(node, message);
        }
        else {
            for (int i=0; i<numOfParams; i++) {
                Expression expression = expressions.get(i);
                Type exprType = ASTUtils.getSafeType(expression);
                
                ParameterDeclaration parameter = parameters.get(i);
                Type paramType = parameter.getTypeSpecifier();
                
                if (!TypeUtils.isAssignable(paramType, exprType)) { // If the type of given parameter is not equals to the type of constuctor parameter
                    String message = "\nWrong type in function " + id + "\nExpected Type : " + paramType + "!" + "\nGiven Type :  " + exprType;
                    ASTUtils.error(node, message);
                }
            }
        }
        
        ASTUtils.setType(node, type);
    }

    @Override
    public void visit(ConstructorExpression node) throws ASTVisitorException {
        String className = node.getId();
        Type type = Type.getType(TypeUtils.MAKE_TYPE+className+";");
        List<Expression> expressions = node.getExpressions();
        int numOfExpressions = expressions.size();
        
        for (Expression e : expressions) {
            e.accept(this);
        }
        SymTable<SymTableEntry> env = Registry.getInstance().getMap().get(type);
        
        if (env == null) {
            String message = "Class with type "+type+" don't exist!";
            ASTUtils.error(node, message);
        }
        
        SymTableEntry symTable = env.lookup(className); // find the symbol table of the constuctor
        
        if (symTable != null) { // if the symbol table isn't null
            for (FieldOrFunctionDefinition ffd : symTable.getDefinitions()){ // for each fieldor function definition
                FunctionDefinition fd = ffd.getFunctionDefinition(); // take the function definition
                if ( fd != null) {
                    String functionName = fd.getId(); // take the name of the function
                    int numOfParams = fd.getParameters().size(); // the number of parameters in constuctor
                    if (functionName.equals(className)) { // if the constuctor exist
                        if (numOfParams != numOfExpressions) { // if the number of parameter is different of the given parameters
                            String message = "Wrong value of parameters in class " + className;
                            ASTUtils.error(node, message);
                        } else { // if the number of parameter is equals of the given parameters
                            for (int i = 0; i < numOfParams; i++) { // for each parameter check the type
                                Expression expression = expressions.get(i); // the given parameter
                                Type exprType = ASTUtils.getSafeType(expression);  // the given parameter type

                                ParameterDeclaration parameter = fd.getParameters().get(i); // the constuctor parameter
                                Type paramType = parameter.getTypeSpecifier();

                                if (!TypeUtils.isAssignable(paramType, exprType)) { // If the type of given parameter is not equals to the type of constuctor parameter
                                    String message = "\nWrong type in constuctor " + className + "\nExpected Type : " + paramType + "!" + "\nGiven Type :  " + exprType;
                                    ASTUtils.error(node, message);
                                }
                            }
                        }
                    } else {
                        if (numOfExpressions != 0) { // If the class has no fields and constructor
                            String message = "Constuctor don't exist in class " + className;
                            ASTUtils.error(node, message);
                        }
                    }
                }
            }
        }
        
        ASTUtils.setType(node, type);
    }

    @Override
    public void visit(ReferenceClassVariableExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);  
        Type expressionType = ASTUtils.getSafeType(node.getExpression());
        String exprTypeString = expressionType.toString();
        String [] findClass = null;
        if (exprTypeString.contains("/")) {
            findClass = exprTypeString.split("/");
        }
        else {
            throw new IllegalArgumentException("Expression type does not contain /");
        }
        int splitSize = findClass.length - 1;
        String className = findClass[splitSize].replace(";", "");

        SymTable<SymTableEntry> classEnv = Registry.getInstance().getMap().get(expressionType);
        
        String fieldName = node.getId();
        SymTableEntry symTableEntry = classEnv.lookup(className);
        
        if (symTableEntry == null) {
            String message = " The class "+className+" don't exist!";
            ASTUtils.error(node, message);
        }
        else {
            Type type = symTableEntry.getType();
            List<FieldOrFunctionDefinition> definitions = symTableEntry.getDefinitions();
            FieldDefinition field = null;
            
            for (FieldOrFunctionDefinition ffd : definitions) {
                FieldDefinition fd = ffd.getFieldDefinition();
                if (fd != null) {
                    String fdName = fd.getId();
                    if (fdName.equals(fieldName)) {
                        field = fd;
                    }
                }
            }
            
            if (field == null) {
                String message = "The field " + fieldName+ " does not exist in class " +className;
                ASTUtils.error(node, message);
            }
            else {
                type = field.getTypeSpecifier();
            } 
            
            ASTUtils.setType(node, type);
            ASTUtils.setClassName(node, className);
        }
    }

    @Override
    public void visit(ReferenceClassMethodExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);  
        Type expressionType = ASTUtils.getSafeType(node.getExpression());
        String exprTypeString = expressionType.toString();
        String [] findClass = null;
        if (exprTypeString.contains("/")) {
            findClass = exprTypeString.split("/");
        }
        else {
            throw new IllegalArgumentException("Expression type does not contain /");
        }
        int splitSize = findClass.length - 1;
        String className = findClass[splitSize].replace(";", "");

        SymTable<SymTableEntry> classEnv = Registry.getInstance().getMap().get(expressionType);
        
        String methodName = node.getId();
        List<Expression> expressions = node.getExpressions();

        SymTableEntry symTableEntry = classEnv.lookup(className);
        
        if (symTableEntry == null) {
            String message = " The class "+className+" don't exist!";
            ASTUtils.error(node, message);
        }
        else {
            Type type = symTableEntry.getType();
            for (Expression e : expressions) {
                e.accept(this);
            }
            
            List<FieldOrFunctionDefinition> definitions = symTableEntry.getDefinitions();
            FunctionDefinition function = null;
            
            for (FieldOrFunctionDefinition ffd : definitions) {
                FunctionDefinition fd = ffd.getFunctionDefinition();
                if (fd != null) {
                    String funName = fd.getId();
                    if (funName.equals(methodName)) {
                        function = fd;
                    }
                }
            }
            
            if (function == null) {
                String message = "The Function " + methodName+ " does not exist in class " +className;
                ASTUtils.error(node, message);
            }
            else {
                List<ParameterDeclaration> parameters = function.getParameters();
                int paramSize = parameters.size();
                int exprSize = expressions.size();
                
                if (paramSize != exprSize) {
                    String message = "Wrong value of parameters in method " + methodName;
                    ASTUtils.error(node, message);
                }
                else {
                    for (int i=0; i<paramSize; i++) {
                        Expression expression = expressions.get(i);
                        Type exprType = ASTUtils.getSafeType(expression);
                        ParameterDeclaration parameter  = parameters.get(i);
                        Type paramType = parameter.getTypeSpecifier();
                        
                        if (!TypeUtils.isAssignable(paramType,exprType)) {
                            String message = "\nWrong type in constuctor " + className + "\nExpected Type : " + paramType + "!" + "\nGiven Type :  " + exprType;
                            ASTUtils.error(node, message);
                        }
                    }
                    
                    type = function.getTypeSpecifier();
                }
            }
            
            ASTUtils.setType(node, type);
            ASTUtils.setClassName(node, className);
        }
    }
    
    @Override
    public void visit(IdentifierExpression node) throws ASTVisitorException {
        SymTable<SymTableEntry> env = ASTUtils.getSafeEnv(node);
        String id = node.getIdentifier();
        SymTableEntry symTableEntry = env.lookup(id);
        
        if ( symTableEntry == null ) {
            ASTUtils.error(node, "Not Definied!");
        }
        else {
            Type type = symTableEntry.getType();
            ASTUtils.setType(node, type);
        }
    }

    @Override
    public void visit(IntegerLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setType(node, Type.INT_TYPE);
        
    }

    @Override
    public void visit(FloatLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setType(node, Type.FLOAT_TYPE);
    }

    @Override
    public void visit(StringLiteralExpression node) throws ASTVisitorException {
        ASTUtils.setType(node, TypeUtils.STRING_TYPE);
    }

    @Override
    public void visit(ParenthesisExpression node) throws ASTVisitorException {
        node.getExpression().accept(this);
        ASTUtils.setType(node, ASTUtils.getSafeType(node.getExpression()));
    }
    
    @Override
    public void visit(ThisExpression node) throws ASTVisitorException {
        Boolean exist = ASTUtils.getThisInClassFun(node);
        if (!exist) {
            String message = "This out of Class Function!";
            ASTUtils.error(node, message);
        }
        ASTUtils.setType(node, ASTUtils.getThisType(node));
    }

    @Override
    public void visit(NullExpression node) throws ASTVisitorException {
        ASTUtils.setType(node, TypeUtils.NULL_TYPE);
    }

}
