job('T6J2'){
    description('Pulling Commnad')
    label('Docker_host')
    scm{
        github('Nii11/do-task6')
    }
    triggers{
        scm('*****')
    }
    steps{
        shell(''' cp -v * /ws6/do-task6/
cp -v * /ws6/exec/''')
    }
}

job('T6J3'){
    description('Code interprator')
    label('Docker_host')
    triggers{
    upstream('T6J2', 'SUCCESS')
    }    
    steps{
    shell('''cd /ws6/exec/
filename=`ls`
ext="${filename##*.}"
file="${filename%.*}"
case $ext in
	html)
    	cp -v /ws6/exec/* /ws6/env/html/
        cd /ws6/env/html/
        sudo docker build -t niraj1102/simplehtml:latest .
        sudo docker login -u niraj1102 -p lionkinG22!
        sudo docker push niraj1102/simplehtml:latest
        #export POD1="`sudo kubectl get pod |grep web`"
        if [ "`sudo kubectl get pod |grep webpod-testp6`" != "" ]
        then
          sudo kubectl delete pod webpod-testp6
        fi
        sudo kubectl create  -f webpod.yml
     ;;
	py)
		cp -v /ws6/exec/* /ws6/env/python/
        cd /ws3/env/python/
        sudo docker build -t niraj1102/simplepython:latest .
        sudo docker login -u niraj1102 -p lionkinG22!
        sudo docker push niraj1102/simplepython:latest
        #export POD1="`sudo kubectl get pod |grep web`"
        if [ "`sudo kubectl get pod |grep pythonpod-testp6`" != "" ]
        then
          sudo kubectl delete pod pythonpod-testp6
        fi
        sudo kubectl create  -f pypod.yml
     ;;
     	
esac''')
    }
}

job('T6J4'){
	description('Code interprator')
    label('Docker_host')
    triggers{
    upstream('T6J3', 'SUCCESS')
    }    
    steps{
    shell('''cd /ws6/exec/
filename=`ls`
ext="${filename##*.}"
file="${filename%.*}"
case $ext in	
	html)
    	sudo kubectl exec -i webpod-testp6 -- /bin/bash
    	echo "WEB PAGE TESTING==========="		
        exit
	 ;;
     
	py)
		sudo kubectl exec -it pythonpod-testp6 -- bash -c "python3 /python/gcd.py >> /ws/log-loc/python.log"
		sudo kubectl exec -it pythonpod-testp6 -- bash -c "cat /ws/log-loc/python.log"
     ;;
esac''')
    }
}
job('T6J5'){
	description('Result checker')
    label('Docker_host')
    triggers{
    upstream('T6J4' , 'SUCCESS')
    }    
    steps{
    shell('''cd /ws6/exec/
filename=`ls`
ext="${filename##*.}"
file="${filename%.*}"
case $ext in	
	html)
		if [ "`curl http:192.168.99.100:31000/task6.html`" ==  "curl: (7) Failed to connect to 192.168.99.100 port 31000: Connection refused"]
		then
		python3 /ws6/env/html/sendmail.py
		fi
	;;
	py)
		python3 /ws6/env/python/py

	;;
esac
rm /ws6/exec/*''')
    }
}
