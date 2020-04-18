#include "main.hpp"

using namespace meanscript;

std::ostream& vrbout()
{
	return std::cout;
}

#ifdef ALLOC_COUNTER
std::size_t numNew = 0;
std::size_t numDelete = 0;
bool counterExited = false;
void* operator new  (std::size_t count ) {numNew++; return std::malloc(count);}
void* operator new[](std::size_t count ) {numNew++; return std::malloc(count);}
void* operator new  (std::size_t count, const std::nothrow_t& tag) {numNew++; return std::malloc(count);}
void* operator new[](std::size_t count, const std::nothrow_t& tag) {numNew++; return std::malloc(count);}
void del(void*ptr)
{
	numDelete++;
	std::free(ptr);
	if (counterExited) std::cout<<"+ 1 (global object)"<<std::endl;
}
void operator delete  (void* ptr) {del(ptr);} 	
void operator delete[](void* ptr) {del(ptr);} 
void operator delete  (void* ptr, const std::nothrow_t& tag) {del(ptr);} 
void operator delete[](void* ptr, const std::nothrow_t& tag) {del(ptr);} 
void operator delete  (void* ptr, std::size_t sz) {del(ptr);} 
void operator delete[](void* ptr, std::size_t sz) {del(ptr);}

void printAllocs()
{
	std::cout<<"NEW: "<<numNew<<", DELETE: "<<numDelete<<std::endl;
	counterExited = true;
}
#endif

int main(int argc, char* argv[])
{
	MicroLexer::printTitle("C++");
	
	if (argc == 2)
	{
		MNode* root = 0;
		std::string arg(argv[1]);

		if (arg == "-i")
		{
			
			MicroLexer::printStdinInfo();

			std::string code, line;
			while (std::getline(std::cin, line))
			{
				code += line;
				code += '\n';
			}
			root = MicroLexer::lex(code);
		}
		else if (arg == "-t")
		{
			root = MicroLexer::lex("abc 123; def {hij (klm)}");
		}

		if (root != 0)
		{
			root->printTree(true);
			delete root;
		}
	}
	else
	{
		MicroLexer::printArgInfo();
	}
	
#ifdef ALLOC_COUNTER
	std::atexit(printAllocs);
#endif
	return 0;
}
