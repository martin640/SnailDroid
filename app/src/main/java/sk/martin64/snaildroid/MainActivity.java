package sk.martin64.snaildroid;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.slider.Slider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import butterknife.BindView;
import butterknife.ButterKnife;
import sk.martin64.snaildroid.tests.ArrayAllocationTest;
import sk.martin64.snaildroid.tests.FrameRateTest;
import sk.martin64.snaildroid.tests.NetworkTest;
import sk.martin64.snaildroid.tests.StorageReadTest;
import sk.martin64.snaildroid.tests.TestBase;
import sk.martin64.snaildroid.tests.ThreadsSpawningTest;
import sk.martin64.snaildroid.view.MeasureGraphView;
import sk.martin64.snaildroid.view.Utils;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.graph1)
    MeasureGraphView graph1;
    @BindView(R.id.speed_text)
    TextView speedText;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.appbar)
    AppBarLayout appbar;
    @BindView(R.id.button)
    AppCompatButton button;
    @BindView(R.id.spinner)
    Spinner spinner;
    @BindView(R.id.spinner2)
    Spinner spinner2;
    @BindView(R.id.textView2)
    TextView status;
    @BindView(R.id.seekBar)
    Slider seekBar;
    @BindView(R.id.dummyFrame)
    FrameLayout dummyFrame;

    private File dummyFile;
    private TestBase activeTest = null;
    private MeasureGraphView.GraphAdapter<?> adapter;
    private ExecutorService watcher, executor;
    private Future<Integer> pendingTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setSupportActionBar(toolbar);
        Utils.applyStatusBarPadding(appbar);

        dummyFile = new File(getFilesDir(), "raw.bin");
        if (!dummyFile.exists()) {
            int toWrite = 800 * 1024 * 1024;

            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMax(toWrite); // 800 MB
            progressDialog.setTitle("Preparing application");
            progressDialog.setMessage("Generating 800 MB file for testing. This file will be removed when you uninstall app.");
            progressDialog.setCancelable(false);
            progressDialog.show();

            Executors.newSingleThreadExecutor().submit(() -> {
                try (FileOutputStream fos = new FileOutputStream(dummyFile)) {
                    byte[] buffer = new byte[2048];
                    Arrays.fill(buffer, Byte.MAX_VALUE);
                    long read = 0, lastUpdate = 0;

                    while (read < toWrite) {
                        fos.write(buffer);
                        read += buffer.length;
                        int finalRead = (int) read;
                        if ((read - lastUpdate) > 1024 * 1024) {
                            runOnUiThread(() -> progressDialog.setProgress(finalRead));
                            lastUpdate = read;
                        }
                    }

                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Dummy file has been created", Toast.LENGTH_SHORT).show();
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        progressDialog.dismiss();

                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Error occurred");
                        builder.setMessage("Failed to create dummy testing file because exception was thrown: " + e.toString());
                        builder.setCancelable(false);
                        builder.setPositiveButton("Close", (dialog, which) -> finish());
                        builder.show();
                    });
                }
            });
        }

        graph1.post(() -> {
            graph1.setGraphColor(0xFFF6C510);
            graph1.setShader(new LinearGradient(0, 0, 0, graph1.getMeasuredHeight(),
                    Color.RED, Color.YELLOW, Shader.TileMode.MIRROR));
        });

        SharedPreferences savedState = getSharedPreferences("abc", MODE_PRIVATE);

        ArrayAdapter<String> aa = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{
                        "Network speed",
                        "Internal storage read speed",
                        "Empty array allocation speed",
                        "Filled array allocation speed",
                        "Single array fill speed",
                        "Thread spawning speed",
                        "FPS test",
                });
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(aa);
        spinner.setSelection(savedState.getInt("a", 0));

        aa = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{
                        "Bits", "Bytes"
                });
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(aa);
        spinner2.setSelection(savedState.getInt("b", 0));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                savedState.edit()
                        .putInt("a", position)
                        .apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                savedState.edit()
                        .putInt("b", position)
                        .apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        seekBar.setLabelFormatter(value -> (int) (5 + value * 55) + " s");
        seekBar.setValue(savedState.getFloat("c", 0));
        seekBar.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) savedState.edit()
                    .putFloat("c", value)
                    .apply();
        });

        setupButton();
    }

    private void setupButton() {
        button.setOnClickListener(view -> {
            int t = spinner.getSelectedItemPosition();
            long time = (long) (5000 + seekBar.getValue() * 35000);
            switch (t) {
                case 0:
                    runTest(new NetworkTest(), time);
                    break;
                case 1:
                    runTest(new StorageReadTest(dummyFile), time);
                    break;
                case 2:
                    runTest(new ArrayAllocationTest(1), time);
                    break;
                case 3:
                    runTest(new ArrayAllocationTest(2), time);
                    break;
                case 4:
                    runTest(new ArrayAllocationTest(3), time);
                    break;
                case 5:
                    runTest(new ThreadsSpawningTest(), time);
                    break;
                case 6:
                    runTest(new FrameRateTest(dummyFrame), time);
                    break;
            }
        });
    }

    private void enableButtons(boolean b) {
        spinner.setEnabled(b);
        spinner2.setEnabled(b);
        seekBar.setEnabled(b);

        if (b) {
            button.setText("Run");
            setupButton();
        } else {
            button.setText("Stop");
            button.setOnClickListener((v) -> {
                watcher.shutdown();
                watcher.shutdownNow();
            });
        }
    }

    private void runTest(TestBase test, long time) {
        enableButtons(false);
        activeTest = null;

        status.setText("Running test...");
        status.setTextColor(0xFF02B90A);

        long step = time / graph1.getMeasuredWidth();

        adapter = test.getGraphAdapter();
        graph1.setAdapter(adapter);

        watcher = Executors.newSingleThreadExecutor();
        watcher.submit(() -> {
            for (int i = 0; i < graph1.getMeasuredWidth(); i++) {
                if (Thread.interrupted()) break;

                if (activeTest == null || test.getTimeStarted() == 0) {
                    adapter.addZeroPoint(i);
                    graph1.postInvalidate();
                } else {
                    String speed = test.getSpeed(spinner2.getSelectedItemPosition() == 0 ?
                            TestBase.UNIT_BIT : TestBase.UNIT_BYTE, i);

                    int finalI = i;
                    runOnUiThread(() -> {
                        speedText.setText(speed);
                        status.setText(String.format("Running test... (%.0f %%)", ((float) finalI / graph1.getMeasuredWidth()) * 100));
                    });
                }

                try {
                    Thread.sleep(step);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
            runOnUiThread(() -> {
                status.setText("Waiting for thread to shutdown");
                status.setTextColor(0xFFE9F011);

                executor.shutdown();
                executor.shutdownNow();

                Executors.newSingleThreadExecutor().submit(() -> {
                    try {
                        int i = pendingTask.get();

                        runOnUiThread(() -> {
                            if (i == TestBase.CODE_OK) {
                                CharSequence res = test.getResultData();
                                if (res == null) {
                                    status.setText(String.format("Data processed: %s",
                                            spinner2.getSelectedItemPosition() == 0 ?
                                                    Utils.humanReadableBitsCount(test.getDataUsed(), 1) :
                                                    Utils.humanReadableByteCountSI(test.getDataUsed())));
                                } else status.setText(res);
                                status.setTextColor(0xFF11E9F0);
                            } else {
                                status.setText("Process exited with code " + i);
                                status.setTextColor(0xFFF03611);
                            }
                            enableButtons(true);
                        });
                    } catch (ExecutionException e) {
                        runOnUiThread(() -> {
                            status.setText(e.getMessage());
                            status.setTextColor(0xFFF03611);
                            enableButtons(true);
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            });
        });

        executor = Executors.newSingleThreadExecutor();
        pendingTask = executor.submit(() -> {
            activeTest = test;
            return test.run();
        });
    }
}