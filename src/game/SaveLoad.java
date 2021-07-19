package game;

/** 
 * SaveLoad type struct. 
 * Do NOT use this directly but use the SLE_ 
 * macros defined just below! 
 **/
public class SaveLoad {
		byte cmd;             /// the action to take with the saved/loaded type, All types need different action
		VarType type;         /// type of the variable to be saved, int
		int offset;        /// offset of this variable in the struct (max offset is 65536)
		int length;        /// (conditional) length of the variable (eg. arrays) (max array size is 65536 elements)
		int version_from;  /// save/load the variable starting from this savegame version
		int version_to;    /// save/load the variable until this savegame version

}
