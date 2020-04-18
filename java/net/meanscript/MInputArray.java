package net.meanscript;
import net.meanscript.core.*;
import net.meanscript.java.*;
/*

 *

 *    Meanscript ByteAutomata (c) 2020, Meanwhale

 *

 *    GitHub page:     https://github.com/Meanwhale/ByteAutomata

 *    Email:           meanwhale@gmail.com

 *    Twitter:         https://twitter.com/TheMeanwhale

 *

 */
public class MInputArray extends MInputStream {
byte buffer [];
int size;
int index;
public MInputArray (String s)
{
 buffer = s.getBytes();
 size = buffer.length;
 index = 0;
}
@Override
public int getByteCount ()
{
 return size;
}
@Override
public byte readByte () throws MException
{
 MSJava.assertion(!end(),null,"readInt: buffer overflow");
 return buffer[index++];
}
@Override
public boolean end ()
{
 return index >= size;
}
@Override
public void close ()
{
}

}
