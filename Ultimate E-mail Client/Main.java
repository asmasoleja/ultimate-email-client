/**
 * Runs the main simulation
 * @author Adam
 *
 */
public class Main 
{
	public static void main(String[] args)
	{
		if (args.length != 4)
		{
			System.err.println("Error: Need 4 arguments: queue_size arrival_rate number_of_servers execution_time");
			System.exit(1);
		}
		int queueSize = Integer.parseInt(args[0]);
		int arrivalRate = Integer.parseInt(args[1]);
		int noServers = Integer.parseInt(args[2]);
		int executionTime = Integer.parseInt(args[3]);
		
		Simulator sim = new Simulator(executionTime, arrivalRate, noServers, queueSize);
		sim.runSimulation();
	}
}
