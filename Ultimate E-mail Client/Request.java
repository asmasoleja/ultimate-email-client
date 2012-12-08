/**
 * Represents a request in the system
 * @author Adam
 *
 */
public class Request 
{
	//The time the request was made
	private int requestTimeStart;
	//The time this request was finished
	private int requestTimeFinish;
	
	public Request(int requestStart)
	{
		this.requestTimeStart = requestStart;
		this.requestTimeFinish = -1;
	}

	public int getRequestTimeStart() {
		return requestTimeStart;
	}

	public int getRequestTimeFinish() {
		return requestTimeFinish;
	}
	
	public void setRequestTimeFinish(int requestTimeFinish) {
		this.requestTimeFinish = requestTimeFinish;
	}
	

}
