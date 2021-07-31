package game.struct;

public class Textbuf {
	//char *buf;                  /* buffer in which text is saved */
	public char [] buf;                  /* buffer in which text is saved */
	public int maxlength, maxwidth; /* the maximum size of the buffer. Maxwidth specifies screensize in pixels */
	public int length, width;       /* the current size of the buffer. Width specifies screensize in pixels */
	public boolean caret;                 /* is the caret ("_") visible or not */
	public int caretpos;            /* the current position of the caret in the buffer */
	public int caretxoffs;          /* the current position of the caret in pixels */

}
