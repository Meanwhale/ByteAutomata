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
public class MicroLexer {
public static final String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
public static final String numbers = "1234567890";
public static final String whitestateSpace = " \t\n\r";
public static final String linebreak = "\n\r";
public static final String expressionBreak = ",;";
public static final String blockStart = "([{";
public static final String blockEnd = ")]}";
public static final int BUFFER_SIZE = 512;
public static final int CFG_MAX_NAME_LENGTH = 128;
private static byte [] tmp = new byte[BUFFER_SIZE];
private static byte [] buffer = new byte[BUFFER_SIZE];
private static byte stateSpace, stateName, stateNumber;
private static ByteAutomata automata;
private static boolean stayNextStep, running;
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
private static void stay() throws MException
{
 MSJava.assertion(!stayNextStep, "can't go backwards twice");
 stayNextStep = true;
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
 MSJava.verbosen("NEW TOKEN: ").print(data).print("\n");
 MNode token = new MNode(currentExpr, tokenType, data);
 if (currentToken == null) currentExpr.child = token;
 else currentToken.next = token;
 currentExpr.numChildren++;
 currentToken = token;
 lastStart = index;
}
private static void endBlock(int blockType) throws MException
{
 MSJava.assertion((currentBlock != null),null,"unexpected block end");
 // Check that block-end character is the right one.
 // The 'type' is block start/end character's ASCII code.
 if (currentBlock.type == 40 ) MSJava.assertion(blockType == 41,null,"invalid block end; parenthesis was expected");
 else if (currentBlock.type == 91 ) MSJava.assertion(blockType == 93,null,"invalid block end; square bracket was expected");
 else if (currentBlock.type == 123) MSJava.assertion(blockType == 125,null,"invalid block end; curly bracket was expected");
 else { MSJava.assertion(false,null,"unhandled block end: " + blockType); }
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
 MNode block = new MNode(currentExpr, blockType, "<BLOCK>");
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
 stateSpace = ba.addState("space");
 stateName = ba.addState("name");
 stateNumber = ba.addState("number");
 ba.transition(stateSpace, whitestateSpace, null);
 ba.transition(stateSpace, letters, () -> { next(stateName); });
 ba.transition(stateSpace, numbers, () -> { next(stateNumber); });
 ba.transition(stateSpace, expressionBreak, () -> { exprBreak(); });
 ba.transition(stateSpace, blockStart, () -> { addBlock((int)inputByte);});
 ba.transition(stateSpace, blockEnd, () -> { endBlock((int)inputByte);});
 ba.transition(stateName, letters, null);
 ba.transition(stateName, whitestateSpace, () -> { addToken(0); next(stateSpace); });
 ba.transition(stateName, blockStart, () -> { addToken(0); stay(); next(stateSpace); });
 ba.transition(stateName, blockEnd, () -> { addToken(0); stay(); next(stateSpace); });
 ba.transition(stateName, expressionBreak, () -> { addToken(0); exprBreak(); next(stateSpace); });
 ba.transition(stateNumber, numbers, null);
 ba.transition(stateNumber, whitestateSpace, () -> { addToken(0); next(stateSpace); });
 ba.transition(stateNumber, blockStart, () -> { addToken(0); stay(); next(stateSpace); });
 ba.transition(stateNumber, blockEnd, () -> { addToken(0); stay(); next(stateSpace); });
 ba.transition(stateNumber, expressionBreak, () -> { addToken(0); exprBreak(); next(stateSpace); });
}
private static MNode parse(MInputStream input) throws MException
{
 ByteAutomata ba = new ByteAutomata();
 automata = ba;
 defineTransitions(ba);
 ba.next((byte)1);
 root = new MNode(null, 0, "<ROOT>");
 currentExpr = root;
 currentBlock = null;
 currentToken = null;
 lastStart = 0;
 running = true;
 stayNextStep = false;
 int lineNumber = 1;
 inputByte = 0;
 index = 0;
 while ((!input.end() || stayNextStep) && running && ba.ok)
 {
  if (!stayNextStep)
  {
   index ++;
   inputByte = input.readByte();
   buffer[index % BUFFER_SIZE] = inputByte;
   if (inputByte == '\n') lineNumber++;
  }
  else
  {
   stayNextStep = false;
  }
  MSJava.printn("[ ").print((char)(inputByte)).print(" ]").print("\n");
  running = ba.step(inputByte);
 }
 if (!stayNextStep) index++;
 ba.step((byte)' '); // ended cleanly: last white space
 if (currentBlock != null)
 {
  MSJava.print("closing parenthesis missing at the end");
  ba.ok = false;
 }
 if (!running || !(ba.ok))
 {
  MSJava.printn("ERROR: parser state [").print(ba.stateNames.get((int)ba.currentState)).print("]").print("\n");
  MSJava.printn("Line ").print(lineNumber).print(": \"");
  // print nearby code
  int start = index-1;
  while (start > 0 && index - start < BUFFER_SIZE && (char)buffer[start % BUFFER_SIZE] != '\n')
  {
   start --;
  }
  while (++start <= index)
  {
   MSJava.verbosen((char)(buffer[start % BUFFER_SIZE]));
  }
  MSJava.print("\"");
  root = null;
  return null;
 }
 MSJava.verbose("\nFINISHED!");
 return root;
}
public static MNode lex (String s) throws MException
{
 MInputArray input = new MInputArray(s);
 return parse(input);
}
}
