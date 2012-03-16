/*
 * File:         soccer_supervisor.c
 * Date:         February 11th, 2003
 * Description:  Supevisor the soccer game from soccer.wbt
 *               Send the coordinates and orientations of each robot and the 
 *               coordinates of the ball to each robot via an emitter.       
 * Author:       Olivier Michel
 * Modifications:Simon Blanchoud - September 4th, 2006
 *                - Adapted the code to the Webots standards
 *               Yvan Bourquin - October 2nd, 2007
 *                - Adapted to new packet-oriented emitter/receiver API
 *               Markus Schlafi & Steven Eardley SDP 2012 - Group 5
 *
 * Copyright (c) 2006 Cyberbotics - www.cyberbotics.com
 */

#include <stdio.h>
#include <string.h>
#include <webots/robot.h>
#include <webots/supervisor.h>
#include <webots/emitter.h>

#define ROBOTS 2                /* number of robots */
#define TIME_STEP 40          // simulating 25 fps


int main() {
  const char *robot_name[ROBOTS] = {  "Y1", "NXT" };
  WbNodeRef node;
  WbFieldRef robot_translation_field[ROBOTS],robot_rotation_field[ROBOTS],ball_translation_field;
  int i,j;
  double time = 3 * 60;    /* a match lasts for 3 minutes */
  //double ball_reset_timer = 0;
  double ball_initial_translation[3] = {0,0,0};
  double robot_initial_translation[ROBOTS][3] = {
      {-1.00032, 0.14444, -0.00943952},
      {1.0347, 0.14444, 0.00752448}};
  double robot_initial_rotation[ROBOTS][4] = {
      {0, -1, 0, 1.60867},
      {0, -1, 0, 4.7419}};
  double packet[ROBOTS * 3 + 2];
  char time_string[64];
  const double *robot_translation[ROBOTS], *robot_rotation[ROBOTS], *ball_translation;

  wb_robot_init();

  for (i = 0; i < ROBOTS; i++) {
    node = wb_supervisor_node_get_from_def(robot_name[i]);
    robot_translation_field[i] = wb_supervisor_node_get_field(node,"translation");
    robot_translation[i] = wb_supervisor_field_get_sf_vec3f(robot_translation_field[i]);
    for(j=0;j<3;j++) 
      robot_initial_translation[i][j]=robot_translation[i][j];
    robot_rotation_field[i] = wb_supervisor_node_get_field(node,"rotation");
    robot_rotation[i] = wb_supervisor_field_get_sf_rotation(robot_rotation_field[i]);
    for(j=0;j<4;j++) 
      robot_initial_rotation[i][j]=robot_rotation[i][j];
  }

  node = wb_supervisor_node_get_from_def("BALL");
  ball_translation_field = wb_supervisor_node_get_field(node,"translation");
  ball_translation = wb_supervisor_field_get_sf_vec3f(ball_translation_field);
  for(j=0;j<3;j++) 
    ball_initial_translation[j]=ball_translation[j];
  /* printf("ball initial translation = %g %g %g\n",ball_translation[0],ball_translation[1],ball_translation[2]); */
 

  while(wb_robot_step(TIME_STEP)!=-1) {
    ball_translation = wb_supervisor_field_get_sf_vec3f(ball_translation_field);
    for (i = 0; i < ROBOTS; i++) {
      robot_translation[i]=wb_supervisor_field_get_sf_vec3f(robot_translation_field[i]);
      printf("coords for robot %d: %g %g %g\n",i,robot_translation[i][0],robot_translation[i][1],robot_translation[i][2]); 
      packet[3 * i]     = robot_translation[i][0];  /* robot i: X */
      packet[3 * i + 1] = robot_translation[i][2];  /* robot i: Z */

      if (robot_rotation[i][1] > 0) {               /* robot i: rotation Ry axis */
        packet[3 * i + 2] = robot_rotation[i][3];   /* robot i: alpha */
      } else { /* Ry axis was inverted */
        packet[3 * i + 2] = -robot_rotation[i][3];   
      }
    }
    packet[3 * ROBOTS]     = ball_translation[0];  /* ball X */
    packet[3 * ROBOTS + 1] = ball_translation[2];  /* ball Z */
    //wb_emitter_send(emitter, packet, sizeof(packet));

  
    /* Adds TIME_STEP ms to the time */
    time -= (double) TIME_STEP / 1000;
    if (time < 0) {
      time = 3 * 60; /* restart */
    }
    sprintf(time_string, "%02d:%02d", (int) (time / 60), (int) time % 60);
    wb_supervisor_set_label(2, time_string, 0.45, 0.01, 0.07, 0x000000, 0.0);   /* black */


  }
  
  wb_robot_cleanup();

  return 0;
}
