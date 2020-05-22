mud:
	javac cs3524/solutions/mud/Edge.java; \
	javac cs3524/solutions/mud/Vertex.java; \
	javac cs3524/solutions/mud/MUD.java; \
	javac cs3524/solutions/mud/MUDServer.java; \
	javac cs3524/solutions/mud/MUDServerInterface.java; \
	javac cs3524/solutions/mud/MUDServerMainline.java; \
	javac cs3524/solutions/mud/MUDClient.java; \
	javac cs3524/solutions/mud/MUDClientImpl.java; \
	javac cs3524/solutions/mud/MUDClientInterface.java; 

mudclean:
	rm -f cs3524/solutions/mud/*.class