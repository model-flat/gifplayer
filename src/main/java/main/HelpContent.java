package main;

public final class HelpContent {

	public static final String text = 
			"usage:\ngifplaya.jar [options] <filename>\n"
			+ "[options] - one or more of following options:"
			+ "-?, -h, --help - display this text and exit;\n"
			+ "-rot <angle> - specify gif rotation (clockwise);\n"
			+ "-loc <x>:<y> - specify window location on screen;\n"
			+ "-ref <x|y> - reflect gif with respect to the x- or y-axis;\n"
			+ "-scale <scaling factor> - scale gif by specified factor;\n"
			+ "-debug - enable debug mode;\n"
			+ "-nontop - make gif non-topmost window;\n"
			+ "-cloc - make -loc argument determine center of window instead of upper-left corner.\n"
			+ "\n"
			+ "<filename> - path/name of the gif to be played;\n"
			+ "\n"
			+ "you can close playing gif easily by holding Ctrl+Alt and clicking on it\n";
	
	public static final String version = "\nVersion: 0.8\n";
	
}
