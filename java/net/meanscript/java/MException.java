package net.meanscript.java;
import net.meanscript.core.*;

public class MException extends Exception
{
	private final static String begin = "---------------- EXCEPTION ----------------\n";
	private final static String end   = "\n-------------------------------------------";
	public final MSJavaError error;
	private String info = "Meanscript exception";
	public MException()
	{
		error = null;
	}
	public MException(MSJavaError _error, String msg)
	{
		error = null;
		info = msg;
	}
	public String toString()
	{
		return begin + info + end;
	}
}

// Java END
