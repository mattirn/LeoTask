package org.leores.mapreduce.demo;

import java.util.ArrayList;
import java.util.List;

import org.leores.mapreduce.math.rand.Binomial;
import org.leores.mapreduce.math.rand.RandomEngine;
import org.leores.mapreduce.net.Node;
import org.leores.mapreduce.plot.JGnuplot;
import org.leores.mapreduce.plot.JGnuplot.Plot;
import org.leores.mapreduce.task.Tasks;
import org.leores.mapreduce.task.app.EpiNode;
import org.leores.mapreduce.task.app.Epidemic;
import org.leores.mapreduce.util.DelimitedReader;
import org.leores.mapreduce.util.RandomUtil;
import org.leores.mapreduce.util.Timer;
import org.leores.mapreduce.util.U;
import org.leores.mapreduce.util.able.NewInstanceable;
import org.leores.mapreduce.util.data.DataTable;
import org.leores.mapreduce.util.data.DataTableSet;
import org.leores.mapreduce.util.data.Statistic;
import org.leores.mapreduce.util.data.Statistics;

public class EpidemicDemo extends Epidemic {
	private static final long serialVersionUID = 2186149327686010965L;

	public void compareNaiveAdvEst() {
		Tasks tasks = new Tasks(true);
		tasks.sFLoad = "tasks-epidemic-compareNaiveAdvEst.xml";
		tasks.start();

		Epidemic epiAdvNoBn = new Epidemic();
		epiAdvNoBn.sMethods = "msSpreadNets";
		epiAdvNoBn.bnp = -1f;

		Epidemic epiNaiveNoBn = new Epidemic();
		epiNaiveNoBn.sMethods = "msSpreadNetsNaive";
		epiNaiveNoBn.bnp = -1f;

		Epidemic epiAdvBn = new Epidemic();
		epiAdvBn.sMethods = "msSpreadNets";
		epiAdvBn.bnp = 0.1f;

		Epidemic epiNaiveBn = new Epidemic();
		epiNaiveBn.sMethods = "msSpreadNetsNaive";
		epiNaiveBn.bnp = 0.1f;

		Epidemic epiEst = new Epidemic();
		epiEst.sMethods = "msSpreadNetsEst";

		Statistics stats = tasks.getStatistics();
		List<Statistic> lStat = stats.find(".*test.*");
		for (int i = 0, mi = lStat.size(); i < mi; i++) {
			Statistic stat = lStat.get(i);
			DataTable dtAdvNoBn = stat.getDataTable(null, epiAdvNoBn, 2);
			dtAdvNoBn.info += " AdvNoBn";
			DataTable dtNaiveNoBn = stat.getDataTable(null, epiNaiveNoBn, 2);
			dtNaiveNoBn.info += " NaiveNoBn";
			DataTable dtAdvBn = stat.getDataTable(null, epiAdvBn, 2);
			dtAdvBn.info += " AdvBn";
			DataTable dtNaiveBn = stat.getDataTable(null, epiNaiveBn, 2);
			dtNaiveBn.info += " NaiveBn";
			DataTable dtEst = stat.getDataTable(null, epiEst, 2);
			dtEst.info += " Est";
			Plot plot = new Plot(stat.info);
			plot.addNewDataTableSet(stat.info).add(dtAdvNoBn, dtNaiveNoBn, dtAdvBn, dtNaiveBn, dtEst);
			JGnuplot jg = new JGnuplot();
			jg.execute(plot);
		}
	}

	public void example() {
		Tasks tasks = new Tasks(true);
		tasks.sFLoad = "tasks-epidemic.xml";
		tasks.start();
	}

	public static void demo() {
		EpidemicDemo demo = new EpidemicDemo();
		demo.compareNaiveAdvEst();
		demo.example();
	}
}
