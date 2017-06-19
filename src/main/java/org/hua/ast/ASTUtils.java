package org.hua.ast;

import java.util.ArrayList;
import java.util.List;
import org.hua.symbol.LocalIndexPool;
import org.objectweb.asm.Type;
import org.hua.symbol.SymTable;
import org.hua.symbol.SymTableEntry;
import org.objectweb.asm.tree.JumpInsnNode;

/**
 * Class with static helper methods for AST handling
 */
public class ASTUtils {

    public static final String SYMTABLE_PROPERTY = "SYMTABLE_PROPERTY";
    public static final String LOCAL_INDEX_POOL_PROPERTY = "LOCAL_INDEX_POOL_PROPERTY";
    public static final String IS_BOOLEAN_EXPR_PROPERTY = "IS_BOOLEAN_EXPR_PROPERTY";
    public static final String TYPE_PROPERTY = "TYPE_PROPERTY";
    public static final String BREAK_IN_LOOP = "BREAK_IN_LOOP";
    public static final String CONTINUE_IN_LOOP = "CONTINUE_IN_LOOP";
    public static final String THIS_IN_CLASS_FUN = "THIS_IN_CLASS_FUN";
    public static final String CONSTRUCTOR_EXIST = "CONSTRUCTOR_EXIST";
    public static final String THIS_TYPE = "THIS_TYPE";
    public static final String FUNCTION_TYPE = "FUNCTION_TYPE";
    public static final String CLASS_NAME = "CLASS_NAME";
    public static final String NEXT_LIST_PROPERTY = "NEXT_LIST_PROPERTY";
    public static final String BREAK_LIST_PROPERTY = "BREAK_LIST_PROPERTY";
    public static final String CONTINUE_LIST_PROPERTY = "CONTINUE_LIST_PROPERTY";
    public static final String TRUE_LIST_PROPERTY = "TRUE_LIST_PROPERTY";
    public static final String FALSE_LIST_PROPERTY = "FALSE_LIST_PROPERTY";

    private ASTUtils() {
    }

    @SuppressWarnings("unchecked")
    public static SymTable<SymTableEntry> getEnv(ASTNode node) {
        return (SymTable<SymTableEntry>) node.getProperty(SYMTABLE_PROPERTY);
    }

    @SuppressWarnings("unchecked")
    public static SymTable<SymTableEntry> getSafeEnv(ASTNode node)
            throws ASTVisitorException {
        SymTable<SymTableEntry> symTable = (SymTable<SymTableEntry>) node
                .getProperty(SYMTABLE_PROPERTY);
        if (symTable == null) {
            ASTUtils.error(node, "Symbol table not found.");
        }
        return symTable;
    }

    public static void setEnv(ASTNode node, SymTable<SymTableEntry> env) {
        node.setProperty(SYMTABLE_PROPERTY, env);
    }

    public static void setLocalIndexPool(ASTNode node, LocalIndexPool pool) {
        node.setProperty(LOCAL_INDEX_POOL_PROPERTY, pool);
    }

    @SuppressWarnings("unchecked")
    public static LocalIndexPool getSafeLocalIndexPool(ASTNode node)
            throws ASTVisitorException {
        LocalIndexPool lip = (LocalIndexPool) node.getProperty(LOCAL_INDEX_POOL_PROPERTY);
        if (lip == null) {
            ASTUtils.error(node, "Local index pool not found.");
        }
        return lip;
    }

    public static boolean isBooleanExpression(Expression node) {
        Boolean b = (Boolean) node.getProperty(IS_BOOLEAN_EXPR_PROPERTY);
        if (b == null) {
            return false;
        }
        return b;
    }

    public static void setBooleanExpression(Expression node, boolean value) {
        node.setProperty(IS_BOOLEAN_EXPR_PROPERTY, value);
    }

    public static Type getType(ASTNode node) {
        return (Type) node.getProperty(TYPE_PROPERTY);
    }

    public static Type getSafeType(ASTNode node) throws ASTVisitorException {
        Type type = (Type) node.getProperty(TYPE_PROPERTY);
        if (type == null) {
            ASTUtils.error(node, "Type not found.");
        }
        return type;
    }

    public static void setType(ASTNode node, Type type) {
        node.setProperty(TYPE_PROPERTY, type);
    }

    public static void setBreakInLoop (ASTNode node, Boolean inLoop) {
        node.setProperty(BREAK_IN_LOOP, inLoop);
    }
    
    public static Boolean getBreakInLoop (ASTNode node) {
        return ( Boolean ) node.getProperty(BREAK_IN_LOOP);
    }
    
    public static void setContinueInLoop (ASTNode node, Boolean inLoop) {
        node.setProperty(CONTINUE_IN_LOOP, inLoop);
    }
    
