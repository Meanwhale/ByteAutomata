package net.meanscript.java;
import net.meanscript.core.*;

public abstract class MSJava
{
	public static boolean debug = true;
	
	public static Printer debugPrinter = new Printer();
	public static Printer verbosePrinter = new Printer();
	public static Printer userPrinter = new Printer();

	public static Printer print(Object a)
	{
		printn(a);
		debugPrinter.print("\n");
		return debugPrinter;
	}
	public static Printer printn(Object o)
	{
		debugPrinter.print(o.toString());
		return debugPrinter;
	}
	public static Printer verbose(Object o)
	{
		verbosen(o);
		verbosePrinter.print("\n");
		return verbosePrinter;
	}
	public static Printer verbosen(Object o)
	{
		verbosePrinter.print(o.toString());
		return verbosePrinter;
	}
	public static Printer userPrint(Object o)
	{
		userPrinter.print(o.toString());
		userPrinter.print("\n");
		return userPrinter;
	}
	public static void assertion(boolean b, String msg) throws MException
	{
		if (!b)
		{
			throw new MException(null, msg);
		}
	}
	public static void assertion(boolean b, MSJavaError error, String msg) throws MException
	{
		if (!b)
		{
			throw new MException(error, msg);
		}
	}

	public static float parseFloat(String s) throws MException
	{
		try {
			return Float.parseFloat(s);
		} catch (NumberFormatException e) {
			assertion(false, null, "malformed float: " + s);
			return Float.NaN;
		}
	}
	public static int floatToIntFormat(float f)
	{
		return Float.floatToIntBits(f);
	}
	public static float intFormatToFloat(int i)
	{
		return Float.intBitsToFloat(i);
	}
	public static byte[] intsToBytes(int [] ia, int iaOffset, int bytesLength)
	{
		byte [] bytes = new byte[bytesLength];
		int shift = 24;
		for (int i = 0; i < bytesLength;)
		{
			bytes[i] = (byte)((ia[iaOffset + (i/4)] >> shift) & 0x000000FF);
			
			i++;
			if (i % 4 == 0) shift = 24;
			else shift -= 8;
		}
		return bytes;
	}
	
	public static int[] bytesToInts(byte[] ba) 
	{
		int bytesLength = ba.length;
		int intsLength = (bytesLength / 4) + 1;
		int[] ints = new int[intsLength];
		
		int shift = 24;
		for (int i = 0; i < bytesLength;)
		{
			ints[i/4] += (ba[i] & 0x000000FF) << shift;
			
			i++;
			if (i % 4 == 0) shift = 24;
			else shift -= 8;
		}
		return ints;
	}
}