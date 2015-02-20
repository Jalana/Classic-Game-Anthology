package tru.kyle.mylists;

public class MyQueue<T>
{
	private MyLinkedList<T> list;

	public MyQueue()
	{
		list = new MyLinkedList<T>();
	}

	public void enqueue(T item)
	{
		list.addToEnd(item);
	}

	public T dequeue()
	{
		return list.removeAtFront();
	}

	public T peek()
	{
		return list.getAtFront();
	}

	public int positionOf(T item)
	{
		return list.positionOf(item);
	}

	public T getAtIndex(int index)
	{
		return list.getAtIndex(index);
	}

	public void remove(T item)
	{
		list.remove(item);
	}

	public boolean isEmpty()
	{
		return list.isEmpty();
	}

	public int size()
	{
		return list.size();
	}

	public void clear()
	{
		list.clear();
	}
}