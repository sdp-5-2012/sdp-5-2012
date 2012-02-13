/* include headers */
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <math.h>

#include <webots/differential_wheels.h>
#include <webots/robot.h>

/* Misc Stuff */
#define MAX_SPEED 40
#define NULL_SPEED 0
#define HALF_SPEED 20
#define MIN_SPEED -40

#define WHEEL_RADIUS 0.031
#define AXLE_LENGTH 0.271756
#define ENCODER_RESOLUTION 507.919

/* helper functions */
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
  }

/* main */
int main(){
  int counter = 0;
  wb_robot_init();
  srand(time(NULL));
  while (true) {
  go_forward();
  step();
  counter++;
  if (counter==100){
    break;
  }
  }
  return EXIT_SUCCESS;
}