# ByteAutomata

_**Tools for programming code processing**_


ByteAutomata provides tools to implement a hard-coded finite-state machine for text code tokenizing.
This project includes an example program that takes a text and outputs a token tree.

It's developed as a part of Meanscript, a work-in-progress multi-platform scripting and bytecode language.

## Compile and run

You can input a text code (see code syntax below) to the example program and see a resulting token tree printed,
if there's not any errors.

### Languages
Execute the example program to see guide for command line arguments.
<ul>
<li>Java

Go to folder _java_. Compile classes and run the main class _ByteAutomataJava_: 
```
    javac ByteAutomataJava.java
    java ByteAutomataJava
```
<li>C++

You need GCC (https://gcc.gnu.org/) to compile.
Go to folder _cpp_. Compile and run _byteautomata_ (.exe):
```
    g++ -std=gnu++11 main.cpp src/code.cpp -o byteautomata
    byteautomata
```
</ul>

### Input Code Syntax

The example program recognizes simple C-like code containing
<ul>
	<li> <b>text</b> characters a-z and A-Z (no numbers or underscore) </li>
	<li> <b>number</b> : characters 0-9 </li>
	<li> <b>code blocks</b> starting with ([{ and ending with )]} respectively </li>
	<li> <b>expression breaks</b>: comma and semicolon </li>
	<li> <b>white space</b>: space, linebreak, and tab </li>
</ul>

The syntax is defined in class MicroLexer in source code.
For example input

```
abc 123; foo (456, bar[i])
```

results this token tree:
  
```
[<ROOT>]             // expression, token tree root
  [abc]              // text token
  [123]              // number token
[<EXPR>]             // expression
  [foo]
  [<BLOCK>]          // code block root
    [<EXPR>]
      [456]
    [<EXPR>]
      [bar]
      [<BLOCK>]
        [<EXPR>]
          [i]
```

## Project content

Project code is generated from base code, written in C-like languages and macros (not included to the oriject for now).
Base code is run thru GCC preprocessor to target languages, which are currently C++ and Java.

### Folder structure

| folder | content |
|-|-|
| / | root folder: README, LICENCE, and an example script file. |
| /cpp | main source file, header, and utils |
| /cpp/src | generated source code and header files |
| /java/ | main source class|
| /java/net/meanscript/ | generated code: classes for public interface |
| /java/net/meanscript/core/ | generated code: internal classes |
| /java/net/meanscript/java/ | Java-specific code |


## How It Works

ByteAutomata has a (logically) two-dimensional byte array, which has a column for each byte (256) and for each state.
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
For example, define transition from 'white space' to 'number' state when input is a number character, call
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
