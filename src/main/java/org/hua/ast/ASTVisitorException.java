/**
 * This code is part of the lab exercises for the Compilers course at Harokopio
 * University of Athens, Dept. of Informatics and Telematics.
 */
package org.hua.ast;

/**
 * An exception thrown by any method of a {@link ASTVisitor} while traversing an
 * abstract syntax tree.
 */
public class ASTVisitorException extends Exception {

    private static final long serialVersionUID = 1L;

    public ASTVisitorException() {
        super();
    }

    public ASTVisitorException(String msg) {
        super(msg);
    }

    public ASTVisitorException(String msg, Throwable t) {
        super(msg, t);
    }

    public ASTVisitorException(Throwable t) {
        super(t);
    }

}
