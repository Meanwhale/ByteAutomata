using System;

namespace meanscript
{
public class MException : System.Exception
{
	private static string begin = "---------------- EXCEPTION ----------------\n";
	private static string end   = "\n-------------------------------------------";
	public readonly MSError error;
	private string info = "Meanscript exception";
	public MException() { error = null; }
	public MException(MSError err, string s) { error = err; info = s; }
	public string toString() { return begin + info + end; }
}

public class Printer
{
	// override to implement own printer
	public Printer print(object o)
	{
		Console.Write(o);
		return this;
	}
	public Printer print(int i)
	{
		print(""+i);
		return this;
	}
	public Printer print(char c)
	{
		print(""+c);
		return this;
	}
	public static string [] hexs = new string [] {
			"0","1","2","3",
			"4","5","6","7",
			"8","9","a","b",
			"c","d","e","f",
			};
	public Printer printHex(int h) {
		print("0x");
		for (int i = 28; i >= 0; i -=4 )
		{
			int index = (h>>i); // TODO: zero fill?
			index &= 0x0000000f;
			print(hexs[index]);
		}
		return this;
	}
}
public class MSError
{
	public readonly MSError type;
	public readonly string title;

	public MSError(MSError type, string title)
	{
		this.type = type;
		this.title = title;
	}
}

public class MeanCS
{
public static bool debug = true;

public delegate void MAction();
//public delegate void MCallbackAction(MeanMachine mm, MArgs args);
	
public static Printer debugPrinter = new Printer();
public static Printer verbosePrinter = new Printer();
public static Printer userPrinter = new Printer();

public static Printer print(params object [] args)
{
	printn(args);
	debugPrinter.print("\n");
	return debugPrinter;
}
public static Printer printn(params object [] args)
{
	foreach (object o in args) debugPrinter.print(o);
	return debugPrinter;
}
public static Printer verbose(params object [] args)
{
	verbosen(args);
	verbosePrinter.print("\n");
	return verbosePrinter;
}
public static Printer verbosen(params object [] args)
{
	foreach (object o in args) verbosePrinter.print(o);
	return verbosePrinter;
}
public static Printer userPrint(params object [] args)
{
	foreach (object o in args) userPrinter.print(o);
	userPrinter.print("\n");
	return userPrinter;
}
public static void assertion(bool b, string msg) 
{
	if (!b) { throw new MException(null, "assertion failed: " + msg); }
}
public static void assertion(bool b, MSError err, string msg) 
{
	if (!b) { throw new MException(err, "assertion failed: " + msg); }
}
	
public static void test()
{
	string s = "Toimii!";
	byte [] bytes = System.Text.Encoding.ASCII.GetBytes(s);
	int [] ia = bytesToInts(bytes);
	byte [] ba = intsToBytes(ia,0,7);
	string ns = System.Text.Encoding.UTF8.GetString(ba);
		
	debugPrinter.print("Toimii? " + s.Equals(ns));
}
	
public static byte[] intsToBytes(int [] ia, int iaOffset, int bytesLength)
{
	byte [] bytes = new byte[bytesLength];

	int shift = 0;
	for (int i = 0; i < bytesLength;)
	{
	    //ints[i/4] += (ba[i] & 0x000000FF) << shift;
	    bytes[i] = (byte)((ia[iaOffset + (i/4)] >> shift) & 0x000000FF);

	    i++;
	    if (i % 4 == 0) shift = 0;
	    else shift += 8;
	}
	return bytes;
}
	
public static int[] bytesToInts(byte[] ba) 
{
	int bytesLength = ba.Length;
	int intsLength = (bytesLength / 4) + 1;
	int[] ints = new int[intsLength];
	    
	int shift = 0;
	for (int i = 0; i < bytesLength;)
	{
	    ints[i/4] += (ba[i] & 0x000000FF) << shift;
	        
	    i++;
	    if (i % 4 == 0) shift = 0;
	    else shift += 8;
	}
	return ints;
}
public static int floatToIntFormat(float f)
{
	return BitConverter.ToInt32(BitConverter.GetBytes(f),0);
}
public static float intFormatToFloat(int i)
{
	return BitConverter.ToSingle(BitConverter.GetBytes(i), 0);
}

		internal static float parseFloat(string s)
		{
			try
			{
				return float.Parse(s, System.Globalization.CultureInfo.InvariantCulture);
			}
			catch (Exception)
			{
				throw new Exception("float parsing failed: " + s);
			}
		}
	}
}
