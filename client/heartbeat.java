package client_linux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class heartbeat extends Thread{
	
	private PrintWriter beat_out;
	private BufferedReader beat_in;
	private char stat = '1';
	private String IP = "0.0.0.0";
	private static String tmp;
	private UDP_conn UDP;
	private InetAddress server_addr;
	

	// stat //1 = active,// 0 = logout //2 = error //3 = request, //4 = port update
	public heartbeat(UDP_conn UDP, PrintWriter out, BufferedReader in, InetAddress server_addr) throws Exception { 
		beat_out = out;
		beat_in = in;
		this.UDP = UDP;
		this.server_addr = server_addr;
	}
	
	public void set_stat(char stat) {
		this.stat = stat;
	}
	
	public void run() {
		try {
			while(true) {
					beat_out.print(stat);
					beat_out.flush();
					tmp = beat_in.readLine();
					if(!(IP.equals(tmp))) {
						IP = tmp;
						beat_out.print('4');
						beat_out.flush();
						UDP.update_port_to_server();
					}
					sleep(3000);
			}
		}
		catch (InterruptedException e) {
			beat_out.close();
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}