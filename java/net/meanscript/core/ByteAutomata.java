package net.meanscript.core;
import net.meanscript.java.*;
import net.meanscript.*;
/*

 *

 *    Meanscript ByteAutomata (c) 2020, Meanwhale

 *

 *    GitHub page:     https://github.com/Meanwhale/ByteAutomata

 *    Email:           meanwhale@gmail.com

 *    Twitter:         https://twitter.com/TheMeanwhale

 *

 */
public class ByteAutomata {
 boolean ok;
 byte[] tr;
 byte currentInput;
 byte currentState;
 java.util.TreeMap<Integer, String> stateNames = new java.util.TreeMap<Integer, String>();
 MJAction actions[]=new MJAction[64];
 byte stateCounter;
 byte actionCounter; // 0 = end
// running:
 byte inputByte = 0;
 int index = 0;
 int lineNumber = 0;
 boolean stayNextStep = false;
 boolean running = false;
 byte[] buffer;
 byte[] tmp;
public static final int MAX_STATES = 32;
public static final int BUFFER_SIZE = 512;
public static final int CFG_MAX_NAME_LENGTH = 128;
public ByteAutomata()
{
 ok = true;
 currentInput = 0;
 currentState = 0;
 stateCounter = 0;
 actionCounter = 0;
 tr = new byte[MAX_STATES * 256];
 for (int i=0; i<MAX_STATES * 256; i++) tr[i] = (byte)0xff;
 inputByte = 0;
 index = 0;
 lineNumber = 0;
 stayNextStep = false;
 running = false;
 buffer = new byte[BUFFER_SIZE];
 tmp = new byte[BUFFER_SIZE];
}

public void print ()
{
 for (int i = 0; i <= stateCounter; i++)
 {
  MSJava.verbosen("state: ").print(i).print("\n");
  for (int n = 0; n < 256; n++)
  {
   byte foo = tr[(i * 256) + n];
   if (foo == 0xff) MSJava.verbosen(".");
   else MSJava.verbosen(foo);
  }
  MSJava.verbose("");
 }
}
public byte addState (String stateName)
{
 stateCounter++;
 stateNames.put((int)stateCounter,stateName);
 return stateCounter;
}
public void transition (byte state, String input, MJAction action)
{
 byte actionIndex = 0;
 if (action != null)
 {
  actionIndex = addAction(action);
 }
 byte[] bytes = input.getBytes();
 int i = 0;
 while (i<input.length())
 {
  tr[(state * 256) + bytes[i]] = actionIndex;
  i++;
 }
 //DEBUG(VR("New Transition added: id ")X(actionIndex)XO);
}
public void fillTransition (byte state, MJAction action)
{
 byte actionIndex = 0;
 if (action != null) actionIndex = addAction(action);
 for (int i=0; i<256; i++)
 {
  tr[(state * 256) + i] = actionIndex;
 }
 //DEBUG(VR("New Transition filled: id ")X(actionIndex)XO);
}
public byte addAction (MJAction action)
{
 actionCounter++;
 actions[actionCounter] = action;
 return actionCounter;
}
public void next (byte nextState)
{
 currentState = nextState;
 {if (MSJava.debug) {MSJava.verbosen("Next state: ").print(stateNames.get((int)currentState)).print("\n");}};
}
public boolean step (byte input) throws MException
{
 currentInput = input;
 int index = (currentState * 256) + input;
 byte actionIndex = tr[index];
 if (actionIndex == 0) return true; // stay on same state and do nothing else
 if (actionIndex == 0xff||actionIndex < 0)
 {
  ok = false;
  return false; // end
 }
 MJAction act = actions[actionIndex];
 if (act == null)
 {
  MSJava.assertion(false, "invalid action index");
 }
 act.action();
 return true;
}
public int getIndex ()
{
 return index;
}
public int getInputByte ()
{
 return inputByte;
}
public void stay () throws MException
{
 // same input byte on next step
 MSJava.assertion(!stayNextStep, "'stay' is called twice");
 stayNextStep = true;
}
public String getString (int start, int length) throws MException
{
 MSJava.assertion(length < CFG_MAX_NAME_LENGTH,null,"name is too long");
 int i = 0;
 for (; i < length; i++)
 {
  tmp[i] = buffer[start++ % BUFFER_SIZE];
 }
 return new String(tmp,0,length);
}
public void run (MInputStream input) throws MException
{
 inputByte = 0;
 index = 0;
 lineNumber = 1;
 stayNextStep = false;
 running = true;
 while ((!input.end() || stayNextStep) && running && ok)
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
  running = step(inputByte);
 }
 if (!stayNextStep) index++;
}
public void printError ()
{
 MSJava.printn("ERROR: parser state [").print(stateNames.get((int)currentState)).print("]").print("\n");
 MSJava.printn("Line ").print(lineNumber).print(": \"");
 // print nearby code
 int start = index-1;
 while (start > 0 && index - start < BUFFER_SIZE && (char)buffer[start % BUFFER_SIZE] != '\n')
 {
  start --;
 }
 while (++start < index)
 {
  MSJava.verbosen((char)(buffer[start % BUFFER_SIZE]));
 }
 MSJava.print("\"");
}
}
