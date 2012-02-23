/* include headers */
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <math.h>

#include <webots/differential_wheels.h>
#include <webots/robot.h>
#include <webots/servo.h>


/* Our variables */

bool moving = false; // Whether robot is moving forwards. False when rotating.
bool kicked = false; // Whether the kicker is up
static WbDeviceTag servo;

/* Misc Stuff */
#define MAX_SPEED 20
#define NULL_SPEED 0
#define HALF_SPEED 10
#define MIN_SPEED -10

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

static void check_keyboard() {

  int key = wb_robot_keyboard_get_key();
  if (key) {
    printf("%d",key);
    switch (key) {
      case WB_ROBOT_KEYBOARD_UP:
        if (moving)
          stop();
        else
          go_forward();
        break;
      case WB_ROBOT_KEYBOARD_DOWN:
        if (moving)
          stop();
        else
          go_backward();
        break;
      case WB_ROBOT_KEYBOARD_RIGHT:
        if (moving)
          stop();
        else
          turn_right();
        break;
      case WB_ROBOT_KEYBOARD_LEFT:
        if (moving)
          stop();
        else
          turn_left();
        break;
      case WB_ROBOT_KEYBOARD_NUMPAD_UP:
        if (kicked)
          unkick();
        else
          kick();
        break;
    }
  }
}

/* main */
int main(){
  wb_robot_init();
  srand(time(NULL));

  int TIME_STEP = get_time_step();
  // enable keyboard
  wb_robot_keyboard_enable(TIME_STEP);
  servo = wb_robot_get_device("servo");
  wb_servo_set_velocity(servo, 20.0);
  // main loop
  do {
    check_keyboard();
    step();
  }
  while (wb_robot_step(TIME_STEP) != -1);
  return EXIT_SUCCESS;
}