package com.upc.confin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private final List<ExpensesActivity.ExpenseDisplay> expenseList;

    public ExpenseAdapter(List<ExpensesActivity.ExpenseDisplay> expenseList) {
        this.expenseList = expenseList;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        ExpensesActivity.ExpenseDisplay expense = expenseList.get(position);
        holder.bind(expense);
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private final ImageView icon;
        private final TextView categoryName;
        private final TextView date;
        private final TextView amount;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iv_category_icon);
            categoryName = itemView.findViewById(R.id.tv_category_name);
            date = itemView.findViewById(R.id.tv_date);
            amount = itemView.findViewById(R.id.tv_amount);
        }

        public void bind(ExpensesActivity.ExpenseDisplay expense) {
            icon.setImageResource(expense.getIconResId());
            categoryName.setText(expense.getCategoryName());
            date.setText(expense.getDate());
            amount.setText(expense.getAmount());
        }
    }
}