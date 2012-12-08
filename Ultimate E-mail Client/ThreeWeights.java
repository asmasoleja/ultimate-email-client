public class ThreeWeights
{
  public static void main(String[] args)
  {
    // Declare the variables from the command line
    int weight1 = Integer.parseInt(args[0]);
    int weight2 = Integer.parseInt(args[1]);
    int weight3 = Integer.parseInt(args[2]);
    
    //Calculate all possible values the weight can be
    //This is given by R - L, where R is the right weight, and L is the left
    
    System.out.println(0 - (weight1 + weight2 + weight3));
    System.out.println(0 - (weight1 + weight2));
    System.out.println(weight3 - (weight1 + weight2));
    System.out.println(0 - (weight1 + weight3));
    System.out.println(0 - weight1);
    System.out.println(weight3 - weight1);
    System.out.println(weight2 - (weight1 + weight3));
    System.out.println(weight2 - weight1);
    System.out.println((weight2 + weight3) - weight1);
    System.out.println(0 - (weight2 + weight3));
    System.out.println(0 - weight2);
    System.out.println(weight3 - weight2);
    System.out.println(0 - weight3);
    System.out.println(0);
    System.out.println(weight3);
    System.out.println(weight2 - weight3);
    System.out.println(weight2);
    System.out.println(weight2 + weight3);
    System.out.println(weight1 - (weight2 + weight3));
    System.out.println(weight1 - weight2);
    System.out.println((weight1 + weight3) - weight2);
    System.out.println(weight1 - weight3);
    System.out.println(weight1);
    System.out.println(weight1 + weight3);
    System.out.println((weight1 + weight2) - weight3);
    System.out.println(weight1 + weight2);
    System.out.println(weight1 + weight2 + weight3);
  }
}
