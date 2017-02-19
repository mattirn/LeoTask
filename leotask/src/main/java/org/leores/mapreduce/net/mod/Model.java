package org.leores.mapreduce.net.mod;

import org.leores.mapreduce.math.rand.RandomEngine;
import org.leores.mapreduce.net.Link;
import org.leores.mapreduce.net.Network;
import org.leores.mapreduce.net.Networks;
import org.leores.mapreduce.net.Node;
import org.leores.mapreduce.util.Logger;
import org.leores.mapreduce.util.able.NewInstanceable;

public abstract class Model extends Logger {
	public RandomEngine rand;
	public NewInstanceable<Node> nIaNode;
	public NewInstanceable<Link> nIaLink;
	public Integer nNode;

	public void initialize(RandomEngine rand, Integer nNode, NewInstanceable<Node> nIaNode, NewInstanceable<Link> nIaLink) {
		if (rand != null) {
			this.rand = rand;
		} else {
			this.rand = RandomEngine.makeDefault();
		}
		this.nNode = nNode;
		this.nIaNode = nIaNode;
		this.nIaLink = nIaLink;
	}

	public void initialize(RandomEngine rand, Integer nNode) {
		initialize(rand, nNode, null, null);
	}

	public Network newNetwork() {
		return new Network(null, nNode, nNode, null, nIaNode, nIaLink);
	}

	public Networks newNetworks(Integer nNet) {
		return new Networks(nNet, nIaNode, nIaLink);
	}

	public abstract Network genNetwork(Network net);

	public abstract Networks genNetworks(Networks nets);
}
