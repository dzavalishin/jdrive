package test.game.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import game.IntContainer;
import game.util.BitOps;

class BitOpsTest {

	@Test
	void testGB() {
		assertEquals(BitOps.GB(0x55, 4, 4), 0x5 );
	}

	@Test
	void testSB() {
		IntContainer ip = new IntContainer();
		
		ip.v = 2;
		
		BitOps.SB(ip, 4, 4, 5);
		
		assertEquals( ip.v, 0x52);
	}

	@Test
	void testRETSB() {
		assertEquals(BitOps.RETSB(0, 4, 4, 5), 0x50);
	}

	@Test
	void testRETAB() {
		fail("Not yet implemented");
	}

	@Test
	void testMin() {
		assertEquals(BitOps.min(3, 4), 3 );
	}

	@Test
	void testMax() {
		assertEquals(BitOps.max(3, 4), 4 );
	}

	@Test
	void testMax64() {
		assertEquals(BitOps.max64(33l, 44l), 44l );
	}

	@Test
	void testMinu() {
		assertEquals(BitOps.minu(-33, -44), Integer.toUnsignedLong(-44) );
	}

	@Test
	void testMaxu() {
		assertEquals(BitOps.maxu(-33, -44), Integer.toUnsignedLong(-33) );
	}

	@Test
	void testClamp() {
		assertEquals( BitOps.clamp(0, 2, 5), 2 );
		assertEquals( BitOps.clamp(100, 2, 5), 5 );
		assertEquals( BitOps.clamp(3, 2, 5), 3 );
	}

	@Test
	void testBIGMULSS() {
		fail("Not yet implemented");
	}

	@Test
	void testBIGMULSS64() {
		fail("Not yet implemented");
	}

	@Test
	void testIS_INSIDE_1D() {
		fail("Not yet implemented");
	}

	@Test
	void testHASBIT() {
		fail("Not yet implemented");
	}

	@Test
	void testRETCLRBITByteInt() {
		fail("Not yet implemented");
	}

	@Test
	void testRETSETBITByteInt() {
		fail("Not yet implemented");
	}

	@Test
	void testRETTOGGLEBITByteInt() {
		fail("Not yet implemented");
	}

	@Test
	void testRETCLRBITIntInt() {
		fail("Not yet implemented");
	}

	@Test
	void testRETSETBITIntInt() {
		fail("Not yet implemented");
	}

	@Test
	void testRETTOGGLEBITIntInt() {
		fail("Not yet implemented");
	}

	@Test
	void testHASBITS() {
		fail("Not yet implemented");
	}

	@Test
	void testRETSETBITSIntInt() {
		fail("Not yet implemented");
	}

	@Test
	void testRETSETBITSByteByte() {
		fail("Not yet implemented");
	}

	@Test
	void testRETCLRBITSIntInt() {
		fail("Not yet implemented");
	}

	@Test
	void testRETCLRBITSByteByte() {
		fail("Not yet implemented");
	}

	@Test
	void testFIND_FIRST_BIT() {
		System.err.print("FIND_FIRST_BIT: ");
		System.err.println(BitOps.FIND_FIRST_BIT(0x40));

		assertEquals(BitOps.FIND_FIRST_BIT(0x20), 5);
		assertEquals(BitOps.FIND_FIRST_BIT(0x40), 6);
		assertEquals(BitOps.FIND_FIRST_BIT(0x80), 7);
	}

	@Test
	void testKILL_FIRST_BIT() {
		assertEquals(BitOps.KILL_FIRST_BIT(0x20), 0); // TODO bits 6 & 7?
		assertEquals(BitOps.KILL_FIRST_BIT(0x22), 2); // TODO bits 6 & 7?
	}

	@Test
	void testFindFirstBit2x64() {
		fail("Not yet implemented");
		//System.err.println(BitOps.FindFirstBit2x64(0x80));
		assertEquals(BitOps.FindFirstBit2x64(0x1), 0 );
		assertEquals(BitOps.FindFirstBit2x64(0x80), 7 ); // TODO which bits must work?
	}

