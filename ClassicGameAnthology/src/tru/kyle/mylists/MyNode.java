
package tru.kyle.mylists;
public class MyNode<T>
{
	private T data;
	private MyNode<T> last;
	private MyNode<T> next;

	public MyNode(T newData, MyNode<T> pre, MyNode<T> nex)
	{
		data = newData;
		last = pre;
		next = nex;
	}

	public T getData()
	{
		return data;
	}

	public void setData(T newData)
	{
		data = newData;
	}

	public MyNode<T> getNext()
	{
		return next;
	}

	public void setNext(MyNode<T> newNext)
	{
		next = newNext;
	}

	public MyNode<T> getLast()
	{
		return last;
	}

	public void setLast(MyNode<T> newLast)
	{
		last = newLast;
	}
}