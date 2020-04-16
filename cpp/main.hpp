#ifndef BYTECODE_MAIN
#define BYTECODE_MAIN
#include <stdio.h>
#include <iostream>
#include <fstream>
#include <map>
#include <string>
std::ostream& vrbout();
namespace meanscript
{
	class ByteAutomata;
	class MicroLexer;
	class MInputStream;
	class MInputArray;
	class MNode;
	class NodeIterator;
}
#define HALT std::exit(0) 
#define STR(x) #x
#define ASSERT(x,msg) { if (!(x)) { printf("FAIL: (%s), file %s, line %d.\n", STR(x), __FILE__, __LINE__); std::cout<<msg<<std::endl; HALT; }}
#define EXIT(msg) {std::cout<<"ERROR:"<<std::endl<<msg<<std::endl;std::exit(0);}
#include "Array.h"
#include "src/ByteAutomata.h"
#include "src/MicroLexer.h"
#include "src/MInputStream.h"
#include "src/MInputArray.h"
#include "src/MNode.h"
#include "src/NodeIterator.h"
#endif