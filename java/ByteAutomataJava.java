import net.meanscript.core.*;
import net.meanscript.java.*;
import java.util.Scanner;

public class ByteAutomataJava
{
	public static void main (String [] args)
	{
		try
		{
			MicroLexer.printTitle("Java");

			if (args.length == 1)
			{
				MNode root = null;
				if (args[0].equals("-i"))
				{
					MicroLexer.printStdinInfo();
				        Scanner scanner = new Scanner(System.in);
				        String input = "";
					while (scanner.hasNext()) input += scanner.nextLine() + '\n';
					root = MicroLexer.lex(input);
				}
				else if (args[0].equals("-t"))
				{
					root = MicroLexer.lex("abc 123 (def 456 [ghi])");
				}
				else
				{
					root = MicroLexer.lex(args[0]);
				}
				MSJava.print("TOKEN TREE:");
				if (root != null) root.printTree(true);
			}
			else
			{
				MicroLexer.printArgInfo();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}