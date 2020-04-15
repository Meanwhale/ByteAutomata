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
public class NodeIterator {
 MNode node;
public NodeIterator (MNode _node)
{
 node = _node;
}
public NodeIterator copy ()
{
 return new NodeIterator(node);
}
public int type ()
{
 return node.type;
}
public String data ()
{
 return node.data;
}
public MNode getChild()
{
 return node.child;
}
public MNode getNext()
{
 return node.next;
}
public MNode getParent()
{
 return node.parent;
}
public int numChildren ()
{
 return node.numChildren;
}
public boolean hasNext()
{
 return node.next != null;
}
public boolean hasChild()
{
 return node.child != null;
}
public boolean hasParent()
{
 return node.parent != null;
}
public int nextType() throws MException
{
 MSJava.assertion(hasNext(), "nextType: no next");
 return node.next.type;
}
public void toNext() throws MException
{
 MSJava.assertion(hasNext(), "toNext: no next");
 node = node.next;
}
public boolean toNextOrFalse()
{
 if (!hasNext()) return false;
 node = node.next;
 return true;
}
public void toChild() throws MException
{
 MSJava.assertion(hasChild(), "toChild: no child");
 node = node.child;
}
public void toParent() throws MException
{
 MSJava.assertion(hasParent(), "toParent: no parent");
 node = node.parent;
}
public void printTree(boolean deep) throws MException
{
 node.printTree(deep);
}
}
