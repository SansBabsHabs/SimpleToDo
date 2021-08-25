package com.sanusi.simpletodo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String KEY_ITEM_TEXT = "item_text";
    public static final String KEY_ITEM_POSITION = "item_position";
    public static final int EDIT_TEXT_CODE = 20;

    List<String> items;

    Button addButton;
    EditText editItem;
    RecyclerView rvItems;
    ItemsAdapter itemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addButton = findViewById(R.id.addButton);
        editItem = findViewById(R.id.editItem);
        rvItems = findViewById(R.id.rvItems);

        //editItem.setText("How far na?");

        //items = new ArrayList<>();
        loadItems();

        //items.add("Go to work");
        //items.add("Go home");
        //items.add("Go to school");

        ItemsAdapter.OnLongClickListener onLongClickListener = new ItemsAdapter.OnLongClickListener(){
            @Override
            public void onItemLongClicked(int position){
                String rem = items.get(position);
                //delete item
                items.remove(position);
                //notify adapter
                itemsAdapter.notifyItemRemoved(position);
                Toast.makeText(getApplicationContext(), rem + " was removed", Toast.LENGTH_SHORT).show();
                rem = "";
                saveItems();
            }
        };

        ItemsAdapter.OnClickListener onClickListener = new ItemsAdapter.OnClickListener() {
            @Override
            public void onItemClicked(int position){
                Log.d("MainActivity", "Single Click at Position" + position);

                //create new activity
                Intent i = new Intent(MainActivity.this, EditActivity.class);

                //pass data being edited
                i.putExtra(KEY_ITEM_TEXT, items.get(position));
                i.putExtra(KEY_ITEM_POSITION, position);

                //display in editActivity page
                startActivityForResult(i,EDIT_TEXT_CODE);
            }
        };

        itemsAdapter = new ItemsAdapter(items, onLongClickListener, onClickListener);
        rvItems.setAdapter(itemsAdapter);
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String todoItem = editItem.getText().toString();
                //add item to list
                items.add(todoItem);
                //notify
                itemsAdapter.notifyItemInserted(items.size()-1);
                editItem.setText("");
                Toast.makeText(getApplicationContext(), todoItem + " was added", Toast.LENGTH_SHORT).show();
                saveItems();
            }
        });
    }

    //handle result of EditActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == EDIT_TEXT_CODE && resultCode == RESULT_OK) {
            //retrieve the updated value
            String itemText = data.getStringExtra(KEY_ITEM_TEXT);

            //extract the original position of the edited item from the position key
            int position =data.getExtras().getInt(KEY_ITEM_POSITION);

            //update the model with new text
            items.set(position, itemText);

            //notify adapter
            itemsAdapter.notifyItemChanged(position);

            //persist (SAVE)
            saveItems();
            Toast.makeText(getApplicationContext(), "Item updated successfully", Toast.LENGTH_SHORT).show();

        } else{
            Log.w("MainActivity", "Unknown call to onActivityResult");
        }
    };
        private File getDataFile() {
        return new File(getFilesDir(), "data.txt");
        }

        private void loadItems() {
            try {
                items = new ArrayList<>(FileUtils.readLines(getDataFile(), Charset.defaultCharset()));
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("MainActivity", "Error reading items", e);
                items = new ArrayList<>();
            }
        }

        private void saveItems() {
            try {
                FileUtils.writeLines(getDataFile(), items);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("MainActivity", "Error writing items", e);
            }
        }
    }
