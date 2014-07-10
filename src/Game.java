import java.io.*;
import java.net.*;
import java.util.ArrayList;
public class Game implements Runnable 
{
    private Socket socket;
    private BufferedReader inFromClient;
    private PrintWriter outToClient;
    private CelebTree game;
    private ArrayList<Socket> clientList;
    private Node current;
    private boolean moreQuestions,valid;
    private String input;

    public Game(Socket socket,CelebTree game,ArrayList<Socket> clientList) throws IOException 
    {
    	this.socket = socket;
    	this.game=game;
    	this.clientList=clientList;
    	inFromClient =
    			new BufferedReader(new InputStreamReader(socket.getInputStream()));
    	outToClient =
    			new PrintWriter(socket.getOutputStream());
    }
    
    public void run() 	
    {
    	moreQuestions = true;
    	input = null;
    	try
    	{
    		outToClient.println("Connection: "+ socket.getInetAddress()+", Port #:"+ socket.getPort());
    		outToClient.println("Would you like to play a celebrity guessing game?");
    		outToClient.flush();
    		input = processBackspace(inFromClient.readLine());
    		input = input.toLowerCase();
    		if (input.equals("yes"))
    		{
    			do
    			{
    				current = game.getRoot();
    				moreQuestions=true;
    				socket.setSoTimeout(0);
        			do
        			{
        				if(current.isLeaf())
    					{
        					Node check = current.getParent();
        					synchronized(game.findKey(game.getRoot(), current.getString()))
        					{
        						socket.setSoTimeout(20000);
        						//anything past the root...
        						//if start than no....
        						
        						try{
        							if(!current.isRoot())
        							{
			        					if(check == null || !check.equals(current.getParent()))
			        					{
			        						current= current.getParent();
			        					}
        							}
        							askQuestion();
        						}
        						catch (SocketTimeoutException e)
        						{
        					        outToClient.println("\nSOCKET TIMEOUT\n");
        					        outToClient.flush();
        					        moreQuestions = false;
        					    }
        					}
        				}
        				else
        				{
        					askQuestion();
        				}
        				
        			}while(moreQuestions==true);
    				socket.setSoTimeout(0);
        			outToClient.println("Would you like to play a celebrity guessing game?");
    	    		outToClient.flush();
    	    		input = processBackspace(inFromClient.readLine());
    	    		input = input.toLowerCase();
    			}while(input.equals("yes"));
    		}
    		else if (input.equals("no"))
    		{
    			outToClient.println("Goodbye, my friend! See you next time!");
    			outToClient.flush();
    		}
    		else
    		{
    			outToClient.println("ERROR - Only Yes/No accepted");
    			outToClient.flush();
    			throw new IOException();
    		}
		}
    	catch(IOException e) 
    	{
		    System.err.println(e);
		    e.printStackTrace();
		}
		finally
		{
			try 
			{
				if (socket != null)
				{
					socket.close();
				}
				removeSocket();
			}
			catch(IOException e) 
			{
			    System.err.println(e);
			}
		}
    }
    
    private void askQuestion() throws IOException
    {
    	//ask question
    	if(current.isLeaf())
		{
			moreQuestions=false;
			outToClient.println("Is your celebrity " + current.getString());
		}
		else 
		{
			outToClient.println(current.getString());
		}
		outToClient.flush();
		
		//gets answer
		input = processBackspace(inFromClient.readLine());
		input = input.toLowerCase();
		if(input.equals("yes"))
		{
			valid = true;
		}
		else if(input.equals("no"))
		{
			valid = false;
		}
		else
		{
			outToClient.println("ERROR - Only Yes/No accepted");
			outToClient.flush();
			throw new IOException();
		}
		
		if(!current.isLeaf())
		{
			current=game.nextQuestion(current, valid);
		}
		else
		{
			if(input.equals("yes"))
			{
				outToClient.println("I'm Smarter than you!\n");
				sendToClient(current.getUser());
			}
			else if(input.equals("no"))
			{
				updateTree();
			}
			else
			{
				outToClient.println("ERROR - Only Yes/No accepted");
				outToClient.flush();
				throw new IOException();
			}
		}
    }
    
    private void updateTree() throws IOException
    {
    	//gets the person whom the individual was thinking about
		outToClient.println("Who are you thinking of?");
		outToClient.flush();
		String newPerson = processBackspace(inFromClient.readLine());
		
		//gets question that distinguishes the two people
		outToClient.println("What question would distinguish between "+ current.getString() +" and "+ newPerson+"?");
		outToClient.flush();
		String question = processBackspace(inFromClient.readLine());
		
		//get validity of question in relation to newPerson
		outToClient.println("Would an answer of yes indicate "+newPerson+"?");
		outToClient.flush();
		String validity = processBackspace(inFromClient.readLine());
		validity = validity.toLowerCase();
		if(validity.equals("yes"))
		{
			valid = true;
		}
		else if(validity.equals("no"))
		{
			valid = false;
		}
		else
		{
			outToClient.println("ERROR - Only Yes/No accepted");
			outToClient.flush();
			throw new IOException();
		}
		//adds a the new celebrity to the celebrity tree
		game.addNewCelebrity(socket.getRemoteSocketAddress(), current,question,newPerson,valid);
		outToClient.println("Thanks for adding "+ newPerson+ "\n");
    }
    
    private String processBackspace(String answer) 
    {
        StringBuilder sb = new StringBuilder();
        for (char c : answer.toCharArray()) 
        {
            if (c == '\b') 
            {
                if (sb.length() > 0) 
                {
                    sb.deleteCharAt(sb.length() - 1);
                }
            } 
            else 
            {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    
    private void removeSocket()
    {
    	for(int i=0;i<clientList.size();i++)
    	{
     	   Socket user = clientList.get(i);
     	   SocketAddress location = user.getRemoteSocketAddress();
     	   if(location.equals(socket.getRemoteSocketAddress()))
     	   {
     		   clientList.remove(i);
     	   }
        }
    }
    
    public void sendToClient(SocketAddress client)throws IndexOutOfBoundsException, IOException 
    {
       for(int i=0;i<clientList.size();i++)
       {
    	   Socket user = clientList.get(i);
    	   SocketAddress location = user.getRemoteSocketAddress();
    	   if(!user.equals(socket) && location.equals(client))
    	   {
    		   PrintWriter out = new PrintWriter(user.getOutputStream());
    		   out.println("Alert: " +socket.getRemoteSocketAddress()+ " thought of your celebrity "+ current.getString());
    		   out.flush();
    	   }
       }
    }
}
