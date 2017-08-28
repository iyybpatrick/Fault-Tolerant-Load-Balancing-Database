# elasticDB
A fault tolerant load balancing database. Built with 4 AWS EC2 virtual machine instances.

# Project description :
Developed an elastic distributed database using MySQL replication and used it to serve as the backend for a multi-tier web application running TPC-W benchmark (AWS EC2, MySQL(replication), multi-tier web application)
Among 4 AWS EC2 instances, 1 serves as master server, 2 serve as slave server, 1 serve as candidate server.

Developed a load balancer to route the browsing queries to master and route the ordering queries to slaves in a round-robin manner (Maven, multi-thread, high availability & dynamic scalability distributed database)

# My Laptop side

1) My own laptop serves as client emulator, load balancer, controller, executor with multi-thread technique.

2) Client emulator create 10 ~ 60 user session threads and sending read and write queries to load balancer.

3) Load balancer gets request from client emulator and redirect this query to one of slaves in a round robin manner. If availability can not be satisfied, it will put a SCALE OUT message into event queue(later executed by executor).

4) Controller is enabled every passing minute, and it calculates average network access latency(within this minute), if availability can not be satisfied, a SCALE OUT message will be put into event queue. If availability performance is beyond good(lantency less than 50ms), a SCALE IN message will be put into event queue.

5) Executor executes events in the event queue(SCALE OUT message has higher priority).

# AWS EC2 side

1) Master is the only server responses to clients' write query.

2) Implemented with MySQL replication, slaves listen to the master by pulling master's binary log when master's database get updated(write query from clients).
