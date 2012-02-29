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

/* Commands:                     
 *                                                                           
 * F: drive forwards                                                 
 * S: stop                                                              
 * B: go backwards                                                   
 * L: turn left                                                          
 * R: turn right                                                                                                   
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
bool moving = false; // Whether robot is in motion
bool kicked = false; // Whether the kicker is up
static WbDeviceTag servo;

/* Misc Stuff */
#define MAX_SPEED 20
#define NULL_SPEED 0
#define HALF_SPEED 10
#define MIN_SPEED -10

#define WHEEL_RADIUS 0.04
#define AXLE_LENGTH 0.2
#define ENCODER_RESOLUTION 100.0
// Robot Actions:
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

static void go_forward() {
  wb_differential_wheels_set_speed(MAX_SPEED, MAX_SPEED);
  moving = true;
  }

static void go_backward() {
  wb_differential_wheels_set_speed(MIN_SPEED, MIN_SPEED);
  moving = true;
  }

static void stop() {
  wb_differential_wheels_set_speed(NULL_SPEED, NULL_SPEED);
  moving = false;
  }
  
static void turn_right() {
  wb_differential_wheels_set_speed(HALF_SPEED, -HALF_SPEED);
  moving = true;
  }
  
static void turn_left() {
  wb_differential_wheels_set_speed(-HALF_SPEED, HALF_SPEED);
  moving = true;
  }

static void kick(){
  wb_servo_set_position(servo, INFINITY);
  kicked = true;
  }
static void unkick(){
  wb_servo_set_position(servo, 0);
  kicked = false;
  }
  
static void turn(){//double angle) {
  moving = false;
  stop();
  double angle = -1.5707;
  wb_differential_wheels_enable_encoders(get_time_step());
  wb_differential_wheels_set_encoders(0.0, 0.0);
  step();
  double neg = (angle < 0.0)? -1.0: 1.0;
  wb_differential_wheels_set_speed((neg*HALF_SPEED / 4), (-neg*HALF_SPEED / 4));
  double orientation;
  do {
    double l = wb_differential_wheels_get_left_encoder();
    double r = wb_differential_wheels_get_right_encoder();
    double dl = l / ENCODER_RESOLUTION * WHEEL_RADIUS; // distance covered by left wheel in meter
    double dr = r / ENCODER_RESOLUTION * WHEEL_RADIUS; // distance covered by right wheel in meter
    orientation = neg * (dl - dr) / AXLE_LENGTH; // delta orientation in radian
    printf("Orientation %f \n",orientation);
    printf("Angle %f \n",angle);
    step();
  } while (orientation < neg*angle); // overshooting here. REPORT ME.
  stop();
  wb_differential_wheels_disable_encoders();
  step();
  }

static int fd;
static fd_set rfds;

static int accept_client(int server_fd)
{
    int cfd;
    struct sockaddr_in client;
#ifndef WIN32
    socklen_t asize;
#else
    int asize;
#endif 
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

#ifdef WIN32
    /* initialize the socket api */
    WSADATA info;

    rc = WSAStartup(MAKEWORD(1, 1), &info); /* Winsock 1.1 */
    if (rc != 0) {
        printf("cannot initialize Winsock\n");
        return -1;
    }
#endif
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
    char buffer[256];
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
    n = recv(fd, buffer, 256, 0);
    if (n < 0) {
        printf("error reading from socket\n");
    }
    buffer[n] = '\0';
    printf("Received %d bytes: %s\n", n, buffer);

    if (buffer[0] == 'F') {
        go_forward()   ;    
        send(fd, "f\r\n", 3, 0);

    } else if (buffer[0] == 'S') {
        stop();
        send(fd, "s\r\n", 3, 0);

    } else if (buffer[0] == 'B') {
        go_backward();
        send(fd, "b\r\n", 3, 0);

    } else if (buffer[0] == 'L') {
        turn_left();
        send(fd, "l\r\n", 3, 0);

	} else if (buffer[0] == 'R') {
        turn_right();
        send(fd, "r\r\n", 3, 0);
        
  } else if (buffer[0] == 'T') {
        turn();
        send(fd, "t\r\n", 3, 0);
        
  } else if (buffer[0] == 'K') {
        if (kicked)
          unkick();
        else
          kick();
        send(fd, "k\r\n", 3, 0);

    } else if (strncmp(buffer, "exit", 4) == 0) {
        printf("connection closed\n");
#ifdef WIN32
        closesocket(fd);
        ret = WSACleanup();
#else
        ret = close(fd);
#endif
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
  servo = wb_robot_get_device("servo");
  wb_servo_set_velocity(servo, 20.0);
    while (1) {
      run();
      step();
    }
  while (wb_robot_step(TIME_STEP) != -1);
  return EXIT_SUCCESS;
}
