package com.bittiger.logic;

public class ScaleOutInfo extends ControlAction{
	Server src;
	Server dest;
	Server master;
	public ScaleOutInfo(Server src, Server dest, Server master) {
		super();
		this.src = src;
		this.dest = dest;
		this.master = master;
	}
}
