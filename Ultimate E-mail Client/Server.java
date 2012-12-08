/**
 * Represents a server in this project
 * @author Adam
 *
 */
public class Server 
{
	//The time taken to execute something
	private int executionTime;
	//The current Request being run on this server
	private Request currentRequest;

	//The time the next request finishes
	private int nextRequestFinish;
	
	//The last request, used to get info about it
	private Request lastRequest;
	
	public Server(int executionTime)
	{
		this.executionTime = executionTime;
		this.currentRequest = null;
		this.nextRequestFinish = 0;
	}
	
	/**
	 * Attempts to add a request to the server.
	 * @param request The request to add
	 * @return true if server can compute the request, false otherwise
	 */
	public void addRequest(int time, Request request)
	{
		this.currentRequest = request;
		this.nextRequestFinish = time + executionTime;
	}
	
	/**
	 * Mimics doing some work on a request, just increments
	 * the current time
	 */
	public void execute(int time)
	{
		if ((this.currentRequest != null) && (time >= this.nextRequestFinish))
		{
			this.lastRequest = currentRequest;
			this.currentRequest.setRequestTimeFinish(time);
			this.currentRequest = null;
		}
		
		/*this.currentTime++;
		
		if (this.currentRequest == null)
			this.executionTime++;
		
		if (this.currentTime >= this.executionTime)
		{
			this.currentRequest.setRequestTimeFinish(this.currentTime);
			this.executionTime += this.currentTime;
			this.currentRequest = null;
		}*/
	}
	
	public Request getCurrentRequest()
	{
		return this.currentRequest;
	}
	
	public Request getLastRequest()
	{
		return this.lastRequest;
	}
	
	/**
	 * Shows whether this Server is idle
	 * @return true for saying the server is idle, false otherwise.
	 */
	public boolean isIdle()
	{
		return this.currentRequest == null;
	}
}
