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
		fail("Not yet implemented");
	}

	@Test
	void testMinu() {
		fail("Not yet implemented");
	}

	@Test
	void testMaxu() {
		fail("Not yet implemented");
	}

	@Test
	void testClamp() {
		fail("Not yet implemented");
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
		fail("Not yet implemented");
	}

	@Test
	void testCHANCE16R() {
		fail("Not yet implemented");
	}

	@Test
	void testCHANCE16I() {
		fail("Not yet implemented");
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
	void testROR() {
		fail("Not yet implemented");
	}

	@Test
	void testALIGN() {
		fail("Not yet implemented");
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
		fail("Not yet implemented");
	}

	@Test
	void testIsValidAsciiChar() {
		fail("Not yet implemented");
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
	void testSubArray() {
		fail("Not yet implemented");
	}

	@Test
	void testREAD_LE_UINT16() {
		fail("Not yet implemented");
	}

}
