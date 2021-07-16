package game;



public class StringID //extends AbstractID 
{
	public final int id;
	
	public StringID(int i) {
		id = i;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj instanceof StringID) {
			StringID si = (StringID) obj;
			return si.id == id;
		}
		
		return super.equals(obj);
	}
	
	//String value; // don't mess
}
