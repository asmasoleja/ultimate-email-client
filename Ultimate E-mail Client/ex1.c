////////////////////////////////////////////////////////////////
// School of Computer Science
// The University of Manchester
//
// This code is licensed under the terms of the Creative Commons 
// Attribution 2.0 Generic (CC BY 3.0) License.
//
// Skeleton code for COMP37111 coursework, 2012-13
//
// Authors: Arturs Bekasovs and Toby Howard
//
/////////////////////////////////////////////////////////////////

#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include <math.h>

#ifdef MACOSX
  #include <GLUT/glut.h>
#else
  #include <GL/glut.h>
#endif

#define DEG_TO_RAD 0.017453293

#define GRAVITY_EARTH 9.81f;
#define GRAVITY_HEAVY 40.81f;

// Display list for coordinate axis 
GLuint axisList;

int AXIS_SIZE= 200;
int axisEnabled= 1;




float gravity = GRAVITY_HEAVY;

/* A particle struct, representing a single particle
 * in space. Multiple particles are released by a single
 * emitter */
typedef struct Particle
{
  GLfloat x, y, z; //Position in world space
  GLfloat r, g, b; //Colour in r/g/b
  GLfloat angle; //Angle of release
  GLfloat yRot; //Rotation around y axis
  GLfloat initVelX, initVelY; //Initial velocities
  int isAlive; //Whether the particle should be drawn
} Particle;

typedef struct Emitter
{
  GLfloat x, y, z; //The pos of the emitter
  GLfloat rI, gI, bI; //r/g/b initial colour of particle
  GLfloat rF, gF, bF; // r/g/b final colour of particle
  GLfloat lifeTime; //life time of a particle in seconds
  GLfloat initVel; //The initial velocity
  int noParticles; //The no of particles released by emitter

  Particle* particles; //Particle array held by emitter
} Emitter;

Emitter* myEmitter = NULL;

float currentTime;
float lastTime;
float elapsedTime;
float totalTime;



void initClock()
{
  currentTime = glutGet(GLUT_ELAPSED_TIME);
  lastTime = currentTime;
  elapsedTime = 0.0f;
  totalTime = 0.0f;
}

void tick()
{
  currentTime = glutGet(GLUT_ELAPSED_TIME);
  elapsedTime = (currentTime - lastTime) / 1000;
  //printf("Elapsed time: %f\n", elapsedTime);
  totalTime += elapsedTime;
  lastTime = currentTime;
}

///////////////////////////////////////////////

double myRandom()
//Return random double within range [0,1]
{
  return (rand()/(double)RAND_MAX);
}

int randomBetween(int low, int high)
{
  return (rand() % (low - high)) + low;
}

//See notebook for explanation of formula
void updateParticle(Emitter* emitter, int index)
{
  if (emitter->particles[index].y <= 0.0f)
  {
  	return;
  }
    
  /*if (totalTime >= emitter->particles[index].lifeTime)
  	emitter->particles[index].isAlive = 0;*/
  	
  //float elapsedTime = currentTime - lastTime;
  //printf("Elapsed time: %f\n", elapsedTime);
  GLfloat angle = emitter->particles[index].angle;
  
  emitter->particles[index].x = emitter->x + 
                (emitter->particles[index].initVelX * totalTime);
                
  emitter->particles[index].y 
          = emitter->y + ((emitter->particles[index].initVelY * totalTime) 
            - (gravity * totalTime * totalTime * 0.5));
            
  if (emitter->particles[index].y < 0.0f)
  	emitter->particles[index].y = 0.0f;
            
  //Update particle rotation
  //x = z * sin(yRot) + x * cos(yRot)
  //y = y
  //z = z * cos(yRot) - x * sin(yRot)
  emitter->particles[index].x = (emitter->particles[index].z * sinf(emitter->particles[index].yRot * DEG_TO_RAD)) + (emitter->particles[index].x * cosf(emitter->particles[index].yRot * DEG_TO_RAD));
  emitter->particles[index].z = (emitter->particles[index].z * cosf(emitter->particles[index].yRot * DEG_TO_RAD)) - (emitter->particles[index].x * sinf(emitter->particles[index].yRot * DEG_TO_RAD));
  
  if (emitter->particles[index].r > emitter->rF)
  	emitter->particles[index].r -= 0.005f;
	if (emitter->particles[index].g > emitter->gF)
		emitter->particles[index].g -= 0.005f;
  if (emitter->particles[index].b > emitter->bF)  
   emitter->particles[index].b -= 0.005f;      
   
  printf("X: %f, Y: %f, Z: %f, yRot: %f\n", emitter->particles[index].x, emitter->particles[index].y, emitter->particles[index].z, emitter->particles[index].yRot);
}

