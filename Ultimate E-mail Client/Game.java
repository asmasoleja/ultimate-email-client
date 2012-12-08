public class Game
{

// ----------------------------------------------------------------------
// Part a: the score message
  //The score message
  private String scoreMessage;
  
  //Get the score message
  public String getScoreMessage()
  {
    return this.scoreMessage;
  } // getScoreMessage

  //Set the score message
  public void setScoreMessage(String message)
  {
    this.scoreMessage = message;
  } // setScoreMessage

  // Get the author of the program
  public String getAuthor()
  {
    return "Adam Higginson";
  } // getAuthor


// ----------------------------------------------------------------------
// Part b: constructor and grid accessors
  // The grid size
  private final int gridSize;
  // The grid itself, constructed from an array of cells
  private final Cell grid [][];
  
  //Constructor
  public Game(int requiredGridSize)
  {
    this.gridSize = requiredGridSize;
    this.grid = new Cell [this.gridSize][this.gridSize];
    
    //Draws the grid  
    for (int column = 0; column < this.gridSize; column++)
      for (int row = 0; row < this.gridSize; row++)
	     this.grid [column][row] = new Cell();
  } // Game

  // Get size of grid
  public int getGridSize()
  {
    return this.gridSize;
  } // getGridSize

  // Get a particular cell
  public Cell getGridCell(int x, int y)
  {
    return this.grid [x][y];
  } // getGridCell


// ----------------------------------------------------------------------
// Part c: initial game state

// Part c-1: setInitialGameState method
  //Holds the score value
  private int score;
  //Initialises a game, 
  public void setInitialGameState(int requiredTailX, int requiredTailY,
                                  int requiredLength, int requiredDirection)
  {
    //Clear all cells
    for (int column = 0; column < this.gridSize; column++)
      for (int row = 0; row < this.gridSize; row++)
	     this.grid [column][row].setClear(); 
  
  //Score is initially 0
  this.score = 0;
  //Number of trees is initially 0
  this.noOfTrees = 0;
  //Place the food
  placeFood();
  //Place trees if enabled
  if (isTreesEnabled)
    placeTree();  
  //Place the snake
  placeSnake(requiredTailX, requiredTailY,
             requiredLength, requiredDirection);
	     
  //set the food direction in terms of X and Y
  this.foodDirectionX=1;
  this.foodDirectionY=0; 
	   
	
  } // setInitialGameState


// ----------------------------------------------------------------------
// Part c-2 place food
  //X Value of food
  private int foodX;
  //Y Value of food
  private int foodY;
  //Places the food
  private void placeFood()
  {
    do
    {
      //Get a random value for food
      this.foodX = (int) (Math.random() * this.gridSize);
      this.foodY = (int) (Math.random() * this.gridSize);
    } //do
      while (grid [foodX][foodY].getType() != Cell.CLEAR);
      //Place the food while not clear
      this.grid [foodX][foodY].setFood();
	
  } //placeFood

// ----------------------------------------------------------------------
// Part c-3: place snake

  private int snakeDirection, tailX, tailY, headX, headY, snakeLength;

  
  private void placeSnake(int requiredTailX, int requiredTailY,
                         int requiredLength, int requiredDirection)
  {
    this.tailX = requiredTailX;
    this.tailY = requiredTailY;
    this.snakeLength = requiredLength;
    this.snakeDirection = requiredDirection;
    
    //Construct a tail at the given X and Y
    grid [this.tailX][this.tailY].setSnakeTail(
	                          Direction.opposite(this.snakeDirection), 
	                          this.snakeDirection);
    
    // from the tail, work out the snake's x and y direction
    // if snakeDirection = North, xDelta=0, yDelta=-1
    // if snakeDirection = East, xDelta=1, yDelta=0 
    // if snakeDirection = South, xDelta=0, yDelta=1     
    // if snakeDirection = West, xDelta=-1, yDelta=0
    
    int bodyX = this.tailX + Direction.xDelta(this.snakeDirection);
    int bodyY = this.tailY + Direction.yDelta(this.snakeDirection);
    
    //now add the snakebody cells, minus the head and tail, so 
    //loop snakelength - 2      
    for (int i = 0; i < this.snakeLength - 2; i++)
    {
    	//Display the snakebody
	grid [bodyX][bodyY].setSnakeBody(
	                              Direction.opposite(this.snakeDirection),
	                              this.snakeDirection);
	
	//Calculate next cell position for the body
	bodyX = bodyX + Direction.xDelta(this.snakeDirection);
        bodyY = bodyY + Direction.yDelta(this.snakeDirection);
    
     } //for    
     //set the position of the head 
     this.headX = bodyX;
     this.headY = bodyY;
     
     //now display the head
     grid [this.headX][this.headY].setSnakeHead(
	                           Direction.opposite(this.snakeDirection),
	                           this.snakeDirection);
    
  }  //placeSnake

// ----------------------------------------------------------------------
// Part d: set snake direction


  public void setSnakeDirection(int requiredDirection)
      
  {
    //ensure that the requiredDirection is not in the direction of snakeIn
    if (grid[this.headX][this.headY].getSnakeInDirection() == requiredDirection)
    {
    	setScoreMessage("Error: Snake Cannot Face Backwards");
    	return;
    }
    else
    {    
         //now display the head in the required direction in/out
         //back/front respectively
         
         grid[this.headX][this.headY].setSnakeOutDirection(requiredDirection);
	      this.snakeDirection = requiredDirection;                        
       }
  } // setSnakeDirection


// ----------------------------------------------------------------------
// Part e: snake movement

// Part e-1: move method
  
//variables to hold the current snake direction
  int xDirection, yDirection;  

  public void move(int moveValue)
  {
    //Check if snake head is not bloody
    if (!grid [this.headX][this.headY].isSnakeBloody())
    {
      //Compute new position of head]
      int newHeadX = this.headX + Direction.xDelta(this.snakeDirection);
      int newHeadY = this.headY + Direction.yDelta(this.snakeDirection);
      
      //it is ok to move = check for crash at new position and deal with it
     if (!isCrash(newHeadX, newHeadY))
     {

       //remember whether there is food at the new head position
       boolean isFood = this.grid [newHeadX][newHeadY].getType() == Cell.FOOD;
       //move head to new position
       moveHead(newHeadX, newHeadY);
      
        //Proposed Head position is ok, so set Head coordinates to new position
        this.headX = newHeadX;
        this.headY = newHeadY;
  
  
       
        //if new cell type for head is food
        if (isFood)
        {
	 //eat food
	 eatFood(moveValue);
        }
        else
        {
   	 //move the tail
	 //Compute new position of tail
         this.xDirection = Direction.xDelta(
	     grid[this.tailX][this.tailY].getSnakeOutDirection());
         this.yDirection = Direction.yDelta(
	     grid[this.tailX][this.tailY].getSnakeOutDirection());
            
         int newTailX = this.tailX + this.xDirection;
         int newTailY = this.tailY + this.yDirection;

         //if gutter Trails are enabled via 'g' toggle, display trails
         // otherwise clear the current tail cell
     
         if (this.isGutterEnabled)
           showTrails();
         else
           grid[this.tailX][this.tailY].setClear();

         moveTail(newTailX, newTailY);
       
         //Update Tail position
         this.tailX = newTailX;
         this.tailY = newTailY;
      
         //If food movement enabled via 'm' toggle, enable food movement
         if (this.isFoodMoveEnabled)
           foodMove();
      
       } // end if
     } //end if
   } //end if      
  } // move


// ----------------------------------------------------------------------
// Par`	t e-2: move the snake head

  public void moveHead(int requiredHeadX, int requiredHeadY)
  {
    //Make the existing head a body
    grid [this.headX][this.headY].setSnakeBody();
      
    //Get head out direction
    int headOut = grid [this.headX][this.headY].getSnakeOutDirection();
    //display the head in the required direction in the new position
      grid[requiredHeadX][requiredHeadY].setSnakeHead(
	                              Direction.opposite(headOut),
	                              headOut); 
  } //moveHead

// ----------------------------------------------------------------------
// Part e-3: move the snake tail

  private void moveTail(int requiredTailX, int requiredTailY)
  {
     
     //Construct a tail at the given X and Y
     grid [requiredTailX][requiredTailY].setSnakeTail();
     
  } //moveTail

// ----------------------------------------------------------------------
// Part e-4: check for and deal with crashes
  
  private boolean isCrash(int newHeadX, int newHeadY)
  {

    //check that new coordinates of snake head is within the grid
    if ((newHeadX >= (gridSize)) || (newHeadY >= (gridSize)) || (newHeadX < 0)
	|| (newHeadY < 0))

    {
      //Check for countdown	
      if (isSnakeDead())
      {
        setScoreMessage("Snake Has Crashed");
       //set the head to red
       
        grid[this.headX][this.headY].setSnakeBloody(true); 
       }
       return true;
     }
      //check if it's crashed into itself
      
     else if (grid[newHeadX][newHeadY].isSnakeType())
     { 
       //Check for countdown	
    	 
       if (isSnakeDead())
       {
       setScoreMessage("The Snake Cannot Eat Itself");
       grid[this.headX][this.headY].setSnakeBloody(true);	
       grid[newHeadX][newHeadY].setSnakeBloody(true);
       }
       return true;
     } //if
     //Check if it's not crashed into a tree
     else if (grid[newHeadX][newHeadY].getType() == Cell.TREE)
     { 
       //Check for countdown	
    	 
       if (isSnakeDead())
       {
       setScoreMessage("You cannot crash into a tree");  
       grid[this.headX][this.headY].setSnakeBloody(true);
       }
       return true;
     } //if
            
     else return false;
  } //checkCrash


// ----------------------------------------------------------------------
// Part e-5: eat the food


  private void eatFood(int moveValue)
  {     
  	  	 
  	 //Fix the score. If trees are enabled, a different formula is used
  	 if (isTreesEnabled)
  	   {
  	   	//Increase noOfTrees

  	   	placeTree();
  	   	
  	      //Calculate raw score
  	       int rawScore =
  	               this.score + (moveValue * ((this.snakeLength /
  	                           (this.gridSize * this.gridSize
  	                           / 36 + 1)) + 1));
  	      this.score = rawScore * this.noOfTrees;                     
  	      setScoreMessage("Raw score: " + rawScore + " No. of trees: "
  	                     + noOfTrees + " Actual score increase: "
  	                     + (this.score - rawScore));
  	  }
  	 else
  	   this.score = this.score + (moveValue * ((this.snakeLength /
  	                           (this.gridSize * this.gridSize
  	                           / 36 + 1)) + 1));
  	 
  	                           
  	 //Place some more food
  	 placeFood();
  } //eatFood
  
  public int getScore()
  {
  	
    return this.score;
  } // getScore


// ----------------------------------------------------------------------
// Part f: cheat


  public void cheat()
  {
  	  //Half the score
  	  this.score = this.score / 2;
  	  
  	  //Send a message
  	  setScoreMessage("You have cheated, your score has been halved!");
  	  
  	  //All cells made not bloody
  	  for (int column = 0; column < this.gridSize; column++)
            for (int row = 0; row < this.gridSize; row++)
  	     this.grid [column][row].setSnakeBloody(false);
  	     
  	  //reset countdown
  	  resetCountdown();
  } // cheat


// ----------------------------------------------------------------------
// Part g: trees
  //Number of trees currently
  private int noOfTrees;
  //Are trees enabled?
  private boolean isTreesEnabled = false;
  
  public void toggleTrees()
  {
  	  //toggle the trees
  	  this.isTreesEnabled = !this.isTreesEnabled;
  	  
  	  if (isTreesEnabled)
  	  {
  	  	//Places a tree
  	  	placeTree();
  	  } //if
  	  else
  	  {
  	//Remove trees
       for (int column = 0; column < this.gridSize; column++)
        for (int row = 0; row < this.gridSize; row++)
          {
          if (this.grid [column][row].getType() == Cell.TREE)
  	       this.grid [column][row].setClear();
  	  } //for
  	  } //else

  } // toggleTrees
  
  private void placeTree()
  {
      int treeX, treeY;
      //Place a certain number of trees, depending on food collected
      do
      {
      //Get a random value for tree
      treeX = (int) (Math.random() * this.gridSize);
      treeY = (int) (Math.random() * this.gridSize);
    } //do
      while (grid [treeX][treeY].getType() != Cell.CLEAR);
      //Place the tree while not clear
      this.grid [treeX][treeY].setTree();
      this.noOfTrees++;
  } //placeTree


// ----------------------------------------------------------------------
// Part h: crash countdown
  //No of moves before crash is counted, 5 is recommended
  private final int noOfMovesCrash = 5;
  //The current countdown value
  private int currentCountdown;
  
  private void resetCountdown()
  {
  	 //Reset countdown to countdown start
  	 this.currentCountdown = this.noOfMovesCrash;
  } //resetCountdown
  
  private boolean isSnakeDead()
  {
  	  //Reduces countdown by 1
  	  this.currentCountdown--;
  	  
  	  //If after decrement it is greater than 0, indicate how
  	  //many moves until death.
  	  if (this.currentCountdown > 0)
  	  {
  	  	  setScoreMessage("You are lucky! You have " 
		                 + this.currentCountdown
  	  	                 + " moves left");
  	  	  return false;
  	 } //if
  	 //Otherwise reset countdown start
  	 else
  	 {
  	 	resetCountdown();
  	 	return true;
  	 } //else
  } //isSnakeDead

// ----------------------------------------------------------------------
// Part i: optional extras

  //---Used to toggle Gutter Trails---
  private boolean isGutterEnabled = false;
  private int gutterVisibility = 50;
  
  //---Used to toggle food movement---
  private boolean isFoodMoveEnabled = false;

  public String optionalExtras()
  {
    return " g: Gutter Trails\nb: Burn Trees\nm: Food Movement ";
  } // optionalExtras


  public void optionalExtraInterface(char c)
  {
  	 if (c == 'g')
           //toggle gutter trails enabled  	 
  	   isGutterEnabled = !isGutterEnabled;
	 else if (c == 'b')
	   //burn tree
	   burnTree();
	 else if (c =='m')
	   {
	   //toggle food movement enabled
	   isFoodMoveEnabled = !isFoodMoveEnabled;
	   //set the food direction in terms of X and Y
	   this.foodDirectionX=1; 
	   this.foodDirectionY=0; 
	   }//else if
	 else setScoreMessage("Key " + new Character(c).toString()
                             + " is unrecognised (try h)");
	   
  	 
    //if (c > ' ' && c <= '~')
      //setScoreMessage("Key " + new Character(c).toString()
        //              + " is unrecognised (try h)");
  } // optionalExtraInterface
  

  private void showTrails()
  {
   	 //Scan all cells and decrease visibility if trails
   for (int column = 0; column < this.gridSize; column++)
    for (int row = 0; row < this.gridSize; row++)
    {
      if (this.grid [column][row].getType() == Cell.OTHER)
       if (this.grid [column][row].getOtherLevel() == 0)
         this.grid [column][row].setClear();
        else
  	  this.grid [column][row].setOther(
	    this.grid [column][row].getOtherLevel()-1);
     } //for
     //create the new trail
     grid [this.tailX][this.tailY].setOther(this.gutterVisibility);
  
  } //isGutterEnabled
  
  private void burnTree()
  {
    //Compute position in front of head
    
  //  try
  //  {
      int treeHeadX = this.headX + Direction.xDelta(this.snakeDirection);
      int treeHeadY = this.headY + Direction.yDelta(this.snakeDirection);
      
      //determine if there is a tree at the next position
      boolean isTree = this.grid [treeHeadX][treeHeadY].getType() == Cell.TREE;
      
      //Clear cell if there is a tree and decrement total no of trees
      if (isTree)
      {
        this.grid [treeHeadX][treeHeadY].setClear();
        this.noOfTrees--;
      } //if
      
   // } //try
    
   // catch (ArrayIndexOutOfBoundsException arrayOutOfBounds)
   // {
     //Check to see if array is out of bounds
  //  } //catch
  } //burnTree
  


  //The initial direction of the food
  private int foodDirectionX=1, foodDirectionY=0; 
  
  //Allows food to move
  private void foodMove()
  {
    int currentFoodX, currentFoodY;
    boolean isValidCell = false;
    
        
    currentFoodX = this.foodX;
    currentFoodY = this.foodY;
        
    
    do
    { 
      currentFoodX += this.foodDirectionX;
      currentFoodY += this.foodDirectionY;
      
      //calculate the geometric distance between the new position and head
      double distanceToHead = Math.sqrt(Math.pow((this.headX - this.foodX), 2)
                                   + Math.pow((this.headY - this.foodY), 2));
      double distanceToNewPos = Math.sqrt(Math.pow((this.headX - currentFoodX)
	                        , 2)
                                 + Math.pow((this.headY - currentFoodY), 2));
    

      //check that new coordinates of food is within the grid
      //is clear (or contains trails) and is further from the head
      if (((currentFoodX < (gridSize)) && (currentFoodY < (gridSize)) 
	&& (currentFoodX >= 0)
	&& (currentFoodY >= 0))
	&& ((this.grid [currentFoodX][currentFoodY].getType() == Cell.CLEAR) 
	|| (this.grid [currentFoodX][currentFoodY].getType() == Cell.OTHER))
	&& distanceToNewPos >= distanceToHead)
      {
      
        //if ok, then position found
	
	//Clear the current food cell
	this.grid [this.foodX][this.foodY].setClear();
	this.foodX = currentFoodX;
	this.foodY = currentFoodY;
	
		
	//Add food to the new position
	this.grid [this.foodX][this.foodY].setFood();
	isValidCell = true;
       }//if
       else 
      {
           //undo previous calculation
	   currentFoodX = this.foodX;
           currentFoodY = this.foodY;
           //Change direction
	   if (this.foodDirectionX==1)
	   {
	    this.foodDirectionX=-1;
	    this.foodDirectionY=0;
	   }
	   else if (this.foodDirectionX==-1)
	   {
	     this.foodDirectionX=0;
	     this.foodDirectionY=1;
	   } 
	   else if (this.foodDirectionY==1)
	   {
              this.foodDirectionX=0;
	      this.foodDirectionY=-1;
	   }
	   else if (this.foodDirectionY==-1)
	   {
              this.foodDirectionX=1;
	      this.foodDirectionY=0;
	      isValidCell = true;
	   }
	   //There are no valid directions
	   else isValidCell = true;	   
	   
      }//else			   
     }//do
     while (!isValidCell);
  }//moveFood()
  

} // class Game
