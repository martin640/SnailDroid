package sk.martin64.snaildroid.tests;

import sk.martin64.snaildroid.view.MeasureGraphView;

public interface TestBase {

    // return codes for run() method
    int CODE_OK = 0;
    int CODE_BAD_REQUEST = -1;
    int CODE_EXCEPTION = -2;

    int UNIT_BIT = 1;
    int UNIT_BYTE = 2;

    /**
     * Method is started from background thread and should return to allow another test to run
     * @return exit status code
     */
    int run();

    /**
     * @param unit preferred speed unit provided by user. In case of custom units (e.g. Operations/s) this variable can be ignored.
     * @return String representation of process speed
     */
    String getSpeed(int unit, int x);

    MeasureGraphView.GraphAdapter<?> getGraphAdapter();

    /**
     * @return Time in milliseconds when test has started. Timestamp should be initialized in {@link #run(), never in constructor}.
     */
    long getTimeStarted();

    /**
     * @return Total bytes used by test or 0 if test isn't related to data testing.
     */
    long getDataUsed();

    /**
     * @return Test name that should be displayed in app
     */
    String getName();

    /**
     * @return Result statistics for test. If null, default "Data processed: {@link #getDataUsed()}" is used.
     */
    default CharSequence getResultData() { return null; }
}