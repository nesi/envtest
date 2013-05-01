package nz.org.nesi.envtester

import java.util.concurrent.TimeUnit

import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtilities
import org.jfree.chart.JFreeChart
import org.jfree.data.time.FixedMillisecond
import org.jfree.data.time.TimeSeries
import org.jfree.data.time.TimeSeriesCollection

import com.google.common.base.Stopwatch
import com.google.common.io.Resources
import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.jcraft.jsch.SftpProgressMonitor

class SftpTest {

	class LogSftpProgressMonitor implements  SftpProgressMonitor {

		private double count
		private double max
		private String src
		private int percent
		private int lastDisplayedPercent

		//		private timestamps = [:]

		private final TimeSeriesCollection progressDataset = new TimeSeriesCollection();
		private final TimeSeries dataSeries = new TimeSeries("Data transferred");
		private final TimeSeries percentSeries = new TimeSeries("Percent finished");

		public LogSftpProgressMonitor() {
			count = 0
			max = 0
			percent = 0
			lastDisplayedPercent = 0
		}

		@Override
		public void init(int op, String src, String dest, long max) {
			this.max = max
			this.src = src
			count = 0
			percent = 0
			lastDisplayedPercent = 0
			status()
		}

		@Override
		public boolean count(long count) {
			this.count += count
			percent = (int) ((this.count / max) * 100.0)
			status()
			return true
		}

		@Override
		public void end() {
			percent = (int) ((count / max) * 100.0)
			status()
			progressDataset.addSeries(dataSeries)


			final JFreeChart chart = ChartFactory.createTimeSeriesChart("Data transferred",
				"date", // x-axis label
				"MB transferred", // y-axis label
				progressDataset, // data
				true, // create legend?
				true, // generate tooltips?
				false // generate URLs?
				);

			ChartUtilities.saveChartAsPNG(new File('/home/markus/Desktop/chart.png'), chart, 1024, 800);
			
			progressDataset.removeAllSeries()
			progressDataset.addSeries(percentSeries)
			chart = ChartFactory.createTimeSeriesChart("Data transferred",
					"date", // x-axis label
					"Percent finished", // y-axis label
					progressDataset, // data
					true, // create legend?
					true, // generate tooltips?
					false // generate URLs?
					);
			
			ChartUtilities.saveChartAsPNG(new File('/home/markus/Desktop/percent.png'), chart, 1024, 800);
		}

		private void status() {
			if (lastDisplayedPercent <= percent - 1) {

				FixedMillisecond ms = new FixedMillisecond()

				dataSeries.addOrUpdate(ms, count / 1048576);
				percentSeries.addOrUpdate(ms, percent);

				//				System.out.println(src + ": " + percent + "% " + ((long) count) + "/" + ((long) max))
				lastDisplayedPercent = percent
			}
		}

	}

	public upload(char[] pw, File file) {
		String SFTPHOST = "pan.nesi.org.nz"
		int SFTPPORT = 22
		String SFTPUSER = "mbin029"
		String SFTPWORKINGDIR = "temp"

		Session session = null
		Channel channel = null
		ChannelSftp channelSftp = null
		System.out.println("preparing the host information for sftp.")
		try {
			JSch jsch = new JSch()

			byte[] key = Resources.toByteArray(Resources.getResource(SftpTest.class, '/envtest'))
			byte[] cert = Resources.toByteArray(Resources.getResource(SftpTest.class, '/envtest.pub'))
			
			jsch.addIdentity('anonymous', key, cert, null);

			session = jsch.getSession(SFTPUSER, SFTPHOST, SFTPPORT)

			java.util.Properties config = new java.util.Properties()
			config.put("StrictHostKeyChecking", "no")
			session.setConfig(config)
			session.connect()
			System.out.println("Host connected.")
			channel = session.openChannel("sftp")
			channel.connect()
			System.out.println("sftp channel opened and connected.")
			channelSftp = (ChannelSftp) channel
			channelSftp.cd(SFTPWORKINGDIR)
			channelSftp.put(file.getAbsolutePath(), file.getName(), new LogSftpProgressMonitor())
			println("File transfered successfully to host.")
		} catch (Exception ex) {
			System.out.println("Exception found while tranfer the response.")
			ex.printStackTrace()
		}
		finally{
			channelSftp.exit()
			System.out.println("sftp Channel exited.")
			channel.disconnect()
			System.out.println("Channel disconnected.")
			session.disconnect()
			System.out.println("Host Session disconnected.")
		}
	}

	static double getSpeed(long size_in_bytes, long time_in_ms) {

		long size_in_mb = size_in_bytes / 1048576

		double result = size_in_mb / (time_in_ms / 1000)

		return result
	}

	static main(args) {

		Stopwatch stopwatch = new Stopwatch()

		SftpTest tester = new SftpTest()

		File file = new File(args[1])

		long size_in_bytes = file.length()

		stopwatch.start()
		tester.upload(args[0].toCharArray(), file)
		stopwatch.stop()

		double speed = getSpeed(size_in_bytes, stopwatch.elapsedTime(TimeUnit.MILLISECONDS))

		println ("Speed: "+speed+" MB/s")

	}


}
