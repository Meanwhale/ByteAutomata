/*
 *
 *    Meanscript ByteAutomata (c) 2020, Meanwhale
 *
 *    GitHub page:     https://github.com/Meanwhale/ByteAutomata
 *    Email:           meanwhale@gmail.com
 *    Twitter:         https://twitter.com/TheMeanwhale
 *
 */
package net.meanscript;
import net.meanscript.core.*;
import net.meanscript.java.*;
public abstract class MInputStream {
public MInputStream ()
{
}
public abstract int getByteCount ();
public abstract byte readByte () throws MException;
public abstract boolean end ();
public abstract void close ();
public int readInt () throws MException
{
 // bytes:	b[0] b[1] b[2] b[3] b[4] b[5] b[6] b[7]   ...
 // ints:	_________i[0]______|_________i[1]______|_ ...
 int i = 0;
 i |= (readByte() << 24) & 0xff000000;
 i |= (readByte() << 16) & 0x00ff0000;
 i |= (readByte() << 8) & 0x0000ff00;
 i |= (readByte()) & 0x000000ff;
 return i;
}
public void readArray (int [] trg, int numInts) throws MException
{
 MSJava.assertion(numInts <= (getByteCount() * 4) + 1,null,"readArray: buffer overflow");
 for (int i=0; i < numInts; i++)
 {
  trg[i] = readInt();
 }
 MSJava.assertion(end(), "all bytes not read");
}
}
