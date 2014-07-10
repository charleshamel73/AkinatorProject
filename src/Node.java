import java.io.Serializable;
import java.net.SocketAddress;

public class Node implements Serializable
{
	private int id;
	private Node parent;
	private Node left;
	private Node right;
	private String question;
	private SocketAddress user;
	private String padding;
	
	public Node(int id,String question,SocketAddress origin)
	{
		parent=null;
		left=null;
		right=null;
		this.question=question;
		user= origin; 
		padding=addPadding();
		this.id = id;
	}
	
	private String addPadding()
	{
		final int maxSize=100;
		int spaces=maxSize-question.length();
		StringBuilder sb = new StringBuilder();
		while(sb.length()<spaces)
		{
			sb.append('~');
		}
		return sb.toString();
	}
	
	public int getID(){
		return id;
	}
	
	
	public String getSave(){
		return question + padding;
	}
	
	public boolean isRoot()
	{
		if(parent==null)
		{
			return true;
		}
		return false;
	}
	
	public SocketAddress getUser()
	{
		return user;
	}
	
	public Node getLeft()
	{
		return left;
	}
	
	public Node getRight()
	{
		return right;
	}
	
	public Node getParent()
	{
		return parent;
	} 
	
	public String getString()
	{
		return question;
	}
	
	public void setParent(Node parent)
	{
		this.parent=parent;
	}
	
	public void setLeft(Node left)
	{
		left.parent=this;
		this.left=left;
	}
	
	public void setRight(Node right)
	{
		right.parent=this;
		this.right=right;
	}
	
	public boolean isLeaf()
	{
		if(left==null && right==null)
		{
			return true;
		}
		return false;
	}
	
}
