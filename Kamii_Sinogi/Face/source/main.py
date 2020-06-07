import bluetooth
import light
import battery
import operate
import ultra
import opt
import json
import time
import thread
bluetooth_check = False
while True:
    try:
        with open('/home/pi/Kamii_Sinogi/Face/source/setting.json') as file_read:
            setting = json.load(file_read)
        file_read.close()
    except ValueError:
        with open('/home/pi/Kamii_Sinogi/Face/source/setting_normal.json') as file_open:
                        setting = json.load(file_open)
                        file_open.close()
    time_now = time.localtime()
    # for i in setting["autoctrl_open"]:
    #     if cmp(time_now[3:4], i):
    #         with open('/home/pi/Kamii_Sinogi/Face/source/setting.json','w') as file_open:
    #             setting["sys_control"] = True
    #             json.dump(setting, file_open)
    #         file_open.close()
    # for i in setting["autoctrl_close"]:
    #     if cmp(time_now[3:4],i):
    #         with open('/home/pi/Kamii_Sinogi/Face/source/setting.json','w') as file_close:
    #             setting["sys_control"] = False
    #             json.dump(setting,file_close)
    #         file_close.close()
    if setting["sys_control"]==False:
        light.set_light("black","still")
        continue
    operate.angle_qingxie = setting["steer_angle"]
    operate.unlock_direction = setting["unlock_direction"]
    if opt.CameraJudge() == True:
        if setting["safety_mode"] == 0:
            bluetooth_check = True
        elif setting["safety_mode"] == 1:
            bluetooth_check = False
        elif setting["safety_mode"] == 2:
            bluetooth_check = bluetooth.sys_bluetooth()
        light.set_light("qing","sblink")
        if bluetooth_check == True:
            if setting["unlock_direction"] != 1:
                operate.op_operate(setting["steer_angle"], 'r')
            elif setting['unlock_direction'] == 1:
                operate.op_operate(setting["steer_angle"], 'l')
    else:
        light.set_light("blue","sblink")
        time.sleep(1)
