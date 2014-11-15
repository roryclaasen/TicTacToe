package net.gogo98901.ox;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class Intro extends JPanel {
	private static final long serialVersionUID = 1L;
	public static String[] names = { "", "" };
	private JTextField player1, player2;
	private JTextArea page1TextArea;
	private JLabel moreInfo;
	private JPanel in1;

	private String page1Text = "To play click on avalible grid squares (the ones that are not shaded, and not red or blue)\n" + //
			"The aim is to get three in a row in the sperate grid on the right by getting three in a row in the sectors in the main grid\n" + //
			"You take turns with your oponent and can go in the sectors your oponent sends you\n\n" + //
			"The Reset button will keep the player names but reset the map\nThe New button go this screen and reset the map";

	private String more = "http://github.com/GOGO98901/TicTacToe#tictactoe";

	public Intro(int width, int height) {
		setLayout(null);
		player1 = new JTextField(names[0]);
		player1.setBounds(width / 2 - 85, 26, 170, 26);
		add(player1);
		player2 = new JTextField(names[1]);
		player2.setBounds(width / 2 - 85, 56, 170, 26);
		add(player2);

		JLabel lblPlayer1 = new JLabel("Player 1");
		lblPlayer1.setBounds(width / 2 - 85 - 50, 26, 100, 26);
		add(lblPlayer1);
		JLabel lblPlayer2 = new JLabel("Player 2");
		lblPlayer2.setBounds(width / 2 - 85 - 50, 56, 100, 26);
		add(lblPlayer2);

		JButton btnPlay = new JButton("PLAY");
		btnPlay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				names[0] = player1.getText();
				names[1] = player2.getText();
				Window.goToPage(false);
			}
		});
		btnPlay.setBounds(width / 2 - 85, height - 60, 170, 26);
		add(btnPlay);

		JButton btnMulti = new JButton("PLAY MULTIPLAYER");
		btnMulti.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				names[0] = player1.getText();
				names[1] = player2.getText();
				Window.goToLobby();
			}
		});
		btnMulti.setBounds(width / 2 - 85, height - 90, 170, 26);
		add(btnMulti);

		moreInfo = new JLabel("<html><b>Click <a href=\"" + more + "\">Here</a> to find out more</b></html>");
		moreInfo.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				try {
					Desktop.getDesktop().browse(new URI(more));
				} catch (URISyntaxException | IOException ex) {
					System.err.println(" INTRO] [ERROR] " + ex);
				}
			}
		});
		moreInfo.setHorizontalAlignment(SwingConstants.CENTER);
		moreInfo.setBounds(0, height - 150, width, 26);
		add(moreInfo);

		in1 = new JPanel(new BorderLayout());
		in1.setBounds(width / 2 - 300, 100, 600, 300);
		page1TextArea = new JTextArea();
		page1TextArea.setSelectionColor(getBackground());
		page1TextArea.setSelectedTextColor(Color.BLACK);
		page1TextArea.setLineWrap(true);
		page1TextArea.setText(page1Text);
		page1TextArea.setEditable(false);
		page1TextArea.setBackground(getBackground());
		page1TextArea.setFont(lblPlayer1.getFont().deriveFont(12F));
		page1TextArea.setWrapStyleWord(true);
		in1.add(page1TextArea, BorderLayout.CENTER);
		add(in1);
	}

	public void reset() {
		names[0] = "";
		names[1] = "";
		player1.setText(names[0]);
		player2.setText(names[1]);
	}
}
