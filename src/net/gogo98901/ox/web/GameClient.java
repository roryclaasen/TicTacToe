package net.gogo98901.ox.web;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

import net.gogo98901.ox.Bootstrap;
import net.gogo98901.ox.Page;
import net.gogo98901.ox.PlayerMP;
import net.gogo98901.ox.Window;
import net.gogo98901.ox.web.packet.Packet;
import net.gogo98901.ox.web.packet.Packet.PacketTypes;
import net.gogo98901.ox.web.packet.Packet00Login;
import net.gogo98901.ox.web.packet.Packet01Disconnect;
import net.gogo98901.ox.web.packet.Packet02Move;
import net.gogo98901.ox.web.packet.Packet03Click;
import net.gogo98901.ox.web.packet.Packet04Start;
import net.gogo98901.ox.web.packet.Packet05Response;

public class GameClient extends Thread {

	private InetAddress ipAddress;
	private DatagramSocket socket;
	private Page game;
	private boolean connected;

	public GameClient(Page game, String ipAddress) {
		this.game = game;
		/* if (!Lobby.scanning) */System.out.println("CLIENT] Scanning [" + ipAddress + "] for game");
		try {
			this.socket = new DatagramSocket();
			this.ipAddress = InetAddress.getByName(ipAddress);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		while (true) {
			byte[] data = new byte[1024];
			DatagramPacket packet = new DatagramPacket(data, data.length);
			try {
				socket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.parsePacket(packet.getData(), packet.getAddress(), packet.getPort());
		}
	}

	private void parsePacket(byte[] data, InetAddress address, int port) {
		String message = new String(data).trim();
		PacketTypes type = Packet.lookupPacket(message.substring(0, 2));
		Packet packet = null;
		switch (type) {
		default:
		case INVALID:
			break;
		case LOGIN:
			packet = new Packet00Login(data);
			handleLogin((Packet00Login) packet, address, port);
			break;
		case DISCONNECT:
			packet = new Packet01Disconnect(data);
			System.out.println("CLIENT] [" + address.getHostAddress() + ":" + port + "] " + ((Packet01Disconnect) packet).getUsername() + " has left the game...");
			game.playerMP = null;
			JOptionPane.showMessageDialog(null, ((Packet01Disconnect) packet).getUsername() + " has quit the game", Bootstrap.getTitle(), JOptionPane.ERROR_MESSAGE);
			Window.goToIntro();
			break;
		case MOVE:
			packet = new Packet02Move(data);
			handleMove((Packet02Move) packet);
		case CLICK:
			packet = new Packet03Click(data);
			this.handleClick(((Packet03Click) packet));
		case START:
			packet = new Packet04Start(data);
			this.handleStart(((Packet04Start) packet));
		case RESPONES:
			packet = new Packet05Response(data);
			this.handleResponse(((Packet05Response) packet));
		}
	}

	private void handleStart(Packet04Start packet) {
		if (packet.getStart() && !Window.hasStarted) {
			System.out.println("CLIENT] Game Started");
			Window.hasStarted = true;
			Window.goToPage(true);
		}
	}

	public void sendData(byte[] data) {
		DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, 1331);
		try {
			socket.send(packet);
		} catch (IOException e) {
			System.err.println("CLIENT] [ERROR] " + e);
		}
	}

	private void handleLogin(Packet00Login packet, InetAddress address, int port) {
		if (game.playerMP == null) {
			System.out.println("CLIENT] [" + address.getHostAddress() + ":" + port + "] " + packet.getUsername() + " has joined the game...");
			PlayerMP player = new PlayerMP(packet.getUsername(), address, port);
			game.playerMP = player;
		}
	}

	private void handleMove(Packet02Move packet) {
		game.mouse(packet.getX(), packet.getY());
		game.repaint();
	}

	private void handleClick(Packet03Click packet) {
		if (packet.getClicked()) game.click();
		game.repaint();
	}

	private void handleResponse(Packet05Response packet) {
		if (packet.getResponse()) connected = true;
		else connected = false;
		if (!Window.hasStarted) System.out.println("CLIENT] connection to " + ipAddress.getHostAddress() + " is " + connected);
	}

	public boolean isConnected() {
		return connected;
	}
}