/* Draw a single particle in space.
   We give the array index to allow indexing
   into the array of particles to draw it */
void drawParticle(Emitter* emitter, int index)
{
	if (emitter->particles[index].isAlive)
	{
		//printf("Drawing: %d at X: %f, Y: %f, Z: %f,  with angle: %f\n", index, emitter->particles[index].x, emitter->particles[index].y, emitter->particles[index].z, emitter->particles[index].angle);
		glBegin(GL_POINTS);
		  glColor3f(emitter->particles[index].r, emitter->particles[index].g, emitter->particles[index].b);
		  glVertex3f(emitter->particles[index].x, emitter->particles[index].y, emitter->particles[index].z);
		  //glTranslatef(emitter->particles[index].x, emitter->particles[index].y, 0.0f);
		glEnd();
  }
}

void makeEmitter(Emitter *emitter, int noParticles)
{
  emitter = (Emitter *)malloc(sizeof(Emitter));
  emitter->x = 0.0f;
  emitter->y = 5.0f;
  emitter->z = 0.0f;
  
  emitter->rI = 1.0f;
  emitter->gI = 0.4f;
  emitter->bI = 0.0f;
  
  emitter->rF = 0.0f;
  emitter->gF = 0.0f;
  emitter->bF = 0.0f;
  //TODO make this a random initial velocity?
  emitter->initVel = 50.0f;
  emitter->noParticles = noParticles;
  emitter->particles = (Particle *) malloc(sizeof(Particle) * noParticles);
  
  //Fill in each particle data
  int i = 0;
  for (i = 0; i < emitter->noParticles; ++i)
  {
    emitter->particles[i].x = emitter->x;
    emitter->particles[i].y = emitter->y;
    emitter->particles[i].z = emitter->z;
    
    emitter->particles[i].r = emitter->rI;
    emitter->particles[i].g = emitter->gI;
    emitter->particles[i].b = emitter->bI;
    
    emitter->particles[i].angle = (float) randomBetween(5, 45);
    emitter->particles[i].yRot = (float) randomBetween(0, 359);
    //printf("Angle selected: %f\n", emitter->particles[i].yRot);
    emitter->particles[i].initVelX = emitter->initVel * cosf(emitter->particles[i].angle * DEG_TO_RAD);
    emitter->particles[i].initVelY = emitter->initVel * sinf(emitter->particles[i].angle * DEG_TO_RAD);
    
    emitter->particles[i].isAlive = 1;
    
    //printf("InitX: %f, InitY: %f\n", emitter->particles[i].initVelX, emitter->particles[i].initVelY);
  }
    
  //Set point size to be bigger so we can see the points
  glPointSize(5.0f);
  
  myEmitter = emitter;
  
}

void resetEmitter(Emitter *emitter)
{
  if (emitter == NULL)
    return;     
  int i;
  for (i = 0; i < emitter->noParticles; ++i)
  {
    emitter->particles[i].x = emitter->x;
    emitter->particles[i].y = emitter->y;
    emitter->particles[i].z = emitter->z;
    
    emitter->particles[i].r = emitter->rI;
    emitter->particles[i].g = emitter->gI;
    emitter->particles[i].b = emitter->bI;
    
    emitter->particles[i].angle = (float) randomBetween(5, 45);
    emitter->particles[i].yRot = (float) randomBetween(0, 359);
    
    emitter->particles[i].initVelX = emitter->initVel * cosf(emitter->particles[i].angle * DEG_TO_RAD);
    emitter->particles[i].initVelY = emitter->initVel * sinf(emitter->particles[i].angle * DEG_TO_RAD);
    
    emitter->particles[i].isAlive = 1;
    printf("\nReset to: X: %f, Y: %f, Z: %f\n", emitter->particles[i].x, emitter->particles[i].y, emitter->particles[i].z);
  }
}

