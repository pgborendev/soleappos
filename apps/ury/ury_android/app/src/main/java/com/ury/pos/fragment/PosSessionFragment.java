package com.ury.pos.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ury.pos.R;
import com.ury.pos.api.response.PosSessionStatusResponse;
import com.ury.pos.viewmodel.POSViewModel;

public class PosSessionFragment extends BottomSheetDialogFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false); // cashier must act before dismissing
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pos_session, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        POSViewModel viewModel = new ViewModelProvider(requireActivity()).get(POSViewModel.class);

        TextView tvTitle   = view.findViewById(R.id.tv_session_title);
        TextView tvMessage = view.findViewById(R.id.tv_session_message);
        Button   btnAction = view.findViewById(R.id.btn_session_action);

        viewModel.sessionStatus.observe(getViewLifecycleOwner(), status -> {
            if (status == null) return;

            if (status.needsClosePrevious) {
                tvTitle.setText("Close Previous Session");
                tvMessage.setText("A session from a previous day is still open. Close it before starting today's session.");
                btnAction.setText("Close Previous Session");
                btnAction.setOnClickListener(v -> {
                    btnAction.setEnabled(false);
                    viewModel.closeSession(
                            () -> requireActivity().runOnUiThread(() -> {
                                // status observer will re-evaluate and dismiss if now open
                            }),
                            err -> requireActivity().runOnUiThread(() -> {
                                btnAction.setEnabled(true);
                            })
                    );
                });
            } else if (!status.isOpen) {
                tvTitle.setText("Open POS Session");
                tvMessage.setText("No active session found. Open a session to start taking orders.");
                btnAction.setText("Open Session");
                btnAction.setOnClickListener(v -> {
                    btnAction.setEnabled(false);
                    viewModel.openSession(
                            () -> requireActivity().runOnUiThread(() -> {}),
                            err -> requireActivity().runOnUiThread(() -> {
                                btnAction.setEnabled(true);
                            })
                    );
                });
            } else {
                // Session is now ready
                dismiss();
            }
        });

        // Dismiss when posOpen becomes true
        viewModel.posOpen.observe(getViewLifecycleOwner(), open -> {
            if (Boolean.TRUE.equals(open)) dismiss();
        });
    }
}
