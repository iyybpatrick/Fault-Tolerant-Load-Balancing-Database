package com.bittiger.client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bittiger.logic.Controller;
import com.bittiger.logic.Monitor;

public class ClientEmulator{

	private TPCWProperties tpcw = null;
	private int numOfRunningThreads = 0;
	private boolean endOfSimulation = false;
	private Monitor monitor;
	private Controller controller;
	OpenSystemTicketProducer producer;
	private long startTime;

	private static transient final Logger LOG = LoggerFactory
			.getLogger(ClientEmulator.class);

	public ClientEmulator() throws IOException, InterruptedException {
		super();
		tpcw = new TPCWProperties("tpcw");
	}

	public synchronized void increaseThread() {
		numOfRunningThreads++;
	}

	private synchronized void setEndOfSimulation() {
		endOfSimulation = true;
		LOG.info("Trigger ClientEmulator.isEndOfSimulation()= "
				+ this.isEndOfSimulation());

	}

	public synchronized boolean isEndOfSimulation() {
		return endOfSimulation;
	}

	public void start() {
		long warmup = tpcw.warmup;
		long mi = tpcw.mi;
		long warmdown = tpcw.warmdown;
		this.startTime = System.currentTimeMillis();
		this.monitor = new Monitor(this);
		this.monitor.init();
		
		int maxNumSessions = 0;
		int workloads[] = tpcw.workloads;
		for (int i = 0; i < workloads.length; i++) {
			if (workloads[i] > maxNumSessions) {
				maxNumSessions = workloads[i];
			}
		}
		LOG.info("The maximum is : " + maxNumSessions);
		BlockingQueue<Integer> bQueue = new LinkedBlockingQueue<Integer>();
		UserSession[] sessions = new UserSession[maxNumSessions];
		for (int i = 0; i < maxNumSessions; i++) {
			sessions[i] = new UserSession(i, this, bQueue);
			sessions[i].holdThread();
			sessions[i].start();
		}

		int currNumSessions = 0;
		int currWLInx = 0;
		int diffWL = 0;

		long endTime = startTime + warmup + mi + warmdown;
		long currTime;
		
		
		if (tpcw.mixRate > 0) {
			producer = new OpenSystemTicketProducer(
					this, bQueue);
			producer.start();
		}
		this.controller = new Controller(this);
		this.controller.start();

		LOG.info("Client starts......");
		while (true) {
			currTime = System.currentTimeMillis();
			if (currTime >= endTime) {
				break;
			}
			diffWL = workloads[currWLInx] - currNumSessions;
			LOG.info("Workload......" + workloads[currWLInx]);
			if (diffWL > 0) {
				for (int i = currNumSessions; i < (currNumSessions + diffWL); i++) {
					sessions[i].releaseThread();
					sessions[i].notifyThread();
				}
			} else if (diffWL < 0) {
				for (int i = (currNumSessions - 1); i > workloads[currWLInx]; i--) {
					sessions[i].holdThread();
				}
			}
			try {
				LOG.info("Client emulator sleep......" + tpcw.interval);
				Thread.sleep(tpcw.interval);
			} catch (InterruptedException ie) {
				LOG.error("ERROR:InterruptedException" + ie.toString());
			}
			currNumSessions = workloads[currWLInx];
			currWLInx = ((currWLInx + 1) % workloads.length);
		}
		setEndOfSimulation();
		for (int i = 0; i < maxNumSessions; i++) {
			sessions[i].releaseThread();
			sessions[i].notifyThread();
		}
		LOG.info("Client: Shutting down threads ...");

		for (int i = 0; i < maxNumSessions; i++) {
			try {
				LOG.info("UserSession " + i + " joins.");
				sessions[i].join();
			} catch (java.lang.InterruptedException ie) {
				LOG.error("ClientEmulator: Thread " + i
						+ " has been interrupted.");
			}
		}

		if (tpcw.mixRate > 0) {
			try {
				producer.join();
				LOG.info("Producer joins");
			} catch (java.lang.InterruptedException ie) {
				LOG.error("Producer has been interrupted.");
			}
		}
		try {
			controller.join();
		} catch (java.lang.InterruptedException ie) {
			LOG.error("Controller has been interrupted.");
		}
		this.monitor.close();
		LOG.info("Done\n");
		Runtime.getRuntime().exit(0);
	}

	public Monitor getMonitor() {
		return monitor;
	}

	public void setMonitor(Monitor monitor) {
		this.monitor = monitor;
	}

	public Controller getController() {
		return controller;
	}

	public void setController(Controller controller) {
		this.controller = controller;
	}

	public TPCWProperties getTpcw() {
		return tpcw;
	}

	public void setTpcw(TPCWProperties tpcw) {
		this.tpcw = tpcw;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		ClientEmulator client = new ClientEmulator();
		client.start();
	}

}
