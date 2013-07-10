package org.crowdcomputer.bpmn.utils;

public class ConsoleOutput {
	boolean print = true;

	public ConsoleOutput(boolean print) {
		this.print = print;
	}

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";

	public void println(String s) {
		if (print)
			System.out.println(s);
	}

	public void print(String s) {
		if (print)
			System.out.print(s);
	}

	public void colorPrintln(String color, String s) {
		println(color + s + ANSI_RESET);
	}

	public void colorPrint(String color, String s) {
		print(color + s + ANSI_RESET);
	}

}
