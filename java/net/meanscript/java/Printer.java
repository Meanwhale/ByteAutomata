package net.meanscript.java;
import net.meanscript.core.*;

public class Printer
{
	// override to implement own printer
	public Printer print(Object o)
	{
		System.out.print(o.toString());
		return this;
	}
	public final Printer print(int i)
	{
		print(Integer.toString(i));
		return this;
	}
	public final Printer print(char c)
	{
		print(Character.toString(c));
		return this;
	}
	public final static String [] hexs = {
			"0","1","2","3",
			"4","5","6","7",
			"8","9","a","b",
			"c","d","e","f",
			};
	public final Printer printHex(int h) {
		print("0x");
		for (int i = 28; i >= 0; i -=4 )
		{
			int index = (h>>>i);
			index &= 0x0000000f;
			print(hexs[index]);
		}
		return this;
	}
}
// Java END
