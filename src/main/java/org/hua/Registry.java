/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
package org.hua;

import java.util.Map;
import org.hua.ast.ASTNode;
import org.hua.symbol.SymTable;
import org.hua.symbol.SymTableEntry;
import org.objectweb.asm.Type;
/**
 * Global registry (Singleton pattern)
 */
public class Registry {

    ASTNode root;
    Map <Type,SymTable<SymTableEntry>> map;

    private Registry() {
        root = null;
        map = null;
    }

    private static class SingletonHolder {

        public static final Registry instance = new Registry();

    }

    public static Registry getInstance() {
        return SingletonHolder.instance;
    }

    public ASTNode getRoot() {
        return root;
    }

    public void setRoot(ASTNode root) {
        this.root = root;
    }

    public Map<Type, SymTable<SymTableEntry>> getMap() {
        return map;
    }

    public void setMap(Map<Type, SymTable<SymTableEntry>> map) {
        this.map = map;
    }
}
