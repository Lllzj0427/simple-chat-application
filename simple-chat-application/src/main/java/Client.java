import java.io.IOException;


import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;


public class Client extends Thread{

	
	Socket socketClient;
	
	ObjectOutputStream out;
	ObjectInputStream in;
	
	// display the message sent by other users on listview
	private Consumer<Serializable> callback;
	
	// display message if other users left or joined the server
	private Consumer<Serializable> otherMsg;
	
	// verify if the entered client # is valid or not
	private Consumer<Serializable> Nonvaliduser;
	private Consumer<Serializable> Validuser;
	
	private Consumer<Serializable> NonvalidOneuser;
	private Consumer<Serializable> ValidOneuser;
	
	String sendType;
	ServerData userInfo;
	
	
	Client(Consumer<Serializable> call){
	
		userInfo = new ServerData();
		userInfo.inputUserList = new ArrayList<>();
		callback = call;
	}
	
	public void otherLists(Consumer<Serializable> call)
	{
		otherMsg = call;
	}
	
	public void NonvalidUserScene(Consumer<Serializable> call)
	{
		Nonvaliduser = call;
	}
	
	public void ValidUserScene(Consumer<Serializable> call)
	{
		Validuser = call;
	}
	
	public void NonvalidOneUserScene(Consumer<Serializable> call)
	{
		NonvalidOneuser = call;
	}
	
	public void ValidOneUserScene(Consumer<Serializable> call)
	{
		ValidOneuser = call;
	}
	
	// send the entered client numbers to server, to verify if it is valid
	public void verifyClient(ArrayList<Integer> inputClientList, String type)
	{
		userInfo.inputUserList = inputClientList;
		
		userInfo.cmd = "verify";
		sendType = type;
		try {
			this.out.writeObject(userInfo);
			this.out.reset();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run() {
		
		
		try {
		socketClient= new Socket("127.0.0.1",5555);
	    out = new ObjectOutputStream(socketClient.getOutputStream());
	    in = new ObjectInputStream(socketClient.getInputStream());
	    socketClient.setTcpNoDelay(true);
		}
		catch(Exception e) {}
		
		while(true) {
			
			// the lock in client.java
			//synchronized(lock) {
			try {
			userInfo = (ServerData) in.readObject();
			
			
			// the entered client numbers is not valid
			if (userInfo.cmd.equals("no"))
			{
				if(sendType.equals("one"))
				{
					NonvalidOneuser.accept("Error, input user " + userInfo.inputUserList.get(0) + " not defined. Please enter again.");
				}
				else
				{
					Nonvaliduser.accept("Error, input user  " + userInfo.userMsg + "not defined. Please enter again.");
				}
						
				
			}
			
			// the entered client numbers is valid
			else if (userInfo.cmd.equals("yes"))
			{
				if(sendType.equals("one"))
				{
					//Thread.sleep(5000);
					ValidOneuser.accept("");
				}
				else
				{
					Validuser.accept("");
				}
				
			}
					
			// update the list if any users left, and display the current clients in the server
			else if (userInfo.cmd.equals("update"))
			{
				//int clientNum = userInfo.clientIndex+1;
				//otherMsg.accept("");
				otherMsg.accept("Client #" + userInfo.userMsg + " left the server.");
				otherMsg.accept("------------------- Current users in server update start -------------------");
				otherMsg.accept(userInfo.userInSever);
				otherMsg.accept("------------------- Current users in server update end -------------------");
				otherMsg.accept("");
				
			}
			
			// one client sent a message to all users
			else if (userInfo.cmd.equals("all outputMsg"))
			{
				callback.accept("Client #" + userInfo.clientIndex + " sent a message to all users: " + userInfo.userMsg);
			}
			
			// one client sent a message to specific user
			else if (userInfo.cmd.equals("one outputMsg"))
			{
				callback.accept("Client #" + userInfo.clientIndex + " sent a message to you directly: " + userInfo.userMsg);
			}
			
			// one client sent a message to group of users
			else if (userInfo.cmd.equals("multi outputMsg"))
			{
				callback.accept("Client #" + userInfo.clientIndex + " sent a message to these # of users: ");
				String users = "";
				for (int i =0; i< userInfo.inputUserList.size(); i++)
				{
					int userNum = userInfo.inputUserList.get(i);
					
					if (i == userInfo.inputUserList.size()-1)
					{
						users += "#"+ userNum;
					}
					else
					{
						users += "#"+ userNum + ", ";
					}				
					
				}
				callback.accept(users + "\n" + "Sent message: " + userInfo.userMsg);
			}
			
			// update the list if any users joined, and display the current clients in the server
			else if (userInfo.cmd.equals("new join"))
			{
				if (userInfo.userMsg.equals(Integer.toString(userInfo.clientIndex)))
				{
					otherMsg.accept("You are now become client #" + userInfo.userMsg + " in the server.");
				}
				else
				{
					//otherMsg.accept("");
					otherMsg.accept("Client #" + userInfo.userMsg + " join the server.");
				}
				
				otherMsg.accept("");
				otherMsg.accept("------------------- Current users in server update start -------------------");
				otherMsg.accept(userInfo.userInSever);
				otherMsg.accept("------------------- Current users in server update end -------------------");
				otherMsg.accept("");
				
			}
			
			}

			catch(Exception e) {}
		}
		//}
	

	}
	
	// send the message to other users through server
	public void send(String data, ArrayList<Integer> userList, String type) {
		
		userInfo.userMsg = data;
		
		if (type.equals("all"))
		{
			userInfo.cmd = "all";
		}
		else if (userList.size() == 1 && type.equals("one"))
		{
			userInfo.cmd = "one";
			userInfo.inputUserList = userList;
		}
		else if (type.equals("multi"))
		{
			userInfo.cmd = "multi";
			userInfo.inputUserList = userList;
		}
		
		try {
			
			out.writeObject(userInfo);
			this.out.reset();
			userInfo.inputUserList.clear();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}


