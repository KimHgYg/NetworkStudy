package gui_client;

import java.net.InetAddress;

public class user_list {
	
	public String ID = null;
	public InetAddress pub_IP = null, pri_IP = null;
	int pri_port, pub_port;
	
	public user_list(String ID, InetAddress pub_IP, InetAddress pri_IP, int pub_port, int pri_port) {
		// TODO Auto-generated constructor stub
		this.ID = ID;
		this.pub_IP = pub_IP;
		this.pri_IP = pri_IP;
		this.pub_port = pub_port;
		this.pri_port = pri_port;
	}

}
