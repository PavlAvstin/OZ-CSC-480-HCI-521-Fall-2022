#include "pch.h"
#include <iostream>


// This program prints the first five letters 
int main()
{
	int stepper = 1;
	char letters[] = { 'a','b','c','e','d' };
    std::cout << "Hello World!\n";
	std::cout << "This is the method debug line\n";
	char temp = 'q';
	while (stepper < 5)
	{
		temp = letters[stepper];
		std::cout << temp;
		stepper += 1;
	}
}