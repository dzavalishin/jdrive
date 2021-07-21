/* $Id: namegen.h 2962 2005-09-18 20:56:44Z Darkvater $ */

#ifndef NAMEGEN_H
#define NAMEGEN_H

typedef byte TownNameGenerator(char *buf, uint32 seed);

extern TownNameGenerator * const _town_name_generators[];

#endif /* NAMEGEN_H */
