
import java.util.HashMap;
import java.util.ArrayList;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;




//  Server GUI have one scene: an scence that display all server message
//  Client GUI have one scene: disaplay all message sent by other users, display message if someone joined or left the server,
//  can send the message to all users, selected group of clients, and specific client
public class GuiServer extends Application{

	TextField c1, multiMsg, oneMsg;
	TextField groupMsg, specificMsg;
	
	Button serverChoice,clientChoice,b1, multiSend, oneSend, multiTextSend, oneTextSend;
	Button serverQuit, clientQuit;
	
	HashMap<String, Scene> sceneMap;
	GridPane grid;
	HBox buttonBox;
	VBox clientBox;
	Scene startScene;
	BorderPane startPane;
	Server serverConnection;
	Client clientConnection;
	
	// listItems 2 displays message sent by other users
	ListView<String> listItems, listItems2;
	
	// display message when a client joint or left
	ListView<String> clientJoin = new ListView<>();
	
	// arraylist to stores multiple clients selected by user
	ArrayList<Integer> userList = new ArrayList<>();
	
	// arraylist to stores one client selected by user
	ArrayList<Integer> oneUserList = new ArrayList<>();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		primaryStage.setTitle("The Networked Client/Server GUI Example");
		
		// user can leave the server by clicking quit button 
		serverQuit = new Button("Quit");
		serverQuit.setOnAction(e->{
			
			Platform.exit();
            System.exit(0);
		});
		
		// user can leave the client by clicking quit button 
		clientQuit = new Button("Quit");
		clientQuit.setOnAction(e->{
					
					Platform.exit();
		            System.exit(0);
		});
		
		this.serverChoice = new Button("Server");
		this.serverChoice.setStyle("-fx-pref-width: 300px");
		this.serverChoice.setStyle("-fx-pref-height: 300px");
		
		clientJoin.getItems().add("Other user information: ");
		
		multiMsg = new TextField();
		oneMsg = new TextField();
		
		groupMsg = new TextField();
		specificMsg = new TextField();
		
		groupMsg.setDisable(true);
		specificMsg.setDisable(true);
		
		
		// connect to server
		this.serverChoice.setOnAction(e->{ primaryStage.setScene(sceneMap.get("server"));
											primaryStage.setTitle("This is the Server");
				serverConnection = new Server(data -> {
					Platform.runLater(()->{
						listItems.getItems().add(data.toString());
					});

				});
											
		});
		serverChoice.setStyle("-fx-border-width: 15;" + "-fx-border-height: 8;" + "-fx-background-color: lightblue;"+ "-fx-font-size: 20;"
				+ "-fx-border-color: lightgreen;");
		
		this.clientChoice = new Button("Client");
		this.clientChoice.setStyle("-fx-pref-width: 300px");
		this.clientChoice.setStyle("-fx-pref-height: 300px");
		
		// connect to client
		this.clientChoice.setOnAction(e-> {primaryStage.setScene(sceneMap.get("client"));
											primaryStage.setTitle("This is a client");
											clientConnection = new Client(data->{
							Platform.runLater(()->{listItems2.getItems().add(data.toString());
											});
							});
							
											clientConnection.start();
											
											// display message sent by other user through listview
											clientConnection.otherLists(data->{
												Platform.runLater(()->{clientJoin.getItems().add(data.toString());
												});
											});
											
											// update the screen if the user entered invalid group of users
											clientConnection.NonvalidUserScene(data->{
												Platform.runLater(()->{
																		groupMsg.setDisable(true);
																		multiTextSend.setDisable(true);
																		
																		multiMsg.setDisable(false);
																		multiSend.setDisable(false);
																		sceneMap.put("client",  createClientGui((String) data));
												
																		primaryStage.setScene(sceneMap.get("client"));
																		userList.clear();
																		
												});
											});
											
											// update the screen if the user entered valid group of users
											clientConnection.ValidUserScene(data->{
												Platform.runLater(()->{
																		groupMsg.setDisable(false);
																		multiTextSend.setDisable(false);
																		
																		multiMsg.setDisable(true);
																		multiSend.setDisable(true);
																		sceneMap.put("client",  createClientGui((String) data));
												
																		primaryStage.setScene(sceneMap.get("client"));
																		
																		
												});
											});
											
											// update the screen if the user entered invalid specific user
											clientConnection.NonvalidOneUserScene(data->{
												Platform.runLater(()->{
																		specificMsg.setDisable(true);
																		oneTextSend.setDisable(true);
																		
																		oneMsg.setDisable(false);
																		oneSend.setDisable(false);
																		sceneMap.put("client",  createClientGui((String) data));
												
																		primaryStage.setScene(sceneMap.get("client"));
																		oneUserList.clear();
																		
												});
											});
											
											// update the screen if the user entered valid specific user
											clientConnection.ValidOneUserScene(data->{
												Platform.runLater(()->{
																		specificMsg.setDisable(false);
																		oneTextSend.setDisable(false);
																		
																		oneMsg.setDisable(true);
																		oneSend.setDisable(true);
																		sceneMap.put("client",  createClientGui((String) data));
												
																		primaryStage.setScene(sceneMap.get("client"));
																		
																		
												});
											});
		});
		
		
		clientChoice.setStyle("-fx-border-width: 15;" + "-fx-border-height: 8;" + "-fx-background-color: lawngreen;"+ "-fx-font-size: 20;"
				+ "-fx-border-color: lightblue;");
		
