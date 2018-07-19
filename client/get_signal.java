package client_linux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class get_signal extends Thread{

	private DatagramSocket sock;
	private boolean flag = true;
	private String IP,port;
	private UDP_conn udp;
	public get_signal(DatagramSocket sock, UDP_conn udp) {
		this.sock= sock;
		this.udp = udp;
	}
	
	public void Off() {
		this.flag = false;
	}
	public void On() {
		this.flag = true;
	}

	public void run() {
		while(flag) {
			try {
				byte[] buf = new byte[100];
				DatagramPacket p = new DatagramPacket(buf, buf.length);
				sock.receive(p);
				String byteTostring = new String(buf); //IP, port
				String[] tmp = byteTostring.split(" |\n");
				System.out.println("got signal! " + tmp[0] + " " + tmp[1] + "I'm this : " + sock.getLocalPort());
				udp.UDP_ready(InetAddress.getByName(tmp[0]), Integer.parseInt(tmp[1]));
				this.Off();
			} catch (IOException | InterruptedException e) {
				continue;
			}
		}
	}
}
