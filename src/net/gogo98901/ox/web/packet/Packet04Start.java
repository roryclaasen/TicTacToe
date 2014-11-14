package net.gogo98901.ox.web.packet;

import net.gogo98901.ox.web.GameClient;
import net.gogo98901.ox.web.GameServer;

public class Packet04Start extends Packet {

	private boolean start;

	public Packet04Start(byte[] data) {
		super(04);
		String[] dataArray = readData(data).split(",");
		this.start = Boolean.parseBoolean(dataArray[0]);
	}

	public Packet04Start(boolean clicked) {
		super(04);
		start = clicked;
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
		return ("04" + this.start).getBytes();
	}

	public boolean getStart() {
		return start;
	}

	public void setStart(boolean b) {
		this.start = b;
	}
}
