package net.gogo98901;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.gogo98901.ox.Window;

public class Bootstrap {
	public static final int WIDTH = 700;
	public static final int HEIGHT = 500;
	public static boolean showUndo = false;

	private static Window window;

	public static void main(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("-undo")) showUndo = true;
		}
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		window = new Window(WIDTH, HEIGHT);
		window.setDefaultCloseOperation(3);
		window.setLocationRelativeTo(null);
		window.setResizable(false);
		window.setTitle("Tic Tac Toe");
		window.setName("A game by Rory Claasen");
		try {
			window.setIconImage(ImageIO.read(Bootstrap.class.getClassLoader().getResourceAsStream("icon.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		window.setVisible(true);
	}

	public static void setTitle(String title) {
		window.setTitle(title);
	}

	public static String getTitle() {
		return "Tic Tac Toe";
	}
}
