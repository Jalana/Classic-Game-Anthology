package tru.kyle.mylists;

public class MyStack<T>
{
	private MyLinkedList<T> list;

	public MyStack()
	{
		list = new MyLinkedList<T>();
	}

	public void push(T item)
	{
		list.addToEnd(item);
	}

	public T pop()
	{
		return list.removeAtEnd();
	}

	public T peek()
	{
		return list.getAtEnd();
	}

	public boolean isEmpty()
	{
		return list.isEmpty();
	}

	public int size()
	{
		return list.size();
	}
}