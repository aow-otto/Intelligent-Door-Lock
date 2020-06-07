from __future__ import division
import time
import math
import Adafruit_PCA9685
import light
import threading

servo_min = 120
servo_max = 630
time_sleep = 1
time_wait = 10
num_lock = 600
num_lunlock = 800
num_runlock = 400
angle_qingxie = 45
unlock_direction = 2


def op_set_angle(pwm, port, angle):
    num = servo_min + int((servo_max - servo_min) / 180 * (angle + 90))
    pwm.set_pwm(port, 0, num)


def op_operate(angle_qingxie=-15, unlock_direction='r'):

    angle_rarm_close = 90
    angle_larm_close = 90
    angle_body_close = 0
    angle_leg_close = 0

    angle_rarm_open = -90
    angle_larm_open = -90
    angle_body_open = -angle_qingxie
    angle_leg_open = -angle_qingxie

    unlock = Adafruit_PCA9685.PCA9685()
    leg = Adafruit_PCA9685.PCA9685()
    body = Adafruit_PCA9685.PCA9685()
    larm = Adafruit_PCA9685.PCA9685()
    rarm = Adafruit_PCA9685.PCA9685()

    port_rarm = 5
    port_larm = 4
    port_body = 2
    port_leg = 1
    port_unlock = 0

    unlock.set_pwm_freq(60)
    leg.set_pwm_freq(60)
    body.set_pwm_freq(60)
    larm.set_pwm_freq(60)
    rarm.set_pwm_freq(60)

    light.set_light("green","sblink",True)
    op_set_angle(larm, port_larm, angle_larm_open)
    op_set_angle(rarm, port_rarm, angle_rarm_open)
    print 'Moving servo on arm to open.'
    time.sleep(time_sleep)
    op_set_angle(body, port_body, angle_body_open)
    print 'Moving servo on body to open.'
    op_set_angle(leg, port_leg, angle_leg_open)
    print 'Moving servo on leg to open.'
    # time.sleep(time_sleep)

    if unlock_direction == 'r':
        unlock.set_pwm(port_unlock, 0, num_runlock)
    elif unlock_direction == 'l':
        unlock.set_pwm(port_unlock, 0, num_lunlock)
    print 'Unlock.'

    light.set_light("green","still",True)
    time.sleep(time_wait)
    unlock.set_pwm(port_unlock, 0, num_lock)
    print 'Lock.'

    light.set_light("blue","still",True)

    op_set_angle(larm, port_larm, angle_larm_close)
    op_set_angle(rarm, port_rarm, angle_rarm_close)
    time.sleep(4*time_sleep)
    print 'Moving servo on arm to close.'
    op_set_angle(body, port_body, angle_body_close)
    print 'Moving servo on body to close.'
    op_set_angle(leg, port_leg, angle_leg_close)
    print 'Moving servo on leg to close.'
    time.sleep(time_sleep)
    light.set_solid(False)

class operate (threading.Thread):
    def __init__(self, angle_qingxie=45, unlock_direction='r'):
        threading.Thread.__init__(self)
        self.angle_qingxie = angle_qingxie
        self.unlock_direction = unlock_direction

    def run(self):
        op_operate(self.angle_qingxie, self.unlock_direction)

def op_operate_with_light(angle_qingxie=45, unlock_direction='r'):
    op = operate(angle_qingxie,unlock_direction)
    op.start()
    light_first = light.light("green", "sblink", 2)
    light_first.start()
    light_first.join()
    light_first = light.light("green", "still", 10)
    light_first.start()
    light_second.join()
    light_third = light.light_still("blue")
    light_third.start()
    light_third.join()
    op.join()

