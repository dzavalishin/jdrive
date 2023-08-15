package com.dzavalishin.game.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.dzavalishin.ids.StringID;
import com.dzavalishin.util.BinaryString;

class BinaryStringTest {

	@Test
	void testBinaryString() {
		BinaryString bs = new BinaryString();
		assertEquals(0, bs.length());
		assertEquals("", bs.toString());
	}

	@Test
	void testBinaryStringBinaryString() {
		BinaryString bs = new BinaryString();
		BinaryString bs2 = new BinaryString(bs);
		
		assertEquals(0, bs2.length());
		assertEquals("", bs2.toString());

		bs.append("hello");

		BinaryString bs3 = new BinaryString(bs);

		assertEquals(0, bs2.length());
		assertEquals("", bs2.toString());

		assertEquals(5, bs3.length());
		assertEquals("hello", bs3.toString());
	}

	@Test
	void testBinaryStringString() {
		BinaryString bs = new BinaryString("hello");
		assertEquals(5, bs.length());
		assertEquals("hello", bs.toString());
	}

	@Test
	void testBinaryStringByteArrayIntInt() {
		byte [] bytes = { 'a', 'b', 'c', 'd', 'e', 'f',};
		BinaryString bs = new BinaryString(bytes, 2, 3);
		assertEquals("cde", bs.toString());
	}

	@Test
	void testCharAt() {
		BinaryString bs = new BinaryString("hello");
		assertEquals('e', bs.charAt(1));
	}

	@Test
	void testLength() {
		byte [] bytes = { 'a', 'b', 'c', 'd', 'e', 'f',};
		BinaryString bs = new BinaryString(bytes, 2, 3);
		assertEquals(3, bs.length());
	}

	@Test
	void testSetLength() {
		BinaryString bs = new BinaryString("hello");
		bs.setLength(3);
		assertEquals("hel", bs.toString());

		bs = new BinaryString("hello");
		bs.setLength(12);
		assertEquals("hello", bs.toString());
	}

	@Test
	void testAppendChar() {
		BinaryString bs = new BinaryString();
		bs.append('h');
		bs.append('e');
		bs.append('l');
		assertEquals("hel", bs.toString());
	}

	@Test
	void testAppendByte() {
		BinaryString bs = new BinaryString();
		bs.append((byte)'h');
		bs.append((byte)'e');
		bs.append((byte)'l');
		assertEquals("hel", bs.toString());
	}

	@Test
	void testAppendString() {
		BinaryString bs = new BinaryString();
		bs.append('h');
		bs.append((byte)'e');
		bs.append("lp me");
		assertEquals("help me", bs.toString());
	}

	@Test
	void testToCharArray() {
		BinaryString bs = new BinaryString("hell");
		char [] ca = {'h', 'e', 'l', 'l' };
		assertArrayEquals(ca, bs.toCharArray());
	}

	@Test
	void testInlineStringStringID() {
		BinaryString bs = BinaryString.inlineString(new StringID(22) );
		char [] ca = {0x81, 22, 0 };
		assertArrayEquals(ca, bs.toCharArray());
	}

	@Test
	void testInlineStringInt() {
		BinaryString bs = BinaryString.inlineString(33 << 8);
		char [] ca = {0x81, 0, 33 };
		assertArrayEquals(ca, bs.toCharArray());
	}

	@Test
	void testAppendInlineString() {
		BinaryString bs = new BinaryString("qq");
		bs.appendInlineString(33 << 8);
		char [] ca = { 'q', 'q', 0x81, 0, 33 };
		assertArrayEquals(ca, bs.toCharArray());
	}

	@Test
	void testREAD_LE_int() {
		BinaryString bs = new BinaryString("qq");
		bs.appendInlineString(33 << 8);
		bs.append("dd");
		assertEquals( bs.READ_LE_int(3), 33 << 8 );
	}

	@Test
	void testToString() {
		BinaryString bs = new BinaryString("hello");
		assertEquals("hello", bs.toString());
	}

}
