# ByteAutomata

_**Tools for programming code processing**_


ByteAutomata is a technique to implement a hard-coded finite-state machine for text code tokenizing.
This project includes an example program that takes a text and outputs a token tree.

It's developed as a part of Meanscript, a multi-platform scripting and bytecode language.

## Compile and run

Execute the example program to see guide for command line arguments.

### Java

Go to folder 'java'. Compile classes and run the main class at ByteAutomataJava: 
```
    javac ByteAutomataJava.java
    java ByteAutomataJava
```
### C++

You need GCC (https://gcc.gnu.org/) to compile.
Go to folder 'cpp'. Compile and run 'byteautomata' (.exe):
```
    g++ -std=gnu++11 main.cpp src/code.cpp -o byteautomata
    byteautomata
```

## How It Works

ByteAutomata has a (logiacally) two-dimensional byte array, which has a column for each byte (256) and for each state.
For example a simple state machine (https://en.wikipedia.org/wiki/Finite-state_machine) in ASCII art:
```
                        (0-9)                 (a-z)
	            ---------- [white space] ----------
		   |             ^       ^             |
         (a-z)     v             |       |             v    (0-9)
    [X] <----- [number]-----------       ------------[text] -----> [X]
                  v ^      {space}         {space}    v ^
                 (0-9)                               (a-z)

```
[X] marks an error state.
ByteAutomata's byte array describes similar state machine like this:
```
                             0 ... {space} ... ' 0' '1' '2' ... '9' .... 'a' ... 'z'
			     
    state 1: white space              0          2   2   2  ...  2        3  ...  3
    state 2: number                   1          0   0   0       0       [X] ... [X]
    state 3: text                     1         [X] [X] [X]     [X]       0  ...  0
```
`[X]` is some error code, like 255 (`0xff`). All bytes are `[X]` by default. State transitions are defined for ByteAutomata by calling
```
    transition(state, inputBytes, callback);
```
For example,to transition from 'white space' to 'number' state, call
```
    transition(whiteSpaceState, "0123456789", () -> {nextState(numberState);});
```
in pseudo-Java.

To create a token tree, add nodes to the tree on transitions.
For example, in 'number' state, when we get a space byte as an input, we can add a number token:
```
    transition(numberState, " ", () -> {addToken(startIndex, currentIndex); nextState(whiteSpaceState);});
```
So, if input is "abc 123 de", status at the end of "123" is like
```
             startIndex   currentIndex
                      v   v
    index     0 1 2 3 4 5 6 7 8 9
    input     a b c   1 2 3   d e
```
Then `addToken(4, 6)` is called. It copies the number characters [4], [5], and [6] from input buffer, save them to a new node, and add the node to the token tree.

<hr>
Copyright (c) 2020, Meanwhale