	@Test
	void testKillFirstBit2x64() {
		fail("Not yet implemented");
	}

	@Test
	void testIS_BYTE_INSIDE() {
		assertTrue(BitOps.IS_BYTE_INSIDE(1, 0, 5) );
		assertFalse(BitOps.IS_BYTE_INSIDE(10, 0, 5) );
	}

	@Test
	void testIS_INT_INSIDE() {
		assertTrue(BitOps.IS_INT_INSIDE(1, 0, 5) );
		assertFalse(BitOps.IS_INT_INSIDE(10, 0, 5) );
	}

	@Test
	void testCHANCE16() {
		// test randomness
		boolean vPrev = BitOps.CHANCE16(1, 10);
		for( int i = 0; i < 100; i++)
		{
			boolean v = BitOps.CHANCE16(1, 10);
			if( v != vPrev )
			{
				assertTrue(true);				
				return;
			}
			vPrev = v;
		}
		fail();
	}

	@Test
	void testCHANCE16R() {
		// test callability
		int [] r = { -1 };

		// test randomness
		boolean vPrev = BitOps.CHANCE16(1, 10);
		for( int i = 0; i < 100; i++)
		{
			boolean v = BitOps.CHANCE16R(1, 10, r);
			assertTrue(r[0] > 0);
			//System.err.println(String.format("CHANCE16R %x", r[0] ) );

			if( v != vPrev )
			{
				assertTrue(true);				
				return;
			}
			vPrev = v;
		}
		fail();
	
	}

	@Test
	void testCHANCE16I() {
		// test callability
		boolean v = BitOps.CHANCE16I(1, 10, 5);
	}

	@Test
	void testMyabs() {
		assertEquals(BitOps.myabs(-5), 5);
		assertEquals(BitOps.myabs(5), 5);
	}

	@Test
	void testMyabs64() {
		assertEquals(BitOps.myabs64(-5l), 5l);
		assertEquals(BitOps.myabs64(5l), 5l);
	}

	@Test
	void testROR8() {
		//System.err.println(String.format("ROR %x", BitOps.ROR8(0x01,1)));
		assertEquals(BitOps.ROR8(0x01,1), 0x80);
	}

	@Test
	void testROR16() {
		//System.err.println(String.format("ROR %x", BitOps.ROR8(0x01,1)));
		assertEquals(BitOps.ROR16(0x01,1), 0x8000);
	}

	@Test
	void testROR32() {
		//System.err.println(String.format("ROR %x", BitOps.ROR8(0x01,1)));
		assertEquals(BitOps.ROR32(0x01,1), 0x80000000);
	}

	@Test
	void testALIGN() {
		assertEquals(BitOps.ALIGN(0x012,0x10), 0x20);
		assertEquals(BitOps.ALIGN(12,10), 20);
	}

	@Test
	void testFindFirstBitInt() {
		assertEquals(BitOps.FindFirstBit(0x01), 0);
		assertEquals(BitOps.FindFirstBit(0x02), 1);
		assertEquals(BitOps.FindFirstBit(0x04), 2);
		assertEquals(BitOps.FindFirstBit(0x08), 3);
		assertEquals(BitOps.FindFirstBit(0x10), 4);
		assertEquals(BitOps.FindFirstBit(0x20), 5);
		assertEquals(BitOps.FindFirstBit(0x40), 6);
		assertEquals(BitOps.FindFirstBit(0x80), 7);

		assertEquals(BitOps.FindFirstBit(0x03), 1);
		assertEquals(BitOps.FindFirstBit(0x05), 2);
		assertEquals(BitOps.FindFirstBit(0x09), 3);
		assertEquals(BitOps.FindFirstBit(0x11), 4);
		assertEquals(BitOps.FindFirstBit(0x21), 5);
		assertEquals(BitOps.FindFirstBit(0x41), 6);
		assertEquals(BitOps.FindFirstBit(0x82), 7);
	}

