package com.dzavalishin.ai;

/* The struct for an AIScript Player */
class AIPlayer {
	boolean active;            //! Is this AI active?
	AICommand queue;       //! The commands that he has in his queue
	AICommand queue_tail;  //! The tail of this queue
}