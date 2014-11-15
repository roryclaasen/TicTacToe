package net.gogo98901.ox.web.packet;

import net.gogo98901.ox.web.GameClient;
import net.gogo98901.ox.web.GameServer;

public class Packet05Response extends Packet{
	private boolean response;

	public Packet05Response(byte[] data) {
		super(04);
		String[] dataArray = readData(data).split(",");
		this.response = Boolean.parseBoolean(dataArray[0]);
	}

	public Packet05Response(boolean response) {
		super(04);
		this.response = response;
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
		return ("05" + this.response).getBytes();
	}

	public boolean getResponse() {
		return response;
	}

	public void setResponse(boolean response) {
		this.response = response;
	}
}
