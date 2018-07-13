package client_linux;

import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class heartbeat extends Thread{
	
	private PrintWriter beat_out;
	private Socket beat;
	private char stat = '1';
	// stat 1 = active, 0 = logout 2 = error
	public heartbeat(InetSocketAddress isa) throws Exception { 
		
		beat = new Socket();
		beat.setReuseAddress(true);
		beat.connect(isa);
		beat.setSoTimeout(0);
		beat.setSoLinger(true, 0);
		
		beat_out = new PrintWriter(this.beat.getOutputStream(),true);
	}
	
	public void set_stat(char stat) {
		this.stat = stat;
	}
	
	public void run() {
		try {
			while(true) {
					beat_out.print(stat);
					beat_out.flush();
					sleep(3000);
			}
		}
		catch (InterruptedException e) {
			//TODO Auto-generated catch block
			beat_out.close();
			return;
		}
	}
}