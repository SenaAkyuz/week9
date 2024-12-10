package msku.ceng.madlab.week9;

import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;


public class MainActivity extends AppCompatActivity {
    private EditText txtURL;
    private Button btnDownload;
    private ImageView imgView;
    private ProgressDialog progressDialog;

    // İzinler
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtURL = findViewById(R.id.txtURL);
        btnDownload = findViewById(R.id.btnDownload);
        imgView = findViewById(R.id.imgView);

        btnDownload.setOnClickListener(v -> {

            // URL'nin geçerli olup olmadığını kontrol edin
            String url = txtURL.getText().toString();
            if (Patterns.WEB_URL.matcher(url).matches()) {
                // İzinleri kontrol edin ve gerekirse isteyin
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
                } else {
                    // İzinler zaten verilmişse, indirme işlemini başlatın
                    new DownloadTask().execute(url);
                }
            } else {
                Toast.makeText(this, "Geçerli bir URL giriniz.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // İzin verildiyse, indirme işlemini başlatın
                String url = txtURL.getText().toString();
                new DownloadTask().execute(url);
            } else {
                Toast.makeText(this, "Depolama izni verilmedi.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class DownloadTask extends AsyncTask<String, Integer, Bitmap> {
        private String imagePath;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // İndirme işlemi başlamadan önce ProgressDialog gösterin
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMax(100);
            progressDialog.setIndeterminate(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setTitle("İndiriliyor");
            progressDialog.setMessage("Lütfen bekleyiniz...");
            progressDialog.show();
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String urlStr = urls[0];
            String fileName = "temp.jpg";
            imagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    .toString() + "/" + fileName;

            try {
                URL url = new URL(urlStr);
                URLConnection connection = url.openConnection();
                connection.connect();
                int fileSize = connection.getContentLength();

                try (InputStream input = new BufferedInputStream(url.openStream(), 8192);
                     OutputStream output = new FileOutputStream(imagePath)) {
                    byte[] data = new byte[1024];
                    long total = 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        publishProgress((int) ((total * 100) / fileSize));
                        output.write(data, 0, count);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            return BitmapFactory.decodeFile(imagePath);
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // İndirme ilerlemesini güncelleyin
            progressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            // ProgressDialog'u kapatın
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            if (bitmap != null) {
                // Resmi ImageView'da gösterin
                imgView.setImageBitmap(bitmap);
            } else {
                Toast.makeText(MainActivity.this, "Resim indirilemedi.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