		listItems = new ListView<String>();
		listItems2 = new ListView<String>();
		
		c1 = new TextField();
		
		// button to send message to all users
		b1 = new Button("Send to all users");
		b1.setStyle("-fx-border-width: 25;" + "-fx-border-height: 15;" + "-fx-background-color: lightblue;");
		b1.setOnAction(e->{
			
			clientConnection.send(c1.getText(), userList, "all");
			c1.clear();
			//userList.clear();
		});
		
		
		// button to select one or more users to send message to 
		multiSend = new Button("Select one or more users");
		multiSend.setStyle("-fx-border-width: 25;" + "-fx-border-height: 15;" + "-fx-background-color: lightblue;");
		multiSend.setOnAction(e->{
			
			// making sure it follow the format: 1,2,3,4
			try {
				String inputUsers = multiMsg.getText();
				char[] userArr;
				userArr = inputUsers.toCharArray();
				
				int index = 0;
				String userStr = "";
				while (index != userArr.length)
				{
					// convert the number before any ','
					if (Character.compare(',', userArr[index]) == 0 && index +1 != userArr.length)
					{
						int userDef = Integer.parseInt(userStr);
						userList.add(userDef);
						userStr = "";
						index++;
					}
					else
					{
						userStr += userArr[index];
						
						index++;
					}
					
					if (index == userArr.length)
					{
						int userDef = Integer.parseInt(userStr);
						userList.add(userDef);
					}
					
					
				}
				multiMsg.clear();
				clientConnection.verifyClient(userList, "multi");
				
			} 
			catch(Exception e1) {
			
				// wrong input format
				multiMsg.clear();
				sceneMap.put("client",  createClientGui("Error: User not defined. Please enter again."));
				primaryStage.setScene(sceneMap.get("client"));
				userList.clear();
				
			}
		});
		
		// button to send message to selected clients by user
		multiTextSend = new Button("Send to group of selected users");
		multiTextSend.setStyle("-fx-border-width: 25;" + "-fx-border-height: 15;" + "-fx-background-color: lightblue;");
		multiTextSend.setDisable(true);
		
		multiTextSend.setOnAction(e->{
			
			clientConnection.send(groupMsg.getText(), userList, "multi");
			groupMsg.clear();
			userList.clear();
			
			multiTextSend.setDisable(true);
			groupMsg.setDisable(true);
			
			multiMsg.setDisable(false);
			multiSend.setDisable(false);
		});
		
		// button to select one user to send message to 
		oneSend = new Button("Select the specific user");
		oneSend.setStyle("-fx-border-width: 25;" + "-fx-border-height: 15;" + "-fx-background-color: lightblue;");
		oneSend.setOnAction(e->{
			
			try {
				
				int userDef = Integer.parseInt(oneMsg.getText());
				oneUserList.add(userDef);
				
				clientConnection.verifyClient(oneUserList, "one");			
			} 
			catch(Exception e1) {

				sceneMap.put("client",  createClientGui("Error: User not defined."));
				primaryStage.setScene(sceneMap.get("client"));
				oneUserList.clear();
			}
			
			
			oneMsg.clear();

		});
		
		// button to send message to selected client by user
		oneTextSend = new Button("Send to specific user");
		oneTextSend.setStyle("-fx-border-width: 25;" + "-fx-border-height: 15;" + "-fx-background-color: lightblue;");
		
		oneTextSend.setDisable(true);
		oneTextSend.setOnAction(e->{
			
			clientConnection.send(specificMsg.getText(), oneUserList, "one");
			specificMsg.clear();
			
			specificMsg.setDisable(true);
			oneTextSend.setDisable(true);
			
			oneSend.setDisable(false);
			oneMsg.setDisable(false);
			oneUserList.clear();
		});
		
		
		sceneMap = new HashMap<String, Scene>();
		