    public static Boolean getContinueInLoop (ASTNode node) {
        return ( Boolean ) node.getProperty(CONTINUE_IN_LOOP);
    }
    
    public static void setThisInClassFun (ASTNode node, Boolean inClassFun) {
        node.setProperty(THIS_IN_CLASS_FUN, inClassFun);
    }
    
    public static Boolean getThisInClassFun (ASTNode node) {
        return ( Boolean ) node.getProperty(THIS_IN_CLASS_FUN);
    }
    
    public static void setConstructorExist (ASTNode node, Boolean constructorExist) {
        node.setProperty(CONSTRUCTOR_EXIST, constructorExist);
    }
    
    public static Boolean getConstructorExist (ASTNode node) {
        return ( Boolean ) node.getProperty(CONSTRUCTOR_EXIST);
    }
    
    public static void setThisType (ASTNode node, Type thisType) {
        node.setProperty(THIS_TYPE, thisType);
    }
    
    public static Type getThisType (ASTNode node) {
        return ( Type ) node.getProperty(THIS_TYPE);
    }
    
    public static void setFunctionType (ASTNode node, String functionType) {
        node.setProperty(FUNCTION_TYPE, functionType);
    }
    
    public static String getFunctionType (ASTNode node) {
        return ( String ) node.getProperty(FUNCTION_TYPE);
    }
    
    public static void setClassName (ASTNode node, String className) {
        node.setProperty(CLASS_NAME, className);
    }
    
    public static String getClassName (ASTNode node) {
        return ( String ) node.getProperty(CLASS_NAME);
    }

    @SuppressWarnings("unchecked")
    public static List<JumpInsnNode> getTrueList(ASTNode node) {
        List<JumpInsnNode> l = (List<JumpInsnNode>) node.getProperty(TRUE_LIST_PROPERTY);
        if (l == null) {
            l = new ArrayList<JumpInsnNode>();
            node.setProperty(TRUE_LIST_PROPERTY, l);
        }
        return l;
    }

    public static void setTrueList(ASTNode node, List<JumpInsnNode> list) {
        node.setProperty(TRUE_LIST_PROPERTY, list);
    }

    @SuppressWarnings("unchecked")
    public static List<JumpInsnNode> getFalseList(ASTNode node) {
        List<JumpInsnNode> l = (List<JumpInsnNode>) node.getProperty(FALSE_LIST_PROPERTY);
        if (l == null) {
            l = new ArrayList<JumpInsnNode>();
            node.setProperty(FALSE_LIST_PROPERTY, l);
        }
        return l;
    }

    public static void setFalseList(ASTNode node, List<JumpInsnNode> list) {
        node.setProperty(FALSE_LIST_PROPERTY, list);
    }

    @SuppressWarnings("unchecked")
    public static List<JumpInsnNode> getNextList(ASTNode node) {
        List<JumpInsnNode> l = (List<JumpInsnNode>) node.getProperty(NEXT_LIST_PROPERTY);
        if (l == null) {
            l = new ArrayList<JumpInsnNode>();
            node.setProperty(NEXT_LIST_PROPERTY, l);
        }
        return l;
    }

    public static void setNextList(ASTNode node, List<JumpInsnNode> list) {
        node.setProperty(NEXT_LIST_PROPERTY, list);
    }

    @SuppressWarnings("unchecked")
    public static List<JumpInsnNode> getBreakList(ASTNode node) {
        List<JumpInsnNode> l = (List<JumpInsnNode>) node.getProperty(BREAK_LIST_PROPERTY);
        if (l == null) {
            l = new ArrayList<JumpInsnNode>();
            node.setProperty(BREAK_LIST_PROPERTY, l);
        }
        return l;
    }

    public static void setBreakList(ASTNode node, List<JumpInsnNode> list) {
        node.setProperty(BREAK_LIST_PROPERTY, list);
    }

    @SuppressWarnings("unchecked")
    public static List<JumpInsnNode> getContinueList(ASTNode node) {
        List<JumpInsnNode> l = (List<JumpInsnNode>) node.getProperty(CONTINUE_LIST_PROPERTY);
        if (l == null) {
            l = new ArrayList<JumpInsnNode>();
            node.setProperty(CONTINUE_LIST_PROPERTY, l);
        }
        return l;
    }

    public static void setContinueList(ASTNode node, List<JumpInsnNode> list) {
        node.setProperty(CONTINUE_LIST_PROPERTY, list);
    }

    public static void error(ASTNode node, String message)
            throws ASTVisitorException {
        throw new ASTVisitorException(node.getLine() + ":" + node.getColumn()
                + ": " + message);
    }

}
