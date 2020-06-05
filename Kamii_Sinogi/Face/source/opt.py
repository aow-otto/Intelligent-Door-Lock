import os,sys,re,requests,json,base64,time,picamera

account={'api_key':'','api_secret':''}
account['api_key']='pLkXcTYc1irj2Lqq2_ZzLPWflVrPjSG9'
account['api_secret']='F6tjVfZxmsngYwC52TlSROKRtYqS5FBx'

def FileClear(path):
	list=os.listdir(path)
	for file in list:
		stp=path+'/'+file
		if os.path.isdir(stp):
			FileClear(stp)
			os.rmdir(stp)
		else: os.remove(stp)

def getbase64(image):
	return base64.b64encode(open(image,'rb').read())

def CameraCatch(path='Kamii_Sinogi/Face/'):
	with picamera.PiCamera() as camera:
		camera.start_preview();time.sleep(1)
		camera.capture(path+'stp.jpg')
		face_token=FaceDetect(path+'stp.jpg')
	return face_token

def HistorySave(path):
	now=1
	while os.path.exists(path+'%d'%now): now+=1
	path+='%d'%now+'/'
	os.makedirs(path)
	return path

def CameraJudge(path='Kamii_Sinogi/Face/history/',setname='Sinogi'):
	path=HistorySave(path)
	with picamera.PiCamera() as camera:
		camera.start_preview();time.sleep(1)
		for now in range(1,4):
			camera.capture(path+'%d'%now+'.jpg')
			unknownface=FaceDetect(path+'%d'%now+'.jpg')
			possibleface=FaceSearch(unknownface,'Sinogi')
			if(FaceCompare(unknownface,possibleface)==0): return 0
			time.sleep(0.5)
	return 1

def HistoryClear(path='Kamii_Sinogi/Face/history'):
	FileClear(path)

def FaceDetect(path):
	url='https://api-cn.faceplusplus.com/facepp/v3/detect'
	data=account;data['image_base64']=getbase64(path)
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

def FacesetCreate(setname='Sinogi'):
	path='Kamii_Sinogi/Face/data/'+setname
	if os.path.exists(path):
		path+='_';setname+='_';now=1
		while(os.path.exists(path+'%d'%now)): now+=1
		path+='%d'%now;setname+='%d'%now
	url='https://api-cn.faceplusplus.com/facepp/v3/faceset/create'
	data=account;data['display_name']=data['outer_id']=setname
	reply=requests.post(url,data).json()
	os.makedirs(path)

def FacesetDelete(setname,check=0):
	path='Kamii_Sinogi/Face/data/'+setname
	url='https://api-cn.faceplusplus.com/facepp/v3/faceset/delete'
	data=account;data['outer_id']=setname;data['check_empty']=check
	reply=requests.post(url,data).json()
	FileClear(path);os.rmdir(path)

def FacesetAdd(setname='Sinogi'):
	face_token=CameraCatch()
	path='Kamii_Sinogi/Face/data/'+setname
	path_image='Kamii_Sinogi/Face'
	old_file=os.path.join(path_image,'stp.jpg')
	new_file=os.path.join(path,face_token+'.jpg')
	os.rename(old_file,new_file)
	url='https://api-cn.faceplusplus.com/facepp/v3/faceset/addface'
	data=account;data['outer_id']=setname;data['face_tokens']=face_token
	reply=requests.post(url,data).json()

def FacesetRemove(setname,face):
	path='Kamii_Sinogi/Face/data/'+setname
	url='https://api-cn.faceplusplus.com/facepp/v3/faceset/removeface'
	data=account;data['outer_id']=setname;data['face_tokens']=face
	reply=requests.post(url,data).json()
	os.remove(path+'/'+face+'.jpg')

def FacesetClear(setname):
	list=os.listdir('Kamii_Sinogi/Face/data/'+setname)
	for file in list:
		FacesetRemove(setname,file.split('.')[0])

def DatabaseClear():
	list=FacesetGet()
	for faceset in list:
		FacesetDelete(faceset['outer_id'])