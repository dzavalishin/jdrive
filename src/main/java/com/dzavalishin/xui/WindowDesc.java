package com.dzavalishin.xui;

import java.io.Serializable;

public class WindowDesc implements Serializable 
{
	public final int	left;
    public int			top;
    public final int	width;
    public final int	height;
	final int 			cls;
	final int 			parent_cls;
	final int 			flags;
	final Widget []		widgets;
	final WindowProc 	proc;
    

	// flags
	public static final int WDF_STD_TOOLTIPS   = 1; /* use standard routine when displaying tooltips */
	public static final int WDF_DEF_WIDGET     = 2;	/* default widget control for some widgets in the on click event */
	public static final int WDF_STD_BTN        = 4;	/* default handling for close and drag widgets (widget no 0 and 1) */

	public static final int WDF_UNCLICK_BUTTONS=16; /* Unclick buttons when the window event times out */
	public static final int WDF_STICKY_BUTTON  =32; /* Set window to sticky mode; they are not closed unless closed with 'X' (widget 2) */
	public static final int WDF_RESIZABLE      =64; /* A window can be resized */


	public WindowDesc(
			int left, int top, int width, int height,
			
			//WindowClass cls,
			//WindowClass parent_cls,
			int cls,
			int parent_cls,
			
			int flags,
			Widget []widgets,
			WindowProc proc
			//BiConsumer<Window,WindowEvent> proc
			) {
		this.left = left;
		this.top = top;
		this.width = width;
		this.height = height;
		this.cls = cls;
		this.parent_cls =  parent_cls;
		this.flags = flags;
		this.widgets = widgets;
		this.proc = proc;
	}
};
