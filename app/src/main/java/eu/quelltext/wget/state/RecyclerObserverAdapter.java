package eu.quelltext.wget.state;

import androidx.recyclerview.widget.RecyclerView;

public class RecyclerObserverAdapter implements CommandDB.Observer {
    private RecyclerView.Adapter adapter;

    public RecyclerObserverAdapter(RecyclerView.Adapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void notifyItemInserted(int position) {
        adapter.notifyItemInserted(position);
    }

    @Override
    public void notifyItemRemoved(int position) {
        adapter.notifyItemRemoved(position);
    }

    @Override
    public void notifyDataSetChanged() {
        adapter.notifyDataSetChanged();
    }
}
