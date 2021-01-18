package com.appnuggets.lensminder.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.appnuggets.lensminder.R;
import com.appnuggets.lensminder.adapter.DropsAdapter;
import com.appnuggets.lensminder.bottomsheet.DropsBottomSheetDialog;
import com.appnuggets.lensminder.database.AppDatabase;
import com.appnuggets.lensminder.database.entity.Drops;
import com.appnuggets.lensminder.model.UsageProcessor;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DropsFragment extends Fragment {

    private CircularProgressBar dropsProgressbar;
    private TextView dropsLeftDaysCount;
    private RecyclerView dropsHistoryRecyclerView;

    public DropsFragment() {
        // Required empty public constructor
        System.out.println("DropsFragment constructor called!");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_drops, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FloatingActionButton dropsShowAddSheet = view.findViewById(R.id.drops_show_add_sheet);
        MaterialButton deleteCurrentDrops = view.findViewById(R.id.delete_drops_button);
        dropsProgressbar = view.findViewById(R.id.drops_progressbar);
        dropsLeftDaysCount = view.findViewById(R.id.drops_days_count);
        dropsHistoryRecyclerView = view.findViewById(R.id.drops_recycler_view);

        setRecyclerView();
        dropsShowAddSheet.setOnClickListener(v -> {
            DropsBottomSheetDialog dropsBottomSheetDialog = new DropsBottomSheetDialog();
            dropsBottomSheetDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetTheme);
            dropsBottomSheetDialog.show(getChildFragmentManager(), "bottomSheetDrops");
        });

        deleteCurrentDrops.setOnClickListener(v -> {
            AppDatabase db = AppDatabase.getInstance(getContext());
            Drops inUseDrops = db.dropsDao().getInUse();
            inUseDrops.inUse = false;
            try {
                Date today = new Date();
                SimpleDateFormat simpleFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.UK);
                inUseDrops.endDate = simpleFormat.parse(simpleFormat.format(today));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            db.dropsDao().update(inUseDrops);
            Toast.makeText(getContext(), "Drops deleted", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        AppDatabase db = AppDatabase.getInstance(getContext());
        Drops dropsInUse = db.dropsDao().getInUse();
        updateDropsSummary(dropsInUse);
    }

    private void setRecyclerView() {
        Context context = getContext();
        AppDatabase db = AppDatabase.getInstance(context);
        DropsAdapter dropsAdapter = new DropsAdapter(context, db.dropsDao().getAllNotInUse());

        dropsHistoryRecyclerView.setHasFixedSize(true);
        dropsHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        dropsHistoryRecyclerView.setAdapter(dropsAdapter);
    }

    private void updateDropsSummary(Drops drops) {
        if (null == drops) {
            dropsProgressbar.setProgressMax(100f);
            dropsProgressbar.setProgressWithAnimation(0f, (long) 1000); // =1s
            dropsLeftDaysCount.setText("-");
        }
        else {
            UsageProcessor usageProcessor = new UsageProcessor();
            Long daysLeft = usageProcessor.calculateUsageLeft(drops.startDate,
                    drops.expirationDate, drops.useInterval);

            dropsProgressbar.setProgressMax(drops.useInterval);
            dropsProgressbar.setProgressWithAnimation(Math.max(daysLeft, 0),
                    1000L);

            dropsLeftDaysCount.setText(String.format(Locale.getDefault(), "%d", daysLeft));
        }
    }
}