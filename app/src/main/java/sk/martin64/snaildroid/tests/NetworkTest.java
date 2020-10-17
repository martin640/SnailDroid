package sk.martin64.snaildroid.tests;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.URL;

import sk.martin64.snaildroid.tests.adapters.DefaultLongGraphAdapterImpl;
import sk.martin64.snaildroid.view.MeasureGraphView;
import sk.martin64.snaildroid.view.Utils;

public class NetworkTest implements TestBase {

    private long started = 0;
    private long dlStart;
    private DummyOutputStream baos;
    private DefaultLongGraphAdapterImpl adapter = new DefaultLongGraphAdapterImpl();

    public static final String SOURCE = "https://file-examples-com.github.io/uploads/2017/04/file_example_MP4_1920_18MG.mp4";

    @Override
    public int run() {
        started = System.currentTimeMillis();
        baos = new DummyOutputStream();

        while (!Thread.interrupted()) {
            try (InputStream is = new URL(SOURCE).openStream()) {
                byte[] buffer = new byte[2048];
                int read;

                dlStart = System.nanoTime();
                baos.reset();
                while ((read = is.read(buffer)) > 0) {
                    if (Thread.interrupted()) { // caller wants test to stop
                        return CODE_OK;
                    }
                    baos.write(buffer, 0, read);
                }
            } catch (InterruptedIOException e) {
                return CODE_OK;
            } catch (IOException e) {
                e.printStackTrace();
                return CODE_EXCEPTION;
            }
        }

        return CODE_OK;
    }

    @Override
    public String getSpeed(int unit, int x) {
        long s = baos.getLength();
        long est = System.nanoTime() - dlStart;
        double estSec = est / 1000000000d;
        long speed = estSec > 0 ? (long) (s / estSec) : 0;

        adapter.addPoint(x, speed);

        if (unit == UNIT_BYTE)
            return String.format("%s/s", Utils.humanReadableByteCountSI(speed, 2));
        else if (unit == UNIT_BIT)
            return String.format("%s/s", Utils.humanReadableBitsCount(speed, 2));
        else return null;
    }

    @Override
    public MeasureGraphView.GraphAdapter<?> getGraphAdapter() {
        return adapter;
    }

    @Override
    public long getTimeStarted() {
        return started;
    }

    @Override
    public long getDataUsed() {
        return baos.getLengthTotal();
    }

    @Override
    public String getName() {
        return "Network speed";
    }
}