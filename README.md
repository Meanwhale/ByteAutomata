# ByteAutomata

_**Language recognition for parsing**_

ByteAutomata provides a method to implement a hard-coded finite-state machine for text code tokenizing.
This project includes an example program that turns input code to a token tree.
It can be used as a template for your own parser.

It's developed as a part of Meanscript, a work-in-progress multi-platform scripting and bytecode language.

## Compile and run

Compile the example program in Java and/or C++.
You can give a code (see the syntax below) to the example program and see a resulting token tree printed,
if there isn't any errors.

Execute the example program to see guide for command line arguments.
<ul>
<li>Java

You need JDK 8 to compile and run. Go to folder _java_. Compile classes and run the main class _ByteAutomataJava_: 
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

Project code is generated from base code, written in C-like language and macros (not included to this repository for now).
Base code is run thru GCC preprocessor for target languages, which are currently C++ and Java.
The most essential classes are **ByteAutomata**, a general use state machine, and **MicroLexer**, an example implementation.


### Folder structure

| folder | content |
|-|-|
| / | root folder: README, LICENCE, and an example script file. |
| /cpp/ | main() source file, header, and utils |
| /cpp/src/ | generated source (`code.cpp`) code and header files |
| /java/ | main() class source |
| /java/net/meanscript/ | generated code: classes for public interface |
| /java/net/meanscript/core/ | generated code: internal classes |
| /java/net/meanscript/java/ | Java-specific code |


## How It Works

ByteAutomata has a (logically) two-dimensional byte array, which has a column for each input byte (256) and a row for each state.
For example here's a simple state machine (https://en.wikipedia.org/wiki/Finite-state_machine), that recognizes text and numbers separated with white space (eg. `abc 123 ef`), drawn in ASCII art:
```
                        (0-9)                 (a-z)
	            ---------- [white space] ----------
		   |             ^       ^             |
         (a-z)     v             |       |             v    (0-9)
    [X] <----- [number]-----------       ------------[text] -----> [X]
                  v ^      {space}         {space}    v ^
                 (0-9)                               (a-z)

```
States are denote with square brackets and [X] marks an error state.
Here's how it's done in ByteAutomata's byte array, where array items are indexes of transition callbacks:
```
                             0 ... {space} ... ' 0' '1' '2' ... '9' .... 'a' ... 'z'
			     
    state 1: white space              0          a   a   a  ...  a        b ...  _b_
    state 2: number                   c          0   0   0       0       [X] ... [X]
    state 3: text                     d         [X] [X] [X]     [X]       0  ...  0
    
    callback a: nextState(number)
    callback b: nextState(text)
    callback c: addNumberToken(), nextState(white space)
    callback d: addTextToken(), nextState(white space)
```
For example, if we're in 'white space' state, and input byte is 'z', then callback 'b' is called (the array item for this is surrounded with underscores).
`[X]` is an error code `0xff` (255), and `0` is for staying on the same state without a callback call. All bytes are `[X]` by default. 

State transitions are created for ByteAutomata by calling
```
    transition(state, inputBytes, callback);
```
For example, to define a transition from 'white space' to 'number' state when input is a number character, call (in pseudo-Java)
```
    transition(whiteSpaceState, "0123456789", () -> { nextState(numberState); } );
```

To create a token tree, add nodes to the tree on transitions.
For example, in 'number' state, when we get a space byte as an input, we can add a number token:
```
    transition(numberState, " ", () -> {addNumberToken(startIndex, currentIndex); nextState(whiteSpaceState);});
```
So, if input is "abc 123 de", status at the end of "123" is like
```
             startIndex   currentIndex
                      v   v
    index     0 1 2 3 4 5 6 7 8 9
    input     a b c   1 2 3   d e
```
Then `addToken(4, 6)` is called. It copies the number characters [4], [5], and [6] from input buffer, save them to a new node, and add the node to the token tree.

Token tree is made of `MNode`s whose member are references/pointers to the next sibling and a child node, and token data.
You can iterate thru the node tree nodes using class `NodeIterator` or directly class `MNode`.
For example printing all nodes with a recursive function, in pseudo-Java:

```
void printTree (MNode node)
{
    print(node.data);
    if (node.child != null) printTree(node.child);
    if (node.next != null) printTree(node.next);
}

// ...

    printTree(rootNode);
```

You'll find the full version of the function in class `MNode`.

<hr>
Copyright (c) 2020, Meanwhale
