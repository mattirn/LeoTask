package org.leores.mapreduce.demo;

import java.io.FileNotFoundException;
import java.util.List;

import org.leores.mapreduce.util.DelimitedReader;
import org.leores.mapreduce.util.Logger;
import org.leores.mapreduce.util.U;
import org.leores.mapreduce.util.able.Processable1;
import org.leores.mapreduce.util.data.DataTable;

public class DelimitedReaderDemo extends Demo {
	String sFile = "dreader.csv";

	public void delimitedReader() {
		String[] rowToStart1 = { "data", "start1" };
		String[] columnsToRead = { "c4", "c1" };
		String[] rowToEnd = { "data", "end" };
		String[] rowToStart2 = { "data", "start2" };
		String[] rowToStart = { "data", "start\\d" };//Regex matching 
		String[] row;
		try {
			DelimitedReader dr;

			log("Read from file directly:");
			dr = new DelimitedReader(sFile);
			while ((row = dr.readValidRow()) != null) {
				log(row);
			}
			dr.close();

			dr = new DelimitedReader(sFile);
			log("Read from file directly but stops at: " + U.toStr(rowToEnd));
			dr.prep(null, null, rowToEnd);
			while ((row = dr.readValidRow()) != null) {
				log(row);
			}
			log("Continue reading from : " + U.toStr(rowToStart1) + " and stops at: " + U.toStr(rowToEnd));
			dr.prep(rowToStart1, null, rowToEnd);
			while ((row = dr.readValidRow()) != null) {
				log(row);
			}
			log("Continue reading from " + U.toStr(rowToStart2) + " and only read some columns (in a differet order c4 before c1) and stops at: " + U.toStr(rowToEnd));
			dr.prep(rowToStart2, columnsToRead, rowToEnd);
			while ((row = dr.readValidRow()) != null) {
				log(row);
			}
			dr.close();

			log("Read using a loop & regex matching for multiple blocks");
			dr = new DelimitedReader(sFile);
			while (dr.prep(rowToStart, columnsToRead, rowToEnd)) {
				while ((row = dr.readValidRow()) != null) {
					log(row);
				}
				log("----------");
			}
			dr.close();

			log("Read columns with same rowToStart and rowToEnd");
			dr = new DelimitedReader(sFile);
			while (dr.prep(rowToStart, columnsToRead, rowToStart)) {
				while ((row = dr.readValidRow()) != null) {
					log(row);
				}
				log("----------");
			}
			dr.close();

			log("Read from null rowToStart:");
			dr = new DelimitedReader(sFile);
			dr.prep(null, columnsToRead, rowToEnd);
			while ((row = dr.readValidRow()) != null) {
				log(row);
			}
			dr.close();
		} catch (FileNotFoundException e) {
			log(e);
		}

		log("To avoid try and catch:");
		DelimitedReader dr = (DelimitedReader) U.newInstance(DelimitedReader.class, sFile);
		if (dr != null) {
			while ((row = dr.readValidRow()) != null) {
				log(row);
			}
			dr.close();
		}

		return;
	}

	protected void _readRowPatterns(String[] rowPattern, String[] rowToStart, String[] columnsToRead, String[] rowToEnd) {
		DelimitedReader dr;
		String[] row;
		List<String[]> lrow;
		List<String> lrow1;

		log("readValidRow:");
		dr = (DelimitedReader) U.newInstance(DelimitedReader.class, sFile);
		while (dr.prep(rowToStart, columnsToRead, rowToEnd)) {
			dr.setValidRowPattern(rowPattern);
			while ((row = dr.readValidRow()) != null) {
				log(row);
			}
			log("----------");
		}
		dr.close();
		log("readValidRow(true):");
		dr = (DelimitedReader) U.newInstance(DelimitedReader.class, sFile);
		while (dr.prep(rowToStart, columnsToRead, rowToEnd)) {
			dr.setValidRowPattern(rowPattern);
			while ((row = dr.readValidRow(true)) != null) {
				log(row);
			}
			log("----------");
		}
		dr.close();
		log("readValidRows multiple rows read (block by block):");
		dr = (DelimitedReader) U.newInstance(DelimitedReader.class, sFile);
		Processable1 pa1 = new Processable1.ArrayToString();
		while (dr.prep(rowToStart, columnsToRead, rowToEnd)) {
			dr.setValidRowPattern(rowPattern);
			while ((lrow = dr.readValidRows(true)) != null) {
				log(U.toStr(lrow, pa1));
			}
			log("----------");
		}
		dr.close();
		log("readValidRows All:");
		dr = (DelimitedReader) U.newInstance(DelimitedReader.class, sFile);
		while (dr.prep(rowToStart, columnsToRead, rowToEnd)) {
			dr.setValidRowPattern(rowPattern);
			while ((lrow = dr.readValidRows(false)) != null) {
				log(U.toStr(lrow, pa1));
			}
			log("----------");
		}
		dr.close();
		log("readValidRows All (without setting pattern):");
		dr = (DelimitedReader) U.newInstance(DelimitedReader.class, sFile);
		while (dr.prep(rowToStart, columnsToRead, rowToEnd)) {
			//dr.setValidRowPattern(rowPattern);
			while ((lrow = dr.readValidRows(false)) != null) {
				log(U.toStr(lrow, pa1));
			}
			log("----------");
		}
		dr.close();
		log("readValidRows All a single column and supress all log levels below LOG_ERROR:");
		dr = (DelimitedReader) U.newInstance(DelimitedReader.class, sFile);
		dr.setLogOutputLevel(Logger.LOG_ERROR);
		while (dr.prep(rowToStart, columnsToRead, rowToEnd)) {
			dr.setValidRowPattern(rowPattern);
			while ((lrow1 = dr.readValidRows(false, 0)) != null) {
				log(lrow1);
			}
			log("----------");
		}
		dr.close();
	}

	public void readRowPatterns() {
		String[] rowToStartEnd = { "data", "start\\d" };
		String[] columnsToRead = { "c4", "c1" };
		String[] rowPatterns = { null, "1" };

		log("Use null for rowToStart and rowToEnd:");
		_readRowPatterns(rowPatterns, null, columnsToRead, null);

		log("\n\n");
		log("Use rowToStart: " + U.toStr(rowToStartEnd) + " and rowToEnd: " + U.toStr(rowToStartEnd));
		_readRowPatterns(rowPatterns, rowToStartEnd, columnsToRead, rowToStartEnd);
	}

	public void readRelatedData() {
		log("Read from related data:");
		try {
			DelimitedReader dr = new DelimitedReader(sFile);
			dr.prep(null, new String[] { "id", "name" });
			dr.setValidRowPattern(new String[] { null, "Emily" });
			String[] row = dr.readValidRow();
			String id = row[0];
			dr.prep(null, new String[] { "id", "date", "mood", "blood pressure", "heart rate" });
			dr.setValidRowPattern(new String[] { id });
			DataTable dt = dr.readValidDataTable();

			log(dt.getColNames());
			for (int i = 0, mi = dt.nRows(); i < mi; i++) {
				log(dt.getRow(i));
			}
		} catch (FileNotFoundException e) {
			log(e);
		}
	}

	public static void demo() {
		DelimitedReaderDemo drd = new DelimitedReaderDemo();
		drd.delimitedReader();
		drd.readRowPatterns();
		drd.readRelatedData();
	}

}
