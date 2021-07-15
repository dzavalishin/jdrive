public abstract class Hal
{
    // graphics
	abstract start_video(const char * const *parm);
	abstract void stop_video(void);
	abstract void make_dirty(int left, int top, int width, int height);
	abstract void main_loop(void);
	abstract boolean change_resolution(int w, int h);
    
    void toggle_fullscreen(bool fullscreen) { }


}