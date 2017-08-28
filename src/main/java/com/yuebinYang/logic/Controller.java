package com.bittiger.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bittiger.client.ClientEmulator;

public class Controller extends Thread {
	public static final int poolSize = 10;
	List<Server> readQueue = new ArrayList<Server>();
	List<Server> writeQueue = new ArrayList<Server>();
	List<Server> candidateQueue = new ArrayList<Server>();
	int nextReadServer = 0;
	int nextWriteServer = 0;
	Map<Server, Integer> serverCount = new HashMap<Server, Integer>();
	private ClientEmulator c;
	private static transient final Logger LOG = LoggerFactory
			.getLogger(Controller.class);
	private ActionType actionType = null;

	public Controller(ClientEmulator ce) {
		this.c = ce;
		writeQueue.add(new Server(ce.getTpcw().writeQueue[0]));
		for (int i = 0; i < ce.getTpcw().readQueue.length; i++) {
			readQueue.add(new Server(ce.getTpcw().readQueue[i]));
		}
		for (int i = 0; i < ce.getTpcw().candidateQueue.length; i++) {
			candidateQueue.add(new Server(ce.getTpcw().candidateQueue[i]));
		}
	}

	public synchronized Server getNextWriteServer() {
		nextWriteServer = (nextWriteServer + 1) % writeQueue.size();
		return writeQueue.get(nextWriteServer++);
	}

	public synchronized Server getNextReadServer() {
		nextReadServer = (nextReadServer + 1) % readQueue.size();
		Server s = readQueue.get(nextReadServer);
		if (!serverCount.containsKey(s)) {
			serverCount.put(s, 1);
		} else {
			serverCount.put(s, serverCount.get(s) + 1);
		}
		return s;
	}

	public synchronized void returnBack(Server server) {
		serverCount.put(server, serverCount.get(server) - 1);
		notify();
	}

	public int getAction(String perf) {
		String[] tokens = perf.split(",");
		String[] details = tokens[0].split(":");
		if (!details[3].equals("NA")) {
			if (Double.parseDouble(details[3]) > 400) {
				return 1;
			} else if (Double.parseDouble(details[3]) < 200
					&& Double.parseDouble(details[3]) > 0) {
				return -1;
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}

	public synchronized ActionType getActionType() {
		return actionType;
	}

	public synchronized void setActionType(ActionType actionType) {
		this.actionType = actionType;
	}

	public synchronized void addServer(Server server) {
		readQueue.add(server);
	}

	public synchronized Server removeServer() {
		Server server = readQueue.remove(readQueue.size() - 1);
		while (serverCount.get(server) > 0) {
			LOG.info("wait for requests to " + server.getIp() + " to finish");
			try {
				wait();
			} catch (InterruptedException e) {
				LOG.error(e.getMessage());
			}
		}
		candidateQueue.add(server);
		return server;
	}

	public void run() {
		LOG.info("Controller starts......");
		while (true) {
			long currTime = System.currentTimeMillis();
			if (currTime > c.getStartTime() + c.getTpcw().warmup
					+ c.getTpcw().mi + c.getTpcw().warmdown) {
				return;
			}
			try {
				Thread.sleep(c.getTpcw().interval);
			} catch (Exception e) {
				e.printStackTrace();
			}
			String perf = c.getMonitor().readPerformance();
			if (getAction(perf) == 1) {
				if (getActionType() == null) {
					setActionType(ActionType.ScaleOut);
					Executor executor = new Executor(this, ActionType.ScaleOut);
					executor.start();
				} else {
					LOG.info(getActionType().name() + " is in progress");
				}
			} else if (getAction(perf) == -1) {
				if (getActionType() == null) {
					setActionType(ActionType.ScaleIn);
					Executor executor = new Executor(this, ActionType.ScaleIn);
					executor.start();
				} else {
					LOG.info(getActionType().name() + " is in progress");
				}
			} else {
				LOG.info("no action is needed");
			}
		}
	}

}
