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
		fail("Not yet implemented");
	}

	@Test
	void testFindFirstBit2x64() {
		fail("Not yet implemented");
		//System.err.println(BitOps.FindFirstBit2x64(0x80));
		assertEquals(BitOps.FindFirstBit2x64(0x1), 0 );
		assertEquals(BitOps.FindFirstBit2x64(0x8000), 0 );
	}

	@Test
	void testKillFirstBit2x64() {
		fail("Not yet implemented");
	}

	@Test
	void testIS_BYTE_INSIDE() {
		fail("Not yet implemented");
	}

	@Test
	void testIS_INT_INSIDE() {
		fail("Not yet implemented");
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
		fail("Not yet implemented");
	}

	@Test
	void testMyabs64() {
		fail("Not yet implemented");
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
		fail("Not yet implemented");
	}

	@Test
	void testFindFirstBitLong() {
		fail("Not yet implemented");
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
