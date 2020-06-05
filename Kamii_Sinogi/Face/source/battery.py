from upspackv2 import *

def sys_get_battery():
    test = UPS2("/dev/ttyAMA0")
    version,vin,batcap,vout = test.decode_uart()
    return batcap

def sys_get_voltage():
    test = UPS2("/dev/ttyAMA0")
    version,vin,batcap,vout = test.decode_uart()
    return float(vout)/1000

# print sys_get_battery()
# print sys_get_voltage()