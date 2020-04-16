import net.meanscript.core.*;
import net.meanscript.java.*;
import java.util.Scanner;

public class BAJava
{
	public static void main (String [] args)
	{
		try
		{
			MSJava.print("\n    ByteAutomata (c) 2020, Meanwhale");
			MSJava.print("    https://github.com/Meanwhale/ByteAutomata\n");

			if (args.length == 1)
			{
				MNode root = null;
				if (args[0].equals("-i"))
				{
					MSJava.print("Read from standard input...");
					MSJava.print("Hit Ctrl-Z (Windows) or ^D (Linux/Mac) at the start of a line and press enter to finish.");
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
				MSJava.print("Syntax:\n");
				MSJava.print("    <cmd> -i            read standard input, eg. <cmd> -i < textfile.txt");
				MSJava.print("    <cmd> -t            run default test");
				MSJava.print("    <cmd> \"<string>\"    parse <string>\n");
				MSJava.print("<cmd> is something like 'java BAJava'");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}