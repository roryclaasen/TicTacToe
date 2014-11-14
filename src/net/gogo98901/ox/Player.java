package net.gogo98901.ox;

import net.gogo98901.ox.web.packet.Packet02Move;

public class Player {
	private int x, y;
	private String username;
	@SuppressWarnings("unused")
	private boolean clicked;

	public Player(String username) {
		this.username = username;
		clicked = false;
	}

	public void update() {
		Packet02Move packet = new Packet02Move(this.getUsername(), this.x, this.y);
		packet.writeData(Window.getPage().socketClient);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public String getUsername() {
		return username;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setUser(String name) {
		username = name;
	}

	public void setClicked(boolean clicked) {
		this.clicked = clicked;
	}
}
