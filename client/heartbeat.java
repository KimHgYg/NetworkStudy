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
	private InetAddress server_addr;
	private String IP = "0.0.0.0";
	private String myIP = "0.0.0.0";
	private static String tmp,tmp2;
	
	private UDP_conn[] UDP;
	private get_info gi;
	

	// stat //1 = active,// 0 = logout //2 = error //3 = request, //4 = port update
	public heartbeat(get_info gi, UDP_conn[] UDP, PrintWriter out, BufferedReader in, InetAddress server_addr) throws Exception { 
		beat_out = out;
		beat_in = in;
		this.UDP = UDP;
		this.gi = gi;
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
					tmp2 = UDP[0].get_myIP();
					//UDP 통신 상대 정보 바뀌었을 때
					if(tmp.equals("-1")) {
						String tmp = beat_in.readLine();
					}
					//내 정보 바꼈을 때
					else if(!(IP.equals(tmp))||!(myIP.equals(tmp2))) {
						IP = tmp;
						myIP = tmp2;
						for(int i = 0 ;i < 10; i ++)
							UDP[i].update_port_to_server(beat_out);
					}
					sleep(3000);
			}
		}catch (InterruptedException e) {
			beat_out.close();
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}