	@Test
	void testFindFirstBitLong() {
		//System.err.print("FindFirstBit: "+					BitOps.FindFirstBit(0x02l) );
		
		assertEquals(BitOps.FindFirstBit(0x01l), 0);
		assertEquals(BitOps.FindFirstBit(0x02l), 1);
		assertEquals(BitOps.FindFirstBit(0x04l), 2);
		assertEquals(BitOps.FindFirstBit(0x08l), 3);
		assertEquals(BitOps.FindFirstBit(0x10l), 4);
		assertEquals(BitOps.FindFirstBit(0x20l), 5);
		assertEquals(BitOps.FindFirstBit(0x40l), 6);
		assertEquals(BitOps.FindFirstBit(0x80l), 7);

		assertEquals(BitOps.FindFirstBit(0x03l), 1);
		assertEquals(BitOps.FindFirstBit(0x05l), 2);
		assertEquals(BitOps.FindFirstBit(0x09l), 3);
		assertEquals(BitOps.FindFirstBit(0x11l), 4);
		assertEquals(BitOps.FindFirstBit(0x21l), 5);
		assertEquals(BitOps.FindFirstBit(0x41l), 6);
		assertEquals(BitOps.FindFirstBit(0x82l), 7);

		assertEquals(BitOps.FindFirstBit(0x100l), 8);
		assertEquals(BitOps.FindFirstBit(0x1000l), 12);
		assertEquals(BitOps.FindFirstBit(0x10000l), 16);
		assertEquals(BitOps.FindFirstBit(0x100000l), 20);
	}

	@Test
	void testBIGMULUS() {
		assertEquals(BitOps.BIGMULUS(1000000, 2, 1), 1000000 );
	}

	@Test
	void testIsValidAsciiChar() {
		assertTrue(BitOps.IsValidAsciiChar((byte) ' '));
		assertTrue(BitOps.IsValidAsciiChar((byte) 'a'));
		assertTrue(BitOps.IsValidAsciiChar((byte) 'z'));
		assertTrue(BitOps.IsValidAsciiChar((byte) 'A'));
		assertTrue(BitOps.IsValidAsciiChar((byte) 'Z'));
		assertTrue(BitOps.IsValidAsciiChar((byte) '0'));
		assertTrue(BitOps.IsValidAsciiChar((byte) '9'));
		assertTrue(BitOps.IsValidAsciiChar((byte) 0xff));

		assertFalse(BitOps.IsValidAsciiChar((byte) 0xAA));
		assertFalse(BitOps.IsValidAsciiChar((byte) 0xAC));
		assertFalse(BitOps.IsValidAsciiChar((byte) 0xAD));
		assertFalse(BitOps.IsValidAsciiChar((byte) 0xAF));

		assertFalse(BitOps.IsValidAsciiChar((byte) 0xB5));
		assertFalse(BitOps.IsValidAsciiChar((byte) 0xB6));
		assertFalse(BitOps.IsValidAsciiChar((byte) 0xB7));
		assertFalse(BitOps.IsValidAsciiChar((byte) 0xB9));
	}

	@Test
	void testI2b() {
		assertEquals(BitOps.i2b(0), false);
		assertNotEquals(BitOps.i2b(33), false);
	}

	@Test
	void testB2i() {
		assertEquals(BitOps.b2i(false), 0);
		assertEquals(BitOps.b2i(true), 1);
	}

	@Test
	void testSubArray() 
	{
		byte [] a = {1, 2, 3, 4, 5 };
		byte [] sub = { 3, 4, 5};
		assertArrayEquals( sub, BitOps.subArray( a , 2) );
	}

	@Test
	void testREAD_LE_UINT16() {
		fail("Not yet implemented");
	}

}
