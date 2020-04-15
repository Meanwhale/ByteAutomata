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
public class ByteAutomata {
 boolean ok;
 byte[] tr;
 byte currentInput;
 byte currentState;
 java.util.TreeMap<Integer, String> stateNames = new java.util.TreeMap<Integer, String>();
 MJAction actions[]=new MJAction[64];
 byte stateCounter;
 byte actionCounter; // 0 = end
private static int MAX_STATES = 32;
public ByteAutomata()
{
 ok = true;
 currentInput = 0;
 currentState = 0;
 stateCounter = 0;
 actionCounter = 0;
 tr = new byte[MAX_STATES * 256];
 for (int i=0; i<MAX_STATES * 256; i++) tr[i] = (byte)0xff;
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
}
