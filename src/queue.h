/* $Id: queue.h 2701 2005-07-24 14:12:37Z tron $ */

#ifndef QUEUE_H
#define QUEUE_H

// NB! ----------- TTDQueue ----------------


//#define NOFREE
//#define QUEUE_DEBUG
//#define HASH_DEBUG
//#define HASH_STATS


typedef struct Queue Queue;
typedef bool Queue_PushProc(Queue* q, void* item, int priority);
typedef void* Queue_PopProc(Queue* q);
typedef bool Queue_DeleteProc(Queue* q, void* item, int priority);
typedef void Queue_ClearProc(Queue* q, bool free_values);
typedef void Queue_FreeProc(Queue* q, bool free_values);

// Get top without popping
typedef void* Queue_GetTopProc(Queue* q);

typedef struct InsSortNode InsSortNode;
struct InsSortNode {
	void* item;
	int priority;
	InsSortNode* next;
};
typedef struct BinaryHeapNode BinaryHeapNode;
	struct BinaryHeapNode {
	void* item;
	int priority;
};


struct Queue{
	/*
	 * Pushes an element into the queue, at the appropriate place for the queue.
	 * Requires the queue pointer to be of an appropriate type, of course.
	 */
	Queue_PushProc* push;
	/*
	 * Pops the first element from the queue. What exactly is the first element,
	 * is defined by the exact type of queue.
	 */
	Queue_PopProc* pop;
	/*
	 * Deletes the item from the queue. priority should be specified if
	 * known, which speeds up the deleting for some queue's. Should be -1
	 * if not known.
	 */
	Queue_DeleteProc* del;

	/* Clears the queue, by removing all values from it. It's state is
	 * effectively reset. If free_items is true, each of the items cleared
	 * in this way are free()'d.
	 */
	Queue_ClearProc* clear;
	/* Frees the queue, by reclaiming all memory allocated by it. After
	 * this it is no longer usable. If free_items is true, any remaining
	 * items are free()'d too.
	 */
	Queue_FreeProc* free;
	/* Obtains the top of the queue, allowing the user to look at the 
	 * queue without destroying it.
	 * WARNING: ONLY IMPLEMENTED IN FIFO SO FAR!
	 */
	Queue_GetTopProc* getTop;

	union {
		struct {
			uint max_size;
			uint size;
			void** elements;
		} stack;
		struct {
			uint max_size;
			uint head; /* The index where the last element should be inserted */
			uint tail; /* The index where the next element should be read */
			void** elements;
		} fifo;
		struct {
			InsSortNode* first;
		} inssort;
		struct {
			uint max_size;
			uint size;
			uint blocks; /* The amount of blocks for which space is reserved in elements */
			BinaryHeapNode** elements;
		} binaryheap;
	} data;

	/* If true, this struct will be free'd when the
	 * Queue is deleted. */
	bool freeq;
};

/* Initializes a stack and allocates internal memory. */
void init_Stack(Queue* q, uint max_size);

/* Allocate a new stack with a maximum of max_size elements. */
Queue* new_Stack(uint max_size);

/*
 * Fifo
 */

/* Initializes a fifo and allocates internal memory for maximum of max_size
 * elements */
void init_Fifo(Queue* q, uint max_size);

/* Allocate a new fifo and initializes it with a maximum of max_size elements. */
Queue* new_Fifo(uint max_size);

Queue* new_Fifo_in_buffer(uint max_size, void* buffer);

int build_Fifo(void* buffer, uint size);

/*
 * Insertion Sorter
 */

/* Initializes a inssort and allocates internal memory. There is no maximum
 * size */
void init_InsSort(Queue* q);

/* Allocate a new fifo and initializes it. There is no maximum size */
Queue* new_InsSort(void);

/*
 *  Binary Heap
 *  For information, see:
 *   http://www.policyalmanac.org/games/binaryHeaps.htm
 */

/* The amount of elements that will be malloc'd at a time */
#define BINARY_HEAP_BLOCKSIZE_BITS 10

/* Initializes a binary heap and allocates internal memory for maximum of
 * max_size elements */
void init_BinaryHeap(Queue* q, uint max_size);

/* Allocate a new binary heap and initializes it with a maximum of max_size
 * elements. */
Queue* new_BinaryHeap(uint max_size);

/*
 * Hash
 */
typedef struct HashNode HashNode;
struct HashNode {
	uint key1;
	uint key2;
	void* value;
	HashNode* next;
};
/**
 * Generates a hash code from the given key pair. You should make sure that
 * the resulting range is clearly defined.
 */
