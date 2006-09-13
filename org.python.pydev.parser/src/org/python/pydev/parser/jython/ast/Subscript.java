// Autogenerated AST node
package org.python.pydev.parser.jython.ast;
import org.python.pydev.parser.jython.SimpleNode;
import java.io.DataOutputStream;
import java.io.IOException;

public class Subscript extends exprType implements expr_contextType {
    public exprType value;
    public sliceType slice;
    public int ctx;

    public Subscript(exprType value, sliceType slice, int ctx) {
        this.value = value;
        this.slice = slice;
        this.ctx = ctx;
    }

    public Subscript(exprType value, sliceType slice, int ctx, SimpleNode
    parent) {
        this(value, slice, ctx);
        this.beginLine = parent.beginLine;
        this.beginColumn = parent.beginColumn;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("Subscript[");
        sb.append("value=");
        sb.append(dumpThis(this.value));
        sb.append(", ");
        sb.append("slice=");
        sb.append(dumpThis(this.slice));
        sb.append(", ");
        sb.append("ctx=");
        sb.append(dumpThis(this.ctx,
        expr_contextType.expr_contextTypeNames));
        sb.append("]");
        return sb.toString();
    }

    public void pickle(DataOutputStream ostream) throws IOException {
        pickleThis(46, ostream);
        pickleThis(this.value, ostream);
        pickleThis(this.slice, ostream);
        pickleThis(this.ctx, ostream);
    }

    public Object accept(VisitorIF visitor) throws Exception {
        return visitor.visitSubscript(this);
    }

    public void traverse(VisitorIF visitor) throws Exception {
        if (value != null)
            value.accept(visitor);
        if (slice != null)
            slice.accept(visitor);
    }

}
