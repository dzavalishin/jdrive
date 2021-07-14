/* $Id: driver.h 2962 2005-09-18 20:56:44Z Darkvater $ */

#ifndef DRIVER_H
#define DRIVER_H

void LoadDriver(int driver, const char *name);

bool GetDriverParamBool(const char* const* parm, const char* name);
int GetDriverParamInt(const char* const* parm, const char* name, int def);

void GetDriverList(char* p);

#endif /* DRIVER_H */
