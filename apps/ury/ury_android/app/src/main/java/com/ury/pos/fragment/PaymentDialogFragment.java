package com.ury.pos.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ury.pos.R;
import com.ury.pos.model.Payment;
import com.ury.pos.util.CurrencyFormatter;
import com.ury.pos.viewmodel.POSViewModel;

import java.util.Collections;
import java.util.List;

public class PaymentDialogFragment extends BottomSheetDialogFragment {

    private POSViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(POSViewModel.class);

        TextView tvTotal       = view.findViewById(R.id.tv_payment_total);
        RadioGroup rgPayment   = view.findViewById(R.id.rg_payment_modes);
        EditText etAmount      = view.findViewById(R.id.et_payment_amount);
        Button btnConfirm      = view.findViewById(R.id.btn_confirm_payment);

        double total = viewModel.getCartTotal();
        tvTotal.setText(CurrencyFormatter.format(total));
        etAmount.setText(CurrencyFormatter.formatNoSymbol(total));

        viewModel.paymentModes.observe(getViewLifecycleOwner(), modes -> {
            rgPayment.removeAllViews();
            for (String mode : modes) {
                RadioButton rb = new RadioButton(requireContext());
                rb.setText(mode);
                rb.setTag(mode);
                rgPayment.addView(rb);
            }
            if (rgPayment.getChildCount() > 0) {
                ((RadioButton) rgPayment.getChildAt(0)).setChecked(true);
            }
        });

        btnConfirm.setOnClickListener(v -> {
            int checkedId = rgPayment.getCheckedRadioButtonId();
            if (checkedId == -1) {
                Toast.makeText(requireContext(), "Select payment mode", Toast.LENGTH_SHORT).show();
                return;
            }
            RadioButton rb = rgPayment.findViewById(checkedId);
            String mode = rb.getText().toString();
            double amount;
            try {
                amount = Double.parseDouble(etAmount.getText().toString().replace(",", ""));
            } catch (NumberFormatException e) {
                amount = total;
            }
            Payment payment = new Payment(mode, amount);
            viewModel.placeOrder(Collections.singletonList(payment));
            dismiss();
        });
    }
}
