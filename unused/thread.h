/* $Id: thread.h 2906 2005-09-02 16:05:59Z Darkvater $ */

#ifndef THREAD_H
#define THREAD_H

typedef struct Thread Thread;

typedef void* (*ThreadFunc)(void*);

Thread* OTTDCreateThread(ThreadFunc, void*);
void*   OTTDJoinThread(Thread*);

#endif /* THREAD_H */
