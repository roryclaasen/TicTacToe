package net.gogo98901.ox.web.packet;

import net.gogo98901.ox.web.GameClient;
import net.gogo98901.ox.web.GameServer;

public class Packet03Click extends Packet {

	private boolean clicked;
	private String username;

	public Packet03Click(byte[] data) {
		super(03);
		String[] dataArray = readData(data).split(",");
		this.username = dataArray[0];
		this.clicked = Boolean.parseBoolean(dataArray[1]);
	}

	public Packet03Click(boolean clicked, String username) {
		super(03);
		this.clicked = clicked;
		this.username = username;
	}

	@Override
	public void writeData(GameClient client) {
		client.sendData(getData());
	}

	@Override
	public void writeData(GameServer server) {
		server.sendDataToAllClients(getData());
	}

	@Override
	public byte[] getData() {
		return ("03" + this.username + "," + this.clicked).getBytes();
	}

	public boolean getClicked() {
		return clicked;
	}

	public String getUsername() {
		return username;
	}

}
