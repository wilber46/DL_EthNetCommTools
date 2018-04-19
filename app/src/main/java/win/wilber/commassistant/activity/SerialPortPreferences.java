package win.wilber.commassistant.activity;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

import win.wilber.commassistant.R;
import win.wilber.commassistant.serialport.SerialPortFinder;

public class SerialPortPreferences extends PreferenceActivity {
    private Application mApplication;
    private SerialPortFinder mSerialPortFinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mApplication = (Application) getApplication();
        this.mSerialPortFinder = this.mApplication.mSerialPortFinder;
        setRequestedOrientation(1);
        addPreferencesFromResource(R.xml.serial_port_preferences);
        ListPreference devices = (ListPreference) findPreference("DEVICE");
        String[] entries = mSerialPortFinder.getAllDevices();
        String[] entryValues = mSerialPortFinder.getAllDevicesPath();
        devices.setEntries(entries);
        devices.setEntryValues(entryValues);
        devices.setSummary(devices.getValue());
//        devices.setValue("/dev/ttyMT3");
        devices.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((String) newValue);
                return true;
            }
        });

        ListPreference baudrates = (ListPreference) findPreference("BAUDRATE");
        baudrates.setSummary(baudrates.getValue());
//        baudrates.setValue("115200");
        baudrates.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((String) newValue);
                return true;
            }
        });
//        ListPreference data_bits = (ListPreference) findPreference("DATABITS");
//        data_bits.setSummary(data_bits.getValue());
//        data_bits.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                preference.setSummary((String) newValue);
//                return true;
//            }
//        });
//        ListPreference check_bit = (ListPreference) findPreference("CHECKBIT");
//        check_bit.setSummary(check_bit.getValue());
//        check_bit.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                preference.setSummary((String) newValue);
//                return true;
//            }
//        });
//        ListPreference stop_bit = (ListPreference) findPreference("STOPBIT");
//        stop_bit.setSummary(stop_bit.getValue());
//        stop_bit.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                preference.setSummary((String) newValue);
//                return true;
//            }
//        });
    }
}
