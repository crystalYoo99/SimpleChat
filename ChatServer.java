import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {

	public static void main(String[] args) {
		try{
			ServerSocket server = new ServerSocket(10001);
			System.out.println("Waiting connection...");
			HashMap hm = new HashMap();
			while(true){
				Socket sock = server.accept();
				ChatThread chatthread = new ChatThread(sock, hm);
				chatthread.start();
			}
		}catch(Exception e){
			System.out.println(e);
		}
	}
}

class ChatThread extends Thread{
	private Socket sock;
	private String id;
	private BufferedReader br;
	private HashMap hm;
	private boolean initFlag = false;
	//String[] no = {"fuck","fuckup", "fuckyou", "shut up", "shit"};

	public ChatThread(Socket sock, HashMap hm){
		this.sock = sock;
		this.hm = hm;
		try{
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			id = br.readLine();
			broadcast(id + " entered.");
			System.out.println("[Server] User (" + id + ") entered.");
			synchronized(hm){
				hm.put(this.id, pw);
			}
			initFlag = true;
		}catch(Exception ex){
			System.out.println(ex);
		}
	}
	public void run(){
		List<String> userArray = new ArrayList<String>();
    //five wrong words
		String[] no = {"fuck","fuckup", "fuckyou", "shut up", "shit"};
		int index = 0;
		try{
			String line = null;
			while((line = br.readLine()) != null){
				int start = line.indexOf(" ") +1;
				int end = line.indexOf(" ", start);
				String msg2 = line.substring(end+1);
				index = 0;
        //if user's input is "/userlist", call send_userlist()
				if(line.equals("/userlist"))
					send_userlist();
				else if(line.equals("/quit"))
					break;
				else if(checkwrong(no, line) == true)
						wrongmsg();
				else if(line.indexOf("/to ") == 0){
					sendmsg(line);
				}else {
					broadcast(id + " : " + line);
				}
			}
		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			synchronized(hm){
				hm.remove(id);
			}
			broadcast(id + " exited.");
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	}

  //it prints userlist.
	public void send_userlist() {
		Object obj = hm.get(id);
		PrintWriter pw = (PrintWriter)obj;
		int count = 0;
		synchronized(hm){
			Collection collection = hm.keySet();
			Iterator iter = collection.iterator();
			pw.println("<Userlist>");
			while(iter.hasNext()){
				count++;
				pw.println("["+ count + "]" + iter.next());
				/*PrintWriter pw = (PrintWriter)iter.next();
				pw.println(msg);
				*/
				pw.flush();
			}
		}
	}
	public void sendmsg(String msg){
		int start = msg.indexOf(" ") +1;
		int end = msg.indexOf(" ", start);
		if(end != -1){
			String to = msg.substring(start, end);
			String msg2 = msg.substring(end+1);
			Object obj = hm.get(to);
			if(obj != null){
				PrintWriter pw = (PrintWriter)obj;
				pw.println(id + " whisphered. : " + msg2);
				pw.flush();
			}
		}
	}

  //check if message include wrongword
	public boolean checkwrong(String[] spamlist, String word) {
		boolean check = false;
		for(int i = 0; i < spamlist.length; i++) {
			if(word.contains(spamlist[i]) == true)
				check = true;
		}
		return check;
	}

  //if message include wrongword, this fuction will be called.
  //it prints error message
	public void wrongmsg() {
		Object obj = hm.get(id);
		PrintWriter pw = (PrintWriter)obj;
		pw.println("Don't use bad words");
		pw.flush();
	}

	public void broadcast(String msg){
		synchronized(hm){
			Collection collection = hm.values();
			Iterator iter = collection.iterator();
			//if get_user is not same with send_user, send message to get_user
			//if get_user is same with send_user, continue
			while(iter.hasNext()){
				PrintWriter get_user = (PrintWriter)iter.next();
				PrintWriter send_user = (PrintWriter)hm.get(id);
				if(pw==pw2) continue;
				pw.println(msg);
				pw.flush();
			}
		}
	} // broadcast
}
