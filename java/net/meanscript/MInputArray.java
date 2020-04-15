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
//CONSTRUCTOR (INT_ARRAY_REF arr) THROWS
//{
//	size = ARRAY_LENGTH(arr);
//	INT_ARRAY_RESET(buffer,size);
//	index = 0;
//	for(INT i=0; i<size; i++) buffer[i] = arr[i];
//}
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
