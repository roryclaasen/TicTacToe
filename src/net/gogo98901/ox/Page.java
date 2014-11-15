package net.gogo98901.ox;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.gogo98901.ox.web.GameClient;
import net.gogo98901.ox.web.GameServer;
import net.gogo98901.ox.web.packet.Packet02Move;
import net.gogo98901.ox.web.packet.Packet03Click;

public class Page extends JPanel {
	private static final long serialVersionUID = 1L;
	private final int WIDTH;
	private final int HEIGHT;
	private int boxX1 = 0;
	private int boxX2 = 0;
	private int boxY1 = 0;
	private int boxY2 = 0;
	private int boxSize = 32;
	private int lines = 9;
	private int blue = Color.BLUE.getRGB();
	private int red = Color.RED.getRGB();
	public int currentColor = blue;
	private int winner = 0;
	private int[] empty = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	private int[] box1 = (int[]) empty.clone();
	private int[] box2 = (int[]) empty.clone();
	private int[] box3 = (int[]) empty.clone();
	private int[] box4 = (int[]) empty.clone();
	private int[] box5 = (int[]) empty.clone();
	private int[] box6 = (int[]) empty.clone();
	private int[] box7 = (int[]) empty.clone();
	private int[] box8 = (int[]) empty.clone();
	private int[] box9 = (int[]) empty.clone();
	private int[] boxFinal = (int[]) empty.clone();
	private int mx;
	private int my;
	private boolean goodSwap = false;
	private int currentSector = -1;
	private JLabel title;

	private int lastSector;
	private int lastSquare;

	private int turns = 0;

	private JButton newGame;
	private JButton reset;
	private JButton undo;

	private boolean isCurrent = true;

	public GameClient socketClient;
	public GameServer socketServer;

	public Player player, playerMP;

