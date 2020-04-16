/*
 *
 *    Meanscript ByteAutomata (c) 2020, Meanwhale
 *
 *    GitHub page:     https://github.com/Meanwhale/ByteAutomata
 *    Email:           meanwhale@gmail.com
 *    Twitter:         https://twitter.com/TheMeanwhale
 *
 */
package net.meanscript.core;
import net.meanscript.java.*;
import net.meanscript.*;
public class MNode {
 int type;
 int numChildren;
 String data;
 MNode next = null;
 MNode child = null;
 MNode parent = null;
public MNode (MNode _parent, int _type, String _data)
{
 data = _data;
 parent = _parent;
 type = _type;
 numChildren = 0;
}
public void printTree (boolean deep) throws MException
{
 printTree(this, 0, deep);
 if (!deep) MSJava.verbose("");
}
public void printTree (MNode _node, int depth, boolean deep) throws MException
{
 MSJava.assertion(_node != null, "<printTree: empty node>");
 MNode node = _node;
 for (int i = 0; i < depth; i++) MSJava.verbosen("  ");
 MSJava.verbosen("[").print(node.data).print("]");
 // if (node.numChildren > 0) { VR(" + ")X(node.numChildren); }
 if (deep) MSJava.verbose("");
 if (node.child != null && deep) printTree(node.child, depth + 1, deep);
 if (node.next != null) printTree(node.next, depth, deep);
}
;
}
