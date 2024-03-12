import java.io.IOException;
import java.io.ObjectInputStream;

import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.scene.control.ListView;

public class Server {

	int count = 1;
	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();

	// map the client with their specific numbers, client # is key, client thread is
	// value
	HashMap<Integer, ClientThread> clientNumMap = new HashMap<>();
	TheServer server;
	private Consumer<Serializable> callback;

	ServerData serverInfo = new ServerData();

	Object lock = new Object();

	Server(Consumer<Serializable> call) {

		callback = call;
		server = new TheServer();
		serverInfo.inputUserList = new ArrayList<>();

		server.start();
	}

	public class TheServer extends Thread {

		public void run() {

			// synchronized(serverLock) {
			try (ServerSocket mysocket = new ServerSocket(5555);) {
				callback.accept("Server is waiting for a client!");

				while (true) {

					ClientThread c = new ClientThread(mysocket.accept(), count);

					// make thread safe in a synchronized block, client must wait until other client
					// finish joined the server
					synchronized (lock) {
						callback.accept("Client has connected to server: " + "client #" + count);

						clients.add(c);
						clientNumMap.put(count, c);
						c.start();

						count++;

					}
				}
			} // end of try
			catch (Exception e) {
				callback.accept("Server socket did not launch");
			}
		}// end of while
		// }
	}

	class ClientThread extends Thread {

		Socket connection;
		int count;
		ObjectInputStream in;
		ObjectOutputStream out;

		Object lock2 = new Object();
		Object lock3 = new Object();

		ClientThread(Socket s, int count) {
			this.connection = s;
			this.count = count;

		}

		public int getCount() {
			return this.count;
		}

		// send data to other users, depending on specific user or group of users, or
		// all users
		public void updateClients(ServerData data) {
			// synchronized(data) {

			// send to all users
			if (data.cmd.equals("all")) {
				callback.accept("Client #" + this.getCount() + " sent message to all users");

				for (int i = 0; i < clients.size(); i++) {
					ClientThread t = clients.get(i);

					serverInfo.cmd = "all outputMsg";
					try {
						// synchronized(data) {
						t.out.writeObject(data);
						t.out.reset();
						// }

					} catch (Exception e) {
					}
				}

				return;
			}

			// send to group of selected users
			else if (data.cmd.equals("multi")) {

				String userNumber = "";
				for (int i = 0; i < data.inputUserList.size(); i++) {
					if (i == data.inputUserList.size() - 1) {
						userNumber += data.inputUserList.get(i) + " ";
					} else {
						userNumber += data.inputUserList.get(i) + ", ";
					}

				}
				callback.accept("Client #" + this.getCount() + " sent message to multi users, user #" + userNumber);
			}

			// send to specific selected user
			else if (data.cmd.equals("one")) {
				callback.accept("Client #" + this.getCount() + " sent message to one users, user #"
						+ data.inputUserList.get(0));
			}

			serverInfo.clientIndex = this.getCount();
			serverInfo.cmd += " outputMsg";
			for (int i = 0; i < data.inputUserList.size(); i++) {
				ClientThread t = clientNumMap.get(data.inputUserList.get(i));
				// t.removeCount();

				try {
					// synchronized(data) {
					t.out.writeObject(data);
					t.out.reset();
					// }

				} catch (Exception e) {
				}
			}
			// }
		}

		public void run() {
			try {
				in = new ObjectInputStream(connection.getInputStream());
				out = new ObjectOutputStream(connection.getOutputStream());
				connection.setTcpNoDelay(true);
			} catch (Exception e) {
				System.out.println("Streams not open");
			}

			// someone joined the server, notify all clients
			synchronized (lock) {
				serverInfo.cmd = "new join";

				String curUser = "";
				for (int i = 0; i < clients.size(); i++) {
					ClientThread t = clients.get(i);
					if (i == clients.size() - 1) {
						curUser += "Client #" + t.getCount() + "";
					} else {
						curUser += "Client #" + t.getCount() + ", ";
					}
				}

				serverInfo.userInSever = curUser;

				for (int i = 0; i < clients.size(); i++) {
					ClientThread t = clients.get(i);
					serverInfo.userMsg = "" + count;
					serverInfo.clientIndex = t.getCount();
					try {
						t.out.writeObject(serverInfo);
						t.out.reset();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}

			while (true) {
				try {

					serverInfo = (ServerData) in.readObject();

					// make thread safe in a synchronized block, client must wait until other client
					// finish sending message
					// or verified the client numbers
					synchronized (lock) {
						if (serverInfo.cmd.equals("verify")) {

							serverInfo.userMsg = "";
							for (int i = 0; i < serverInfo.inputUserList.size(); i++) {
								int inputClientNum = serverInfo.inputUserList.get(i);

								if (i == serverInfo.inputUserList.size() - 1) {
									serverInfo.userMsg += "#" + inputClientNum + " ";
								} else {
									serverInfo.userMsg += "#" + inputClientNum + ", ";
								}

								if (clientNumMap.containsKey(inputClientNum)) {

									serverInfo.cmd = "yes";
								} else {

									serverInfo.cmd = "no";
								}

							}
							callback.accept(
									"Client #" + this.getCount() + " entered user numbers: " + serverInfo.userMsg);

							if (serverInfo.cmd.equals("no")) {
								callback.accept("Client #" + this.getCount() + " entered invalid user numbers.");

							} else {
								callback.accept("Client #" + this.getCount() + " entered valid user numbers.");
							}

							// Thread.sleep(5000);
							callback.accept("Now sending the verify result to client #" + this.getCount() + "...");
							this.out.writeObject(serverInfo);
							this.out.reset();
						}

						else {
							serverInfo.clientIndex = this.getCount();
							callback.accept("Server recieved: client #" + serverInfo.clientIndex + " sent: "
									+ serverInfo.userMsg);
							// callback.accept("Client: " + serverInfo.clientIndex + " sent to server: " +
							// serverInfo.userMsg); // msg will print on server side

							updateClients(serverInfo);
						}

					}
				} catch (Exception e) {

					// make thread safe in a synchronized block, client must wait until other client
					// left the server
					synchronized (lock) {
						callback.accept("OOOOPPs...Something wrong with the socket from client: " + count
								+ "....closing down!");

						clients.remove(this);

						clientNumMap.remove(this.getCount());

						serverInfo.cmd = "update";
						serverInfo.userMsg = "" + count;

						String curUser = "";
						for (int i = 0; i < clients.size(); i++) {
							ClientThread t = clients.get(i);
							if (i == clients.size() - 1) {
								curUser += "Client #" + t.getCount() + "";
							} else {
								curUser += "Client #" + t.getCount() + ", ";
							}

						}

						serverInfo.userInSever = curUser;
						// notified all users that someone left the server

						for (int i = 0; i < clients.size(); i++) {
							ClientThread t = clients.get(i);
							// callback.accept("client num: " + i);
							serverInfo.clientIndex = i;
							try {
								t.out.writeObject(serverInfo);
								t.out.reset();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}

						break;
					}

				}
			}
			// } // synchronized
		}// end of run

	}// end of client thread
}
