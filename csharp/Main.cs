using meanscript;
using System;

public class ByteAutomataCS
{
	public static void Main (string [] args)
	{
		try
		{
			MicroLexer.printTitle("Java");

			if (args.Length == 1)
			{
				MNode root = null;
				if (args[0].Equals("-i"))
				{
					MicroLexer.printStdinInfo();
				    string input = "";
					while (true)
					{	
						string line = Console.ReadLine();
						if (line == null) break;
						input += line + '\n';
					}
					root = MicroLexer.lex(input);
				}
				else if (args[0].Equals("-t"))
				{
					root = MicroLexer.lex("abc 123 (def 456 [ghi])");
				}
				else
				{
					root = MicroLexer.lex(args[0]);
				}
				MeanCS.print("TOKEN TREE:");
				if (root != null) root.printTree(true);
			}
			else
			{
				MicroLexer.printArgInfo();
			}
		}
		catch (Exception e)
		{
			MeanCS.print(e.ToString());
		}
	}
}