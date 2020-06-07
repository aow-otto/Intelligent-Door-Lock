# coding=utf-8
import serial
import operate
import json
import os
from sh import bash

# command = "sudo rfcomm watch hci0 0 /rfcomm_check"
# code = os.popen(command)
while True:
    try:
        ser = serial.Serial("/dev/rfcomm0", 9600)
        ser.write("Successfully connected(input)!".encode())
        while True:
            count = ser.inWaiting()
            if count != 0:
                recv = ser.read(count)
                command = recv.split(' ', -1)
                try:
                    with open('/home/pi/Kamii_Sinogi/Face/source/setting.json') as file_open:
                        setting = json.load(file_open)
                        file_open.close()
                except ValueError:
                    with open('/home/pi/Kamii_Sinogi/Face/source/setting_normal.json') as file_open:
                        setting = json.load(file_open)
                        file_open.close()
                if command[0] == "unlock_door":
                    print command
                    operate.op_operate()
                else:
                    print command
                    if command[0] == "sys_control":
                        if command[1] == "open":
                            setting['sys_control'] = True
                        elif command[1] == "close":
                            setting['sys_control'] = False
                    elif command[0] == "sys_autocontrol":
                        if command[1] == "open":
                            setting['autoctrl_open'].append(
                                [int(command[2]), int(command[3])])
                        elif command[1] == "close":
                            setting['autoctrl_close'].append(
                                [int(command[2]), int(command[3])])
                    elif command[0] == "set_safety_mode":
                        setting['safety_mode'] = int(command[1])
                    elif command[0] == "set_steer_angle":
                        setting['steer_angle'] = int(command[1])
                    elif command[0] == "set_unlock_direction":
                        setting['unlock_direction'] = int(command[1])
                    elif command[0] == "clear_sys_autocontrol":
                        if command[1] == "open":
                            setting['autoctrl_open'] = []
                        elif command[1] == "close":
                            setting['autoctrl_close'] = []
                    print setting
                    with open('/home/pi/Kamii_Sinogi/Face/source/setting.json', 'w') as file_write:
                        json.dump(setting, file_write)
                        file_write.close()
    except KeyboardInterrupt:
        break
    except:
        pass
