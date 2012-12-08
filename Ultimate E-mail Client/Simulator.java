import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * Used to simulate processes being executed on Servers
 * @author Adam
 *
 */
public class Simulator 
{
	//The max value the time can reach
	private static final int MAX_TIME_UNITS = 1000000;
	
	//The current time value
	private int currentTime;
	//The requests made
	private int requests;
	//The request rejected
	private int rejectedRequests;

	
	//The execution time
	private int executionTime;
	//The arrival rate
	private int arrivalRate;
	//The number of servers
	private int noOfServers;
	//All the servers in the system
	private Server[] servers;
	//The queue size
	private int queueSize;
	//The queue
	private Queue<Request> queue;
	
	//The number of requests needed
	private int noOfRequests;
	
	//The times of the request
	private ArrayList<Integer> requestTimesList;
		
	public Simulator(int executionTime, int arrivalRate, int noOfServers, int queueSize) 
	{
		this.executionTime = executionTime;
		this.arrivalRate = arrivalRate;
		this.noOfServers = noOfServers;
		this.queueSize = queueSize;
		
		this.servers = new Server[this.noOfServers];
		for (int i = 0; i < servers.length; i++)
			this.servers[i] = new Server(this.executionTime);
		
		this.queue = new LinkedList<Request>();
		
		this.noOfRequests = MAX_TIME_UNITS / this.arrivalRate;
		
		int timeBetweenRequest = MAX_TIME_UNITS / this.noOfRequests;
		
		
		this.requestTimesList = new ArrayList<Integer>(this.noOfRequests + 1);
		
		//Create a new random number generator and seed it with the current time.
		Random generator = new Random();
		
		//System.out.println("NoOfRequests: " + noOfRequests + " Time between requests: " + timeBetweenRequest);
		//Generate the request times
		int lastValue = 0;
		int randomValue;
		if (timeBetweenRequest % 2 == 0)
			randomValue = timeBetweenRequest * 2;
		else
			randomValue = (timeBetweenRequest * 2) - 1;
		
		System.out.println("Random value: " + randomValue);
		
		int nextRandom = -1;
		lastValue = 0;
		int listCount = 0;
		while (nextRandom <= MAX_TIME_UNITS)
		{
			nextRandom = 1 + generator.nextInt(randomValue) + lastValue;
			this.requestTimesList.add(nextRandom);
			lastValue = this.requestTimesList.get(listCount);
			listCount++;
		}
		
		
		/*float sum = requestTimes[0];
		int max = -1;
		int min = 32768;
		for (int i = 1; i < requestTimes.length; i++)
		{
			sum += (requestTimes[i] - requestTimes[i - 1]);
			int difference = requestTimes[i] - requestTimes[i - 1];
			if ((requestTimes[i] - requestTimes[i - 1]) > max)
				max = requestTimes[i] - requestTimes[i - 1];
			if (difference < min)
				min = difference;
			try{
				log.write(Integer.toString(requestTimes[i - 1]));
				log.newLine();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}*/
		
		
		//System.out.println("Max : " + max + " min: " + min);
		//System.out.println("Average: " + (sum / (float)(requestTimes.length - 1)));
	}
	
	/**
	 * Runs the simulation until complete
	 */
	public void runSimulation()
	{
		//We need to loop while we're not at the maximum value we can reach
		int count = 0;
		//System.out.println("RequestTimes: " + requestTimes[0]);
		//System.out.println("request count: " + requestTimes.length);
		//System.out.println("Last: " + requestTimes[requestTimes.length - 1]);
		
		long averageQueueSize = 0;
		float averageResponseTime = 0;
		int requestsDone = 0;
		long startTime = System.currentTimeMillis();
		System.out.println("------------BEGINNING SIMULATION @---------------\n");
		while (this.currentTime <= MAX_TIME_UNITS)
		{
			
			//Set queue size
			averageQueueSize += this.queue.size();
		
			//System.out.println(count);
			if ((count < this.requestTimesList.size()) && (this.currentTime >= this.requestTimesList.get(count)))
			{
				//Increment requests made
				this.requests++;
				count++;
				//Send a request, only accept if space in queue
				if (queue.size() < this.queueSize)
				{
					Request request = new Request(this.currentTime);
					this.queue.add(request);
				}
				else
				{
					this.rejectedRequests++;
				}
			}
			
			//For each server, execute (increment time).
			for (Server server : servers)
				server.execute(this.currentTime);
				
			//Try to assign requests to servers
			for (Server server : servers)
			{
				//If we can add a request to a server, remove it from
				//the queue
				if (server.isIdle() && !this.queue.isEmpty())
				{
					Request lastRequest = server.getLastRequest();
					if (lastRequest != null)
					{
						averageResponseTime += lastRequest.getRequestTimeFinish() - lastRequest.getRequestTimeStart();
						requestsDone++;
					}
					Request currentReq = this.queue.remove();
					server.addRequest(this.currentTime, currentReq);
				}
			}
			
			//Finally, increment the time
			this.currentTime++;
		}

		long endTime = System.currentTimeMillis();
		
		long duration = endTime - startTime;
		float inSeconds = (float)duration / 1000.0f;
		//duration /= 1000;
		System.out.println("\n--------------SIMULATION FINISHED--------------\n");
		System.out.println("Stats:");
		System.out.println("Took: " + inSeconds + " seconds");
		System.out.println("Requests made: " + this.requests);
		System.out.println("Requests rejected: " + this.rejectedRequests);
		System.out.println("As a percentage: " + (100 * ((float)this.rejectedRequests / (float)this.requests)));
		System.out.println("Average queue size: " + ((double)averageQueueSize / (double)MAX_TIME_UNITS));
		System.out.println("Average response time: " + (averageResponseTime / (float)requestsDone));
		System.out.println();
		System.out.println("Requests completed at end of simulation: " + requestsDone);
		//Work out how many requests still to complete
		int requestsStillWorking = 0;
		for (Server server : servers)
		{
			if (!server.isIdle())
				requestsStillWorking++;
		}
		System.out.println("Requests running at end of simulation: " + requestsStillWorking);
		System.out.println("Queue length at end of simulation: " + this.queue.size());
		
	}
	
	
	

}
