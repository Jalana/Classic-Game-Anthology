package tru.kyle.mylists;

public class MyLinkedList<T>
{
	private MyNode<T> head;
	private MyNode<T> tail;
	private int count;

	public MyLinkedList()
	{
		head = null;
		tail = null;
		count = 0;
	}

	public T getAtFront()
	{
		if (this.isEmpty() == true)
			return null;
		
		return head.getData();
	}

	public T getAtEnd()
	{
		if (this.isEmpty() == true)
			return null;
		
		return tail.getData();
	}

	public T getAtIndex(int index)
	{
		if (index <= 0)
			return this.getAtFront();
		else if (index >= count - 1)
			return this.getAtEnd();
		else if (this.isEmpty() == false)
		{
			MyNode<T> current = head;
			for (int tracker = 0; tracker < index; tracker++)
				current = current.getNext();
			if (current != null)
				return current.getData();
		}
		return null;
	}

	public void addToFront(T item)
	{
		if (this.isEmpty() == true)
		{
			head = new MyNode<T>(item, null, null);
			count++;
			tail = head;
		}
		else if (count == 1)
		{
			head = new MyNode<T>(item, null, tail);
			tail.setLast(head);
			count++;
		}
		else
		{
			MyNode<T> second = new MyNode<T>(item, null, head);
			head.setLast(second);
			head = second;
			count++;
		}
	}

	public void addToEnd(T item)
	{
		if (this.isEmpty() == true || head == null)
		{
			this.addToFront(item);
		}
		else if (count == 1)
		{
			tail = new MyNode<T>(item, head, null);
			head.setNext(tail);
			count++;
		}
		else
		{
			MyNode<T> second = new MyNode<T>(item, tail, null);
			tail.setNext(second);
			tail = second;
			count++;
		}
	}

	public T removeAtFront()
	{
		if (this.isEmpty() == true)
			return null;
		else
		{
			T result = head.getData();
			head = head.getNext();
			count--;
			if (this.isEmpty() == false)
				head.setLast(null);
			return result;
		}
	}

	private void removeNode(MyNode<T> node)
	{
		if (node == head)
			this.removeAtFront();
		else if (node == tail)
			this.removeAtEnd();
		else
		{
			MyNode<T> previous = node.getLast();
			MyNode<T> next = node.getNext();
			next.setLast(previous);
			previous.setNext(next);
			count--;
		}
	}

	public void remove(T item)
	{
		if (this.isEmpty() == false)
		{
			MyNode<T> current = head;
			while (current != null && current.getData() != item)
				current = current.getNext();

			if (current != null)
				this.removeNode(current);
		}
	}

	public void removeAll(T item)
	{
		MyNode<T> current = head;
		while (current != null)
		{
			if (current.getData() == item)
				this.removeNode(current);
			current = current.getNext();
		}
	}

	public T removeAtEnd()
	{
		if (this.isEmpty() == true)
			return null;
		else
		{
			T result = tail.getData();
			tail = tail.getLast();
			count--;
			if (this.isEmpty() == false)
				tail.setNext(null);
			return result;
		}
	}

	public int positionOf(T item)
	{
		int index = 0;
		if (this.isEmpty() == false)
		{
			MyNode<T> current = head;
			for (index = 0; current != null && current.getData() != item; index++)
				current = current.getNext();

			if (current != null)
				return index;
		}
		return -1;
	}

	public int size()
	{
		return count;
	}

	public boolean isEmpty()
	{
		if (count == 0)
			return true;
		else
			return false;
	}

	public void clear()
	{
		head = null;
		tail = null;
		count = 0;
	}
}