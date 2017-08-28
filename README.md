# elasticDB
A fault tolerant load balancing database. Built with 4 AWS EC2 virtual machine instances.

# Project description :

Among 4 AWS EC2 instances, 1 serves as master server, 2 serve as slave server, 1 serve as candidate server.

# My Laptop side
1) My own laptop serves as client emulator, load balancer, controller, executor with multi-thread technique.

2) Client emulator create 10 ~ 60 user sessions threads and sending read and write queries to master server.

3) Load balancer gets request from client emulator and distributes this session to one of slaves in a round robin manner. If availability can not be satisfied, it will put a SCALE OUT message to event queue(later executed by executor).

4) Controller is enabled every passing minute, and it calculates average network access latency(within this minute), if availability can not be satisfied, a SCALE OUT message will be put into event queue. If availability performance is beyond good(lantency less than 50ms), a SCALE IN message will be put into event queue.

5) Executor executes events in the event queue(SCALE OUT message has higher priority).

7) 

3) Implemented with MySQL replication, slaves listen to the master by pulling master's binary log when master get write new data on it's own database.
4) 
