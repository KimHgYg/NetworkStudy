package client_linux;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.PrintWriter;

public class get_info {
	private Socket client;
	private BufferedReader in;
	private PrintWriter out;
	private int port;
	private String ID, pswd;
	
	private static heartbeat hb;
	private static UDP_conn[] udp;
	
	private String serverIP;
	private InetSocketAddress isa;
	private InetAddress addr;
	
	public get_info(InetAddress addr, int port, String serverIP) throws IOException {
		isa = new InetSocketAddress(addr, port);
		client = new Socket();
		client.setReuseAddress(true);
		client.connect(isa);
		client.setSoTimeout(0);
		client.setSoLinger(true, 0);
		in = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
		out = new PrintWriter(this.client.getOutputStream(),true);
		
		this.addr = addr;
		this.serverIP = serverIP;
		this.udp = new UDP_conn[10];
	}
	
	//통신 기본 포트는 30000으로 설정
	//로그인 시 UDP_conn 먼저 실행 후 heartbeat로 포트 전달.
	public UDP_conn[] login(String ID, String pswd) throws Exception {
		String status;
		int stat, i;
		this.ID = ID;
		this.pswd = pswd;
		out.print('2');
		out.flush();
		out.print(this.ID + " " + this.pswd);
		out.flush();
		status = in.readLine();
		stat = Integer.parseInt(status);
		if(stat==0) {
			System.out.println("No account");
		}
		else if(stat == 1) {
			System.out.println("Wrong password");
		}
		else {
			for(i = 0 ; i < 10 ; i ++) {
				udp[i]= new UDP_conn(this, addr, ID, i);
			}
			hb = new heartbeat(this, udp, out, in, addr);
			hb.start();
			
			System.out.println("Login");
			
		}
		return udp;
	}
	
	public void logout() throws Exception {
		hb.interrupt();
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		hb.join();
		out.close();
		System.out.println("정상적으로 로그아웃 되었습니다");
	}
	
	public int reqStat(String ID) {
		String stat = null;
		int index = this.get_index();
		if(index == -1) {
			System.out.println("더 이상 채팅 연결을 할 수 없습니다");
			return -1;
		}
		out.print('3' + "" +'0' + "" + ID + "" + " " + "" + index);
		out.flush();
		try {
			stat = in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(stat.equals("1")) { 
			System.out.println("요청 완료");
		}
		else if(stat.equals("0")) {
			System.out.println("로그아웃 상태");
		}
		else if(stat.equals("2")) {
			System.out.println("존재하지 않는 사용자입니다");
		}
		else {
			System.out.println("서버 오류");
			stat = "-1";
		}
		return index;
	}
	
	public int create_account(String Name, String ID, String pswd) throws IOException{
		String status;
		this.ID = ID;
		this.pswd = pswd;
		out.print('1');
		out.flush();
		out.print(Name + " " + this.ID + " " + this.pswd);
		out.flush();
		status = in.readLine();
		
		return Integer.parseInt(status);
	}	
	
	public int get_port() {
		return this.port;	
	}
	
	public int get_index() {
		for(int i = 0 ; i < 10 ; i++) {
			if(udp[i].get_avail())
				return i;
		}
		return -1;
	}
}
