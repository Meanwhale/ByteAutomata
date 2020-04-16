#include "main.hpp"

using namespace meanscript;

std::ostream& vrbout()
{
	return std::cout;
}

int main()
{
	std::cout<<"    ByteAutomata"<<std::endl;

	MNode* root = MicroLexer::lex("int 098, djf 30294 (oieur [9348])");
	if (root != 0)
	{
		root->printTree(true);
		delete root;
	}
	return 0;
}