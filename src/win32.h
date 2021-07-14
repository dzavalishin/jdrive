/* $Id: win32.h 2962 2005-09-18 20:56:44Z Darkvater $ */

#ifndef WIN32_H
#define WIN32_H

bool MyShowCursor(bool show);

typedef void (*Function)(int);
bool LoadLibraryList(Function proc[], const char* dll);

#endif /* WIN32_H */
