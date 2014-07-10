import java.net.*;
import java.util.ArrayList;

public class Server 
{
	private static ArrayList<Socket> clientList;
	
	public static void main(String argv[]) throws Exception
	{
		ServerSocket welcomeSocket = new ServerSocket(6001);
		CelebTree data = new CelebTree();
		clientList = new ArrayList<Socket>();
	    while(true) 
	    {
	    	Socket connectionSocket = null;
	    	try
	    	{
	    		connectionSocket = welcomeSocket.accept();
	    		clientList.add(connectionSocket);
	    		Game game = new Game(connectionSocket,data,clientList);
	    		Thread t = new Thread(game);
	    		t.start();
	    	}
	    	catch(SocketException e) 
	    	{
	    		System.err.println(e);
	    		e.printStackTrace();
	    		continue;
	    	}
	    }
	}
}
