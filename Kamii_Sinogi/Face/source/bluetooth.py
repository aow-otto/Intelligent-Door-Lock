import os
from sh import bash
sys_bluetooth_morethan=150
# honor_mac="DC:16:B2:59:26:76"
def sys_get_mac():
	list_mac=[]
	command = "sudo hcitool inq"
	code = os.popen(command)
	output = code.readlines()
	if(output.__len__)==0:
		return False
	else:
		for i in output:
			if i[0]!='I':
				list_mac.append(i[1:18])
		print list_mac
def sys_get_quality(mac):
	mac = "sudo hcitool lq " + mac
	code = os.popen(mac)
	output = code.readline()
	if output == '':
		return 0
	else:
		quality = int(output[14:-1])
		code.close()
		return quality
def sys_bluetooth_check(mac):
	if sys_get_quality(mac)>sys_bluetooth_morethan:
		return True
	else:
		return False
def sys_bluetooth():
	list_mac=sys_get_mac()
	if list_mac==False:
		return False
	for i in list_mac:
		if sys_bluetooth_check(i):
			return True
	return False
# print sys_get_quality(honor_mac)
# if not sys_bluetooth(honor_mac):
# 	print "cannot"