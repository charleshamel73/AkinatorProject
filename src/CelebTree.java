import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.SocketAddress;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class CelebTree 
{
	private static Node root;
	private File saveFile = new File("saveFile");
	private final int MAX_NUM = 9999;
	private int numOfQuestions;
	public CelebTree()
	{
		if(saveFile.isFile())
		{
			//need to fix load now
			loadTree();
		}
		else
		{
			root = new Node(-1,"Barack Obama",null);
			numOfQuestions = 0;
		}
	}
	
	public Node getRoot()
	{
		return root;
	}
	
	public int getSize()
	{
		return numOfQuestions;
	}
	
	public void addNewCelebrity(SocketAddress user, Node current,String question, String newPerson, boolean valid)
	{
		Node newCeleb;
		Node parent=current.getParent();
		newCeleb = new Node(numOfQuestions,question, user);
		if(valid)
		{
			newCeleb.setLeft(new Node(-1,newPerson,user));
			newCeleb.setRight(current);
		}
		else
		{
			newCeleb.setLeft(current);
			newCeleb.setRight(new Node(-1,newPerson,user));
		}
		boolean isleft=false;
		if(root.isLeaf())
		{
			root=newCeleb;
		}
		else if(parent.getLeft() == current)
		{
			parent.setLeft(newCeleb);
			isleft=true;
		}
		else
		{
			parent.setRight(newCeleb);
			isleft=false;
		}
		numOfQuestions++;
		try {
			saveTree(newCeleb,isleft);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Node nextQuestion(Node current, boolean valid)
	{
		if(valid)
		{
			return current.getLeft();
		}
		else
		{
			return current.getRight();
		}
	}
	
	public void loadTree()
	{	
		FileReader file = null;
		BufferedReader in = null;
		ArrayList<Node> nodes = new ArrayList<Node>();
		//ObjectInputStream in = null;
		try 
		{
			//in = new ObjectInputStream(new FileInputStream(saveFile));
			file = new FileReader(saveFile);
			in = new BufferedReader(file);
			String size = processPadding(in.readLine());
			numOfQuestions = Integer.parseInt(size);
			for(int i=0;i<numOfQuestions;i++){
				String node = in.readLine();
				String[] data = node.split(",");
				int id = Integer.parseInt(processPadding(data[0]));
				String currentQ = processPadding(data[1]);
				String leftQ = processPadding(data[2]);
				String rightQ = processPadding(data[3]);
				Node current = new Node(id,currentQ,null);
				current.setLeft(new Node(-1,leftQ,null));
				current.setRight(new Node(-1,rightQ,null));
				nodes.add(current);
			}
			Node save =reAssembleTree(0,nodes);
			root =save;
		} 
		catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try 
			{
				in.close();
			} 
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
        
	}
	
	
	private Node reAssembleTree(int index, ArrayList<Node> nodeList)
	{
		Node node = nodeList.get(index);
		Node left =null;
		Node right=null;
		String leftQ = node.getLeft().getString();
		if(isInteger(leftQ))
		{
			left = reAssembleTree(Integer.parseInt(leftQ),nodeList);
		}
		else
		{
			left = node.getLeft();
		}
		String rightQ = node.getRight().getString();
		if(isInteger(rightQ))
		{
			right = reAssembleTree(Integer.parseInt(rightQ),nodeList);
		}
		else
		{
			right = node.getRight();
		}
		node.setLeft(left);
		node.setRight(right);
		return node;
	}
	
	public static boolean isInteger(String s) 
	{
	    try 
	    { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) 
	    { 
	        return false; 
	    }
	    // only got here if we didn't return false
	    return true;
	}
	
	public Node findKey(Node start, String key)
	{
		if(start.getString().equals(key))
		{
			return start;
		}
		else
		{
			Node answer1=null;
			Node answer2 = null;
			if(start.getLeft()!=null)
			{
				answer1=findKey(start.getLeft(),key);
			}
			if(start.getRight()!=null)
			{
				answer2=findKey(start.getRight(),key);
			}
			if(answer1!=null)
			{
				return answer1;
			}
			else if(answer2!=null)
			{
				return answer2;
			}
			return null;
		}
	}
	
	private String processPadding(String answer) 
    {
        StringBuilder sb = new StringBuilder();
        for (char c : answer.toCharArray()) 
        {
            if (c == '~') 
            {
                return sb.toString();
            }
            else
            {
            	sb.append(c);
            }
        }
        return sb.toString();
    }
	
	public void traverseSave(Node start,PrintWriter out)
	{
		if(start.getLeft()!=null)
		{
			traverseSave(start.getLeft(),out);
		}
		if(!start.isLeaf()){
			String node = "";
			Node left = start.getLeft();
			Node right = start.getRight();
			node = node.concat(start.getSave()+",");
			node = node.concat(left.getSave()+",");
			node = node.concat(right.getSave());
			out.println(node);
		}
		
		if(start.getRight()!=null)
		{
			traverseSave(start.getRight(),out);
		}
	}
	
	public synchronized void saveTree(Node newCeleb, boolean isleft) throws IOException
	{
		RandomAccessFile raf = null;
		Node modify = newCeleb.getParent();
		try 
		{
			raf = new RandomAccessFile(saveFile,"rw");
			String input=null;
			//for int
			String s = addIdPadding(Integer.toString(numOfQuestions))+"\n";
			raf.write(s.getBytes());
			input=raf.readLine();
			while(input!=null)
			{
				String[] data= input.split(",");
				int id = Integer.parseInt(processPadding(data[0]));
				if(modify.getID() == id)
				{
					//get offset and go to location
					//int offset= 305+((count-1)*303);
					
					raf.seek(raf.getFilePointer()-308);
					//get updated node
					String question = processPadding(data[1]);
					Node update = findKey(root,question);
					//create save
					String node = "";
					Node left = update.getLeft();
					Node right = update.getRight();
					node = node.concat(data[0] + ",");
					node = node.concat(update.getSave()+",");
					if(isleft){
						node = node.concat(addPointerPadding(Integer.toString(numOfQuestions-1))+",");
						node = node.concat(data[3]+ '\n');
					}
					else if(!isleft){
						node = node.concat(data[2]+",");
						node = node.concat(addPointerPadding(Integer.toString(numOfQuestions-1))+ '\n');
					}
					//need to add padding to pointer
					//an addIdPadding that adds up to a size of 4
					raf.write(node.getBytes());
				}
				input = raf.readLine();
			}
			String node = "";
			long x = raf.getFilePointer();
			Node left = newCeleb.getLeft();
			Node right =newCeleb.getRight();
			node = node.concat(addIdPadding(Integer.toString(numOfQuestions-1)) + ",");
			node = node.concat(newCeleb.getSave()+",");
			node = node.concat(left.getSave()+",");
			node = node.concat(right.getSave()+ "\n");
			raf.write(node.getBytes());
		} 
		catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			raf.close();
			long x = saveFile.length();
			System.out.println("Number of Question: "+ numOfQuestions + ", Current Size of File: "+x);
		}
	}
	private String addPointerPadding(String question)
	{
		final int maxSize=100;
		StringBuilder sb = new StringBuilder();
		sb.append(question);
		while(sb.length()<maxSize)
		{
			sb.append('~');
		}
		return sb.toString();
	}
	
	private String addIdPadding(String question)
	{
		final int maxSize=4;
		StringBuilder sb = new StringBuilder();
		sb.append(question);
		while(sb.length()<maxSize)
		{
			sb.append('~');
		}
		return sb.toString();
	}
}