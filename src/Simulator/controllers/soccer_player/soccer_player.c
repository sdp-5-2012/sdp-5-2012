/*
 * File:         soccer_player.c
 * Date:         February 11th, 2003
 * Description:  A controller for players of the soccer game from soccer.wbt
 * Author:       Olivier Michel
 * Modifications:Simon Blanchoud - September 4th, 2006
 *                - Adapting the code to the coding standards of Webots
 *               Yvan Bourquin - October 2nd, 2007
 *                - Adapted to new packet-oriented emitter/receiver API
 *
 * Copyright (c) 2006 Cyberbotics - www.cyberbotics.com
 */

#include <stdlib.h>
#include <time.h>
#include <webots/robot.h>
#include <webots/differential_wheels.h>

#define ROBOTS 6
#define TIME_STEP 64

int main() {
  //const char *name;
  int left_speed, right_speed;
  left_speed = 50;
  right_speed = 50;
  wb_robot_init();
  //name = wb_robot_get_name();
wb_differential_wheels_set_speed(left_speed, right_speed);

  wb_robot_cleanup();

  return 0;
}
