package kz.alfa.ImageViewer;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import kz.alfa.ImageViewer.R;

public class PrefActivity extends PreferenceActivity {
  
  @SuppressWarnings("deprecation")
@Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.pref);
  }
}