		sceneMap.put("server",  createServerGui());
		sceneMap.put("client",  createClientGui(""));
		sceneMap.put("welcome",  createWelcomeGui());
		
		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
		
		 
		
		primaryStage.setScene(sceneMap.get("welcome"));
		primaryStage.show();
		
	}
	
	// create a welcome screen which contains server connection button and client connection button
	public Scene createWelcomeGui() {
		
		BorderPane pane = new BorderPane();
		pane.setPadding(new Insets(70));
		pane.setStyle("-fx-background-color: lightpink");
		
		Label welcomeLael = new Label("Click Server button connect to server or click Client button connect to client"); 
		welcomeLael.setStyle("-fx-font-size: 20;");
		welcomeLael.setTextFill(Color.web("dodgerblue"));
		
		buttonBox = new HBox(400, serverChoice, clientChoice);
		buttonBox.setAlignment(Pos.CENTER);
		
		VBox wewlcomeBox = new VBox(100, welcomeLael, buttonBox);
		wewlcomeBox.setAlignment(Pos.CENTER);
		
		pane.setCenter(wewlcomeBox);
		
		startScene = new Scene(pane, 900,800);
	
		return startScene;
	}
	
	// create server screen which contains a listview to display server message
	public Scene createServerGui() {
		
		BorderPane pane = new BorderPane();
		pane.setPadding(new Insets(70));
		pane.setStyle("-fx-background-color: coral");
		
		pane.setCenter(listItems);
		pane.setBottom(serverQuit);
	
		return new Scene(pane, 700, 700);
	}
	
	// create client screen which contains a listview to display message sent by other users, and a listview to display other
	// clients status 
	public Scene createClientGui(String wrongMsg) {
		
		BorderPane pane = new BorderPane();
		Label errorMsg = new Label(wrongMsg);
		errorMsg.setStyle("-fx-font-size: 20;");
		errorMsg.setTextFill(Color.web("red"));
		
		Label userSendMsg = new Label("Updated list for sent message by users");
		userSendMsg.setStyle("-fx-font-size: 20;");
		userSendMsg.setTextFill(Color.web("violet"));
		
		Label otherClientLabel = new Label("Updated list for other clients status, join or leave");
		otherClientLabel.setStyle("-fx-font-size: 20;");
		otherClientLabel.setTextFill(Color.web("limegreen"));
		
		VBox listBox = new VBox(20, userSendMsg, listItems2, otherClientLabel, clientJoin);
		listBox.setAlignment(Pos.CENTER);
		listBox.setPadding(new Insets(50));
		
		VBox oneUserBox = new VBox(20, oneMsg, oneSend,  specificMsg, oneTextSend);
		oneUserBox.setAlignment(Pos.CENTER);
		oneUserBox.setPadding(new Insets(60));
		
//		Label exampleMulti = new Label("");
//		exampleMulti.setStyle("-fx-font-size: 20;");
//		exampleMulti.setTextFill(Color.web("limegreen"));
		
		Label exampleMSpec = new Label("In order to send message to one specific user,"
				+ "enter the client number in this format, "
				+ "specific current client number in the list view, eg: 7");
		exampleMSpec.setStyle("-fx-font-size: 15;");
		exampleMSpec.setTextFill(Color.web("limegreen"));
		
		Label exampleMulti = new Label("In order to send message to other users,"
				+ "enter the client numbers in this format, "
				+ "current client numbers + ',': 1,3,4,5");
		exampleMulti.setStyle("-fx-font-size: 15;");
		exampleMulti.setTextFill(Color.web("limegreen"));
		
		VBox multiUserBox = new VBox(20,  multiMsg, multiSend,  groupMsg, multiTextSend );
		multiUserBox.setAlignment(Pos.CENTER);
		multiUserBox.setPadding(new Insets(50));
		
		HBox userChatBox = new HBox(20, oneUserBox, multiUserBox);
		userChatBox.setAlignment(Pos.CENTER);
		multiUserBox.setPadding(new Insets(30));
		
		clientBox = new VBox(10, c1,b1, exampleMSpec, exampleMulti, userChatBox, errorMsg, listBox);
		
		
		clientBox.setAlignment(Pos.CENTER);
		pane.setStyle("-fx-background-color: wheat");
		
		
		pane.setCenter(clientBox);
		pane.setBottom(clientQuit);
		return new Scene(pane, 1400, 900);
		
	}

}