	public Page(int width, int height) {
		setLayout(null);
		WIDTH = width;
		HEIGHT = height;
		title = new JLabel();
		title.setHorizontalAlignment(0);
		title.setBounds(0, 0, width, 50);
		title.setFont(getFont().deriveFont(21.0F));

		add(title);
		reset = new JButton("Reset");
		reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reset();
			}
		});
		reset.setBounds(10, 10, 75, 25);
		add(reset);
		newGame = new JButton("New");
		newGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reset();
				Window.goToIntro();
			}
		});
		newGame.setBounds(10, 40, 75, 25);
		add(newGame);
		undo = new JButton("Undo");
		undo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				undo();
			}
		});
		undo.setBounds(10, 70, 75, 25);
		undo.setEnabled(false);
		undo.setVisible(Bootstrap.showUndo);
		add(undo);
		addMouseMotionListener(new MouseMotionListener() {
			public void mouseMoved(MouseEvent e) {
				if (Window.isMultiplayer()) {
					if (isCurrent) {
						mouse(e.getX(), e.getY());
						Packet02Move packet = new Packet02Move(player.getUsername(), e.getX(), e.getY());
						packet.writeData(socketClient);
					}
				} else mouse(e.getX(), e.getY());
				repaint();
			}

			public void mouseDragged(MouseEvent e) {
				if (Window.isMultiplayer()) {
					if (isCurrent) {
						mouse(e.getX(), e.getY());
						Packet02Move packet = new Packet02Move(player.getUsername(), e.getX(), e.getY());
						packet.writeData(socketClient);
					}

				} else mouse(e.getX(), e.getY());
				repaint();
			}
		});
		addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseClicked(MouseEvent e) {
				if (Window.isMultiplayer()) {
					if (isCurrent) {
						Packet03Click packet = new Packet03Click(true, getClientName());
						packet.writeData(socketClient);
					}
				} else click();
				repaint();
			}
		});
	}

	public void start() {
		if (Window.isMultiplayer()) {
			undo.setVisible(false);
			reset.setVisible(false);
			isCurrent = Lobby.hosting;
			if (isCurrent) {
				Intro.names[0] = player.getUsername();
				Intro.names[1] = playerMP.getUsername();
			} else {
				Intro.names[0] = playerMP.getUsername();
				Intro.names[1] = player.getUsername();
			}
		}
	}

	private void reset() {
		box1 = (int[]) empty.clone();
		box2 = (int[]) empty.clone();
		box3 = (int[]) empty.clone();
		box4 = (int[]) empty.clone();
		box5 = (int[]) empty.clone();
		box6 = (int[]) empty.clone();
		box7 = (int[]) empty.clone();
		box8 = (int[]) empty.clone();
		box9 = (int[]) empty.clone();
		boxFinal = ((int[]) empty.clone());
		currentColor = blue;
		currentSector = -1;
		winner = 0;
		repaint();
	}

	private void swapColors() {
		if (currentColor == blue) {
			currentColor = red;
		} else {
			currentColor = blue;
		}
	}

	public void mouse(int x, int y) {
		mx = x;
		my = y + 25;
	}

	private void drawMouse(Graphics g) {
		int offset = 2;
		g.setColor(new Color(currentColor));
		if (winner == 0) {
			if ((mx >= boxX1 - offset) && (mx <= boxX2 + offset) && (my >= boxY1 + boxSize - (offset * 2 + 4)) && (my <= boxY2 + boxSize - (offset * 2 + 4))) {
				g.fillOval(mx - boxSize / 3, my - boxSize, boxSize / 2, boxSize / 2);
				g.setColor(new Color(0));
				g.drawOval(mx - boxSize / 3, my - boxSize, boxSize / 2, boxSize / 2);
				BufferedImage cursorImg = new BufferedImage(16, 16, 2);
				Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
				setCursor(blankCursor);
			} else setCursor(null);
		} else setCursor(null);

	}

	public void click() {
		if ((mx >= boxX1) && (mx <= boxX2) && (my >= boxY1) && (my <= boxY2 + boxSize)) {
			int currentColorINT = 0;
			if (currentColor == blue) {
				currentColorINT = 1;
			}
			if (currentColor == red) {
				currentColorINT = 2;
			}
			int cusX = boxX1;
			int cusY = boxY1;
			for (int i = 1; i < 10; i++) {
				if (i == 2) {
					cusX = boxX1 + boxSize * 3;
					cusY = boxY1;
				} else if (i == 3) {
					cusX = boxX1 + boxSize * 6;
					cusY = boxY1;
				} else if (i == 4) {
					cusX = boxX1;
					cusY = boxY1 + boxSize * 3;
				} else if (i == 5) {
					cusX = boxX1 + boxSize * 3;
					cusY = boxY1 + boxSize * 3;
				} else if (i == 6) {
					cusX = boxX1 + boxSize * 6;
					cusY = boxY1 + boxSize * 3;
				} else if (i == 7) {
					cusX = boxX1;
					cusY = boxY1 + boxSize * 6;
				} else if (i == 8) {
					cusX = boxX1 + boxSize * 3;
					cusY = boxY1 + boxSize * 6;
				} else if (i == 9) {
					cusX = boxX1 + boxSize * 6;
					cusY = boxY1 + boxSize * 6;
				}
				if (mouseIn(cusX, cusY, boxSize * 3, boxSize * 3)) {
					if (mouseIn(cusX + boxSize * 0, cusY + boxSize * 0, boxSize, boxSize)) addBox(i, 0, currentColorINT);
					if (mouseIn(cusX + boxSize * 1, cusY + boxSize * 0, boxSize, boxSize)) addBox(i, 1, currentColorINT);
					if (mouseIn(cusX + boxSize * 2, cusY + boxSize * 0, boxSize, boxSize)) addBox(i, 2, currentColorINT);
					if (mouseIn(cusX + boxSize * 0, cusY + boxSize * 1, boxSize, boxSize)) addBox(i, 3, currentColorINT);
					if (mouseIn(cusX + boxSize * 1, cusY + boxSize * 1, boxSize, boxSize)) addBox(i, 4, currentColorINT);
					if (mouseIn(cusX + boxSize * 2, cusY + boxSize * 1, boxSize, boxSize)) addBox(i, 5, currentColorINT);
					if (mouseIn(cusX + boxSize * 0, cusY + boxSize * 2, boxSize, boxSize)) addBox(i, 6, currentColorINT);
					if (mouseIn(cusX + boxSize * 1, cusY + boxSize * 2, boxSize, boxSize)) addBox(i, 7, currentColorINT);
					if (mouseIn(cusX + boxSize * 2, cusY + boxSize * 2, boxSize, boxSize)) addBox(i, 8, currentColorINT);
				}
			}
		}
	}

	private boolean mouseIn(int x, int y, int xOffset, int yOffset) {
		if ((mx >= x) && (mx <= x + xOffset) && (my >= y + boxSize) && (my <= y + boxSize + yOffset)) return true;
		return false;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		draw(g);
	}

	private void draw(Graphics g) {
		checkBoxes();

		g.setColor(new Color(16777215));
		g.fillRect(0, 0, WIDTH, HEIGHT);
		drawBox(g);
		setFade(g, 0.4F);
		drawSector(g);
		setFade(g, 1.0F);
		drawGrid(g);
		drawWinner();

		g.setColor(new Color(currentColor));
		g.drawRect(boxX1 - 1, boxY1 - 1, boxX2 - boxX1 + 2, boxY2 - boxY1 + 2);
		g.drawRect(boxX1 - 2, boxY1 - 2, boxX2 - boxX1 + 4, boxY2 - boxY1 + 4);
		g.drawRect(boxX1 - 3, boxY1 - 3, boxX2 - boxX1 + 6, boxY2 - boxY1 + 6);

		drawMouse(g);
		g.clearRect(boxX1 - boxSize - 3, boxY1 - 3, boxSize, boxY2 - boxY1 + 7);
		g.clearRect(boxX1 - 3 - boxSize, boxY1 - boxSize - 3, boxX2 - boxX1 + (boxSize * 2) + 7, boxSize);
		g.clearRect(boxX2 + 4, boxY1 - 3, boxSize, boxY2 - boxY1 + 7);
		g.clearRect(boxX1 - 3 - boxSize, boxY2 + 4, boxX2 - boxX1 + (boxSize * 2) + 7, boxSize);
	}

	private void setFade(Graphics g, float value) {
		AlphaComposite composite = AlphaComposite.getInstance(3, value);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setComposite(composite);
	}

	private void drawSector(Graphics g) {
		if (winner != 0) {
			setFade(g, 0.01F);
		}
		g.setColor(new Color(10658466));
		if (currentSector != -1) {
			if (currentSector != 1) g.fillRect(boxX1 + boxSize * 0, boxY1 + boxSize * 0, boxSize * 3, boxSize * 3);
			if (currentSector != 2) g.fillRect(boxX1 + boxSize * 3, boxY1 + boxSize * 0, boxSize * 3, boxSize * 3);
			if (currentSector != 3) g.fillRect(boxX1 + boxSize * 6, boxY1 + boxSize * 0, boxSize * 3, boxSize * 3);
			if (currentSector != 4) g.fillRect(boxX1 + boxSize * 0, boxY1 + boxSize * 3, boxSize * 3, boxSize * 3);
			if (currentSector != 5) g.fillRect(boxX1 + boxSize * 3, boxY1 + boxSize * 3, boxSize * 3, boxSize * 3);
			if (currentSector != 6) g.fillRect(boxX1 + boxSize * 6, boxY1 + boxSize * 3, boxSize * 3, boxSize * 3);
			if (currentSector != 7) g.fillRect(boxX1 + boxSize * 0, boxY1 + boxSize * 6, boxSize * 3, boxSize * 3);
			if (currentSector != 8) g.fillRect(boxX1 + boxSize * 3, boxY1 + boxSize * 6, boxSize * 3, boxSize * 3);
			if (currentSector != 9) g.fillRect(boxX1 + boxSize * 6, boxY1 + boxSize * 6, boxSize * 3, boxSize * 3);

		}
	}

	private void addBox(int sector, int square, int value) {
		if ((sector == currentSector) || (currentSector == -1)) {
			goodSwap = true;
			if (sector == -1) goodSwap = false;
			else if ((sector == 1) && (box1[square] == 0)) box1[square] = value;
			else if ((sector == 2) && (box2[square] == 0)) box2[square] = value;
			else if ((sector == 3) && (box3[square] == 0)) box3[square] = value;
			else if ((sector == 4) && (box4[square] == 0)) box4[square] = value;
			else if ((sector == 5) && (box5[square] == 0)) box5[square] = value;
			else if ((sector == 6) && (box6[square] == 0)) box6[square] = value;
			else if ((sector == 7) && (box7[square] == 0)) box7[square] = value;
			else if ((sector == 8) && (box8[square] == 0)) box8[square] = value;
			else if ((sector == 9) && (box9[square] == 0)) box9[square] = value;
			else if ((sector == 0) && (boxFinal[square] == 0)) boxFinal[square] = value;
			else goodSwap = false;

			if (goodSwap) {
				lastSector = sector;
				lastSquare = square;
				currentSector = square + 1;
				turns += 1;
				swapColors();
				undo.setEnabled(true);
				if (Window.isMultiplayer()) {
					isCurrent = !isCurrent;
				}
			}
		}
	}

	private void drawBox(Graphics g) {
		int xp = WIDTH / 2 - boxSize * lines;
		int yp = HEIGHT / 2 - boxSize * lines / 2;
		int[] box = box1;
		for (int s = 1; s < 10; s++) {
			if (s == 2) {
				xp = WIDTH / 2 - boxSize * lines + boxSize * 3;
				yp = HEIGHT / 2 - boxSize * lines / 2;
				box = box2;
			} else if (s == 3) {
				xp = WIDTH / 2 - boxSize * lines + boxSize * 6;
				yp = HEIGHT / 2 - boxSize * lines / 2;
				box = box3;
			} else if (s == 4) {
				xp = WIDTH / 2 - boxSize * lines;
				yp = HEIGHT / 2 - boxSize * lines / 2 + boxSize * 3;
				box = box4;
			} else if (s == 5) {
				xp = WIDTH / 2 - boxSize * lines + boxSize * 3;
				yp = HEIGHT / 2 - boxSize * lines / 2 + boxSize * 3;
				box = box5;
			} else if (s == 6) {
				xp = WIDTH / 2 - boxSize * lines + boxSize * 6;
				yp = HEIGHT / 2 - boxSize * lines / 2 + boxSize * 3;
				box = box6;
			} else if (s == 7) {
				xp = WIDTH / 2 - boxSize * lines;
				yp = HEIGHT / 2 - boxSize * lines / 2 + boxSize * 6;
				box = box7;
			} else if (s == 8) {
				xp = WIDTH / 2 - boxSize * lines + boxSize * 3;
				yp = HEIGHT / 2 - boxSize * lines / 2 + boxSize * 6;
				box = box8;
			} else if (s == 9) {
				xp = WIDTH / 2 - boxSize * lines + boxSize * 6;
				yp = HEIGHT / 2 - boxSize * lines / 2 + boxSize * 6;
				box = box9;
			}
			for (int i = 0; i < 9; i++) {
				int color = 16777215;
				if (box[i] == 1) color = blue;
				if (box[i] == 2) color = red;

				g.setColor(new Color(color));
				if (i < 3) g.fillRect(xp + boxSize * i, yp, boxSize, boxSize);
				else if (i < 6) g.fillRect(xp + boxSize * (i - 3), yp + boxSize, boxSize, boxSize);
				else if (i < 9) g.fillRect(xp + boxSize * (i - 6), yp + boxSize + boxSize, boxSize, boxSize);

			}
		}
		xp = WIDTH / 2 - boxSize * lines;
		yp = HEIGHT / 2 - boxSize * lines / 2;
		int xOffset = WIDTH / 2;
		int yOffset = (int) Math.ceil(HEIGHT / 5.25D);
		for (int i = 0; i < 9; i++) {
			int color = Color.WHITE.getRGB();
			if (boxFinal[i] == 1) color = blue;
			if (boxFinal[i] == 2) color = red;

			g.setColor(new Color(color));
			if (i < 3) g.fillRect(xOffset + xp + boxSize * i, yOffset + yp, boxSize, boxSize);
			else if (i < 6) g.fillRect(xOffset + xp + boxSize * (i - 3), yOffset + yp + boxSize, boxSize, boxSize);
			else if (i < 9) g.fillRect(xOffset + xp + boxSize * (i - 6), yOffset + yp + boxSize + boxSize, boxSize, boxSize);

		}
	}

	private void checkBoxes() {
		int[][] boxes = { box1, box2, box3, box4, box5, box6, box7, box8, box9 };
		for (int i = 0; i < 9; i++) {
			if (boxFinal[i] == 0) {
				if ((boxes[i][0] != 0) && (boxes[i][0] == boxes[i][1]) && (boxes[i][1] == boxes[i][2])) boxFinal[i] = boxes[i][2];
				if ((boxes[i][3] != 0) && (boxes[i][3] == boxes[i][4]) && (boxes[i][4] == boxes[i][5])) boxFinal[i] = boxes[i][5];
				if ((boxes[i][6] != 0) && (boxes[i][6] == boxes[i][7]) && (boxes[i][7] == boxes[i][8])) boxFinal[i] = boxes[i][8];
				if ((boxes[i][0] != 0) && (boxes[i][0] == boxes[i][3]) && (boxes[i][3] == boxes[i][6])) boxFinal[i] = boxes[i][6];
				if ((boxes[i][1] != 0) && (boxes[i][1] == boxes[i][4]) && (boxes[i][4] == boxes[i][7])) boxFinal[i] = boxes[i][7];
				if ((boxes[i][2] != 0) && (boxes[i][2] == boxes[i][5]) && (boxes[i][5] == boxes[i][8])) boxFinal[i] = boxes[i][8];
				if ((boxes[i][2] != 0) && (boxes[i][2] == boxes[i][4]) && (boxes[i][4] == boxes[i][6])) boxFinal[i] = boxes[i][6];
				if ((boxes[i][0] != 0) && (boxes[i][0] == boxes[i][4]) && (boxes[i][4] == boxes[i][8])) boxFinal[i] = boxes[i][8];
			}
		}
		if ((boxFinal[0] != 0) && (boxFinal[0] == boxFinal[1]) && (boxFinal[1] == boxFinal[2])) winner = boxFinal[2];
		if ((boxFinal[3] != 0) && (boxFinal[3] == boxFinal[4]) && (boxFinal[4] == boxFinal[5])) winner = boxFinal[5];
		if ((boxFinal[6] != 0) && (boxFinal[6] == boxFinal[7]) && (boxFinal[7] == boxFinal[8])) winner = boxFinal[8];
		if ((boxFinal[0] != 0) && (boxFinal[0] == boxFinal[3]) && (boxFinal[3] == boxFinal[6])) winner = boxFinal[6];
		if ((boxFinal[1] != 0) && (boxFinal[1] == boxFinal[4]) && (boxFinal[4] == boxFinal[7])) winner = boxFinal[7];
		if ((boxFinal[2] != 0) && (boxFinal[2] == boxFinal[5]) && (boxFinal[5] == boxFinal[8])) winner = boxFinal[8];
		if ((boxFinal[2] != 0) && (boxFinal[2] == boxFinal[4]) && (boxFinal[4] == boxFinal[6])) winner = boxFinal[6];
		if ((boxFinal[0] != 0) && (boxFinal[0] == boxFinal[4]) && (boxFinal[4] == boxFinal[8])) winner = boxFinal[8];
		if (winner != 0) currentSector = 10;

		boolean full = true;
		for (int i = 0; i < 9; i++) {
			for (int s = 0; s < 9; s++) {
				if (boxes[i][s] == 0) full = false;
			}
		}
		if (full && winner == 0) winner = 3;
	}

	private void drawGrid(Graphics g) {
		int xp = WIDTH / 2 - boxSize * lines;
		int yp = HEIGHT / 2 - boxSize * lines / 2;
		boxX1 = xp;
		boxX2 = (boxX1 + boxSize * lines);
		boxY1 = yp;
		boxY2 = (boxY1 + boxSize * lines);

		g.setColor(new Color(0));
		for (int y = 0; y < lines + 1; y++) {
			int ya = yp + boxSize * y;
			g.drawLine(xp, ya, xp + boxSize * lines, ya);
		}
		for (int x = 0; x < lines + 1; x++) {
			int xa = xp + boxSize * x;
			g.drawLine(xa, yp, xa, yp + boxSize * lines);
		}
		int xOffset = WIDTH / 2;
		int yOffset = (int) Math.ceil(HEIGHT / 5.25D);
		for (int y = 0; y < 4; y++) {
			int ya = yp + boxSize * y;
			g.drawLine(xOffset + xp, yOffset + ya, xOffset + xp + boxSize * 3, yOffset + ya);
		}
		for (int x = 0; x < 4; x++) {
			int xa = xp + boxSize * x;
			g.drawLine(xOffset + xa, yOffset + yp, xOffset + xa, yOffset + yp + boxSize * 3);
		}
		g.drawRect(xp + xOffset - 1, yp + yOffset - 1, boxSize * 3 + 2, boxSize * 3 + 2);
		g.drawRect(xp + xOffset - 2, yp + yOffset - 2, boxSize * 3 + 4, boxSize * 3 + 4);
		g.drawRect(boxX1 + boxSize * 0 + 1, boxY1 + boxSize * 0 + 1, boxSize * 3 - 2, boxSize * 3 - 2);
		g.drawRect(boxX1 + boxSize * 3 + 1, boxY1 + boxSize * 0 + 1, boxSize * 3 - 2, boxSize * 3 - 2);
		g.drawRect(boxX1 + boxSize * 6 + 1, boxY1 + boxSize * 0 + 1, boxSize * 3 - 2, boxSize * 3 - 2);
		g.drawRect(boxX1 + boxSize * 0 + 1, boxY1 + boxSize * 3 + 1, boxSize * 3 - 2, boxSize * 3 - 2);
		g.drawRect(boxX1 + boxSize * 3 + 1, boxY1 + boxSize * 3 + 1, boxSize * 3 - 2, boxSize * 3 - 2);
		g.drawRect(boxX1 + boxSize * 6 + 1, boxY1 + boxSize * 3 + 1, boxSize * 3 - 2, boxSize * 3 - 2);
		g.drawRect(boxX1 + boxSize * 0 + 1, boxY1 + boxSize * 6 + 1, boxSize * 3 - 2, boxSize * 3 - 2);
		g.drawRect(boxX1 + boxSize * 3 + 1, boxY1 + boxSize * 6 + 1, boxSize * 3 - 2, boxSize * 3 - 2);
		g.drawRect(boxX1 + boxSize * 6 + 1, boxY1 + boxSize * 6 + 1, boxSize * 3 - 2, boxSize * 3 - 2);
	}

	private void drawWinner() {
		String currentPlayer = "";
		int player = 0;
		if (currentColor == blue) player = 1;
		if (currentColor == red) player = 2;
		if (Intro.names[(player - 1)].length() != 0) currentPlayer = Intro.names[(player - 1)];
		else currentPlayer = "Player " + player;

		if (winner != 0) {
			if (currentColor == red) player = 1;
			if (currentColor == blue) player = 2;
			if (Intro.names[(player - 1)].length() != 0) currentPlayer = Intro.names[(player - 1)];
			else currentPlayer = "Player " + player;
			if (winner == 3) title.setText("Its a draw");
			else title.setText(currentPlayer + " is the winner");
			undo.setEnabled(false);
		} else title.setText(currentPlayer + "'s turn");
	}

	public void undo() {
		undo.setEnabled(false);
		if (lastSector - 1 == 0) box1[lastSquare] = 0;
		if (lastSector - 1 == 1) box2[lastSquare] = 0;
		if (lastSector - 1 == 2) box3[lastSquare] = 0;
		if (lastSector - 1 == 3) box4[lastSquare] = 0;
		if (lastSector - 1 == 4) box5[lastSquare] = 0;
		if (lastSector - 1 == 5) box6[lastSquare] = 0;
		if (lastSector - 1 == 6) box7[lastSquare] = 0;
		if (lastSector - 1 == 7) box8[lastSquare] = 0;
		if (lastSector - 1 == 8) box9[lastSquare] = 0;
		currentSector = lastSector;
		turns -= 1;
		if (turns <= 0) currentSector = -1;
		swapColors();
		repaint();
	}

	public static String getClientName() {
		if (Window.isMultiplayer()) return Intro.names[0];
		else {
			if (Intro.names[0].length() > 0) return Intro.names[0];
			else return "Player 1";
		}
	}
}
