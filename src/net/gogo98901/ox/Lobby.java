package net.gogo98901.ox;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import net.gogo98901.ox.player.PlayerMP;
import net.gogo98901.ox.web.GameClient;
import net.gogo98901.ox.web.GameServer;
import net.gogo98901.ox.web.packet.Packet00Login;

public class Lobby extends JPanel {
	private static final long serialVersionUID = 1L;
	private int width, height;

	private JLabel status, status2;

	private JButton serverCreate, serverJoin, serverChange, serverScan, nameChange, nameOkay;

	private JTextField usernameField;

	private static String ip = "localhost";
	private static String username = "";

	private static int USERNAME = 0;
	private static int JOINING = 1;
	private static int CREATING = 2;
	private static int LOOKING = 3;
	private static int MODE = USERNAME;

	public static boolean hosting = false, scanning = false;
	private Page game;

	public Lobby(int width, int height) {
		setLayout(null);
		this.width = width;
		this.height = height;
		game = Window.getPage();
		init();
	}

	public static InetAddress getLocalAddress() throws SocketException {
		Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
		while (ifaces.hasMoreElements()) {
			NetworkInterface iface = ifaces.nextElement();
			Enumeration<InetAddress> addresses = iface.getInetAddresses();

			while (addresses.hasMoreElements()) {
				InetAddress addr = addresses.nextElement();
				if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
					return addr;
				}
			}
		}
		return null;
	}

	private void init() {
		try {
			ip = getLocalAddress().toString().replace("/", "");
		} catch (SocketException e) {
			System.err.print(" LOBBY] [ERROR] " + e);
		}

		status = new JLabel("Enter a Username");
		status.setFont(new Font("Tahoma", Font.PLAIN, 20));
		status.setHorizontalAlignment(SwingConstants.CENTER);
		status.setBounds(0, 0, width, 30);
		add(status);

		status2 = new JLabel("");
		status2.setFont(new Font("Tahoma", Font.PLAIN, 15));
		status2.setHorizontalAlignment(SwingConstants.CENTER);
		status2.setBounds(0, 30, width, 30);
		add(status2);

		serverJoin = new JButton("Join Server");
		serverJoin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MODE = JOINING;
				update();
			}
		});

		usernameField = new JTextField(System.getProperty("user.name"));
		usernameField.setBounds(width / 4, 75, width / 2, 20);
		add(usernameField);

		nameChange = new JButton("Change Username");
		nameChange.setBounds(width / 4, 100, width / 4, 24);
		nameChange.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MODE = USERNAME;
				update();
				nameOkay.setEnabled(true);
				nameChange.setEnabled(false);
				status.setText("Enter a Username");
				repaint();
			}
		});
		add(nameChange);

		nameOkay = new JButton("Set Username");
		nameOkay.setBounds(width / 2, 100, width / 4, 24);
		nameOkay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (usernameField.getText().length() > 0) MODE = LOOKING;
				else MODE = USERNAME;
				update();
				username = usernameField.getText();
				status.setText(username);
				repaint();
			}
		});
		add(nameOkay);

		serverJoin.setBounds(width / 2, height - 100, width / 4, 23);
		add(serverJoin);

		serverChange = new JButton("Change IP");
		serverChange.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String newIP = JOptionPane.showInputDialog("Enter a new IP", ip);
				if (newIP != null) {
					status2.setText(ip = newIP);
					update();
				}
			}
		});
		serverChange.setBounds(width / 4, height - 100, width / 4, 23);
		serverChange.setVisible(false);
		add(serverChange);

		serverCreate = new JButton("Create Server");
		serverCreate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MODE = CREATING;
				update();
			}
		});
		serverCreate.setBounds(width / 4, height - 100, width / 4, 23);
		add(serverCreate);

		serverScan = new JButton("Scan for Server on local network");
		serverScan.setBounds(width / 4, height - 130, width / 2, 23);
		serverScan.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scanForIPs();
			}
		});
		add(serverScan);

		nameChange.setEnabled(false);
		serverChange.setEnabled(false);
		serverJoin.setEnabled(false);
		serverCreate.setEnabled(false);
		serverScan.setEnabled(false);
		usernameField.requestFocus();
	}

	private void update() {
		serverReset();
		repaint();
		revalidate();
		repaint();

		if (MODE == USERNAME) {
			status.setText("Enter a Username");
			status2.setText("");
			serverChange.setVisible(false);
			serverChange.setEnabled(false);
			serverJoin.setEnabled(false);
			serverScan.setEnabled(false);
			serverCreate.setEnabled(false);
			serverCreate.setVisible(true);
			usernameField.setEnabled(true);
		}
		if (MODE == LOOKING) {
			usernameField.setEnabled(false);
			serverChange.setEnabled(true);
			serverJoin.setEnabled(true);
			serverCreate.setEnabled(true);
			nameOkay.setEnabled(false);
			nameChange.setEnabled(true);
		}
		if (MODE == JOINING) {
			status.setText("Looking for server");
			status2.setText(ip);
			serverCreate.setVisible(false);
			serverChange.setVisible(true);
			serverScan.setEnabled(true);
		}
		if (MODE == CREATING) {
			serverScan.setEnabled(false);
			nameChange.setEnabled(false);
			status.setText("Waiting for player");
			status2.setText(ip);
		}
		if (MODE != USERNAME && MODE != LOOKING) server();
	}

	private void serverReset() {
		if (game.socketServer != null) game.socketServer.stopServer();
		if (game.socketClient != null) game.socketClient.stopClient();
		game.socketClient = null;
		game.socketServer = null;
		// System.out.println(" LOBBY] sockets set to null");
	}

	private void server() {
		if (MODE == CREATING) {
			game.socketServer = new GameServer(game);
			game.socketServer.start();
			hosting = true;
		}
		game.socketClient = new GameClient(Window.getPage(), ip);
		game.socketClient.start();

		game.player = new PlayerMP(username, null, -1);
		Packet00Login loginPacket = new Packet00Login(username);
		if (game.socketServer != null) {
			game.socketServer.addConnection((PlayerMP) game.player, loginPacket);
		}
		loginPacket.writeData(game.socketClient);
	}

	public static String getIp() {
		return ip;
	}

	public static String getUsername() {
		return username;
	}

	@SuppressWarnings("static-access")
	public void scanForIPs() {
		Thread t = new Thread() {
			public void run() {
				status.setText("Scanning network for reachable servers");
				repaint();
				String IPV4 = "";
				try {
					String currentIP = getLocalAddress().toString().replaceFirst("/", "");
					String[] parts = currentIP.split("\\.");
					for (int i = 0; i < parts.length; i++) {
						if (i != parts.length - 1) IPV4 += parts[i] + ".";
					}
				} catch (SocketException e) {
					System.err.println(" LOBBY] [ERROR]" + e);
				}
				System.out.println(" LOBBY] [SCAN] Started");
				for (int i = 0; i < 256; i++) {
					scanning = true;
					final String tryIp = IPV4 + i;
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							status2.setText(ip = tryIp);
							status2.repaint();
						}
					});
					try {
						new Thread().sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					serverReset();
					server();
					try {
						new Thread().sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (game.socketClient.isConnected()) break;
				}
				scanning = false;
				try {
					ip = getLocalAddress().toString();
				} catch (SocketException e) {
					System.err.println(" LOBBY] [ERROR]" + e);
				}
			}
		};
		t.start();

	}
}
