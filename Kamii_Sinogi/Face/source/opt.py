import os,sys,re,requests,json,base64,time,picamera,light

account={'api_key':'','api_secret':''}
account['api_key']='pLkXcTYc1irj2Lqq2_ZzLPWflVrPjSG9'
account['api_secret']='F6tjVfZxmsngYwC52TlSROKRtYqS5FBx'

def AccountLogin(api_key,api_secret):
	account['api_key']=api_key
	account['api_secret']=api_secret

def FileClear(path):
	list=os.listdir(path)
	for file in list:
		stp=path+'/'+file
		if os.path.isdir(stp):
			FileClear(stp)
			os.rmdir(stp)
		else: os.remove(stp)

def Getbase64(image):
	return base64.b64encode(open(image,'rb').read())

def CameraCatch(path='Kamii_Sinogi/'):
	light.set_light("white","still",True)
	with picamera.PiCamera() as camera:
		camera.start_preview();time.sleep(1)
		camera.capture(path+'Face/stp.jpg')
		face_token=FaceDetect(path+'Face/stp.jpg')
	light.set_solid(False)
	light.set_light("blue","still")
	return face_token

def HistorySave(path='Kamii_Sinogi/Face/history/'):
	now=1
	while os.path.exists(path+'%d'%now): now+=1
	path+='%d'%now+'/'
	os.makedirs(path)
	return path

def CameraJudge(setname='Sinogi',judgetimes=3,path='Kamii_Sinogi/'):
	light.set_light("white","still")
	path=HistorySave(path+'Face/history/')
	with picamera.PiCamera() as camera:
		camera.start_preview();time.sleep(1)
		for now in range(0,judgetimes):
			camera.capture(path+'%d'%now+'.jpg')
			unknownface=FaceDetect(path+'%d'%now+'.jpg')
			possibleface=FaceSearch(unknownface,'Sinogi')
			if(FaceCompare(unknownface,possibleface)==0): return 0
			time.sleep(0.5)
	return 1

def FaceDetect(path):
	url='https://api-cn.faceplusplus.com/facepp/v3/detect'
	data=account;data['image_base64']=Getbase64(path)
	reply=requests.post(url,data).json()
	if reply['face_num']==0: return 'KamiiBaka'
	else: return reply['faces'][0]['face_token']

def FaceSearch(face_token,setname='Sinogi'):
	if face_token=='KamiiBaka': return 'Sinogibaka'
	url='https://api-cn.faceplusplus.com/facepp/v3/search'
	data=account;data['face_token']=face_token;data['outer_id']=setname
	reply=requests.post(url,data).json()
	for face in reply['results']:
		if face['confidence']>reply['thresholds']['1e-5']:
			return face['face_token']
	return 'SinogiBaka'

def FaceCompare(face_token1,face_token2):
	if face_token1=='KamiiBaka' or face_token2=='SinogiBaka': return 0
	url='https://api-cn.faceplusplus.com/facepp/v3/compare'
	data=account;data['face_token1']=face_token1;data['face_token2']=face_token2
	reply=requests.post(url,data).json()
	if reply['confidence']>reply['thresholds']['1e-5']: return 1
	else: return 0

def FacesetGet():
	url='https://api-cn.faceplusplus.com/facepp/v3/faceset/getfacesets'
	reply=requests.post(url,account).json()
	return reply['facesets']

def FacesetCreate(setname='Sinogi',path='Kamii_Sinogi/'):
	path=path+'Face/data/'+setname
	if os.path.exists(path):
		path+='_';setname+='_';now=1
		while(os.path.exists(path+'%d'%now)): now+=1
		path+='%d'%now;setname+='%d'%now
	url='https://api-cn.faceplusplus.com/facepp/v3/faceset/create'
	data=account;data['display_name']=data['outer_id']=setname
	reply=requests.post(url,data).json()
	os.makedirs(path)

def FacesetDelete(setname,check=0,path='Kamii_Sinogi/'):
	path=path+'Face/data/'+setname
	url='https://api-cn.faceplusplus.com/facepp/v3/faceset/delete'
	data=account;data['outer_id']=setname;data['check_empty']=check
	reply=requests.post(url,data).json()
	FileClear(path);os.rmdir(path)

def FacesetAdd(setname='Sinogi',path='Kamii_Sinogi/'):
	face_token=CameraCatch()
	path_image=path+'Face'
	if face_token=='KamiiBaka':
		os.remove(path_image+'/stp.jpg')
		print('No face found.')
		return
	path=path+'Face/data/'+setname
	old_file=os.path.join(path_image,'stp.jpg')
	new_file=os.path.join(path,face_token+'.jpg')
	os.rename(old_file,new_file)
	url='https://api-cn.faceplusplus.com/facepp/v3/faceset/addface'
	data=account;data['outer_id']=setname;data['face_tokens']=face_token
	reply=requests.post(url,data).json()

def FacesetRemove(setname,face_token,path='Kamii_Sinogi/'):
	path=path+'Face/data/'+setname
	url='https://api-cn.faceplusplus.com/facepp/v3/faceset/removeface'
	data=account;data['outer_id']=setname;data['face_tokens']=face_token
	reply=requests.post(url,data).json()
	os.remove(path+'/'+face_token+'.jpg')

def FacesetClear(setname,path='Kamii_Sinogi/'):
	list=os.listdir(path+'Face/data/'+setname)
	for file in list:
		FacesetRemove(setname,file.split('.')[0])

def HistoryClear(path='Kamii_Sinogi/'):
	FileClear(path+'Face/history/')

def DatabaseClear(path='Kamii_Sinogi/'):
	list=FacesetGet()
	for faceset in list:
		FacesetDelete(faceset['outer_id'])
	HistoryClear(path)