void releaseEmitter(Emitter *emitter)
{
  //Release particle array
  free(emitter->particles);
  emitter->particles = NULL;
  
  //Release emitter
  free(emitter);
  emitter = NULL;  
}

//Updates an emitter by updating every single particle released
//by this emitter
void updateEmitter(Emitter* emitter)
{
  int i;
  for (i = 0; i < emitter->noParticles; ++i)
  {
    updateParticle(emitter, i);
  }
}

void drawParticles(Emitter* emitter)
{
  int i;
  for (i = 0; i < emitter->noParticles; ++i)
  {
  	glPushMatrix();
      drawParticle(emitter, i);
    glPopMatrix();      
  }
}


///////////////////////////////////////////////

void display()
{
  glLoadIdentity();
  gluLookAt(0.f, 0.f, 400.1f,
            0.0, 0.0, 0.0,
            0.0, 1.0, 0.0);
  // Clear the screen
  glClear(GL_COLOR_BUFFER_BIT);
  // If enabled, draw coordinate axis
  if(axisEnabled) glCallList(axisList);
  
  //////////////////////////////////
  //MY CODE
  //////////////////////////////////
  drawParticles(myEmitter);


  glutSwapBuffers();
}

void idle()
{
  tick();
  updateEmitter(myEmitter);
  
  glutPostRedisplay();
}

///////////////////////////////////////////////

void keyboard(unsigned char key, int x, int y)
{
  if(key == 27) exit(0);
  
  //Reset everything
  if (key == ' ')
  {
    resetEmitter(myEmitter);
    initClock();
  }
  
  glutPostRedisplay();
}

void specialKeys(int key, int x, int y)
{
}

///////////////////////////////////////////////

void reshape(int width, int height)
{
  glClearColor(0.9, 0.9, 0.9, 1.0);
  glViewport(0, 0, (GLsizei)width, (GLsizei)height);
  glMatrixMode(GL_PROJECTION);
  glLoadIdentity();
  gluPerspective(60, (GLfloat)width / (GLfloat)height, 1.0, 10000.0);
  glMatrixMode(GL_MODELVIEW);
}

///////////////////////////////////////////////

void makeAxes() {
// Create a display list for drawing coord axis
  axisList = glGenLists(1);
  glNewList(axisList, GL_COMPILE);
      glLineWidth(2.0);
      glBegin(GL_LINES);
      glColor3f(1.0, 0.0, 0.0);       // X axis - red
      glVertex3f(0.0, 0.0, 0.0);
      glVertex3f(AXIS_SIZE, 0.0, 0.0);
      glColor3f(0.0, 1.0, 0.0);       // Y axis - green
      glVertex3f(0.0, 0.0, 0.0);
      glVertex3f(0.0, AXIS_SIZE, 0.0);
      glColor3f(0.0, 0.0, 1.0);       // Z axis - blue
      glVertex3f(0.0, 0.0, 0.0);
      glVertex3f(0.0, 0.0, AXIS_SIZE);
    glEnd();
  glEndList();
}

///////////////////////////////////////////////

void initGraphics(int argc, char *argv[])
{
  glutInit(&argc, argv);
  glutInitWindowSize(800, 600);
  glutInitWindowPosition(100, 100);
  glutInitDisplayMode(GLUT_DOUBLE);
  glutCreateWindow("COMP37111 Particles");
  glutDisplayFunc(display);
  glutKeyboardFunc(keyboard);
  glutReshapeFunc(reshape);
  makeAxes();
  /////////////////////////////////
  //MY CODE
  /////////////////////////////////
  //Initialise clock
  initClock();
  
  
  //Make emitters
  Emitter *emitter = NULL;
  makeEmitter(emitter, 1);
}

/////////////////////////////////////////////////

int main(int argc, char *argv[])
{
  double f;
  srand(time(NULL));
  initGraphics(argc, argv);
  glEnable(GL_POINT_SMOOTH);
  glutDisplayFunc(display);
  glutIdleFunc(idle);
  glutSpecialFunc(specialKeys);
  glutKeyboardFunc (keyboard);
  glutMainLoop();
  
  //Free system resources
  releaseEmitter(myEmitter);
  
  return 0;
}
