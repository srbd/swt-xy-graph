import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.csstudio.swt.widgets.datadefinition.ColorMap;
import org.csstudio.swt.widgets.datadefinition.ColorMap.PredefinedColorMap;
import org.csstudio.swt.widgets.figures.IntensityGraphFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A live updated Intensity Graph example
 * @author Xihui Chen
 *
 */
public class IntensityGraphExample {
	private static int count = 0;
	private static final int DataHeight = 1024;
	private static final int DataWidth = 1280;

	public static void main(String[] args) {
		final Shell shell = new Shell();
		shell.setSize(300, 250);
		shell.open();

		// use LightweightSystem to create the bridge between SWT and draw2D
		final LightweightSystem lws = new LightweightSystem(shell);

		//Create Intensity Graph
		final IntensityGraphFigure intensityGraph = new IntensityGraphFigure();
		
		//Create Simulation Data
		final short[] simuData = new short[DataWidth * DataHeight * 2];
		final short[] data = new short[DataWidth * DataHeight];
		int seed = count++;
		for (int i = 0; i < DataHeight; i++) {
			for (int j = 0; j < DataWidth; j++) {
				int x = j - DataWidth;
				int y = i - DataHeight;
				int p = (int) Math.sqrt(x * x + y * y);
				simuData[i * DataWidth + j] = (short) (Math.sin(p * 2 * Math.PI
						/ DataWidth + seed * Math.PI / 100) * 100);
			}
		}

		//Configure
		intensityGraph.setMax(100);
		intensityGraph.setMin(-100);
		intensityGraph.setDataHeight(DataHeight);
		intensityGraph.setDataWidth(DataWidth);
		intensityGraph.setColorMap(new ColorMap(PredefinedColorMap.JET, true,true));

		lws.setContents(intensityGraph);

		// Update the graph in another thread.
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
				new Runnable() {

					@Override
					public void run() {
						System.arraycopy(simuData, count % DataWidth, data, 0,
								DataWidth * DataHeight);

						Display.getDefault().asyncExec(new Runnable() {

							public void run() {
								count++;
								intensityGraph.setDataArray(data);
							}
						});
					}
				}, 100, 10, TimeUnit.MILLISECONDS);

		Display display = Display.getDefault();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		future.cancel(true);
		scheduler.shutdown();

	}
}