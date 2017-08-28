package com.bittiger.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Executor extends Thread {

	Controller controller = null;
	ActionType actionType = null;

	public Executor(Controller controller, ActionType actionType) {
		this.controller = controller;
		this.actionType = actionType;
	}

	private static transient final Logger LOG = LoggerFactory
			.getLogger(Executor.class);

	@Override
	public void run() {
		try {
			if (this.actionType == ActionType.ScaleOut) {
				LOG.info("Scale out request received");
				if (controller.candidateQueue.size() == 0) {
					LOG.info("CandidateQueue size is 0, skip scale out");
				} else {
					Server target = controller.candidateQueue.remove(0);
					Server source = controller.readQueue
							.get(controller.readQueue.size() - 1);
					Server master = controller.writeQueue.get(0);
					scaleOut(source.getIp(), target.getIp(), master.getIp());
					controller.addServer(target);
					LOG.info("kick in " + target.getIp() + " done ");
				}
				LOG.info("Scale out request done");
				this.controller.setActionType(null);
			} else {
				LOG.info("Scale in request received");
				if (controller.readQueue.size() == 2) {
					LOG.info("Read queue size is 2, skip scale in");
				} else {
					Server server = controller.removeServer();
					LOG.info("Kick out server" + server.getIp());
					scaleIn(server.getIp());
				}
				LOG.info("Scale in request done");
				this.controller.setActionType(null);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}

	}

	public boolean scaleOut(String source, String target, String master)
			throws InterruptedException, IOException {
		// ssh root@source
		// "/home/ubuntu/elasticDB/scripts/scaleOut.sh source target master"
		// sb.append("ssh root@" + source
		// +" \"/home/ubuntu/elasticDB/scripts/scaleOut.sh " + source +" " +
		// target +" " + master +"\"");
		// LOG.info(sb.toString());
		ProcessBuilder pb = new ProcessBuilder("/bin/bash",
				"script/callScaleOut.sh", source, target, master);
		Process p = pb.start();
		LOG.info("Kick in " + target + " from " + source);
		BufferedReader is = new BufferedReader(new InputStreamReader(
				p.getInputStream()));
		String line;
		while ((line = is.readLine()) != null)
			LOG.info(line);
		p.waitFor();
		return true;
	}

	public boolean scaleIn(String target) throws InterruptedException,
			IOException {
		ProcessBuilder pb = new ProcessBuilder("/bin/bash",
				"script/callScaleIn.sh", target);
		Process p = pb.start();
		LOG.info("Kick out " + target);
		BufferedReader is = new BufferedReader(new InputStreamReader(
				p.getInputStream()));
		String line;
		while ((line = is.readLine()) != null)
			LOG.info(line);
		p.waitFor();
		return true;
	}
}
