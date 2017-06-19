/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
package org.hua.ast;

import java.util.ArrayList;
import java.util.List;

public class CompilationUnit extends ASTNode {

    private List<Definitions> definitions;

    public CompilationUnit() {
        definitions = new ArrayList<Definitions>();
    }

    public CompilationUnit(List<Definitions> definitions) {
        this.definitions = definitions;
    }
    

    public List<Definitions> getDefinitions() {
        return definitions;
    }

    public void setDefinition(List<Definitions> definitions) {
        this.definitions = definitions;
    }
    
    @Override
    public void accept(ASTVisitor visitor) throws ASTVisitorException {
        visitor.visit(this);
    }

}
