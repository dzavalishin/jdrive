package test.game.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import game.util.TTDQueueImpl;

class TTDQueueImplTest {

	@Test
	void testPush() 
	{
		TTDQueueImpl<Integer> q = new TTDQueueImpl<Integer>();
		
		q.push(800, 800);
		q.push(200, 200);
		q.push(900, 900);
		q.push(300, 300);
		q.push(330, 330);
		q.push(200, 200);
		q.push(100, 100);
		
		int max = Integer.MAX_VALUE;
		
		while(true)
		{
			Object o = q.pop();
			if( o == null ) 
				break;
			
			Integer ret = (Integer) o;
			
			assertTrue( ret <= max );
			max = ret;
		}
		
		
	}

	/*@Test
	void testPop() {
		fail("Not yet implemented");
	}*/

	@Test
	void testClear() {
		TTDQueueImpl<Integer> q = new TTDQueueImpl<Integer>();
		
		q.push(800, 800);
		q.push(200, 200);
		q.push(900, 900);
		q.push(300, 300);
		q.push(330, 330);
		q.push(200, 200);
		q.push(100, 100);
		
		q.clear();
		
		assertEquals( null, q.pop() );
	}

	@Test
	void testDel() {
		TTDQueueImpl<Integer> q = new TTDQueueImpl<Integer>();
		q.push(800, 800);
		q.del(800, 0);
		
	}

}
