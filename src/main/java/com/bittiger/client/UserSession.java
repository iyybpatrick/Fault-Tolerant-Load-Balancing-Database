package com.bittiger.client;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bittiger.logic.Server;
import com.bittiger.querypool.QueryMetaData;

public class UserSession extends Thread {
	private TPCWProperties tpcw = null;
	private ClientEmulator client = null;
	private Random rand = null;
	private boolean suspendThread = false;
	private BlockingQueue<Integer> queue;
	private int id;

	int readQ = 0;

	private static transient final Logger LOG = LoggerFactory
			.getLogger(ClientEmulator.class);

	public UserSession(int id, ClientEmulator client,
			BlockingQueue<Integer> bQueue) {
		super("UserSession" + id);
		this.id = id;
		this.queue = bQueue;
		this.client = client;
		this.tpcw = client.getTpcw();
		this.rand = new Random();
	}

	private long TPCWthinkTime(double mean) {
		double r = rand.nextDouble();
		return ((long) (((0 - mean) * Math.log(r))));
	}

	public synchronized void notifyThread() {
		notify();
	}

	public synchronized void releaseThread() {
		suspendThread = false;
	}

	public synchronized void holdThread() {
		suspendThread = true;
	}

	private String computeNextSql(double rwratio, double[] read, double[] write) {
		String sql = "";
		// first decide read or write
		double rw = rand.nextDouble();
		if (rw < rwratio) {
			sql += "bq";
			double internal = rand.nextDouble();
			int num = 0;
			for (int i = 0; i < read.length - 1; i++) {
				if (read[i] < internal && internal <= read[i + 1]) {
					num = i + 1;
					sql += num;
					break;
				}
			}

		} else {
			sql += "wq";
			double internal = rand.nextDouble();
			int num = 0;
			for (int i = 0; i < write.length - 1; i++) {
				if (write[i] < internal && internal <= write[i + 1]) {
					num = i + 1;
					sql += num;
					break;
				}
			}
		}
		return sql;
	}

	private Server computeNextServer(String sql) {
		if (sql.contains("b")) // read
		{
			return client.getController().getNextReadServer();
		} else {
			return client.getController().getNextWriteServer();
		}
	}

	public void run() {
		try {
			synchronized (this) {
				while (suspendThread)
					wait();
			}
		} catch (InterruptedException e) {
			LOG.error("Error while running session: " + e.getMessage());
		}

		while (!client.isEndOfSimulation() && !suspendThread) {
			try {
				// decide of closed or open system
				double r = rand.nextDouble();
				if (r < tpcw.mixRate) {
					int t = queue.take();
					LOG.debug(t + " has been taken");
				} else {
					Thread.sleep((long) ((float) TPCWthinkTime(tpcw.TPCmean)));
				}

				String queryclass = computeNextSql(tpcw.rwratio,
						tpcw.read, tpcw.write);
				Server server = computeNextServer(queryclass);
				String classname = "com.bittiger.querypool." + queryclass;
				QueryMetaData query = (QueryMetaData) Class.forName(classname)
						.newInstance();
				String command = query.getQueryStr();
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				// DriverManager.setLoginTimeout(5);
				Connection con = DriverManager.getConnection(
						Utilities.getUrl(server), tpcw.username,
						tpcw.password);
				con.setAutoCommit(true);
				Statement stmt = con.createStatement();
				if (queryclass.contains("b")) {
					long start = System.currentTimeMillis();
					ResultSet rs = stmt.executeQuery(command);
					long end = System.currentTimeMillis();
					client.getMonitor().addQuery(this.id, queryclass, start,
							end);
					rs.close();
					client.getController().returnBack(server);
				} else {
					long start = System.currentTimeMillis();
					stmt.executeUpdate(command);
					long end = System.currentTimeMillis();
					client.getMonitor().addQuery(this.id, queryclass, start,
							end);
				}
				stmt.close();
				con.close();
			} catch (Exception ex) {
				LOG.error("Error while running session: " + ex.getMessage());
			}
		}
		client.increaseThread();
	}
}
