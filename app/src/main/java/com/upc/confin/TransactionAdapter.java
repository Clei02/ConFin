package com.upc.confin; // Reemplaza con tu package name

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private final List<Transaction> transactionList;
    private final Context context;

    public TransactionAdapter(Context context, List<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        holder.iconCategory.setImageResource(transaction.getIcon());
        holder.textTransactionName.setText(transaction.getName());
        holder.textTransactionDetail.setText(transaction.getDetail());

        // Formatea el monto para mostrarlo con el signo y dos decimales
        String formattedAmount = String.format(Locale.getDefault(), "-$%.2f", transaction.getAmount());
        holder.textAmount.setText(formattedAmount);
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        final ImageView iconCategory;
        final TextView textTransactionName;
        final TextView textTransactionDetail;
        final TextView textAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            iconCategory = itemView.findViewById(R.id.icon_category);
            textTransactionName = itemView.findViewById(R.id.text_transaction_name);
            textTransactionDetail = itemView.findViewById(R.id.text_transaction_detail);
            textAmount = itemView.findViewById(R.id.text_amount);
        }
    }
}