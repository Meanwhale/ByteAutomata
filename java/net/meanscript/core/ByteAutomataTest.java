/*
 *
 *    Meanscript ByteAutomata (c) 2020, Meanwhale
 *
 *    GitHub page:     https://github.com/Meanwhale/ByteAutomata
 *    Email:           meanwhale@gmail.com
 *    Twitter:         https://twitter.com/TheMeanwhale
 *
 */
package net.meanscript.core;
import net.meanscript.java.*;
import net.meanscript.*;
public class ByteAutomataTest {
public static final String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
public static final String numbers = "1234567890";
public static final String whitespace = " \t\n\r";
public static final String linebreak = "\n\r";
public static final String blockStart = "([{";
public static final String blockEnd = ")]}";
public static final String op = "+-/*<>="; // '-' or '/' will be special cases
public static final int BUFFER_SIZE = 512;
public static final int CFG_MAX_NAME_LENGTH = 128;
private static byte [] tmp = new byte[BUFFER_SIZE];
private static byte [] buffer = new byte[BUFFER_SIZE];
private static byte space, name, number;
private static ByteAutomata automata;
private static boolean goBackwards, running, assignment;
private static int index, lastStart;
private static byte inputByte = 0;
private static MNode root;
private static MNode currentBlock;
private static MNode currentExpr;
private static MNode currentToken;
private static void next(byte state)
{
 lastStart = index;
 automata.next(state);
}
private static void nextCont(byte state)
{
 // continue with same token,
 // so don't reset the start index
 automata.next(state);
}
private static void bwd() throws MException
{
 MSJava.assertion(!goBackwards, "can't go backwards twice");
 goBackwards = true;
}
private static String getNewName() throws MException
{
 int start = lastStart;
 int length = index - start;
 MSJava.assertion(length < CFG_MAX_NAME_LENGTH,null,"name is too long");
 int i = 0;
 for (; i < length; i++)
 {
  tmp[i] = buffer[start++ % BUFFER_SIZE];
 }
 return new String(tmp,0,length);
}
private static void addExpr()
{
 MNode expr = new MNode(currentBlock, 0, "<EXPR>");
 currentExpr.next = expr;
 currentExpr = expr;
 currentToken = null;
}
private static void addToken(int tokenType) throws MException
{
 String data = getNewName();
 {if (MSJava.debug) {MSJava.verbosen("TOKEN: ").print(data).print("\n");}};
 MNode token = new MNode(currentExpr, tokenType, data);
 if (currentToken == null) currentExpr.child = token;
 else currentToken.next = token;
 currentExpr.numChildren++;
 currentToken = token;
 lastStart = index;
}
private static void addOperator(int tokenType, String name)
{
 MNode token = new MNode(currentExpr, tokenType, name);
 if (currentToken == null) currentExpr.child = token;
 else currentToken.next = token;
 currentExpr.numChildren++;
 currentToken = token;
 lastStart = index;
}
private static void endBlock(int blockType) throws MException
{
 MSJava.assertion((currentBlock != null),null,"unexpected block end");
 // check that block-end character is the right one
 MSJava.assertion(!(currentBlock.type == 40 ^ blockType == 41),null,"invalid block end; parenthesis was expected");
 MSJava.assertion(!(currentBlock.type == 91 ^ blockType == 93),null,"invalid block end; square bracket was expected");
 MSJava.assertion(!(currentBlock.type == 123 ^ blockType == 125),null,"invalid block end; curly bracket was expected");
 lastStart = -1;
 currentToken = currentBlock;
 currentExpr = currentToken.parent;
 currentBlock = currentExpr.parent;
}
private static void exprBreak() throws MException
{
 if (currentBlock != null) currentBlock.numChildren ++;
 lastStart = -1;
 addExpr();
}
private static void addBlock(int blockType) throws MException
{
 MSJava.verbosen("add block: ").print(blockType).print("\n");
 lastStart = -1;
 MNode block = new MNode(currentExpr, blockType, "block");
 if (currentToken == null)
 {
  currentExpr.child = block;
  currentToken = block;
 }
 else
 {
  currentToken.next = block;
 }
 currentExpr.numChildren++;
 currentBlock = block;
 MNode expr = new MNode(currentBlock, 0, "<EXPR>");
 currentBlock.child = expr;
 currentExpr = expr;
 currentToken = null;
}
private static void defineTransitions(ByteAutomata ba) throws MException
{
 space = ba.addState("space");
 name = ba.addState("name");
 number = ba.addState("number");
 ba.transition(space, whitespace, null);
 ba.transition(space, letters, () -> { next(name); });
 ba.transition(space, numbers, () -> { next(number); });
 ba.transition(space, blockStart, () -> { addBlock((int)inputByte);});
 ba.transition(space, blockEnd, () -> { endBlock((int)inputByte);});
 ba.transition(name, letters, null);
 ba.transition(name, whitespace, () -> { addToken(0); next(space); });
 ba.transition(name, blockStart, () -> { addToken(0); bwd(); next(space); });
 ba.transition(name, blockEnd, () -> { addToken(0); bwd(); next(space); });
 ba.transition(number, numbers, null);
 ba.transition(number, whitespace, () -> { addToken(0); next(space); });
 ba.transition(number, blockStart, () -> { addToken(0); bwd(); next(space); });
 ba.transition(number, blockEnd, () -> { addToken(0); bwd(); next(space); });
}
private static void parse(MInputStream input) throws MException
{
 ByteAutomata ba = new ByteAutomata();
 automata = ba;
 defineTransitions(ba);
 ba.next((byte)1);
 root = new MNode(null, 0, "<ROOT>");
 currentExpr = root;
 lastStart = 0;
 running = true;
 assignment = false;
 goBackwards = false;
 int lineNumber = 1;
 inputByte = 0;
 index = 0;
 while ((!input.end() || goBackwards) && running && ba.ok)
 {
  if (!goBackwards)
  {
   index ++;
   inputByte = input.readByte();
   buffer[index % BUFFER_SIZE] = inputByte;
  }
  else
  {
   goBackwards = false;
  }
  MSJava.printn("[ ").print((char)(inputByte)).print(" ]").print("\n");
  lineNumber += (inputByte=='\n'?1:0);
  running = ba.step(inputByte);
 }
 if (!running || !(ba.ok))
 {
  MSJava.printn("Parser state [").print(ba.stateNames.get((int)ba.currentState)).print("]").print("\n");
  MSJava.printn("Line ").print(lineNumber).print(": \"");
  // print nearby code
  int start = index-1;
  while (start > 0 && index - start < BUFFER_SIZE && (char)buffer[start % BUFFER_SIZE] != '\n')
   start --;
  while (++start <= index)
  {
   MSJava.verbosen((char)(buffer[start % BUFFER_SIZE]));
  }
  MSJava.print("\"");
 }
 else
 {
  if (!goBackwards) index++;
  ba.step((byte)'\n'); // ended cleanly: last command break
  if (currentBlock != null)
  {
   MSJava.print("closing parenthesis missing at the end");
   ba.ok = false;
  }
 }
 if (!ba.ok)
 {
  root = null;
  throw new MException(null, "Parse error");
 }
 else
 {
  MSJava.verbose("\nTOKEN TREE:");
  root.printTree(true);
  MSJava.verbose("\nEND PARSING");
 }
}
public static void runParseTest (String s) throws MException
{
 MInputArray input = new MInputArray(s);
 parse(input);
}
}
