package com.upc.confin; // Reemplaza con tu paquete

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * Adaptador para el RecyclerView de la pantalla de Gastos.
 */
public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private final List<Expense> expenseList;

    public ExpenseAdapter(List<Expense> expenseList) {
        this.expenseList = expenseList;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Crea la vista de cada fila usando el layout item_expense.xml.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        // Obtiene el gasto actual y lo asigna a la vista.
        Expense expense = expenseList.get(position);
        holder.bind(expense);
    }

    @Override
    public int getItemCount() {
        // Devuelve cuántos ítems hay en la lista.
        return expenseList.size();
    }

    /**
     * ViewHolder que contiene las referencias a las vistas de cada fila.
     */
    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private final ImageView icon;
        private final TextView categoryName;
        private final TextView date;
        private final TextView amount;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            // Enlaza las variables con los componentes del layout por su ID.
            icon = itemView.findViewById(R.id.iv_category_icon);
            categoryName = itemView.findViewById(R.id.tv_category_name);
            date = itemView.findViewById(R.id.tv_date);
            amount = itemView.findViewById(R.id.tv_amount);
        }

        // Asigna los datos del objeto Expense a las vistas.
        public void bind(Expense expense) {
            icon.setImageResource(expense.getIconResId());
            categoryName.setText(expense.getCategoryName());
            date.setText(expense.getDate());
            amount.setText(expense.getAmount());
        }
    }
}