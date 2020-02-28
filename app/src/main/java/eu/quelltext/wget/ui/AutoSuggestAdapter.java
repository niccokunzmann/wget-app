/* This package is copied from Stackoverflow
 * see https://stackoverflow.com/a/37298258/1320237
 * licensed under cc by-sa 4.0 with attribution required.
 * https://creativecommons.org/licenses/by-sa/4.0/
 */
package eu.quelltext.wget.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AutoSuggestAdapter extends ArrayAdapter<String>
{
    private Context      context;
    private int          resource;
    private List<String> items;
    private List<String> tempItems;
    private List<String> suggestions;

    public AutoSuggestAdapter(Context context, int resource, List<String> items)
    {
        super(context, resource, 0, items);

        this.context = context;
        this.resource = resource;
        this.items = items;
        tempItems = new ArrayList<String>(items);
        suggestions = new ArrayList<String>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view = convertView;
        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(resource, parent, false);
        }

        String item = items.get(position);

        if (item != null && view instanceof TextView)
        {
            ((TextView) view).setText(item);
        }

        return view;
    }

    @Override
    public Filter getFilter()
    {
        return nameFilter;
    }

    Filter nameFilter = new Filter()
    {
        @Override
        public CharSequence convertResultToString(Object resultValue)
        {
            String str = (String) resultValue;
            return str;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint)
        {
            if (constraint != null)
            {
                suggestions.clear();
                for (String names : tempItems)
                {
                    if (names.toLowerCase().contains(constraint.toString().toLowerCase()))
                    {
                        suggestions.add(names);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            }
            else
            {
                return new FilterResults();
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results)
        {
            List<String> filterList = (ArrayList<String>) results.values;
            if (results != null && results.count > 0)
            {
                clear();
                for (String item : filterList)
                {
                    add(item);
                    notifyDataSetChanged();
                }
            }
        }
    };
}