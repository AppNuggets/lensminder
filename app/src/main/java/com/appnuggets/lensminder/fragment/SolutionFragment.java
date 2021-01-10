package com.appnuggets.lensminder.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.appnuggets.lensminder.R;
import com.appnuggets.lensminder.adapter.ContainerAdapter;
import com.appnuggets.lensminder.adapter.SolutionAdapter;
import com.appnuggets.lensminder.bottomsheet.ContainerBottomSheetDialog;
import com.appnuggets.lensminder.bottomsheet.SolutionBottomSheetDialog;
import com.appnuggets.lensminder.database.AppDatabase;
import com.appnuggets.lensminder.database.entity.Container;
import com.appnuggets.lensminder.database.entity.Solution;
import com.appnuggets.lensminder.model.UsageProcessor;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

public class SolutionFragment extends Fragment {

    private CircularProgressBar solutionProgressBar;
    private TextView solutionLeftDaysCount;
    private CircularProgressBar containerProgressBar;
    private TextView containerLeftDaysCount;

    private RecyclerView solutionHistoryRecyclerView;
    private RecyclerView containerHistoryRecyclerView;

    public SolutionFragment() {
        // Required empty public constructor
        System.out.println("SolutionFragment constructor called!");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_solution, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton deleteCurrentSolution = view.findViewById(R.id.delete_solution_button);
        FloatingActionButton solutionShowAddCurrent = view.findViewById(R.id.showAddSolution);
        solutionProgressBar = view.findViewById(R.id.solution_progressbar);
        solutionLeftDaysCount = view.findViewById(R.id.solution_days_count);
        solutionHistoryRecyclerView = view.findViewById(R.id.solutions_history_recycler_view);

        MaterialButton deleteCurrentContainer = view.findViewById(R.id.delete_container_button);
        FloatingActionButton containerShowAddCurrent = view.findViewById(R.id.showAddContainer);
        containerProgressBar = view.findViewById(R.id.container_progressbar);
        containerLeftDaysCount = view.findViewById(R.id.container_days_count);
        containerHistoryRecyclerView = view.findViewById(R.id.container_history_recycler_view);

        deleteCurrentSolution.setOnClickListener(v -> {
            // TODO Implement method for current solution deletion
        });

        solutionShowAddCurrent.setOnClickListener(v -> {
            SolutionBottomSheetDialog solutionBottomSheetDialog = new SolutionBottomSheetDialog();
            solutionBottomSheetDialog.show(getChildFragmentManager(), "bottomSheetSolution");
        });

        deleteCurrentContainer.setOnClickListener(v -> {
            // TODO Implement method for current container deletion
        });

        containerShowAddCurrent.setOnClickListener(v -> {
            ContainerBottomSheetDialog containerBottomSheetDialog = new ContainerBottomSheetDialog();
            containerBottomSheetDialog.show(getChildFragmentManager(), "bottomSheetContainer");
        });

        setSolutionHistoryRecyclerView();
        setContainerHistoryRecyclerView();
    }

    @Override
    public void onStart() {
        super.onStart();

        AppDatabase db = AppDatabase.getInstance(getContext());

        Solution solutionInUse = db.solutionDao().getInUse();
        updateSolutionSummary(solutionInUse);

        Container containerInUse = db.containerDao().getInUse();
        updateContainerSummary(containerInUse);
    }

    private void setSolutionHistoryRecyclerView(){
        Context context = getContext();
        AppDatabase db = AppDatabase.getInstance(context);
        SolutionAdapter solutionAdapter = new SolutionAdapter(context,
                db.solutionDao().getAllNotInUse());

        solutionHistoryRecyclerView.setHasFixedSize(true);
        solutionHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        solutionHistoryRecyclerView.setAdapter(solutionAdapter);
    }

    private void setContainerHistoryRecyclerView() {
        Context context = getContext();
        AppDatabase db = AppDatabase.getInstance(context);
        ContainerAdapter containerAdapter = new ContainerAdapter(context, db.containerDao().getAllNotInUse());

        containerHistoryRecyclerView.setHasFixedSize(true);
        containerHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        containerHistoryRecyclerView.setAdapter(containerAdapter);
    }

    private void updateSolutionSummary(Solution solution) {
        if (null == solution) {
            solutionProgressBar.setProgressMax(100f);
            solutionProgressBar.setProgressWithAnimation(0f, (long) 1000); // =1s
            solutionLeftDaysCount.setText("-");
        }
        else {
            UsageProcessor usageProcessor = new UsageProcessor();
            Long daysLeft = usageProcessor.calculateUsageLeft(solution.startDate,
                    solution.expirationDate, solution.useInterval);

            solutionProgressBar.setProgressMax(solution.useInterval);
            solutionProgressBar.setProgressWithAnimation(solution.useInterval - daysLeft,
                    1000L);

            solutionLeftDaysCount.setText(daysLeft.toString());
        }
    }

    private void updateContainerSummary(Container container) {
        if (null == container) {
            containerProgressBar.setProgressMax(100f);
            containerProgressBar.setProgressWithAnimation(0f, (long) 1000); // =1s
            containerLeftDaysCount.setText("-");
        }
        else {
            UsageProcessor usageProcessor = new UsageProcessor();
            Long daysLeft = usageProcessor.calculateUsageLeft(container.startDate,
                    container.expirationDate, container.useInterval);

            containerProgressBar.setProgressMax(container.useInterval);
            containerProgressBar.setProgressWithAnimation(container.useInterval - daysLeft,
                    1000L);

            containerLeftDaysCount.setText(daysLeft.toString());
        }
    }
}