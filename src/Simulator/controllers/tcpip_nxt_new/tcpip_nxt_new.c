/*
 * File:         tcpip.c
 * Date:         October, 2003
 * Description:  A simple program implementing a TCP/IP relay controller for
 *               interfacing Webots with any development environment able to
 *               use TCP/IP, including MathLab, Lisp, Java, C, C++, etc.
 * Author:       Darren Smith
 * Modifications: Steven Eardley
 *
 * Copyright (c) 2006 Cyberbotics - www.cyberbotics.com
 */

/* Commands are defined in the opcodes below.
 *                                                                           
 * A sample client program, written in C is included in this directory.      
 * See client.c for the source code                                          
 * compile it with gcc client.c -o client                                    
 *                                                                           
 * Everything relies on standard POSIX TCP/IP sockets.                       
 */

#include <webots/robot.h>
#include <webots/differential_wheels.h>
#include <webots/servo.h>
#include <webots/compass.h>

#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/time.h>
#include <netinet/in.h>         /* definition of struct sockaddr_in */
#include <netdb.h>              /* definition of gethostbyname */
#include <arpa/inet.h>          /* definition of inet_ntoa */
#include <unistd.h>             /* definition of close */


#define SOCKET_PORT 10020

/* Our variables */
static WbDeviceTag servo;
static WbDeviceTag compass;
//static const char *compass_name = "compass";

/* Sim Stuff */
#define MAX_SPEED 12
#define NULL_SPEED 0
#define HALF_SPEED 6
#define MIN_SPEED -10
#define CONTROLLER_SPEED 60 // the speed our robot controller uses

/* Robot Stuff */
#define DO_NOTHING 0X00
#define FORWARDS 0X01
#define BACKWARDS 0x02
#define STOP 0X03
#define KICK 0X04
#define QUIT 0X05
#define FORWARDS_TRAVEL 0X06
#define TRAVEL_BACKWARDS_SLIGHRLY 0X07
#define TRAVEL_ARC 0X08
#define ACCELERATE 0X09
#define ROTATE 0X0A
#define EACH_WHEEL_SPEED 0X0B
#define STEER 0X0C

#define WHEEL_RADIUS 0.04
#define AXLE_LENGTH 0.2
#define ENCODER_RESOLUTION 100.0

// Helper Functions
static int get_time_step() {
  static int time_step = -1;
  if (time_step == -1)
    time_step = (int) wb_robot_get_basic_time_step();
  return time_step;
}

static void step() {
  if (wb_robot_step(get_time_step()) == -1) {
    wb_robot_cleanup();
    exit(EXIT_SUCCESS);
  }
}

bool withinRange(double test, double check, double errorMargin){
  return (test >= (check - errorMargin) && test < (check + errorMargin));
}

// taken from webots website.
double get_bearing_in_degrees() {
  const double *north = wb_compass_get_values(compass);
  double rad = atan2(north[0], north[2]);
  double bearing = (rad - 1.5708) / M_PI * 180.0;
  if (bearing <= 0.0)
    bearing = bearing + 360.0;
  return bearing;
  }
  
static short bytesToShort(char a, char b) {
  return (short)(((a & 0xFF) << 8) | (b & 0xFF));
  }

// Robot Actions
static void do_nothing(){
  step();
  }

// go forward with speed proportional to max speed
static void go_forward(int speed) {
  int wheel_speed = (CONTROLLER_SPEED / speed) * MAX_SPEED;
  wb_differential_wheels_set_speed(wheel_speed, wheel_speed);
  }

static void go_backward() {
  wb_differential_wheels_set_speed(MIN_SPEED, MIN_SPEED);
  }

static void stop() {
  wb_differential_wheels_set_speed(NULL_SPEED, NULL_SPEED);
  }
  
static void kick(){
  wb_servo_enable_position(servo, 5);
  wb_servo_set_position(servo, INFINITY);
  while (wb_servo_get_position(servo) < 2.9){
    step();
  }
  wb_servo_set_position(servo, 0);
  }
  
static void turn_comp(double angle) {
	double current_orientation = get_bearing_in_degrees();
  double goal_orientation = (current_orientation + angle);
  if (goal_orientation <= 0.0)
    goal_orientation  = goal_orientation + 360.0;
  goal_orientation = fmod(goal_orientation,360.0);
  while(!(withinRange(current_orientation, goal_orientation, 3.0))) {
		if((current_orientation < goal_orientation) && (goal_orientation - current_orientation < 180.0)){
			wb_differential_wheels_set_speed((HALF_SPEED), -(HALF_SPEED));	
		} else{
			wb_differential_wheels_set_speed(-(HALF_SPEED), (HALF_SPEED));	
		}
		current_orientation = get_bearing_in_degrees();
		step();
	}
	stop();
	step();
  }

