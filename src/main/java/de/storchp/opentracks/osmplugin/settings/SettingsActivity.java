package de.storchp.opentracks.osmplugin.settings;

import static java.util.stream.Collectors.joining;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Objects;

import de.storchp.opentracks.osmplugin.BuildConfig;
import de.storchp.opentracks.osmplugin.R;
import de.storchp.opentracks.osmplugin.databinding.ActivitySettingsBinding;
import de.storchp.opentracks.osmplugin.utils.FileUtil;
import de.storchp.opentracks.osmplugin.utils.PreferencesUtils;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        var binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.settings, new SettingsFragment())
            .commit();

        setSupportActionBar(binding.toolbar.mapsToolbar);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            if (BuildConfig.offline) {
                var onlineMapConsentPreference = findPreference(getString(R.string.APP_PREF_ONLINE_MAP_CONSENT));
                if (onlineMapConsentPreference != null) {
                    onlineMapConsentPreference.setVisible(false);
                }

                var mapDownloadPreference = findPreference(getString(R.string.APP_PREF_MAP_DOWNLOAD));
                if (mapDownloadPreference != null) {
                    mapDownloadPreference.setVisible(false);
                }
            }

            setSummaries();
        }

        @Override
        public void onResume() {
            super.onResume();
            setSummaries();
        }

        private void setSummaries() {
            var mapsPreference = findPreference(getString(R.string.APP_PREF_MAP_SELECTION));
            if (mapsPreference != null) {
                mapsPreference.setSummaryProvider((Preference.SummaryProvider<Preference>) preference -> {
                    var mapUris = PreferencesUtils.getMapUris();
                    if (mapUris.isEmpty() && !BuildConfig.offline) {
                        return getString(R.string.online_osm_mapnick);
                    }
                    return mapUris.stream()
                            .map(uri -> FileUtil.getDocumentFileFromTreeUri(getContext(), uri))
                            .filter(Objects::nonNull)
                            .map(DocumentFile::getName)
                            .collect(joining(", "));
                });
            }

            var mapDirectoryPreference = findPreference(getString(R.string.APP_PREF_MAP_DIRECTORY));
            if (mapDirectoryPreference != null) {
                mapDirectoryPreference.setSummaryProvider((Preference.SummaryProvider<Preference>) preference -> {
                    var uri = PreferencesUtils.getMapDirectoryUri();
                    return uri != null ? uri.getLastPathSegment() : null;
                });
            }

            var themePreference = findPreference(getString(R.string.APP_PREF_MAP_THEME));
            if (themePreference != null) {
                themePreference.setSummaryProvider((Preference.SummaryProvider<Preference>) preference -> {
                    var themeUri = PreferencesUtils.getMapThemeUri();
                    if (themeUri == null) {
                        return getString(R.string.default_theme);
                    }
                    var documentFile = FileUtil.getDocumentFileFromTreeUri(getContext(), themeUri);
                    if (documentFile == null) {
                        return getString(R.string.default_theme);
                    }
                    return documentFile.getName();
                });
            }

            var themeDirectoryPreference = findPreference(getString(R.string.APP_PREF_MAP_THEME_DIRECTORY));
            if (themeDirectoryPreference != null) {
                themeDirectoryPreference.setSummaryProvider((Preference.SummaryProvider<Preference>) preference -> {
                    var uri = PreferencesUtils.getMapThemeDirectoryUri();
                    return uri != null ? uri.getLastPathSegment() : null;
                });
            }
        }

        @Override
        public void onDisplayPreferenceDialog(@NonNull Preference preference) {
            DialogFragment dialogFragment = null;
            if (preference instanceof MapOverdrawFactorPreference) {
                dialogFragment = MapOverdrawFactorPreference.MapOverdrawFactorPreferenceDialog.newInstance(preference.getKey());
            } else if (preference instanceof TilecacheCapacityFactorPreference) {
                dialogFragment = TilecacheCapacityFactorPreference.TilecacheCapacityFactorPreferenceDialog.newInstance(preference.getKey());
            }

            if (dialogFragment != null) {
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(getParentFragmentManager(), getClass().getSimpleName());
                return;
            }

            super.onDisplayPreferenceDialog(preference);
        }

    }

}