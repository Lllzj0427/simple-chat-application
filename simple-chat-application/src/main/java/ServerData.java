import java.io.Serializable;
import java.util.ArrayList;

// ServerData class that will implement the serializable interface
class ServerData implements Serializable {

	private static final long serialVersionUID = 1L;

	int clientIndex;

	String cmd;
	String userMsg;
	String userInSever;
	ArrayList<Integer> inputUserList;
}