static int fd;
static fd_set rfds;

static int accept_client(int server_fd)
{
    int cfd;
    struct sockaddr_in client;
    int asize;
    struct hostent *client_info;

    asize = sizeof(struct sockaddr_in);

    cfd = accept(server_fd, (struct sockaddr *) &client, &asize);
    if (cfd == -1) {
        printf("cannot accept client\n");
        return -1;
    }
    client_info = gethostbyname((char *) inet_ntoa(client.sin_addr));
    printf("Accepted connection from: %s \n",
                         client_info->h_name);

    return cfd;
}

static int create_socket_server(int port)
{
    int sfd, rc;
    struct sockaddr_in address;

    /* create the socket */
    sfd = socket(AF_INET, SOCK_STREAM, 0);
    if (sfd == -1) {
        printf("cannot create socket\n");
        return -1;
    }

    /* fill in socket address */
    memset(&address, 0, sizeof(struct sockaddr_in));
    address.sin_family = AF_INET;
    address.sin_port = htons((unsigned short) port);
    address.sin_addr.s_addr = INADDR_ANY;

    /* bind to port */
    rc = bind(sfd, (struct sockaddr *) &address, sizeof(struct sockaddr));
    if (rc == -1) {
        printf("cannot bind port %d\n", port);
        return -1;
    }

    /* listen for connections */
    if (listen(sfd, 1) == -1) {
        printf("cannot listen for connections\n");
        return -1;
    }
    printf("Waiting for a connection on port %d...\n", port);

    return accept_client(sfd);
}

static void initialize()
{
    fd = create_socket_server(SOCKET_PORT);
    FD_ZERO(&rfds);
    FD_SET(fd, &rfds);
}

static void run()
{
    int n;
    int ret;
    short angle;
    unsigned char buffer[5];
    struct timeval tv = { 0, 0 };
    int number;

    /* Set up the parameters used for the select statement */

    FD_ZERO(&rfds);
    FD_SET(fd, &rfds);

    /*
     * Watch TCPIP file descriptor to see when it has input.
     * No wait - polling as fast as possible 
     */
    number = select(fd + 1, &rfds, NULL, NULL, &tv);

    /* If there is no data at the socket, then redo loop */
    if (number == 0) {
        return;
    }

    /* ...otherwise, there is data to read, so read & process. */
    n = recv(fd, buffer, 5, 0);
    if (n < 0) {
        printf("error reading from socket\n");
    }
    buffer[n] = '\0';
    printf("Received %d bytes: %s\n", n, buffer);
    printf("%x, %x, %x, %x\n",buffer[0],buffer[1],buffer[2],buffer[3]);
    
    int param0 = (int)buffer[1];
    short param1 = (short)bytesToShort(buffer[2],buffer[3]);
    
    if (buffer[0] == FORWARDS) {
        go_forward(param0);

    } else if (buffer[0] == STOP) {
        stop();

    } else if (buffer[0] == BACKWARDS) {
        go_backward();

    } else if (buffer[0] == DO_NOTHING) {
        do_nothing();
        
  } else if (buffer[0] == ROTATE) {
        angle = param1;
        turn_comp((double)angle);
        send(fd, "f", 1, 0);
        
  } else if (buffer[0] == KICK) {
          kick();

    } else if (strncmp(buffer, "exit", 4) == 0) {
        printf("connection closed\n");

        ret = close(fd);
        if (ret != 0) {
            printf("Cannot close socket\n");
        }
        fd = 0;
    } else {
        send(fd, "\n", 1, 0);
    }
}

int main()
{
    wb_robot_init();
    initialize();
	srand(time(NULL));
	int TIME_STEP = get_time_step();
  compass = wb_robot_get_device("compass");
  wb_compass_enable(compass, TIME_STEP);
  
  servo = wb_robot_get_device("servo");
  wb_servo_set_velocity(servo, 20.0);
    while (1) {
      run();
      step();
    }
  while (wb_robot_step(TIME_STEP) != -1);
  return EXIT_SUCCESS;
}
