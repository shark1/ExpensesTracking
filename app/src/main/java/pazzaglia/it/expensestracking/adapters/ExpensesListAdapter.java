package pazzaglia.it.expensestracking.adapters;

/**
 * Created by IO on 28/06/2016.
 */

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import pazzaglia.it.expensestracking.R;
import pazzaglia.it.expensestracking.activities.ExpenseDetailActivity;
import pazzaglia.it.expensestracking.activities.LandingPageActivity;
import pazzaglia.it.expensestracking.models.Expense;
import pazzaglia.it.expensestracking.models.ExpensesCreatePOJO;
import pazzaglia.it.expensestracking.network.AbstractApiCaller;
import pazzaglia.it.expensestracking.network.ExpensesDeleteCaller;
import pazzaglia.it.expensestracking.shared.Common;

public class ExpensesListAdapter extends RecyclerView.Adapter<ExpensesListAdapter.ViewHolder> {
    Context context;
    private List<Expense> expenses;

    public ExpensesListAdapter(List<Expense> android, Context c) {
        this.expenses = android;
        this.context = c;
    }

    @Override
    public ExpensesListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_row, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ExpensesListAdapter.ViewHolder viewHolder, int i) {
        viewHolder._txtDescription.setText(expenses.get(i).getDescription());
        viewHolder._txtAmount.setText(String.format("%.2f€", expenses.get(i).getAmount()));
        viewHolder._txtDate.setText(expenses.get(i).getDate());
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public void delete(int position) { //removes the row
        expenses.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, expenses.size());
        Activity myActivity = (Activity) context;
        Common.updateTotalExpenses(myActivity,expenses);
    }

    private void onDeleteSuccess(int index, String message){
        if(message!="")
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        delete(index);
    }

    private void onDeleteFailed(String message){
        if(message!="")
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        @Bind(R.id.text_description) TextView _txtDescription;
        @Bind(R.id.text_amount) TextView _txtAmount;
        @Bind(R.id.text_date) TextView _txtDate;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            launchLandingPageActivity();
        }

        private void launchLandingPageActivity(){
            Intent intent = new Intent(context, ExpenseDetailActivity.class);
            intent.putExtra(ExpenseDetailActivity.DESCRIPTION, expenses.get(getAdapterPosition()).getDescription());
            intent.putExtra(ExpenseDetailActivity.AMOUNT, expenses.get(getAdapterPosition()).getAmount());
            intent.putExtra(ExpenseDetailActivity.DATE, expenses.get(getAdapterPosition()).getDate());
            intent.putExtra(ExpenseDetailActivity.CATEGORY, expenses.get(getAdapterPosition()).getCategory());
            intent.putExtra(ExpenseDetailActivity.ID, expenses.get(getAdapterPosition()).getId());
            intent.putExtra(ExpenseDetailActivity.MODE, ExpenseDetailActivity.EDIT);

            ((Activity) context).startActivityForResult(intent,LandingPageActivity.REQUEST_EDIT);
        }

        @Override
        public boolean onLongClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder((Activity) context);
            builder.setMessage("Delete expense?");

            String positiveText = "Delete";
            builder.setPositiveButton(positiveText,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteItem(getAdapterPosition());
                        }
                    });

            String negativeText = "Cancel";
            builder.setNegativeButton(negativeText,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

            AlertDialog dialog = builder.create();
            // display dialog
            dialog.show();

            return false;
        }

        public void deleteItem(final int index){
            //Retrofit delete
            ExpensesDeleteCaller expensesDeleteCaller = new ExpensesDeleteCaller(context, expenses.get(index).getId());
            expensesDeleteCaller.doApiCall(context, "Deleting...", new AbstractApiCaller.MyCallbackInterface<ExpensesCreatePOJO>() {
                @Override
                public void onDownloadFinishedOK(ExpensesCreatePOJO result) {
                    onDeleteSuccess(index, result.getMessage());
                }
                @Override
                public void onDownloadFinishedKO(ExpensesCreatePOJO result) {
                    onDeleteFailed((result != null)? result.getMessage(): "");
                }

                @Override
                public void doApiCallOnFailure() {
                    onDeleteFailed("Please check your network connection and internet permission");
                }
            });
        }
    }


}
