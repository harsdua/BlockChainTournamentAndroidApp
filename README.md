# DecentralizedTournamentAndroidApp

-In development- 

Features so far:
A working android app that allows ELO tracking of players. The "MasterNode" must be hosted on the Local Area Network and the app can connect to it.

Upon connection, the app establishes a peer-to-peer network with any other users that are connected to the MasterNode. 

Each user can umpire(arbitre) a match of a user versus another.

Upon completion of the match, the umpire and players sign who won/lost and then the match is shared to every peer.

The signatures are done using RSA encryption. Each user signs with their private key. The verification is done by the master node using their public key

This allows any user to calculate their ELO. 

The decentralization of MasterNodes allows any network to host a MasterNode that will be specific to any game.

In case the masternode is inactive, the peer to peer network established is used instead. 