typedef uint Hash_HashProc(uint key1, uint key2);
typedef struct Hash {
	/* The hash function used */
	Hash_HashProc* hash;
	/* The amount of items in the hash */
	uint size;
	/* The number of buckets allocated */
	uint num_buckets;
	/* A pointer to an array of num_buckets buckets. */
	HashNode* buckets;
	/* A pointer to an array of numbuckets booleans, which will be true if
	 * there are any Nodes in the bucket */
	bool* buckets_in_use;
	/* If true, buckets will be freed in delete_hash */
	bool freeb;
	/* If true, the pointer to this struct will be freed in delete_hash */
	bool freeh;
} Hash;

/* Call these function to manipulate a hash */

/* Deletes the value with the specified key pair from the hash and returns
 * that value. Returns NULL when the value was not present. The value returned
 * is _not_ free()'d! */
void* Hash_Delete(Hash* h, uint key1, uint key2);
/* Sets the value associated with the given key pair to the given value.
 * Returns the old value if the value was replaced, NULL when it was not yet present. */
void* Hash_Set(Hash* h, uint key1, uint key2, void* value);
/* Gets the value associated with the given key pair, or NULL when it is not
 * present. */
void* Hash_Get(Hash* h, uint key1, uint key2);

/* Call these function to create/destroy a hash */

/* Builds a new hash, with num_buckets buckets. Make sure that hash() always
 * returns a hash less than num_buckets! Call delete_hash after use */
Hash* new_Hash(Hash_HashProc* hash, int num_buckets);
/* Builds a new hash in an existing struct. Make sure that hash() always
 * returns a hash less than num_buckets! Call delete_hash after use */
void init_Hash(Hash* h, Hash_HashProc* hash, uint num_buckets);
/*
 * Deletes the hash and cleans up. Only cleans up memory allocated by new_Hash
 * & friends. If free is true, it will call free() on all the values that
 * are left in the hash.
 */
void delete_Hash(Hash* h, bool free_values);
/*
 * Cleans the hash, but keeps the memory allocated
 */
void clear_Hash(Hash* h, bool free_values);
/*
 * Gets the current size of the Hash
 */
uint Hash_Size(Hash* h);

/* 
 * NOT part of normal Queue structures defined above!
 * This is a special queue designed to have special behaviors
 * for the aircraft queueing algorithm, which has special requirements ;).
 * Note that this is NOT a priority queue!
 */
typedef struct VehicleQueue VehicleQueue;

// O(1), always
typedef bool VQueue_PushProc(VehicleQueue* q, Vehicle* item);
// O(1), unless offset is > 2147483647 - then O(n) -- increments offset
typedef Vehicle* VQueue_PopProc(VehicleQueue* q);
// O(1)
typedef Vehicle* VQueue_GetTopProc(VehicleQueue* q);
// O(n) -- Rebuilds the "position"s, resets the offset, asserts the size.
typedef void VQueue_CleanProc(VehicleQueue* q);
// O(n)
typedef void VQueue_ClearProc(VehicleQueue* q);
// O(1) -- sets dirty bit
typedef void VQueue_DeleteProc(VehicleQueue* q, Vehicle* item);
// O(1) if not dirty, otherwise O(n) -- Gets current position in queue.
typedef uint32 VQueue_GetPosProc(VehicleQueue* q, Vehicle* item);
// O(1)
typedef bool VQueue_InitProc(VehicleQueue* q);

//typedef void VQueue_FreeProc(Queue* q, bool free_values);

/*
 * WARNING: Do NOT directly manipulate data inside this queue!
 * Queue is *very* sensitive and will toss assertions if it detects
 * improper values!
 */
typedef struct VQueueItem VQueueItem;
struct VQueueItem
{
	Vehicle *data;
	// Position in queue
	uint32 position;
	VQueueItem *below;
	VQueueItem *above;

	// Queue item belongs to (so we can have reverse lookups)
	VehicleQueue *queue;
};

//typedef struct VehicleQueue VehicleQueue;

VehicleQueue *new_VQueue();

struct VehicleQueue
{
	// Ahh, yes! Classic C functional programming!
	// Should really be converted to C++, though . . .
	VQueue_PushProc*	push;
	VQueue_PopProc*		pop;
	VQueue_GetTopProc*	getTop;
	VQueue_CleanProc*	clean;
	VQueue_ClearProc*	clear;
	VQueue_DeleteProc*	del;
	VQueue_GetPosProc*	getPos;
	//VQueue_InitProc*	init;

	VQueueItem* top;
	VQueueItem* bottom;

	// Dirty means "position" in VQueueItems is incorrect
	// and needs to be rebuilt.
    bool dirty;	
	uint32 size;

	// Offset for "position" in queue - allows for O(1) pushes & pops
	uint32 offset;
};


#endif /* QUEUE_H */
