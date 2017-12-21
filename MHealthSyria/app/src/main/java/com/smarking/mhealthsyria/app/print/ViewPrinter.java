package com.smarking.mhealthsyria.app.print;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.zxing.BarcodeFormat;
import com.smarking.mhealthsyria.app.Constants;
import com.smarking.mhealthsyria.app.R;
import com.smarking.mhealthsyria.app.api.Recommender;
import com.smarking.mhealthsyria.app.model.Encounter;
import com.smarking.mhealthsyria.app.model.Medication;
import com.smarking.mhealthsyria.app.model.Model;
import com.smarking.mhealthsyria.app.model.Patient;
import com.smarking.mhealthsyria.app.model.Physician;
import com.smarking.mhealthsyria.app.model.Record;
import com.smarking.mhealthsyria.app.model.Test;
import com.smarking.mhealthsyria.app.model.Visit;
import com.smarking.mhealthsyria.app.view.viewmodel.EncounterViewModel;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by tamhok on 1/28/16.
 */
public class ViewPrinter {
    private static final String TAG = ViewPrinter.class.getSimpleName();
    private static final String BROTHER_APP = "com.brother.mfc.brprint";

    public static Bitmap printForm(Context context, LinearLayout mainLLayout) {

        //Provide it with a layout params. It should necessarily be wrapping the
        //content as we not really going to have a parent for it.
        mainLLayout.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        //Pre-measure the mainLLayout so that height and width don't remain null.
        mainLLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        //Assign a size and position to the mainLLayout and all of its descendants 
        mainLLayout.layout(0, 0, mainLLayout.getMeasuredWidth(), mainLLayout.getMeasuredHeight());


        //Create the bitmap
        Bitmap bitmap = Bitmap.createBitmap(mainLLayout.getMeasuredWidth(),
                mainLLayout.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(bitmap);
        c.drawColor(Color.WHITE);

        //Render this view (and all of its children) to the given Canvas
        mainLLayout.draw(c);

        return bitmap;
    }

    public static void printBitmaps(Bitmap[] bitmaps, int[] multiples, Context c, String filename) {
        File outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        if (!outputDir.exists()) {
            outputDir.mkdir();
        }

        ArrayList<Uri> uris = new ArrayList<>(bitmaps.length);

        for (int ctr = 0; ctr < bitmaps.length; ctr++) {
            File file = new File(outputDir, filename + "_" + ctr + ".png");
            OutputStream fOutputStream;

            try {
                fOutputStream = new FileOutputStream(file);

                bitmaps[ctr].compress(Bitmap.CompressFormat.PNG, 100, fOutputStream);

                fOutputStream.flush();
                fOutputStream.close();
                for (int i = 0; i < multiples[ctr]; i++) {
                    uris.add(Uri.fromFile(file));
                }
            } catch (FileNotFoundException e) {
                Log.e(TAG, "File not found: report");
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "File IO Exception: report");
                e.printStackTrace();
            }

        }

        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("image/png")
                .putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                .putExtra(Intent.EXTRA_TITLE, "report");

        //Check for brother app
        PackageManager pm = c.getPackageManager();
        try {
            pm.getPackageInfo("com.brother.mfc.brprint", PackageManager.GET_ACTIVITIES);
            intent.setPackage("com.brother.mfc.brprint");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Brother iPrint not isntalled");
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        c.startActivity(intent);
    }
}
