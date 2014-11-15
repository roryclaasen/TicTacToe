package net.gogo98901.ox.web;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import net.gogo98901.Bootstrap;
import net.gogo98901.ox.Page;
import net.gogo98901.ox.Window;
import net.gogo98901.ox.player.PlayerMP;
import net.gogo98901.ox.web.packet.Packet;
import net.gogo98901.ox.web.packet.Packet.PacketTypes;
import net.gogo98901.ox.web.packet.Packet00Login;
import net.gogo98901.ox.web.packet.Packet01Disconnect;
import net.gogo98901.ox.web.packet.Packet02Move;
import net.gogo98901.ox.web.packet.Packet03Click;
import net.gogo98901.ox.web.packet.Packet04Start;
import net.gogo98901.ox.web.packet.Packet05Response;

public class GameServer extends Thread {

	private DatagramSocket socket;
	@SuppressWarnings("unused")
	private Page game;
	private List<PlayerMP> connectedPlayers = new ArrayList<PlayerMP>();

	public GameServer(Page game) {
		System.out.println("SERVER] Started");
		this.game = game;
		try {
			this.socket = new DatagramSocket(1331);
		} catch (SocketException e) {
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
		try {
			String message = new String(data).trim();
			PacketTypes type = Packet.lookupPacket(message.substring(0, 2));
			Packet packet = null;
			switch (type) {
			default:
			case INVALID:
				break;
			case LOGIN:
				if (connectedPlayers.size() < 2) {
					packet = new Packet00Login(data);
					System.out.println("SERVER] [" + address.getHostAddress() + ":" + port + "] " + ((Packet00Login) packet).getUsername() + " has connected...");
					PlayerMP player = new PlayerMP(((Packet00Login) packet).getUsername(), address, port);
					this.addConnection(player, (Packet00Login) packet);
				}
				if (connectedPlayers.size() == 2 && !Window.hasStarted) {
					packet = new Packet04Start(true);
					packet.writeData(this);
				}
				if (!Window.hasStarted) {
					packet = new Packet05Response(true);
					packet.writeData(this);
				}
				break;
			case DISCONNECT:
				packet = new Packet01Disconnect(data);
				System.out.println("SERVER] [" + address.getHostAddress() + ":" + port + "] " + ((Packet01Disconnect) packet).getUsername() + " has left...");
				this.removeConnection((Packet01Disconnect) packet);
				JOptionPane.showMessageDialog(null, ((Packet01Disconnect) packet).getUsername() + " has quit the game", Bootstrap.getTitle(), JOptionPane.ERROR_MESSAGE);
				Window.goToIntro();
				break;
			case MOVE:
				packet = new Packet02Move(data);
				this.handleMove(((Packet02Move) packet));
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
		} catch (Exception e) {
			System.err.println("SERVER] [ERROR] " + e);
		}
	}

	public void addConnection(PlayerMP player, Packet00Login packet) {
		boolean alreadyConnected = false;
		for (PlayerMP p : this.connectedPlayers) {
			if (player.getUsername().equalsIgnoreCase(p.getUsername())) {
				if (p.ipAddress == null) {
					p.ipAddress = player.ipAddress;
				}
				if (p.port == -1) {
					p.port = player.port;
				}
				alreadyConnected = true;
			} else {
				// relay to the current connected player that there is a new
				// player
				sendData(packet.getData(), p.ipAddress, p.port);

				// relay to the new player that the currently connect player
				// exists
				packet = new Packet00Login(p.getUsername());
				sendData(packet.getData(), player.ipAddress, player.port);
			}
		}
		if (!alreadyConnected) {
			this.connectedPlayers.add(player);
		}
	}

	public void removeConnection(Packet01Disconnect packet) {
		try {
			this.connectedPlayers.remove(getPlayerMPIndex(packet.getUsername()));
			packet.writeData(this);
		} catch (Exception e) {
			System.err.println("SERVER] [ERROR] " + e);
		}
	}

	public PlayerMP getPlayerMP(String username) {
		for (PlayerMP player : this.connectedPlayers) {
			if (player.getUsername().equals(username)) {
				return player;
			}
		}
		return null;
	}

	public int getPlayerMPIndex(String username) {
		int index = 0;
		for (PlayerMP player : this.connectedPlayers) {
			if (player.getUsername().equals(username)) {
				break;
			}
			index++;
		}
		return index;
	}

	public void sendData(byte[] data, InetAddress ipAddress, int port) {
		DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
		try {
			this.socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void sendDataToAllClients(byte[] data) {
		for (PlayerMP p : connectedPlayers) {
			sendData(data, p.ipAddress, p.port);
		}
	}

	private void handleMove(Packet02Move packet) {
		if (getPlayerMP(packet.getUsername()) != null) {
			int index = getPlayerMPIndex(packet.getUsername());
			PlayerMP player = this.connectedPlayers.get(index);
			player.setX(packet.getX());
			player.setY(packet.getY());
			packet.writeData(this);
		}
	}

	private void handleClick(Packet03Click packet) {
		if (getPlayerMP(packet.getUsername()) != null) {
			int index = getPlayerMPIndex(packet.getUsername());
			PlayerMP player = this.connectedPlayers.get(index);
			player.setClicked(packet.getClicked());
			packet.writeData(this);
		}
	}

	private void handleStart(Packet04Start packet) {
		if (connectedPlayers.size() == 2) packet.setStart(true);
		else packet.setStart(false);
		packet.writeData(this);
	}

	private void handleResponse(Packet05Response packet) {

	}
}
