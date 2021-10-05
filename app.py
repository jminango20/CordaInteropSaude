import pycorda as pyc

username = 'sa'
password = ''
#IJC
url = 'jdbc:h2:tcp://localhost:5000/node'
#CONSULTORIO
url = 'jdbc:h2:tcp://localhost:5001/node'
##
partyA = pyc.Node(url,username,password)