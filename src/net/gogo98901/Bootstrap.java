package net.gogo98901;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.gogo98901.ox.Window;

public class Bootstrap {
	public static final int WIDTH = 700;
	public static final int HEIGHT = 500;
	public static boolean showUndo = false, multi = false;

	private static Window window;

	public static void main(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("-undo")) showUndo = true;
			if (args[i].startsWith("-goToMultiplayer")) multi = true;
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
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				window = new Window(WIDTH, HEIGHT);
				window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				window.setLocationRelativeTo(null);
				window.setResizable(false);
				window.setTitle(getTitle());
				window.setName("A game by Rory Claasen");
				try {
					window.setIconImage(ImageIO.read(Bootstrap.class.getClassLoader().getResourceAsStream("icon.png")));
				} catch (IOException e) {
					e.printStackTrace();
				}
				window.setVisible(true);
			}
		});
	}

	public static void setTitle(String title) {
		window.setTitle(title);
	}

	public static String getTitle() {
		return "Tic Tac Toe";
	}
}
